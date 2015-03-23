package bugs;

import java.awt.Color;
import java.util.HashMap;

import tree.Tree;

public class Bug {
	
	public double x, y, angle;
	public String bugName;
	public Color bugColor;
	private HashMap<String, Double> variables;
	private HashMap<String, Tree<Token>> functions;
	private int numLoops;
	
    /**
     * Constructs a Bug and positions it at the origin (0, 0), facing due east, and
     * gives the bug the default color of white.
     */
	public Bug() {
		variables = new HashMap<String, Double>();
		functions = new HashMap<String, Tree<Token>>();
		x = 0;
		y = 0;
		angle = 0;
		numLoops = 0;
		
		// Default color: white
		bugColor = Color.white;
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
	 * Interprets any tree created by the parser, except those which are designed to be
	 * evaluated (those that produce a numeric result).
	 * 
	 * @param tree representing some Bugs language code
	 */
	public void interpret(Tree<Token> tree) {
		String value = tree.getValue().value;
		
		switch(value) {
		case "program":
			interpretProgram(tree);
			break;
		case "Allbugs":
			interpretAllbugs(tree);
			break;
		case "Bug":
			interpretBug(tree);
			break;
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
		}
	}
	
	/**
	 * Interprets a program tree.
	 * 
	 * @param tree
	 */
	void interpretProgram(Tree<Token> tree) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Interprets an Allbugs tree.
	 * 
	 * @param tree
	 */
	void interpretAllbugs(Tree<Token> tree) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Interprets a Bug definition tree. Stores the "child" as the bugName instance
	 * variable and interprets the var declarations and initialization block, as well
	 * as interpreting all of the commands included in the bug definition.
	 * 
	 * @param tree
	 */
	void interpretBug(Tree<Token> tree) {
		String bugName = tree.getChild(0).getValue().value;
		this.bugName = bugName;
		
		interpret(tree.getChild(1));
		
		interpret(tree.getChild(2));
		
		int numCommands = tree.getChild(3).getNumberOfChildren();
		for (int i = 0; i < numCommands; i++) {
			interpret(tree.getChild(3).getChild(i));
		}
		
		// Not handling function declarations in this assignment
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
		
		for (int i = 0; i < numChildren; i++) {
			String varName = tree.getChild(i).getValue().value;
			store(varName, 0);
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
		double distance = evaluate(tree.getChild(0));
		
		x += distance * Math.cos(angle * (Math.PI / 180));
		y += distance * Math.sin(angle * (Math.PI / 180));
	}
	
	/**
	 * Interprets a moveto tree. Updates the bug's x and y coordinates by placing the
	 * bug in the specified (x, y) location.
	 * 
	 * @param tree
	 */
	void interpretMoveto(Tree<Token> tree) {
		double newX = evaluate(tree.getChild(0));
		double newY = evaluate(tree.getChild(1));
		
		x = newX;
		y = newY;
	}
	
	/**
	 * Interprets a turn tree. Updates the direction the bug is facing by increasing
	 * the angle the bug is facing from due east by the specified amount.
	 * 
	 * @param tree
	 */
	void interpretTurn(Tree<Token> tree) {
		double angleDelta = evaluate(tree.getChild(0));
		
		angle += angleDelta;
		
		while (angle < 0) angle += 360;
		while (angle >= 360) angle -= 360;
	}
	
	/**
	 * Interprets a turnto tree. Updates the direction the bug is facing by setting
	 * it to the specified value.
	 * 
	 * @param tree
	 */
	void interpretTurnto(Tree<Token> tree) {
		double newAngle = evaluate(tree.getChild(0));
		
		while (newAngle < 0) newAngle += 360;
		while (newAngle >= 360) newAngle -= 360;
		
		angle = newAngle;
	}
	
	/**
	 * Interprets a return tree.
	 * 
	 * @param tree
	 */
	void interpretReturn(Tree<Token> tree) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Interprets a line tree.
	 * 
	 * @param tree
	 */
	void interpretLine(Tree<Token> tree) {
		// TODO Auto-generated method stub
		
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
		
		if (!variables.containsKey(varName) && !"x".equals(varName) && !"y".equals(varName)
											&& !"angle".equals(varName)) {
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
			bugColor = Color.white;
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
		String funcName = tree.getValue().value;
		
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
			 variables.put(variable, value);
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
		
		if (variables.containsKey(variable)) {
			return variables.get(variable);
		} else {
			throw new IllegalArgumentException("This variable '" + variable + "' has not been declared.");
		}
	}
}