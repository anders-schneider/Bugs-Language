package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;


/**
 * Test class for Bugs recognizer.
 * @author Anders Schneider
 */
public class RecognizerTest {
    
    Recognizer r0, r1, r2, r3, r4, r5, r6, r7, r8;
    
    /**
     * Constructor for RecognizerTest.
     */
    public RecognizerTest() {
        r0 = new Recognizer("2 + 2");
        r1 = new Recognizer("");
    }


    @Before
    public void setUp() throws Exception {
        r0 = new Recognizer("");
        r1 = new Recognizer("250");
        r2 = new Recognizer("hello");
        r3 = new Recognizer("(xyz + 3)");
        r4 = new Recognizer("12 * 5 - 3 * 4 / 6 + 8");
        r5 = new Recognizer("12 * ((5 - 3) * 4) / 6 + (8)");
        r6 = new Recognizer("17 +");
        r7 = new Recognizer("22 *");
        r8 = new Recognizer("#");
    }

    @Test
    public void testRecognizer() {
        r0 = new Recognizer("");
        r1 = new Recognizer("2 + 2");
    }

    @Test
    public void testIsArithmeticExpression() {
        assertTrue(r1.isArithmeticExpression());
        assertTrue(r2.isArithmeticExpression());
        assertTrue(r3.isArithmeticExpression());
        assertTrue(r4.isArithmeticExpression());
        assertTrue(r5.isArithmeticExpression());

        assertFalse(r0.isArithmeticExpression());
        assertFalse(r8.isArithmeticExpression());

        try {
            assertFalse(r6.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            assertFalse(r7.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testIsArithmeticExpressionWithUnaryMinus() {
        assertTrue(new Recognizer("-5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(-5*10)").isArithmeticExpression());
        assertTrue(new Recognizer("+5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(+5*10)").isArithmeticExpression());
    }

    @Test
    public void testIsTerm() {
        assertFalse(r0.isTerm()); // ""
        
        assertTrue(r1.isTerm()); // "250"
        
        assertTrue(r2.isTerm()); // "hello"
        
        assertTrue(r3.isTerm()); // "(xyz + 3)"
        followedBy(r3, "");
        
        assertTrue(r4.isTerm());  // "12 * 5 - 3 * 4 / 6 + 8"
        assertEquals(new Token(Token.Type.SYMBOL, "-"), r4.nextToken());
        assertTrue(r4.isTerm());
        followedBy(r4, "+ 8");

        assertTrue(r5.isTerm());  // "12 * ((5 - 3) * 4) / 6 + (8)"
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r5.nextToken());
        assertTrue(r5.isTerm());
        followedBy(r5, "");
    }

    @Test
    public void testIsFactor() {
        assertTrue(r1.isFactor());
        assertTrue(r2.isFactor());
        assertTrue(r3.isFactor());
        assertTrue(r4.isFactor()); followedBy(r4, "* 5 - 3 * 4 / 6 + 8");
        assertTrue(r5.isFactor()); followedBy(r5, "* ((5");
        assertTrue(r6.isFactor()); followedBy(r6, "+");
        assertTrue(r7.isFactor()); followedBy(r7, "*");

        assertFalse(r0.isFactor());
        assertFalse(r8.isFactor()); followedBy(r8, "#");

        Recognizer r = new Recognizer("foo()");
        assertTrue(r.isFactor());
        r = new Recognizer("bar(5, abc, 2+3)+");
        assertTrue(r.isFactor()); followedBy(r, "+");

        r = new Recognizer("foo.bar$");
        assertTrue(r.isFactor()); followedBy(r, "$");
        
        r = new Recognizer("123.123");
        assertEquals(new Token(Token.Type.NUMBER, "123.123"), r.nextToken());
        
        r = new Recognizer("5");
        assertEquals(new Token(Token.Type.NUMBER, "5.0"), r.nextToken());
    }
    
    @Test
    public void testIsParameterList() {
        Recognizer r = new Recognizer("() $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(5) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(bar, x+3) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
    }

    @Test
    public void testIsAddOperator() {
        Recognizer r = new Recognizer("+ - $");
        assertTrue(r.isAddOperator());
        assertTrue(r.isAddOperator());
        assertFalse(r.isAddOperator());
        followedBy(r, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        Recognizer r = new Recognizer("* / $");
        assertTrue(r.isMultiplyOperator());
        assertTrue(r.isMultiplyOperator());
        assertFalse(r.isMultiplyOperator());
        followedBy(r, "$");
    }

    @Test
    public void testIsVariable() {
        Recognizer r = new Recognizer("foo 23 bar +");
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isFactor());
        
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isAddOperator());
    }
    
	@Test
	public void testIsAction() {
		Recognizer r1 = new Recognizer("move foo + 2\n");
		Recognizer r2 = new Recognizer("moveto 56, bar - 7\n");
		Recognizer r3 = new Recognizer("turn bar\n");
		Recognizer r4 = new Recognizer("turnto 56 / 23\n");
		Recognizer r5 = new Recognizer("line 56, x + y, (77), something\n");
		Recognizer r6 = new Recognizer("moveturn 57\n");
		
		assertTrue(r1.isAction());
		assertTrue(r2.isAction());
		assertTrue(r3.isAction());
		assertTrue(r4.isAction());
		assertTrue(r5.isAction());
		assertFalse(r6.isAction());
	}

	@Test
	public void testIsAllbugsCode() {
		Recognizer r1 = new Recognizer("Allbugs { \n" +
										"}\n");
		
		Recognizer r2 = new Recognizer("Allbugs { \n" +
										"var foo, bar\n" +
										"}\n");

		Recognizer r3 = new Recognizer("Allbugs { \n" +
										"var foo, bar\n" +
										"var x, y\n\n" +
										"}\n");

		Recognizer r4 = new Recognizer("Allbugs { \n" +
										"var foo, bar\n" +
										"var x, y\n\n" +
										"define fun1 using x {\n}\n" +
										"}\n");

		Recognizer r5 = new Recognizer("Allbugs { \n" +
										"var foo, bar\n" +
										"var x, y\n\n" +
										"define fun1 using x {\n}\n" +
										"define fun2 {\n}\n" +
										"}\n");
		
		Recognizer r6 = new Recognizer("{ \n" +
										"var foo, bar\n" +
										"var x, y\n\n" +
										"define fun1 using x {\n}\n" +
										"define fun2 {\n}\n" +
										"}\n");
		
		Recognizer r7 = new Recognizer("Allbugs { \n" +
										"var foo, bar\n" +
										"var x, y\n\n" +
										"define fun1 using x {\n}\n" +
										"define fun2 {\n}\n");
														
		assertTrue(r1.isAllbugsCode());
		assertTrue(r2.isAllbugsCode());
		assertTrue(r3.isAllbugsCode());
		assertTrue(r4.isAllbugsCode());
		assertTrue(r5.isAllbugsCode());
		assertFalse(r6.isAllbugsCode());
		
		try{
			assertTrue(r7.isAllbugsCode());
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsAssignmentStatement() {
		Recognizer r1 = new Recognizer("x = 5\n");
		Recognizer r2 = new Recognizer("5 = x\n");
		Recognizer r3 = new Recognizer("= x + 10\n");
		Recognizer r4 = new Recognizer("x + 9 = y\n");
		Recognizer r5 = new Recognizer("x y + 7\n");
		Recognizer r6 = new Recognizer("x =\n");
		Recognizer r7 = new Recognizer("x = 5*y");
		
		assertTrue(r1.isAssignmentStatement());
		assertFalse(r2.isAssignmentStatement());
		assertFalse(r3.isAssignmentStatement());
		
		try{
			assertTrue(r4.isAssignmentStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isAssignmentStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isAssignmentStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r7.isAssignmentStatement());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsBlock() {
		Recognizer r1 = new Recognizer("{\n\n }\n");
		Recognizer r2 = new Recognizer("{ \n move foo\n} \n");
		Recognizer r3 = new Recognizer("{ \n move foo\n do x\n } \n");
		Recognizer r4 = new Recognizer("\n move foo\n}");
		Recognizer r5 = new Recognizer("{ }\n");
		Recognizer r6 = new Recognizer("{ \n foo + bar > foo\n \n");
		
		assertTrue(r1.isBlock());
		assertTrue(r2.isBlock());
		assertTrue(r3.isBlock());
		assertFalse(r4.isBlock());
				
		try{
			assertTrue(r5.isBlock());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isBlock());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsBugDefinition() {
		Recognizer r1 = new Recognizer("Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n");
		Recognizer r2 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"move 66 + 7\n" + 
										"}\n\n");
		
		Recognizer r3 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"var x\n" +
										"move 66 + 7\n" + 
										"}\n\n");
		
		Recognizer r4 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"var x\n" +
										"initially { \n move foo\n} \n" +
										"move 66 + 7\n" + 
										"}\n\n");
		
		Recognizer r5 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"var x\n" +
										"initially { \n move foo\n} \n" +
										"move 66 + 7\n" +
										"turn x\n" +
										"}\n\n");
		
		Recognizer r6 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"var x\n" +
										"initially { \n move foo\n} \n" +
										"move 66 + 7\n" +
										"turn x\n" +
										"define fun3 using x, y {\n}\n" +
										"}\n\n");

		Recognizer r7 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"var x\n" +
										"initially { \n move foo\n} \n" +
										"move 66 + 7\n" +
										"turn x\n" +
										"define fun3 using x, y {\n}\n" +
										"define fun1 {\n}\n" +
										"}\n\n");

		Recognizer r8 = new Recognizer("Bug bug1 { \n" +
										"var foo, bar\n\n" +
										"var x\n" +
										"initially { \n move foo\n} \n" +
										"define fun3 using x, y {\n}\n" +
										"define fun1 {\n}\n" +
										"}\n\n");
		
		assertTrue(r1.isBugDefinition());
		assertTrue(r2.isBugDefinition());
		assertTrue(r3.isBugDefinition());
		assertTrue(r4.isBugDefinition());
		assertTrue(r5.isBugDefinition());
		assertTrue(r6.isBugDefinition());
		assertTrue(r7.isBugDefinition());
		
		try{
			assertTrue(r8.isBugDefinition());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsColorStatement() {
		Recognizer r1 = new Recognizer("color loop \n");
		Recognizer r2 = new Recognizer("brown do \n");
		Recognizer r3 = new Recognizer("color \n");
		Recognizer r4 = new Recognizer("color notakeyword \n");
		Recognizer r5 = new Recognizer("color color \n \n \n");
		
		assertTrue(r1.isColorStatement());
		assertFalse(r2.isColorStatement());
		
		try {
			assertTrue(r3.isColorStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try {
			assertTrue(r4.isColorStatement());
			fail();
		} catch (SyntaxException e) {}
				
		assertTrue(r5.isColorStatement());
	}

	@Test
	public void testIsCommand() {
		Recognizer r1 = new Recognizer("move 66 + 7\n");
		Recognizer r2 = new Recognizer("do foo(77, x)\n");
		Recognizer r3 = new Recognizer("return 44 < 34\n");
		Recognizer r4 = new Recognizer("line 55, 56, 7, 23\n");
		Recognizer r5 = new Recognizer("55 < x\n");
		Recognizer r6 = new Recognizer("23 + x\n");
		
		assertTrue(r1.isCommand());
		assertTrue(r2.isCommand());
		assertTrue(r3.isCommand());
		assertTrue(r4.isCommand());
		assertFalse(r5.isCommand());
		assertFalse(r6.isCommand());
	}

	@Test
	public void testIsComparator() {
		Recognizer r = new Recognizer("< > foo = bar <= + >= != ! 9");
		
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertTrue(r.isVariable());
		assertTrue(r.isComparator());
		assertTrue(r.isVariable());
		assertTrue(r.isComparator());
		assertFalse(r.isComparator());
		assertTrue(r.isAddOperator());
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		
		try {
			assertTrue(r.isComparator());
			fail();
		}
		catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsDoStatement() {
		Recognizer r1 = new Recognizer("do foo (9, x + 5) \n");
		Recognizer r2 = new Recognizer("foo (9, x + 5) \n");
		Recognizer r3 = new Recognizer("do (9, x + 5) \n");
		Recognizer r4 = new Recognizer("do foo (9, x + 5)");
		
		Recognizer r5 = new Recognizer("do bar (27) \n");
		Recognizer r6 = new Recognizer("do bar \n");
		Recognizer r7 = new Recognizer("do bar (27) (a) (99, b) \n");
		
		assertTrue(r1.isDoStatement());
		assertFalse(r2.isDoStatement());
		
		try{
			assertTrue(r3.isDoStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r4.isDoStatement());
			fail();
		} catch (SyntaxException e) {}
		
		assertTrue(r5.isDoStatement());
		assertTrue(r6.isDoStatement());
		
		try{
			assertFalse(r7.isDoStatement());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsEol() {
		Recognizer r1 = new Recognizer("\n");
		Recognizer r2 = new Recognizer("var \n");
		Recognizer r3 = new Recognizer("");
		
		assertTrue(r1.isEol());
		
		assertFalse(r2.isEol());
		r2.nextToken();
		assertTrue(r2.isEol());
		
		assertFalse(r3.isEol());
	}

	@Test
	public void testIsExitIfStatement() {
		Recognizer r1 = new Recognizer("exit if x > 2\n");
		Recognizer r2 = new Recognizer("exit if 1 = 17\n");
		Recognizer r3 = new Recognizer("exit if 2 + 9\n\n");
		Recognizer r4 = new Recognizer("if 2 < 4\n");
		Recognizer r5 = new Recognizer("exit x + 2\n");
		Recognizer r6 = new Recognizer("exit if\n");
		Recognizer r7 = new Recognizer("exit if x > 1");
		
		assertTrue(r1.isExitIfStatement());
		assertTrue(r2.isExitIfStatement());
		assertTrue(r3.isExitIfStatement());
		assertFalse(r4.isExitIfStatement());
		
		try{
			assertTrue(r5.isExitIfStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isExitIfStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r7.isExitIfStatement());
			fail();
		} catch (SyntaxException e) {}
	}
	
	@Test
	public void testIsExpression() {
		Recognizer r1 = new Recognizer("5 + 7 < 13");
		Recognizer r2 = new Recognizer("-bar = foo + 2");
		Recognizer r3 = new Recognizer("bar + 2 <= foo / 2");
		Recognizer r4 = new Recognizer("bar = foo = x = 8 <= 10");
		Recognizer r5 = new Recognizer("bar + 2");
		Recognizer r6 = new Recognizer("(5, 7)");
		Recognizer r7 = new Recognizer("fun = fun +");
		
		assertTrue(r1.isExpression());
		assertTrue(r2.isExpression());
		assertTrue(r3.isExpression());
		assertTrue(r4.isExpression());
		assertTrue(r5.isExpression());
		
		try{
			assertTrue(r6.isExpression());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r7.isExpression());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsFunctionCall() {
		Recognizer r1 = new Recognizer("foo(22)");
		Recognizer r2 = new Recognizer("bar(bar)");
		Recognizer r3 = new Recognizer("fun(9 / x)");
		Recognizer r4 = new Recognizer("(9)");
		Recognizer r5 = new Recognizer("bar x");
		
		assertTrue(r1.isFunctionCall());
		assertTrue(r2.isFunctionCall());
		assertTrue(r3.isFunctionCall());
		assertFalse(r4.isFunctionCall());
		
		try {
			assertTrue(r5.isFunctionCall());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsFunctionDefinition() {
		Recognizer r1 = new Recognizer("define fun1 {\n}\n");
		Recognizer r2 = new Recognizer("define fun2 using x {\n}\n");
		Recognizer r3 = new Recognizer("define fun3 using x, y {\n}\n");
		Recognizer r4 = new Recognizer("fun4 {\n}\n");
		Recognizer r5 = new Recognizer("define fun5 x, y {\n}\n");
		Recognizer r6 = new Recognizer("define fun6 using {\n}\n");
		Recognizer r7 = new Recognizer("define fun7 using x, y");
		
		assertTrue(r1.isFunctionDefinition());
		assertTrue(r2.isFunctionDefinition());
		assertTrue(r3.isFunctionDefinition());
		assertFalse(r4.isFunctionDefinition());
		
		try {
			assertTrue(r5.isFunctionDefinition());
			fail();
		} catch (SyntaxException e) {}
		
		try {
			assertTrue(r6.isFunctionDefinition());
			fail();
		} catch (SyntaxException e) {}
		
		try {
			assertTrue(r7.isFunctionDefinition());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsInitializationBlock() {
		Recognizer r1 = new Recognizer("initially {\n\n }\n");
		Recognizer r2 = new Recognizer("initially { \n move foo\n} \n");
		Recognizer r3 = new Recognizer("{ \n move foo\n do x\n } \n");
		Recognizer r5 = new Recognizer("initially { }\n");
		Recognizer r6 = new Recognizer("initially { \n foo + bar > foo\n \n");
		
		assertTrue(r1.isInitializationBlock());
		assertTrue(r2.isInitializationBlock());
		assertFalse(r3.isInitializationBlock());
				
		try{
			assertTrue(r5.isInitializationBlock());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isInitializationBlock());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsLineAction() {
		Recognizer r1 = new Recognizer("line 45, x + 9, (90), foo\n");
		Recognizer r2 = new Recognizer("line foo, foo, foo, foo\n\n");
		Recognizer r3 = new Recognizer("foo, foo, foo, foo\n\n");
		Recognizer r4 = new Recognizer("line , bar, bar, bar\n");
		Recognizer r5 = new Recognizer("line foo + 0, foo + 1, foo + 2\n");
		Recognizer r6 = new Recognizer("line 45, x + 9, (90), foo");
		
		assertTrue(r1.isLineAction());
		assertTrue(r2.isLineAction());
		assertFalse(r3.isLineAction());
		
		try{
			assertTrue(r4.isLineAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isLineAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isLineAction());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsLoopStatement() {
		Recognizer r1 = new Recognizer("loop {\n\n }\n");
		Recognizer r2 = new Recognizer("loop { \n move foo\n} \n");
		Recognizer r3 = new Recognizer("{ \n move foo\n do x\n } \n");
		Recognizer r5 = new Recognizer("loop { }\n");
		Recognizer r6 = new Recognizer("loop { \n foo + bar > foo\n \n");
		
		assertTrue(r1.isLoopStatement());
		assertTrue(r2.isLoopStatement());
		assertFalse(r3.isLoopStatement());
				
		try{
			assertTrue(r5.isLoopStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isLoopStatement());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsMoveAction() {
		Recognizer r1 = new Recognizer("move 45\n");
		Recognizer r2 = new Recognizer("move foo\n\n");
		Recognizer r3 = new Recognizer("foo\n\n");
		Recognizer r4 = new Recognizer("move \n");
		Recognizer r5 = new Recognizer("move foo + 0, foo + 1\n");
		Recognizer r6 = new Recognizer("move 45");
		
		assertTrue(r1.isMoveAction());
		assertTrue(r2.isMoveAction());
		assertFalse(r3.isMoveAction());
		
		try{
			assertTrue(r4.isMoveAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isMoveAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isMoveAction());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsMoveToAction() {
		Recognizer r1 = new Recognizer("moveto 45, x + 9\n");
		Recognizer r2 = new Recognizer("moveto foo, foo\n\n");
		Recognizer r3 = new Recognizer("foo, foo, foo, foo\n\n");
		Recognizer r4 = new Recognizer("moveto , bar\n");
		Recognizer r5 = new Recognizer("moveto foo + 0\n");
		Recognizer r6 = new Recognizer("moveto 45, x + 9");
		
		assertTrue(r1.isMoveToAction());
		assertTrue(r2.isMoveToAction());
		assertFalse(r3.isMoveToAction());
		
		try{
			assertTrue(r4.isMoveToAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isMoveToAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isMoveToAction());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsProgram() {
		Recognizer r1 = new Recognizer("Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n");

		Recognizer r2 = new Recognizer("Allbugs { \n" + 
										"var foo, bar\n" +
										"}\n" +
										"Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n");

		Recognizer r3 = new Recognizer("Allbugs { \n" + 
										"var foo, bar\n" +
										"}\n" +
										"Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n" +
										"Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n");

		Recognizer r4 = new Recognizer("Allbugs { \n" + 
										"var foo, bar\n" +
										"}\n" +
										"Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n" +
										"Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n" +
										"Bug bug1 { \n" + 
										"move 66 + 7\n" + 
										"}\n\n");

		Recognizer r5 = new Recognizer("Allbugs { \n" + 
										"var foo, bar\n" +
										"}\n");

		assertTrue(r1.isProgram());
		assertTrue(r2.isProgram());
		assertTrue(r3.isProgram());
		assertTrue(r4.isProgram());
		
		try{
			assertTrue(r5.isProgram());
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsReturnStatement() {
		Recognizer r1 = new Recognizer("return x > 2\n");
		Recognizer r2 = new Recognizer("return 1 = 17\n");
		Recognizer r3 = new Recognizer("return 2 + 9\n\n");
		Recognizer r4 = new Recognizer("2 < 4\n");
		Recognizer r6 = new Recognizer("return\n");
		Recognizer r7 = new Recognizer("return x > 1");
		
		assertTrue(r1.isReturnStatement());
		assertTrue(r2.isReturnStatement());
		assertTrue(r3.isReturnStatement());
		assertFalse(r4.isReturnStatement());
		
		try{
			assertTrue(r6.isReturnStatement());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r7.isReturnStatement());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsStatement() {
		Recognizer r1 = new Recognizer("loop {\n\n }\n");
		Recognizer r2 = new Recognizer("return x > 2\n");
		Recognizer r3 = new Recognizer("x = 5\n");
		Recognizer r4 = new Recognizer("exit if x > 2\n");
		Recognizer r5 = new Recognizer("switch {\n case x = 5\n move 66 + 7\n turn x\n }\n");		
		Recognizer r6 = new Recognizer("do foo (9, x + 5) \n");
		Recognizer r7 = new Recognizer("color loop \n");
		
		assertTrue(r1.isStatement());
		assertTrue(r2.isStatement());
		assertTrue(r3.isStatement());
		assertTrue(r4.isStatement());
		assertTrue(r5.isStatement());
		assertTrue(r6.isStatement());
		assertTrue(r7.isStatement());
		
		Recognizer r8 = new Recognizer("moveto 45, x + 9");
		Recognizer r9 = new Recognizer("turnto foo\n\n");
		
		assertFalse(r8.isStatement());
		assertFalse(r9.isStatement());
		
		Recognizer r10 = new Recognizer("switch {\n case x = 5\n move 66 + 7\n turn x }\n");
		
		try{
			assertTrue(r10.isStatement());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsSwitchStatement() {
		Recognizer r1 = new Recognizer("switch {\n}\n");
		Recognizer r2 = new Recognizer("switch {\n case x = 5\n }\n");
		Recognizer r3 = new Recognizer("switch {\n case x = 5\n move 66 + 7\n }\n");
		Recognizer r4 = new Recognizer("switch {\n case x = 5\n case x = 6\n }\n");
		Recognizer r5 = new Recognizer("switch {\n case x = 5\n move 66 + 7\n case x = 6\n turn x\n }\n");
		Recognizer r6 = new Recognizer("switch {\n case x = 5\n move 66 + 7\n turn x\n }\n");
		Recognizer r7 = new Recognizer("{\n}\n");
		Recognizer r8 = new Recognizer("switch {\n case x = 5\n move 66 + 7\n turn x }\n");
		
		assertTrue(r1.isSwitchStatement());
		assertTrue(r2.isSwitchStatement());
		assertTrue(r3.isSwitchStatement());
		assertTrue(r4.isSwitchStatement());
		assertTrue(r5.isSwitchStatement());
		assertTrue(r6.isSwitchStatement());
		assertFalse(r7.isSwitchStatement());
		
		try{
			assertTrue(r8.isSwitchStatement());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsTurnAction() {
		Recognizer r1 = new Recognizer("turn 45\n");
		Recognizer r2 = new Recognizer("turn foo\n\n");
		Recognizer r3 = new Recognizer("foo\n\n");
		Recognizer r4 = new Recognizer("turn \n");
		Recognizer r5 = new Recognizer("turn foo + 0, foo + 1\n");
		Recognizer r6 = new Recognizer("turn 45");
		
		assertTrue(r1.isTurnAction());
		assertTrue(r2.isTurnAction());
		assertFalse(r3.isTurnAction());
		
		try{
			assertTrue(r4.isTurnAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isTurnAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isTurnAction());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsTurnToAction() {
		Recognizer r1 = new Recognizer("turnto 45\n");
		Recognizer r2 = new Recognizer("turnto foo\n\n");
		Recognizer r3 = new Recognizer("foo\n\n");
		Recognizer r31 = new Recognizer("turn 56\n");
		Recognizer r4 = new Recognizer("turnto \n");
		Recognizer r5 = new Recognizer("turnto foo + 0, foo + 1\n");
		Recognizer r6 = new Recognizer("turnto 45");
		
		assertTrue(r1.isTurnToAction());
		assertTrue(r2.isTurnToAction());
		assertFalse(r3.isTurnToAction());
		assertFalse(r31.isTurnToAction());
		
		try{
			assertTrue(r4.isTurnToAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isTurnToAction());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isTurnToAction());
			fail();
		} catch (SyntaxException e) {}
	}

	@Test
	public void testIsVarDeclaration() {
		Recognizer r1 = new Recognizer("var foo\n");
		Recognizer r2 = new Recognizer("var foo, bar\n\n");
		Recognizer r3 = new Recognizer("foo\n\n");
		Recognizer r4 = new Recognizer("var \n");
		Recognizer r5 = new Recognizer("var foo, foo + 1\n");
		Recognizer r6 = new Recognizer("var foo");
		
		assertTrue(r1.isVarDeclaration());
		assertTrue(r2.isVarDeclaration());
		assertFalse(r3.isVarDeclaration());
		
		try{
			assertTrue(r4.isVarDeclaration());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r5.isVarDeclaration());
			fail();
		} catch (SyntaxException e) {}
		
		try{
			assertTrue(r6.isVarDeclaration());
			fail();
		} catch (SyntaxException e) {}
	}

    @Test
    public void testSymbol() {
        Recognizer r = new Recognizer("++");
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r.nextToken());
    }

    @Test
    public void testNextTokenMatchesType() {
        Recognizer r = new Recognizer("++abc");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertFalse(r.nextTokenMatches(Token.Type.NAME));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertTrue(r.nextTokenMatches(Token.Type.NAME));
    }

    @Test
    public void testNextTokenMatchesTypeString() {
        Recognizer r = new Recognizer("+abc+");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
        assertTrue(r.nextTokenMatches(Token.Type.NAME, "abc"));
        assertFalse(r.nextTokenMatches(Token.Type.SYMBOL, "*"));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
    }

    @Test
    public void testNextToken() {
        // NAME, KEYWORD, NUMBER, SYMBOL, EOL, EOF };
        Recognizer r = new Recognizer("abc move 25 *\n");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "move"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "*"), r.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), r.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), r.nextToken());
        
        r = new Recognizer("foo.bar 123.456");
        assertEquals(new Token(Token.Type.NAME, "foo"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "."), r.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bar"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "123.456"), r.nextToken());
    }

    @Test
    public void testPushBack() {
        Recognizer r = new Recognizer("abc 25");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        r.pushBack();
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
    }
    
//  ----- "Helper" methods

    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether rejected Tokens are
     * pushed back appropriately.
     * 
     * @param recognizer The Recognizer whose Tokenizer is to be tested.
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Recognizer recognizer, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = recognizer.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);
        expected.ordinaryChar('-');
        expected.ordinaryChar('/');

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(expectedType, actualType);
                if (actualType == StreamTokenizer.TT_WORD) {
                    assertEquals(expected.sval, actual.sval);
                }
                else if (actualType == StreamTokenizer.TT_NUMBER) {
                    assertEquals(expected.nval, actual.nval, 0.001);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
