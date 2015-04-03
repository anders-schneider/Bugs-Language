package bugs;

import java.awt.Color;
import java.util.HashMap;
import java.util.Stack;

import tree.Tree;

public class Bug extends Thread {
	
	public double x, y, angle;
	public String bugName;
	public Color bugColor;
	public Stack<HashMap<String, Double>> scopes;
	private HashMap<String, Tree<Token>> functions;
	private int numLoops;
	private double returnValue;
	private boolean afterFuncReturn;
	private Interpreter interpreter;
	private Tree<Token> bugTree;
	private boolean blocked;
	
    /**
     * Constructs a Bug and positions it at the origin (0, 0), facing due east.
     */
	public Bug(Interpreter interpreter) {
		scopes = new Stack<HashMap<String, Double>>();

		HashMap<String, Double> variables = new HashMap<String, Double>();
		scopes.push(variables);
		
		this.interpreter = interpreter;
		functions = new HashMap<String, Tree<Token>>();
		x = 0;
		y = 0;
		angle = 0;
		numLoops = 0;
		blocked = true;
		bugColor = Color.black;
	}
	
	/**
	 * Constructs a Bug and gives it its name, declares its variables, stores its functions,
	 * and runs its initialization block
	 * 
	 * @param interpreter
	 * @param bugTree
	 */
	public Bug(Interpreter interpreter, Tree<Token> bugTree) {
		this(interpreter);
		this.bugTree = bugTree;

		// Bug name
		String bugName = bugTree.getChild(0).getValue().value;
		this.bugName = bugName;
		
		// Var declarations
		interpret(bugTree.getChild(1));
		
		// Function declarations
		interpret(bugTree.getChild(4));
		
		// Initialization block
		interpret(bugTree.getChild(2));
	}

	/**
	 * Evaluates a tree that represents an expression or a case (in a switch statement),
	 * and returns the resulting number as a double. In the case of a switch statement, 
	 * if the expression evaluates to a 1 (true), then the associated block is interpreted.
	 * 
	 * @param tree representing an expression or a case
	 * @return the value represented by the input tree
	 */
	public double evaluate(Tree<Token> tree) {
		String value = tree.getValue().value;
		Token.Type type = tree.getValue().type;
		
		if (Token.Type.NUMBER.equals(type)) {
			return Double.parseDouble(value);
		}
		
		if (Token.Type.NAME.equals(type)) {
			return fetch(value);
		}
		
		if ("call".equals(value)) {
			String funcName = tree.getChild(0).getValue().value;
			
			// Checks for special distance/direction functions
			if ("distance".equals(funcName)) {
				return distance(tree.getChild(1));
			} else if ("direction".equals(funcName)) {
				return direction(tree.getChild(1));
			}
			
			interpretFunctionCall(tree);
			return returnValue;
		}
		
		if ("case".equals(value)) {
			double first = evaluate(tree.getChild(0));
			if (Math.abs(first - 1) <= 0.001) {
				int numChil = tree.getNumberOfChildren();
				for (int i = 1; i < numChil; i++) {
					interpret(tree.getChild(i));
				}
			}
			return first;
		}
		
		if (".".equals(value)) {
			String otherBugName = tree.getChild(0).getValue().value;
			String otherBugVar = tree.getChild(1).getValue().value;
			
			// Checks it otherBug exists
			if (!interpreter.bugs.containsKey(otherBugName)) {
				throw new IllegalArgumentException("Using dot notation, but bug " + otherBugName +
																			" does not exist");
			}
			Bug other = interpreter.bugs.get(otherBugName);
			return other.dotNotationFetch(otherBugVar);
		}
		
		int numChildren = tree.getNumberOfChildren();
		
		double first, second;
		
		if (numChildren > 0) {
			first = evaluate(tree.getChild(0));
			if (numChildren > 1) {
				second = evaluate(tree.getChild(1));
			} else {
				second = 0;
			}
		} else {
			first = 0;
			second = 0;
		}
		
		if ("+".equals(value)) {
			if (numChildren == 1) {
				return first;
			} else {
				return first + second;
			}
		}
		
		if ("-".equals(value)) {
			if (numChildren == 1) {
				return -1 * first;
			} else {
				return first - second;
			}
		}
		
		if ("*".equals(value)) {
			return first * second;
		}
		
		if ("/".equals(value)) {
			return first / second;
		}
		
		if ("=".equals(value)) {
			if (Math.abs(first - second) <= 0.001) {
				return 1;
			} else {
				return 0;
			}
		}
		
		if ("!=".equals(value)) {
			if (Math.abs(first - second) <= 0.001) {
				return 0;
			} else {
				return 1;
			}
		}
		
		if (">".equals(value)) {
			if ((Math.abs(first - second) > 0.001) && (first > second)) {
				return 1;
			} else {
				return 0;
			}
		}
		
		if ("<".equals(value)) {
			if ((Math.abs(first - second) > 0.001) && (first < second)) {
				return 1;
			} else {
				return 0;
			}
		}
		
		if (">=".equals(value)) {
			if ((Math.abs(first - second) <= 0.001) || (first > second)) {
				return 1;
			} else {
				return 0;
			}
		}
		
		if ("<=".equals(value)) {
			if ((Math.abs(first - second) <= 0.001) || (first < second)) {
				return 1;
			} else {
				return 0;
			}
		}
		
		throw new IllegalArgumentException("Unable to evaluate this tree. Root node has value: " + value);
	}
	
