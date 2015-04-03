package bugs;

import static org.junit.Assert.*;

import org.junit.Test;

import tree.Tree;

public class InterpreterTest {

	@Test
	public void testInterpretFunction() {
		Tree<Token> tree;
		Interpreter interpreter = new Interpreter();
		
		tree = useAllbugs("Allbugs {\n"
									+ "define func1 using foo {\n"
															+ "x = foo\n"
															+ "}\n"
								+ "}\n");
		interpreter.allbugs = tree;
		interpreter.interpretAllbugs();
		Bug b1 = new Bug(interpreter);
		tree = useFunctionCall("func1(5)");
		b1.evaluate(tree);
		assertEquals(5, b1.fetch("x"), 0);
		assertEquals(1, b1.scopes.size());
		
		tree = useFunction("define func1 using bar {\n"
													+ "x = 2 * bar\n"
												+ "}\n");
		b1.interpret(tree);
		tree = useFunctionCall("func1(5)");
		b1.evaluate(tree);
		assertEquals(10, b1.fetch("x"), 0);
		assertEquals(1, b1.scopes.size());
	}

	@Test
	public void testInterpretVariables() {
		Tree<Token> tree;
		Interpreter interpreter = new Interpreter();
		
		tree = useAllbugs("Allbugs {\n"
									+ "var foo, bar\n"
									+ "var u, v, t\n"
								+ "}\n");
		interpreter.allbugs = tree;
		Bug b1 = new Bug(interpreter);
		
		try {
			b1.fetch("foo");
			fail();
		} catch (IllegalArgumentException e) { }

		interpreter.interpretAllbugs();
		assertEquals(0, b1.fetch("foo"), 0);
		
		tree = useCommand("foo = 27\n");
		b1.interpret(tree);
		assertEquals(27, b1.fetch("foo"), 0);
		
		Interpreter i2 = new Interpreter();
		Bug b2 = new Bug(i2);
		tree = useAllbugs("Allbugs {\n"
									+ "var u\n"
									+ "define func using v {\n"
															+ "angle = v + u\n"
															+ "}\n"
								+ "}\n");
		i2.allbugs = tree;
		i2.interpretAllbugs();
		tree = useCommand("u = 180\n");
		b2.interpret(tree);
		tree = useFunctionCall("func(90)");
		b2.evaluate(tree);
		assertEquals(270, b2.fetch("angle"), 0);
		assertEquals(180, b2.fetch("u"), 0);
	}
	
	@Test
	public void testDistance() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b1 = new Bug(i);
		Bug b2 = new Bug(i);
		i.bugs.put("bug1", b1);
		i.bugs.put("bug2", b2);
		
		tree = useCommand("moveto 0, 0\n");
		b1.interpret(tree);
		tree = useCommand("moveto 1, 0\n");
		b2.interpret(tree);
		tree = useFunctionCall("distance(bug2)");
		assertEquals(1, b1.evaluate(tree), 0);
		assertEquals(0, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto -22, 0\n");
		b1.interpret(tree);
		tree = useFunctionCall("distance(bug1)");
		assertEquals(0, b1.evaluate(tree), 0);
		assertEquals(23, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto 7, -1\n");
		b1.interpret(tree);
		tree = useCommand("moveto 10, 3\n");
		b2.interpret(tree);
		tree = useFunctionCall("distance(bug2)");
		assertEquals(5, b1.evaluate(tree), 0);
		tree = useFunctionCall("distance(bug1)");
		assertEquals(5, b2.evaluate(tree), 0);
		
		tree = useFunctionCall("distance(bug1, bug2)");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }
		
		tree = useFunctionCall("distance()");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }
		
		tree = useFunctionCall("distance(bug3)");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }		
	}
	
	@Test
	public void testDirection() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b1 = new Bug(i);
		Bug b2 = new Bug(i);
		i.bugs.put("bug1", b1);
		i.bugs.put("bug2", b2);
		
		tree = useCommand("moveto 0, 0\n");
		b1.interpret(tree);
		tree = useCommand("moveto 1, 0\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(0, b1.evaluate(tree), 0);
		
		tree = useCommand("moveto 0, 1\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(90, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(270, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto -1, 0\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(180, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(0, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto 0, -1\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(270, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(90, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto 1, 1\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(45, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(225, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto -1, 1\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(135, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(315, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto -1, -1\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(225, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(45, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto 1, -1\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(315, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(135, b2.evaluate(tree), 0);
		
		tree = useCommand("moveto 3, 7\n");
		b1.interpret(tree);
		tree = useCommand("moveto 5, 9\n");
		b2.interpret(tree);
		tree = useFunctionCall("direction(bug2)");
		assertEquals(45, b1.evaluate(tree), 0);
		tree = useFunctionCall("direction(bug1)");
		assertEquals(225, b2.evaluate(tree), 0);
		
		tree = useFunctionCall("direction(bug1, bug2)");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }
		
		tree = useFunctionCall("direction()");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }
		
		tree = useFunctionCall("direction(bug3)");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }		

	}
	
//	--------------- Helper methods ------------------
	
	private Tree<Token> useAllbugs(String allbugs) {
		Parser p = new Parser(allbugs);
		if (p.isAllbugsCode()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid allbugs definition");			
		}
	}
	
	private Tree<Token> useExpression(String expression) {
		Parser p = new Parser(expression);
		if (p.isExpression()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid expression");
		}
	}
	
	private Tree<Token> useVarDeclaration(String varDec) {
		Parser p = new Parser(varDec);
		if (p.isVarDeclaration()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid expression");
		}
	}
	
	private Tree<Token> useCommand(String command) {
		Parser p = new Parser(command);
		if (p.isCommand()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid command");
		}
	}
	
	private Tree<Token> useBlock(String block) {
		Parser p = new Parser(block);
		if (p.isBlock()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid block");
		}
	}
	
	private Tree<Token> useInitializationBlock(String block) {
		Parser p = new Parser(block);
		if (p.isInitializationBlock()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid block");
		}
	}
	
	private Tree<Token> useBugDefinition(String bugDef) {
		Parser p = new Parser(bugDef);
		if (p.isBugDefinition()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid bug definition");
		}
	}
	
	private Tree<Token> useFunction(String func) {
		Parser p = new Parser(func);
		if (p.isFunctionDefinition()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid function definition");			
		}
	}
	
	private Tree<Token> useFunctionCall(String call) {
		Parser p = new Parser(call);
		if (p.isFunctionCall()) {
			return p.stack.pop();
		} else {
			throw new IllegalArgumentException("Input is not a valid function call");			
		}
	}
}
