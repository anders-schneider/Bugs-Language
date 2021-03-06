package bugs;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Test;

import tree.Tree;

public class BugTest {

	@Test
	public void testStoreAndFetch() {
		Interpreter i = new Interpreter();
		Bug bug = new Bug(i);
		
		bug.store("x", 1.0);
		assertEquals(bug.fetch("x"), 1.0, 0);
		
		try {
			bug.fetch("u");
			fail();
		} catch (IllegalArgumentException e) { }
		
		Tree<Token> tree = useVarDeclaration("var u\n");
		bug.interpret(tree);
		
		bug.store("u", 0);
		assertEquals(bug.fetch("u"), 0, 0);
		
		assertEquals(bug.fetch("angle"), 0, 0);
		
		bug.store("angle", 180);
		assertEquals(bug.fetch("angle"), 180, 0);
	}
	
	@Test
	public void testEvaluateArithmetic() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		tree = useExpression("3");
		assertEquals(3, b.evaluate(tree), 0);
		
		tree = useExpression("4 + 3");
		assertEquals(7, b.evaluate(tree), 0);
		
		tree = useExpression("+ 3");
		assertEquals(3, b.evaluate(tree), 0);
		
		tree = useExpression("- 3");
		assertEquals(-3, b.evaluate(tree), 0);
		
		tree = useExpression("5 * 3");
		assertEquals(15, b.evaluate(tree), 0);
		