	/**
	 * Finds the direction this Bug should point in order to face the other Bug
	 * 
	 * @param tree
	 * @return
	 */
	private double direction(Tree<Token> tree) {
		if (tree.getNumberOfChildren() != 1) {
			throw new IllegalArgumentException("direction function takes exactly one input parameter");
		}
		
		String otherBugName = tree.getChild(0).getValue().value;
		
		if (!interpreter.bugs.containsKey(otherBugName)) {
			throw new IllegalArgumentException(otherBugName + " is not a defined bug");
		}
		
		Bug otherBug = interpreter.bugs.get(otherBugName);
		double yDif = otherBug.y - this.y;
		double xDif = otherBug.x - this.x;
		double angle = Math.atan(yDif / xDif) * (180 / Math.PI);
		
		if (xDif >= 0 && yDif >= 0) {
			return angle;
		} else if (xDif < 0 && yDif < 0) {
			return angle + 180;
		} else if (xDif < 0 && yDif >= 0) {
			return angle + 180;
		} else {
			return angle + 360;
		}
	}

	/**
	 * Finds the distance between this Bug and the input Bug
	 * 
	 * @param tree
	 * @return
	 */
	private double distance(Tree<Token> tree) {
		if (tree.getNumberOfChildren() != 1) {
			throw new IllegalArgumentException("distance function takes exactly one input parameter");
		}
		
		String otherBugName = tree.getChild(0).getValue().value;
		
		if (!interpreter.bugs.containsKey(otherBugName)) {
			throw new IllegalArgumentException(otherBugName + " is not a defined bug");
		}
		
		Bug otherBug = interpreter.bugs.get(otherBugName);
		double yDif = otherBug.y - this.y;
		double xDif = otherBug.x - this.x;
		return Math.sqrt(xDif * xDif + yDif * yDif);
	}

	/**
	 * Interprets a function call by creating a new stack frame, populating it with the
	 * parameters and local variables, and executing the function call
	 * 
	 * @param tree
	 */
	void interpretFunctionCall(Tree<Token> tree) {
		HashMap<String, Double> variables = new HashMap<String, Double>();
		scopes.push(variables);
		
		String functionName = tree.getChild(0).getValue().value;
		Tree<Token> function;
		if (functions.containsKey(functionName)) {
			function = functions.get(functionName);
		} else if (interpreter.functions.containsKey(functionName)) {
			function = interpreter.functions.get(functionName);
		} else {
			scopes.pop();
			throw new IllegalArgumentException("No function named " + functionName);
		}
		
		Tree<Token> funcParam = function.getChild(1);
		Tree<Token> funcParamValues = tree.getChild(1);
		if (!(funcParam.getNumberOfChildren() == funcParamValues.getNumberOfChildren())) {
			scopes.pop();
			throw new RuntimeException("Number of input parameters does not match number "
														+ "of required input parameters");
		}
		interpret(funcParam);
		for (int i = 0; i < funcParamValues.getNumberOfChildren(); i++) {
			String varName = funcParam.getChild(i).getValue().value;
			double varValue = evaluate(funcParamValues.getChild(i));
			
			store(varName, varValue);
		}
		interpret(function.getChild(2));
		
		// If we did not reach a return statement, leave a 0 as the return value
		if (!afterFuncReturn) returnValue = 0;
		else afterFuncReturn = false;
		
		// If the same stack frame is still in scopes, pop it off before ending function call
		if (scopes.peek() == variables) {
			scopes.pop();
		}
	}

