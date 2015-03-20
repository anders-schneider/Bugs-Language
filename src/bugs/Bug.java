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
	
	public Bug() {
		variables = new HashMap<String, Double>();
		x = 0;
		y = 0;
		angle = 0;
	}
	
	public double evaluate(Tree<Token> tree) {
		String value = tree.getValue().value;
		Token.Type type = tree.getValue().type;
		
		if (Token.Type.NUMBER.equals(type)) {
			return Double.parseDouble(value);
		}
		
		if ("case".equals(value)) {
			double first = evaluate(tree.getChild(0));
			if (Math.abs(first) <= 0.001) {
				interpret(tree.getChild(1));
			}
			return first;
		}
		
		int numChildren = tree.getNumberOfChildren();
		
		double first, second;
		
		switch (tree.getNumberOfChildren()) {
		case 1:
			first = evaluate(tree.getChild(0));
			second = 0;
		case 2:
			first = evaluate(tree.getChild(0));
			second = evaluate(tree.getChild(1));
		default:
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
			if ((Math.abs(first - second) <= 0.001) || (first > second)) {
				return 1;
			} else {
				return 0;
			}
		}
		
		throw new IllegalArgumentException("Unable to evaluate this tree. Root node has value: " + value);
		//TODO Test this bad boy!
		
	}
	
	public void interpret(Tree<Token> tree) {
		
	}
	
	void store(String variable, double value) {
		if ("x".equals(variable)) x = value;
		else if ("y".equals(variable)) y = value;
		else if ("angle".equals(variable)) angle = value;
		else {
			 variables.put(variable, value);
		}
	}
	
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