		tree = useExpression("5 * 3 + 8 / 4 - 7");
		assertEquals(10, b.evaluate(tree), 0);
	}

	@Test
	public void testEvaluateComparisons() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		tree = useExpression("5 > 3");
		assertEquals(1, b.evaluate(tree), 0);
		
		tree = useExpression("5 < 3");
		assertEquals(0, b.evaluate(tree), 0);
		
		tree = useExpression("5 = 3");
		assertEquals(0, b.evaluate(tree), 0);
		
		tree = useExpression("5 != 3");
		assertEquals(1, b.evaluate(tree), 0);
		
		tree = useExpression("5 != 3");
		assertEquals(1, b.evaluate(tree), 0);
		
		tree = useExpression("5 = 5.0001");
		assertEquals(1, b.evaluate(tree), 0);
		
		tree = useExpression("5 != 5.0001");
		assertEquals(0, b.evaluate(tree), 0);
		
		tree = useExpression("5 >= 5.0001");
		assertEquals(1, b.evaluate(tree), 0);
		
		tree = useExpression("25 / 5 * 4 - 17 > 2 * 2 * 2 - 7");
		assertEquals(1, b.evaluate(tree), 0);
		
		tree = useExpression("8 <= 0");
		assertEquals(0, b.evaluate(tree), 0);
	}

	@Test
	public void testEvaluateDotNotation() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b1 = new Bug(i);
		Bug b2 = new Bug(i);
		
		b1.bugName = "Alice";
		b2.bugName = "Bob";
		i.bugs.put(b1.bugName, b1);
		i.bugs.put(b2.bugName, b2);
		
		tree = useExpression("Alice.x");
		assertEquals(0,  b2.evaluate(tree), 0);
		
		b1.store("x", 17);
		assertEquals(17, b2.evaluate(tree), 0);
		
		tree = useExpression("Carlos.y");
		try {
			b1.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) {}
		
		Bug b3 = new Bug(i);
		b3.bugName = "Fred";
		i.bugs.put(b3.bugName, b3);
		tree = useBugDefinition("Bug Fred {\n"
										+ "var foo\n"
										+ "initially {\n"
													+ "x = 50\n"
													+ "y = 50\n"
													+ "foo = 30\n"
										+ "}\n"
										+ "x = 2\n"
										+ "define func1 using z {\n"
																+ "return 2 * z\n"
															+ "}\n"
								+ "}\n");
		b3.interpret(tree);
		
		tree = useExpression("Fred.foo");
		assertEquals(30, b2.evaluate(tree), 0);
		
		tree = useExpression("Fred.z");
		try {
			b2.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }
	}


	@Test
	public void testInterpretFunction() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(1, b.scopes.size());
		assertEquals(0, b.fetch("x"), 0);
		tree = useFunction("define func1 {\n"
										+ "x = 2\n"
										+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func1()");
		b.evaluate(tree);
		assertEquals(2, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func2 using foo {\n"
													+ "x = foo + 2\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func2(7)");
		b.evaluate(tree);
		assertEquals(9, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunctionCall("func2(10)");
		b.evaluate(tree);
		assertEquals(12, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunctionCall("func2()");
		try{
			b.evaluate(tree);
			fail();
		} catch (RuntimeException e) { }
		assertEquals(1, b.scopes.size());
		
		tree = useFunctionCall("func2(2, 3)");
		try{
			b.evaluate(tree);
			fail();
		} catch (RuntimeException e) { }
		assertEquals(1, b.scopes.size());
		
		tree = useFunctionCall("func7(2)");
		try {
			b.evaluate(tree);
			fail();
		} catch (IllegalArgumentException e) { }
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func3 using u {\n"
												+ "return 2 * u\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func3(7)");
		assertEquals(14, b.evaluate(tree), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func4 using v {\n"
												+ "x = func3(13) + v\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func4(0.2)");
		b.evaluate(tree);
		assertEquals(26.2, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func5 using u {\n"
												+ "return u * func3(5)\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func5(3)");
		assertEquals(30, b.evaluate(tree), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func6 using u {\n"
												+ "x = 100\n"
												+ "return u\n"
												+ "x = 200\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func6(3)");
		b.evaluate(tree);
		assertEquals(100, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func7 using u {\n"
												+ "x = 100\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func7(3)");
		assertEquals(0, b.evaluate(tree), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func8 using u {\n"
												+ "switch{\n"
													+ "case u = 1\n"
														+ "return u\n"
													+ "case u > 1\n"
														+ "return 1 + func8(u - 1)\n"
													+ "}\n"
												+ "}\n");
		b.interpret(tree);
		tree = useFunctionCall("func8(400)");
		assertEquals(400, b.evaluate(tree), 0);
		assertEquals(1, b.scopes.size());
		
		tree = useFunction("define func9 using u {\n"
												+ "x = 10\n"
												+ "}\n");
		b.interpret(tree);
		tree = useCommand("do func9(400)\n");
		b.interpret(tree);
		assertEquals(10, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
	}
	
	@Test
	public void testDoStatement() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		tree = useFunction("define func9 using u {\n"
												+ "x = 10\n"
												+ "}\n");
		b.interpret(tree);
		tree = useCommand("do func9(400)\n");
		b.interpret(tree);
		assertEquals(10, b.fetch("x"), 0);
		assertEquals(1, b.scopes.size());
	}
	
	@Test
	public void testInterpretBug() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b1 = new Bug(i);
		
		assertEquals(0, b1.fetch("x"), 0);
		assertEquals(0, b1.fetch("y"), 0);
		assertEquals(0, b1.fetch("angle"), 0);
		tree = useBugDefinition("Bug bugA {\n"
										+ "var foo\n"
										+ "initially {\n"
													+ "x = 50\n"
													+ "y = 50\n"
										+ "}\n"
										+ "turnto 90\n"
										+ "move 2\n"
								+ "}\n");
		b1.interpret(tree);
		assertEquals(50, b1.fetch("x"), 0);
		assertEquals(52, b1.fetch("y"), 0);
		assertEquals(90, b1.fetch("angle"), 0);
		
		Bug b2 = new Bug(i);
		assertEquals(0, b2.fetch("x"), 0);
		assertEquals(0, b2.fetch("y"), 0);
		assertEquals(0, b2.fetch("angle"), 0);
		tree = useBugDefinition("Bug bugB {\n"
										+ "var foo\n"
										+ "initially {\n"
													+ "x = 50\n"
													+ "y = 50\n"
													+ "foo = 25\n"
										+ "}\n"
										+ "turnto 90\n"
										+ "move 2\n"
								+ "}\n");
		b2.interpret(tree);
		assertEquals(50, b2.fetch("x"), 0);
		assertEquals(52, b2.fetch("y"), 0);
		assertEquals(90, b2.fetch("angle"), 0);
		assertEquals(25, b2.fetch("foo"), 0);
		
		Bug b3 = new Bug(i);
		assertEquals(0, b3.fetch("x"), 0);
		assertEquals(0, b3.fetch("y"), 0);
		assertEquals(0, b3.fetch("angle"), 0);
		tree = useBugDefinition("Bug bugC {\n"
										+ "var foo\n"
										+ "var bar\n"
										+ "initially {\n"
													+ "x = 50\n"
													+ "y = 50\n"
													+ "foo = 25\n"
										+ "}\n"
										+ "turnto 90\n"
										+ "move 2\n"
								+ "}\n");
		b3.interpret(tree);
		assertEquals(50, b3.fetch("x"), 0);
		assertEquals(52, b3.fetch("y"), 0);
		assertEquals(90, b3.fetch("angle"), 0);
		assertEquals(25, b3.fetch("foo"), 0);
		assertEquals(0, b3.fetch("bar"), 0);
		
		Bug b4 = new Bug(i);
		assertEquals(0, b4.fetch("x"), 0);
		assertEquals(0, b4.fetch("y"), 0);
		assertEquals(0, b4.fetch("angle"), 0);
		tree = useBugDefinition("Bug bugD {\n"
										+ "var foo\n"
										+ "initially {\n"
													+ "x = 50\n"
													+ "y = 50\n"
													+ "foo = 30\n"
										+ "}\n"
										+ "moveto func1(foo), func1(x)\n"
										+ "define func1 using z {\n"
																+ "return 2 * z\n"
															+ "}\n"
								+ "}\n");
		b4.interpret(tree);
		assertEquals(60, b4.fetch("x"), 0);
		assertEquals(100, b4.fetch("y"), 0);
		assertEquals(0, b4.fetch("angle"), 0);
		assertEquals(30, b4.fetch("foo"), 0);
	}

	@Test
	public void testInterpretVar() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		try {
			b.fetch("foo");
			fail();
		} catch (IllegalArgumentException e) { }
		tree = useVarDeclaration("var foo\n");
		b.interpret(tree);
		assertEquals(b.fetch("foo"), 0, 0);
		
		try {
			b.fetch("bar");
			fail();
		} catch (IllegalArgumentException e) { }
		tree = useVarDeclaration("var bar, pie\n");
		b.interpret(tree);
		assertEquals(b.fetch("bar"), 0, 0);
		assertEquals(b.fetch("pie"), 0, 0);
		
		tree = useVarDeclaration("var x\n");
		b.interpret(tree);
		assertEquals(b.fetch("x"), 0, 0);
	}

	@Test
	public void testInterpretInitially() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		tree = useInitializationBlock("initially {\n"
												+ "x = 1\n"
												+ "y = 2\n"
												+ "}\n");
		b.interpret(tree);
		assertEquals(1, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
		
		tree = useInitializationBlock("initially {\n"
												+ "move 2\n"
												+ "turnto 90\n"
												+ "}\n");
		b.interpret(tree);
		assertEquals(3, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
		assertEquals(90, b.fetch("angle"), 0);
		
		tree = useInitializationBlock("initially {\n"
												+ "x = 1\n"
												+ "x = 2\n"
												+ "}\n");
		b.interpret(tree);
		assertEquals(2, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
	}

	@Test
	public void testInterpretBlock() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		tree = useBlock("{\n"
						+ "x = 1\n"
						+ "y = 2\n"
						+ "}\n");
		b.interpret(tree);
		assertEquals(1, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
		
		tree = useBlock("{\n"
				+ "move 2\n"
				+ "turnto 90\n"
				+ "}\n");
		b.interpret(tree);
		assertEquals(3, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
		assertEquals(90, b.fetch("angle"), 0);
		
		tree = useBlock("{\n"
				+ "x = 1\n"
				+ "x = 2\n"
				+ "}\n");
		b.interpret(tree);
		assertEquals(2, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
	}

	@Test
	public void testInterpretMove() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		tree = useCommand("move 4\n");
		b.interpret(tree);
		assertEquals(4, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		
		tree = useCommand("turnto 45\n");
		b.interpret(tree);
		tree = useCommand("move 2\n");
		b.interpret(tree);
		assertEquals(4 + Math.sqrt(2), b.fetch("x"), 0.001);
		assertEquals(Math.sqrt(2), b.fetch("y"), 0.001);
		
		tree = useCommand("turnto 135\n");
		b.interpret(tree);
		tree = useCommand("move 2\n");
		b.interpret(tree);
		assertEquals(4, b.fetch("x"), 0.001);
		assertEquals(2 * Math.sqrt(2), b.fetch("y"), 0.001);
	}

	@Test
	public void testInterpretMoveto() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		tree = useCommand("moveto 1, 2\n");
		b.interpret(tree);
		assertEquals(1, b.fetch("x"), 0);
		assertEquals(2, b.fetch("y"), 0);
		
		tree = useCommand("moveto -100, 2.7\n");
		b.interpret(tree);
		assertEquals(-100, b.fetch("x"), 0);
		assertEquals(2.7, b.fetch("y"), 0);
	}

	@Test
	public void testInterpretTurn() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("angle"), 0);
		tree = useCommand("turn 90\n");
		b.interpret(tree);
		assertEquals(90, b.fetch("angle"), 0);
		
		tree = useCommand("turn -100\n");
		b.interpret(tree);
		assertEquals(350, b.fetch("angle"), 0);
		
		tree = useCommand("turn 20\n");
		b.interpret(tree);
		assertEquals(10, b.fetch("angle"), 0);
	}

	@Test
	public void testInterpretTurnto() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("angle"), 0);
		tree = useCommand("turnto 90\n");
		b.interpret(tree);
		assertEquals(90, b.fetch("angle"), 0);
		
		tree = useCommand("turnto 270\n");
		b.interpret(tree);
		assertEquals(270, b.fetch("angle"), 0);
		
		tree = useCommand("turnto -90\n");
		b.interpret(tree);
		assertEquals(270, b.fetch("angle"), 0);
		
		tree = useCommand("turnto 370\n");
		b.interpret(tree);
		assertEquals(10, b.fetch("angle"), 0);
	}

//	@Test
//	public void testInterpretReturn() {
//		Tree<Token> tree;
//		Bug b = new Bug();
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testInterpretLine() {
//		Tree<Token> tree;
//		Bug b = new Bug();
//		fail("Not yet implemented");
//	}

	@Test
	public void testInterpretAssign() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		tree = useCommand("x = 7\n");
		b.interpret(tree);
		assertEquals(7, b.fetch("x"), 0);
		
		tree = useCommand("foo = 10\n");
		try {
			b.interpret(tree);
			fail();
		} catch (IllegalArgumentException e) { }
		
		tree = useVarDeclaration("var x, foo\n");
		b.interpret(tree);
		tree = useCommand("foo = 10\n");
		b.interpret(tree);
		assertEquals(10, b.fetch("foo"), 0);
		
		tree = useCommand("foo = 9\n");
		b.interpret(tree);
		assertEquals(9, b.fetch("foo"), 0);
	}

	@Test
	public void testInterpretLoop() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		tree = useCommand("loop {\n"
								+ "x = x + 1\n"
								+ "exit if x >= 10\n"
							+ "}\n");
		b.interpret(tree);
		assertEquals(10, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		
		tree = useCommand("loop {\n"
								+ "x = x - 2\n"
								+ "y = y + 1\n"
								+ "exit if x <= 0\n"
							+ "}\n");
		b.interpret(tree);
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(5, b.fetch("y"), 0);
		
		b.store("x", 1);
		tree = useCommand("loop {\n"
								+ "x = x * 2\n"
								+ "y = 0\n"
								+ "exit if x > 20\n"
								+ "y = 1\n"
							+ "}\n");
		b.interpret(tree);
		assertEquals(32, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		
		b.store("x", 0);
		b.store("y", 0);
		tree = useCommand("loop {\n"
								+ "x = x + 1\n"
								+ "y = 1\n"
								+ "loop {\n"
										+ "exit if y >= 3\n"
										+ "x = x * 2\n"
										+ "y = y + 1\n"
									+ "}\n"
								+ "exit if x > 20\n"
							+ "}\n");
		b.interpret(tree);
		assertEquals(84, b.fetch("x"), 0);
		assertEquals(3, b.fetch("y"), 0);
	}

	@Test
	public void testInterpretSwitch() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		tree = useCommand("switch {\n"
							+ "case x = 0\n"
								+ "x = 1\n"
							+ "case y = 0\n"
								+ "y = 1\n"
							+ "case x + y > 10\n"
								+ "x = 0\n"
								+ "y = 0\n"
						+ "}\n");
		b.interpret(tree);
		assertEquals(1, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);
		
		b.interpret(tree);
		assertEquals(1, b.fetch("x"), 0);
		assertEquals(1, b.fetch("y"), 0);
		
		b.interpret(tree);
		assertEquals(1, b.fetch("x"), 0);
		assertEquals(1, b.fetch("y"), 0);
		
		b.store("x", 10);
		
		b.interpret(tree);
		assertEquals(0, b.fetch("x"), 0);
		assertEquals(0, b.fetch("y"), 0);		
	}

	@Test
	public void testInterpretColor() {
		Tree<Token> tree;
		Interpreter i = new Interpreter();
		Bug b = new Bug(i);
		
		assertNull(b.bugColor);
		tree = useCommand("color blue\n");
		b.interpret(tree);
		assertEquals(b.bugColor, Color.blue);
		
		tree = useCommand("color lightGray\n");
		b.interpret(tree);
		assertEquals(b.bugColor, Color.lightGray);
	}
	
//	--------------- Helper methods ------------------
	
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