	/**
	 * Interprets any tree created by the parser, except those which are designed to be
	 * evaluated (those that produce a numeric result).
	 * 
	 * @param tree representing some Bugs language code
	 */
	public void interpret(Tree<Token> tree) {
		String value = tree.getValue().value;
		
		// If this boolean is true, the code we are evaluating is AFTER a return statement
		// and therefore should be disregarded
		if (afterFuncReturn) return;
		
		switch(value) {
		case "list":
			interpretList(tree);
			break;
		case "var":
			interpretVar(tree);
			break;
		case "initially":
			interpretInitially(tree);
			break;
		case "block":
			interpretBlock(tree);
			break;
		case "move":
			interpretMove(tree);
			break;
		case "moveto":
			interpretMoveto(tree);
			break;
		case "turn":
			interpretTurn(tree);
			break;
		case "turnto":
			interpretTurnto(tree);
			break;
		case "return":
			interpretReturn(tree);
			break;
		case "line":
			interpretLine(tree);
			break;
		case "assign":
			interpretAssign(tree);
			break;
		case "loop":
			interpretLoop(tree);
			break;
		case "exit":
			interpretExit(tree);
			break;
		case "switch":
			interpretSwitch(tree);
			break;
		case "color":
			interpretColor(tree);
			break;
		case "function":
			interpretFunction(tree);
			break;
		case "call":
			// Only useful in the case of do statements
			evaluate(tree);
			break;
		}
	}
		
	/**
	 * Interprets a list tree. Interprets each subtree in order left to right.
	 * 
	 * @param tree
	 */
	void interpretList(Tree<Token> tree) {
		int numChildren = tree.getNumberOfChildren();
		
		for (int i = 0; i < numChildren; i++) {
			interpret(tree.getChild(i));
		}
	}
	
	/**
	 * Interprets a var declaration tree. Stores each newly declared var with a
	 * value of 0.
	 * 
	 * @param tree
	 */
	void interpretVar(Tree<Token> tree) {
		int numChildren = tree.getNumberOfChildren();
		
		HashMap<String, Double> variables = scopes.peek();
		
		for (int i = 0; i < numChildren; i++) {
			String varName = tree.getChild(i).getValue().value;
			variables.put(varName, 0.0);
		}
	}
	
	/**
	 * Interprets an initialization block tree.
	 * 
	 * @param tree
	 */
	void interpretInitially(Tree<Token> tree) {
		interpret(tree.getChild(0));
	}
	
	/**
	 * Interprets a block tree. Interprets each command contained therein in order.
	 * 
	 * @param tree
	 */
	void interpretBlock(Tree<Token> tree) {
		int numChil = tree.getNumberOfChildren();
		
		for (int i = 0; i < numChil; i++) {
			interpret(tree.getChild(i));
		}
	}
	
	/**
	 * Interprets a move tree. Updates the bug's x and y coordinates by advancing
	 * the bug in the direction it was already facing.
	 * 
	 * @param tree
	 */
	void interpretMove(Tree<Token> tree) {
		interpreter.getPermissionToAct(this);
		double distance = evaluate(tree.getChild(0));
		
		double oldX = x;
		double oldY = y;
		
		x += distance * Math.cos(angle * (Math.PI / 180));
		y -= distance * Math.sin(angle * (Math.PI / 180));
		
		Command newLine = new Command(oldX, oldY, x, y, bugColor);
		interpreter.commands.add(newLine);
		
		interpreter.completeAction(this);
	}
	
	/**
	 * Interprets a moveto tree. Updates the bug's x and y coordinates by placing the
	 * bug in the specified (x, y) location.
	 * 
	 * @param tree
	 */
	void interpretMoveto(Tree<Token> tree) {
		interpreter.getPermissionToAct(this);
		double newX = evaluate(tree.getChild(0));
		double newY = evaluate(tree.getChild(1));
		
		double oldX = x;
		double oldY = y;
		
		x = newX;
		y = newY;
		
		Command newLine = new Command(oldX, oldY, x, y, bugColor);
		interpreter.commands.add(newLine);
		
		interpreter.completeAction(this);
	}
	
	/**
	 * Interprets a turn tree. Updates the direction the bug is facing by increasing
	 * the angle the bug is facing from due east by the specified amount.
	 * 
	 * @param tree
	 */
	void interpretTurn(Tree<Token> tree) {
		interpreter.getPermissionToAct(this);
		double angleDelta = evaluate(tree.getChild(0));
		
		angle += angleDelta;
		
		while (angle < 0) angle += 360;
		while (angle >= 360) angle -= 360;
		interpreter.completeAction(this);
	}
	
	/**
	 * Interprets a turnto tree. Updates the direction the bug is facing by setting
	 * it to the specified value.
	 * 
	 * @param tree
	 */
	void interpretTurnto(Tree<Token> tree) {
		interpreter.getPermissionToAct(this);
		double newAngle = evaluate(tree.getChild(0));
		
		while (newAngle < 0) newAngle += 360;
		while (newAngle >= 360) newAngle -= 360;
		
		angle = newAngle;
		interpreter.completeAction(this);
	}
	
	/**
	 * Interprets a return tree.
	 * 
	 * @param tree
	 */
	void interpretReturn(Tree<Token> tree) {
		returnValue = evaluate(tree.getChild(0));
		scopes.pop();
		afterFuncReturn = true;
	}
	
	/**
	 * Interprets a line tree.
	 * 
	 * @param tree
	 */
	void interpretLine(Tree<Token> tree) {
		interpreter.getPermissionToAct(this);

		double startX = evaluate(tree.getChild(0));
		double startY = evaluate(tree.getChild(1));
		
		double finishX = evaluate(tree.getChild(2));
		double finishY = evaluate(tree.getChild(3));
		
		Command newLine = new Command(startX, startY, finishX, finishY, bugColor);
		interpreter.commands.add(newLine);
		
		interpreter.completeAction(this);
	}
	
	/**
	 * Interprets an assign tree. Assigns the specified variable the specified value,
	 * unless the variable has not been declared (in that case: throws an
	 * IllegalArgumentException)
	 * 
	 * @param tree
	 */
	void interpretAssign(Tree<Token> tree) {
		String varName = tree.getChild(0).getValue().value;
		double varValue = evaluate(tree.getChild(1));
		
		if ("x".equals(varName) || "y".equals(varName) || "angle".equals(varName)) {
			store(varName, varValue);
			return;
		}

		boolean foundContainer = false;
		for (int i = 0; i < scopes.size(); i++) {
			HashMap<String, Double> variables = scopes.get(i);
			if (variables.containsKey(varName)) {
				variables.put(varName, varValue);
				foundContainer = true;
				break;
			}
		}
		
		if (interpreter.variables.containsKey(varName)) {
			foundContainer = true;
		}
		
		if (!foundContainer) {
			throw new IllegalArgumentException("Variables must be declared before they "
															+ "can be assigned values");
		}
		
		store(varName, varValue);
	}
	
	/**
	 * Interprets a loop tree. Loops through the commands in the loop until an "exit if"
	 * statement is encountered and the condition evaluates to 1 (true).
	 * 
	 * @param tree
	 */
	void interpretLoop(Tree<Token> tree) {
		int numChil = tree.getChild(0).getNumberOfChildren();
		numLoops++;
		int loopID = numLoops;
		while (true) {
			for (int i = 0; i < numChil; i++) {
				if (numLoops == loopID) {
					interpret(tree.getChild(0).getChild(i));
				} else {
					return;
				}
			}
		}
	}
	
	/**
	 * Interprets an exit if tree. Evaluates the condition and - if the condition
	 * evaluates to 1 (true) - exits the most immediately enclosing loop.
	 * 
	 * @param tree
	 */
	void interpretExit(Tree<Token> tree) {
		if (evaluate(tree.getChild(0)) == 1) {
			if (numLoops == 0) {
				throw new RuntimeException("No loop to exit from");
			} else {
				numLoops--;
			}
		}
	}
	
	/**
	 * Interprets a switch tree. Evaluates each case condition and interprets 
	 * the associated commands if the condition is true. After the first true
	 * case-condition is encountered, the switch statement terminates.
	 * 
	 * @param tree
	 */
	void interpretSwitch(Tree<Token> tree) {
		int numChil = tree.getNumberOfChildren();
		
		for (int i = 0; i < numChil; i++) {
			if (evaluate(tree.getChild(i)) == 1) {
				return;
			}
		}
	}
	
	/**
	 * Interprets a color tree. Assigns the bug the specified color or throws
	 * an IllegalArgumentException if the color is not one of the defined colors.
	 * 
	 * @param tree
	 */
	void interpretColor(Tree<Token> tree) {
		String colorName = tree.getChild(0).getValue().value;
		
		switch (colorName) {
		case "black":
			bugColor = Color.black;
			return;
		case "blue":
			bugColor = Color.blue;
			return;
		case "cyan":
			bugColor = Color.cyan;
			return;
		case "darkGray":
			bugColor = Color.darkGray;
			return;
		case "gray":
			bugColor = Color.gray;
			return;
		case "green":
			bugColor = Color.green;
			return;
		case "lightGray":
			bugColor = Color.lightGray;
			return;
		case "magenta":
			bugColor = Color.magenta;
			return;
		case "orange":
			bugColor = Color.orange;
			return;
		case "pink":
			bugColor = Color.pink;
			return;
		case "red":
			bugColor = Color.red;
			return;
		case "white":
			bugColor = Color.white;
			return;
		case "yellow":
			bugColor = Color.yellow;
			return;
		case "brown":
			bugColor = new Color(139, 69, 19);
			return;
		case "purple":
			bugColor = new Color(128, 0, 128);
			return;
		case "none":
			bugColor = null;
			return;
		}
		
		throw new IllegalArgumentException("Illegal color entered");
	}
	
	/**
	 * Interprets a function tree. Stores the name of the function and the
	 * tree that represents it.
	 * 
	 * @param tree
	 */
	void interpretFunction(Tree<Token> tree) {
		String funcName = tree.getChild(0).getValue().value;
		
		functions.put(funcName, tree);
	}
	
	/**
	 * Stores the input variable for this bug with the input value.
	 * 
	 * @param variable
	 * @param value associated with variable
	 */
	void store(String variable, double value) {
		if ("x".equals(variable)) x = value;
		else if ("y".equals(variable)) y = value;
		else if ("angle".equals(variable)) angle = value;
		else {
			for (int i = 0; i < scopes.size(); i++) {
				HashMap<String, Double> variables = scopes.get(i);
				if (variables.containsKey(variable)) {
					variables.put(variable, value);
					return;
				}
			}
			
			if (interpreter.variables.containsKey(variable)) {
				interpreter.variables.put(variable, value);
				return;
			}
			
			throw new IllegalArgumentException("Trying to access an undeclared variable");
		}
	}
	
	/**
	 * Returns the value associated with the input variable, or throws an
	 * IllegalArgumentException if that variable has not yet been declared.
	 * 
	 * @param variable
	 * @return value associated with that variable
	 */
	double fetch(String variable) {
		if ("x".equals(variable)) return x;
		else if ("y".equals(variable)) return y;
		else if ("angle".equals(variable)) return angle;
		
		for (int i = 0; i < scopes.size(); i++) {
			HashMap<String, Double> variables = scopes.get(i);
			if (variables.containsKey(variable)) {
				return variables.get(variable);
			}
		}
		
		if (interpreter.variables.containsKey(variable)) {
			return interpreter.variables.get(variable);
		}
		
		throw new IllegalArgumentException("Trying to access an undeclared variable: " + variable);
	}
	
	/**
	 * Method used to fetch from this Bug by another bug using dot notation (e.g. "Fred.x")
	 * 
	 * @param variable
	 * @return
	 */
	double dotNotationFetch(String variable) {
		if ("x".equals(variable)) return x;
		else if ("y".equals(variable)) return y;
		else if ("angle".equals(variable)) return angle;
		
		
		HashMap<String, Double> variables = scopes.get(0);
		if (variables.containsKey(variable)) {
			return variables.get(variable);
		}
				
		if (interpreter.variables.containsKey(variable)) {
			return interpreter.variables.get(variable);
		}
		
		throw new IllegalArgumentException("Using dot notation: "
				+ "Trying to access an undeclared variable: " + variable);
	}

	@Override
	public void run() {
		// Executes all commands in the main block of the Bug tree
		int numCommands = bugTree.getChild(3).getNumberOfChildren();
		for (int i = 0; i < numCommands; i++) {
			interpret(bugTree.getChild(3).getChild(i));
		}
		
		interpreter.terminateBug(this);
	}
	
	public void setBlocked(boolean b) {
		blocked = b;
	}
	
	public boolean isBlocked() {
		return blocked;
	}
}