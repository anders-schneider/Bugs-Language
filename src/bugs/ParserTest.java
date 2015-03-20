package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;


public class ParserTest {
    Parser parser;

    @Test
    public void testParser() {
        parser = new Parser("");
        parser = new Parser("2 + 2");
    }

    @Test
    public void testIsExpression() {
        Tree<Token> expected;
        
        use("250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("250.0"));
        
        use("hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("hello"));

        use("(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "xyz", "3.0"));
        
        use("(xyz + 3) > 7");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree(">", tree("+", "xyz", "3.0"), "7.0"));

        use("a + b + c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("+", "a", "b"), "c"));

        use("a * b * c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("*", tree("*", "a", "b"), "c"));

        use("3 * 12.5 - 7");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("*", "3.0", "12.5"), createNode("7.0")));

        use("12 * 5 - 3 * 4 / 6 + 8");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("-",
                         tree("*", "12.0", "5.0"),
                         tree("/",
                            tree("*", "3.0", "4.0"),
                            "6.0"
                           )
                        ),
                      "8.0"
                     );
        assertStackTopEquals(expected);
                     
        use("12 * ((5 - 3) * 4) / 6 + (8)");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("/",
                         tree("*",
                            "12.0",
                            tree("*",
                               tree("-","5.0","3.0"),
                               "4.0")),
                         "6.0"),
                      "8.0");
        assertStackTopEquals(expected);
        
        use("");
        assertFalse(parser.isExpression());
        
        use("#");
        assertFalse(parser.isExpression());

        try {
            use("17 +");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            use("22 *");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testUnaryOperator() {       
        use("-250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "250.0"));
        
        use("+250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "250.0"));
        
        use("- hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "hello"));

        use("-(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("+", "xyz", "3.0")));

        use("(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("-", "xyz"), "3.0"));

        use("+(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+",
                                        tree("+",
                                                   tree("-", "xyz"), "3.0")));
    }

    @Test
    public void testIsTerm() {        
        use("12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.0"));
        
        use("12.5");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.5"));

        use("3*12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", "3.0", "12.0"));

        use("x * y * z");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", tree("*", "x", "y"), "z"));
        
        use("20 * 3 / 4");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), createNode("4.0")),
                     stackTop());

        use("20 * 3 / 4 + 5");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), "4.0"),
                     stackTop());
        followedBy(parser, "+ 5");
        
        use("");
        assertFalse(parser.isTerm());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isTerm());followedBy(parser, "#");

    }

    @Test
    public void testIsFactor() {
        use("12");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));

        use("hello");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("hello"));
        
        use("(xyz + 3)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("+", "xyz", "3.0"));
        
        use("12 * 5");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));
        followedBy(parser, "* 5.0");
        
        use("17 +");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("17.0"));
        followedBy(parser, "+");

        use("");
        assertFalse(parser.isFactor());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isFactor());
        followedBy(parser, "#");
    }

    @Test
    public void testIsFactor2() {
        use("hello.world");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree(".", "hello", "world"));
        
        use("foo(bar)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar")));
        
        use("foo(bar, baz)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar", "baz")));
        
        use("foo(2*(3+4))");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                 tree("var",
                                     tree("*", "2.0",
                                         tree("+", "3.0", "4.0")))));
    }

    @Test
    public void testIsAddOperator() {
        use("+ - + $");
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertFalse(parser.isAddOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        use("* / $");
        assertTrue(parser.isMultiplyOperator());
        assertTrue(parser.isMultiplyOperator());
        assertFalse(parser.isMultiplyOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testNextToken() {
        use("12 12.5 bogus switch + \n");
        assertEquals(new Token(Token.Type.NUMBER, "12.0"), parser.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "12.5"), parser.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bogus"), parser.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "switch"), parser.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "+"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), parser.nextToken());
    }
    
    @Test
    public void testIsVarDeclaration() {
    	Tree<Token> expected;
    	
    	use("var foo\n");
    	assertTrue(parser.isVarDeclaration());
    	expected = tree("var", "foo");
    	assertStackTopEquals(expected);
    	
    	use("var foo, bar\n");
    	assertTrue(parser.isVarDeclaration());
    	expected = tree("var", "foo", "bar");
    	assertStackTopEquals(expected);
    	
    	use("var foo, bar, something, else\n");
    	assertTrue(parser.isVarDeclaration());
    	expected = tree("var", "foo", "bar", "something", "else");
    	assertStackTopEquals(expected);
    }
    
    @Test
    public void testIsColorStatement() {
    	Tree<Token> expected;
    	
    	use("color gray\n");
    	assertTrue(parser.isColorStatement());
    	expected = tree("color", "gray");
    	assertStackTopEquals(expected);
    	
    	use("color loop\n");
    	assertTrue(parser.isColorStatement());
    	expected = tree("color", "loop");
    	assertStackTopEquals(expected);
    	
    	use("color lightBlue\n");
    	try{
    		assertTrue(parser.isColorStatement());
    		fail();
    	} catch (SyntaxException e) {}
    	
    	use("color gray");
    	try{
    		assertTrue(parser.isColorStatement());
    		fail();
    	} catch (SyntaxException e) {}
    }
    
    @Test
    public void testIsComparator() {
        use("0 = 1 != 2 < 3 >= 4 <= 5 > 6\n");
        parser.nextToken();
        assertTrue(parser.isComparator());
        parser.nextToken();
        assertTrue(parser.isComparator());
        parser.nextToken();
        assertTrue(parser.isComparator());
        parser.nextToken();
        assertTrue(parser.isComparator());
        parser.nextToken();
        assertTrue(parser.isComparator());
        parser.nextToken();
        assertTrue(parser.isComparator());
        parser.nextToken();
        assertFalse(parser.isComparator());
    }
    
    @Test
    public void testIsMoveAction() {
    	Tree<Token> expected;
    	
    	use("move 23\n");
    	assertTrue(parser.isMoveAction());
    	expected = tree("move", "23.0");
    	assertStackTopEquals(expected);
    	
    	use("move xyz + 6\n");
    	assertTrue(parser.isMoveAction());
    	expected = tree("move", tree("+", "xyz", "6.0"));
    	assertStackTopEquals(expected);
    	
    	use("move 23");
    	try{
    		assertTrue(parser.isMoveAction());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("moveto 23\n");
    	assertFalse(parser.isMoveAction());
    	
    	use("move \n");
    	try{
    		assertTrue(parser.isMoveAction());
    		fail();
    	} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsTurnAction() {
    	Tree<Token> expected;
    	
    	use("turn 23\n");
    	assertTrue(parser.isTurnAction());
    	expected = tree("turn", "23.0");
    	assertStackTopEquals(expected);
    	
    	use("turn xyz + 6\n");
    	assertTrue(parser.isTurnAction());
    	expected = tree("turn", tree("+", "xyz", "6.0"));
    	assertStackTopEquals(expected);
    	
    	use("turn 23");
    	try{
    		assertTrue(parser.isTurnAction());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("moveto 23\n");
    	assertFalse(parser.isTurnAction());
    	
    	use("turn \n");
    	try{
    		assertTrue(parser.isTurnAction());
    		fail();
    	} catch (SyntaxException e) { }
    }

    @Test
    public void testIsTurnToAction() {
    	Tree<Token> expected;
    	
    	use("turnto 23\n");
    	assertTrue(parser.isTurnToAction());
    	expected = tree("turnto", "23.0");
    	assertStackTopEquals(expected);
    	
    	use("turnto xyz + 6\n");
    	assertTrue(parser.isTurnToAction());
    	expected = tree("turnto", tree("+", "xyz", "6.0"));
    	assertStackTopEquals(expected);
    	
    	use("turnto 23");
    	try{
    		assertTrue(parser.isTurnToAction());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("moveto 23\n");
    	assertFalse(parser.isTurnToAction());
    	
    	use("turnto \n");
    	try{
    		assertTrue(parser.isTurnToAction());
    		fail();
    	} catch (SyntaxException e) { }
    }

    @Test
    public void testIsReturnStatement() {
    	Tree<Token> expected;
    	
    	use("return 23\n");
    	assertTrue(parser.isReturnStatement());
    	expected = tree("return", "23.0");
    	assertStackTopEquals(expected);
    	
    	use("return xyz + 6\n");
    	assertTrue(parser.isReturnStatement());
    	expected = tree("return", tree("+", "xyz", "6.0"));
    	assertStackTopEquals(expected);
    	
    	use("return 23");
    	try{
    		assertTrue(parser.isReturnStatement());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("moveto 23\n");
    	assertFalse(parser.isReturnStatement());
    	
    	use("return \n");
    	try{
    		assertTrue(parser.isReturnStatement());
    		fail();
    	} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsMoveToAction() {
    	Tree<Token> expected;
    	
    	use("moveto 23, 45\n");
    	assertTrue(parser.isMoveToAction());
    	expected = tree("moveto", "23.0", "45.0");
    	assertStackTopEquals(expected);
    	
    	use("moveto xyz + 6, 45\n");
    	assertTrue(parser.isMoveToAction());
    	expected = tree("moveto", tree("+", "xyz", "6.0"), "45.0");
    	assertStackTopEquals(expected);
    	
    	use("moveto 23");
    	try{
    		assertTrue(parser.isMoveToAction());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("moveto 23, 9");
    	try{
    		assertTrue(parser.isMoveToAction());
    		fail();
    	} catch (SyntaxException e) { }

    	
    	use("move 23\n");
    	assertFalse(parser.isMoveToAction());
    	
    	use("moveto \n");
    	try{
    		assertTrue(parser.isMoveToAction());
    		fail();
    	} catch (SyntaxException e) { }
    }

    @Test
    public void testIsLineAction() {
    	Tree<Token> expected;
    	
    	use("line 23, 45, 7, 8\n");
    	assertTrue(parser.isLineAction());
    	expected = tree("line", "23.0", "45.0", "7.0", "8.0");
    	assertStackTopEquals(expected);
    	
    	use("line xyz + 6, 45, 7, 8\n");
    	assertTrue(parser.isLineAction());
    	expected = tree("line", tree("+", "xyz", "6.0"), "45.0", "7.0", "8.0");
    	assertStackTopEquals(expected);
    	
    	use("line 23\n");
    	try{
    		assertTrue(parser.isLineAction());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("line 23, 9, 8, 7");
    	try{
    		assertTrue(parser.isLineAction());
    		fail();
    	} catch (SyntaxException e) { }

    	
    	use("move 23, 22, 21, 20\n");
    	assertFalse(parser.isLineAction());
    	
    	use("line \n");
    	try{
    		assertTrue(parser.isLineAction());
    		fail();
    	} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsAssignmentStatement() {
    	Tree<Token> expected;
    	
    	use("foo = 7\n");
    	assertTrue(parser.isAssignmentStatement());
    	expected = tree("assign", "foo", "7.0");
    	assertStackTopEquals(expected);
    	
    	use("bar = 7 > 8\n");
    	assertTrue(parser.isAssignmentStatement());
    	expected = tree("assign", "bar", tree(">", "7.0", "8.0"));
    	assertStackTopEquals(expected);
    	
    	use("= foo 7\n");
    	assertFalse(parser.isAssignmentStatement());
    	
    	use("foo 7\n");
    	try{
    		assertTrue(parser.isAssignmentStatement());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("foo =\n");
    	try{
    		assertTrue(parser.isAssignmentStatement());
    		fail();
    	} catch (SyntaxException e) { }
    }

    @Test
    public void testIsExitIfStatement() {
    	Tree<Token> expected;
    	
    	use("exit if x > 7\n");
    	assertTrue(parser.isExitIfStatement());
    	expected = tree("exit", tree(">", "x", "7.0"));
    	assertStackTopEquals(expected);
    	
    	use("exit if x + 7 > 9\n");
    	assertTrue(parser.isExitIfStatement());
    	expected = tree("exit", tree(">", tree("+", "x", "7.0"), "9.0"));
    	assertStackTopEquals(expected);
    	
    	use("exit x > 7\n");
    	try {
    		assertTrue(parser.isExitIfStatement());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use("if x > 2\n");
    	assertFalse(parser.isExitIfStatement());
    	
    	use("exit if x > y");
    	try{
    		assertTrue(parser.isExitIfStatement());
    		fail();
       	} catch (SyntaxException e) { }
    	
    	use("exit if \n");
    	try{
    		assertTrue(parser.isExitIfStatement());
    		fail();
       	} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsAction() {
    	Tree<Token> expected;
    	
		use("move foo + 2\n");
		assertTrue(parser.isAction());
		expected = tree("move", tree("+", "foo", "2.0"));
		assertStackTopEquals(expected);
		
		use("moveto 56, bar - 7\n");
		assertTrue(parser.isAction());
		expected = tree("moveto", "56.0", tree("-", "bar", "7.0"));
		assertStackTopEquals(expected);
		
		use("turn bar\n");
		assertTrue(parser.isAction());
		expected = tree("turn", "bar");
		assertStackTopEquals(expected);
		
		use("turnto 56 / 23\n");
		assertTrue(parser.isAction());
		expected = tree("turnto", tree("/", "56.0", "23.0"));
		assertStackTopEquals(expected);
		
		use("line 56, x + y, (77), something\n");
		assertTrue(parser.isAction());
		expected = tree("line", "56.0", tree("+", "x", "y"), "77.0", "something");
		assertStackTopEquals(expected);
		
		use("moveturn 57\n");
		assertFalse(parser.isAction());
	}
    
    @Test
    public void testIsEol() {
    	use("\n 5 \n\n 6 \n\n \n");
    	assertTrue(parser.isEol());
    	assertFalse(parser.isEol());
    	parser.nextToken();
    	assertTrue(parser.isEol());
    	assertFalse(parser.isEol());
    	parser.nextToken();
    	assertTrue(parser.isEol());
    }
    
    @Test
    public void testIsParameterList() {
    	Tree<Token> expected;
    	
    	use("()");
    	assertTrue(parser.isParameterList());
    	expected = tree("var");
    	assertStackTopEquals(expected);
    	
    	use("(5)");
    	assertTrue(parser.isParameterList());
    	expected = tree("var", "5.0");
    	assertStackTopEquals(expected);

    	use("(bar, x+3)");
    	assertTrue(parser.isParameterList());
    	expected = tree("var", "bar", tree("+", "x", "3.0"));
    	assertStackTopEquals(expected);
    }
    
    @Test
    public void testIsDoStatement() {
    	Tree<Token> expected;
    	
    	use("do foo \n");
    	assertTrue(parser.isDoStatement());
    	expected = tree("call", "foo");
    	assertStackTopEquals(expected);
    	
    	use("do foo (9)\n");
    	assertTrue(parser.isDoStatement());
    	expected = tree("call", "foo", tree("var", "9.0"));
    	assertStackTopEquals(expected);
    	
    	use("do foo (9, x + 7)\n");
    	assertTrue(parser.isDoStatement());
    	expected = tree("call", "foo", tree("var", "9.0", tree("+", "x", "7.0")));
    	assertStackTopEquals(expected);
    	
    	use("foo (9, x)\n");
    	assertFalse(parser.isDoStatement());
    	
    	use ("do bar");
    	try{
    		assertTrue(parser.isDoStatement());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use ("do bar (9, loop)\n");
    	try{
    		assertTrue(parser.isDoStatement());
    		fail();
    	} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsFunctionCall() {
    	Tree<Token> expected;
    	
    	use("foo (9)\n");
    	assertTrue(parser.isFunctionCall());
    	expected = tree("call", "foo", tree("var", "9.0"));
    	assertStackTopEquals(expected);
    	
    	use("foo (9, x + 7)\n");
    	assertTrue(parser.isFunctionCall());
    	expected = tree("call", "foo", tree("var", "9.0", tree("+", "x", "7.0")));
    	assertStackTopEquals(expected);
    	
    	use("do foo (9, x)\n");
    	assertFalse(parser.isFunctionCall());
    	
    	use ("bar\n");
    	try{
    		assertTrue(parser.isFunctionCall());
    		fail();
    	} catch (SyntaxException e) { }
    	
    	use ("bar (9, loop)\n");
    	try{
    		assertTrue(parser.isFunctionCall());
    		fail();
    	} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsCommand() {
		use("move 66 + 7\n");
		assertTrue(parser.isCommand());
		
		use("do foo(77, x)\n");
		assertTrue(parser.isCommand());
		
		use("return 44 < 34\n");
		assertTrue(parser.isCommand());
		
		use("line 55, 56, 7, 23\n");
		assertTrue(parser.isCommand());
		
		use("55 < x\n");
		assertFalse(parser.isCommand());
		
		use("23 + x\n");
		assertFalse(parser.isCommand());
    }
    
    @Test
    public void testIsSwitchStatement() {
    	Tree<Token> expected;
    	
		use("switch {\n}\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch");
		assertStackTopEquals(expected);
		
		use("switch {\n case x = 5\n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", tree("=", "x", "5.0")));
		assertStackTopEquals(expected);
		
		use("switch {\n case x = 5\n move 66 + 7\n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", tree("=", "x", "5.0"), tree("move", tree("+", "66.0", "7.0"))));
		assertStackTopEquals(expected);
		
		use("switch {\n case x = 5\n case x = 6\n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", tree("=", "x", "5.0")), tree("case", tree("=", "x", "6.0")));
		assertStackTopEquals(expected);
		
		use("switch {\n case x = 5\n move 66 + 7\n case x = 6\n turn x\n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", tree("=", "x", "5.0"), tree("move", tree("+", "66.0", "7.0"))), tree("case", tree("=", "x", "6.0"), tree("turn", "x")));
		assertStackTopEquals(expected);
		
		use("switch {\n case x = 5\n move 66 + 7\n turn x\n }\n");
		assertTrue(parser.isSwitchStatement());
		expected = tree("switch", tree("case", tree("=", "x", "5.0"), tree("move", tree("+", "66.0", "7.0")), tree("turn", "x")));
		assertStackTopEquals(expected);
		
		use("{\n}\n");
		assertFalse(parser.isSwitchStatement());
		
		use("switch {\n case x = 5\n move 66 + 7\n turn x }\n");
		try {
			assertTrue(parser.isSwitchStatement());
			fail();
		} catch (SyntaxException e) { }
    }
    
    @Test
    public void testIsStatement() {
		use("loop {\n\n }\n");
		assertTrue(parser.isStatement());

		use("return x > 2\n");
		assertTrue(parser.isStatement());
		
		use("x = 5\n");
		assertTrue(parser.isStatement());
		
		use("exit if x > 2\n");
		assertTrue(parser.isStatement());
		
		use("switch {\n case x = 5\n move 66 + 7\n turn x\n }\n");		
		assertTrue(parser.isStatement());
		
		use("do foo (9, x + 5) \n");
		assertTrue(parser.isStatement());
		
		use("color loop \n");
		assertTrue(parser.isStatement());
    }
    
    @Test
    public void testIsBlock() {
    	Tree<Token> expected;
    	
		use("{\n\n }\n");
		assertTrue(parser.isBlock());
		expected = tree("block");
		assertStackTopEquals(expected);
		
		use("{ \n move foo\n} \n");
		assertTrue(parser.isBlock());
		expected = tree("block", tree("move", "foo"));
		assertStackTopEquals(expected);
		
		use("{ \n move foo\n do x\n } \n");
		assertTrue(parser.isBlock());
		expected = tree("block", tree("move", "foo"), tree("call", "x"));
		assertStackTopEquals(expected);
		
		use("\n move foo\n}");
		assertFalse(parser.isBlock());
		
		use("{ }\n");
		try{
			assertTrue(parser.isBlock());
			fail();
		} catch (SyntaxException e) { }
		
		use("{ \n foo + bar > foo\n \n");
		try{
			assertTrue(parser.isBlock());
			fail();
		} catch (SyntaxException e) { }
    }

    @Test
    public void testIsInitializationBlock() {
    	Tree<Token> expected;
    	
		use("initially {\n\n }\n");
		assertTrue(parser.isInitializationBlock());
		expected = tree("initially", tree("block"));
		assertStackTopEquals(expected);
		
		use("initially { \n move foo\n} \n");
		assertTrue(parser.isInitializationBlock());
		expected = tree("initially", tree("block", tree("move", "foo")));
		assertStackTopEquals(expected);
		
		use("initially { \n move foo\n do x\n } \n");
		assertTrue(parser.isInitializationBlock());
		expected = tree("initially", tree("block", tree("move", "foo"), tree("call", "x")));
		assertStackTopEquals(expected);
		
		use("{\n move foo\n}");
		assertFalse(parser.isInitializationBlock());
		
		use("initially \n move foo\n}");
		try{
			assertTrue(parser.isInitializationBlock());
			fail();
		} catch (SyntaxException e) { }
		
		use("initially { }\n");
		try{
			assertTrue(parser.isInitializationBlock());
			fail();
		} catch (SyntaxException e) { }
		
		use("initially{ \n foo + bar > foo\n \n");
		try{
			assertTrue(parser.isInitializationBlock());
			fail();
		} catch (SyntaxException e) { }
    }

    @Test
    public void testIsLoopStatement() {
    	Tree<Token> expected;
    	
		use("loop {\n\n }\n");
		assertTrue(parser.isLoopStatement());
		expected = tree("loop", tree("block"));
		assertStackTopEquals(expected);
		
		use("loop { \n move foo\n} \n");
		assertTrue(parser.isLoopStatement());
		expected = tree("loop", tree("block", tree("move", "foo")));
		assertStackTopEquals(expected);
		
		use("loop { \n move foo\n do x\n } \n");
		assertTrue(parser.isLoopStatement());
		expected = tree("loop", tree("block", tree("move", "foo"), tree("call", "x")));
		assertStackTopEquals(expected);
		
		use("{\n move foo\n}");
		assertFalse(parser.isLoopStatement());
		
		use("loop \n move foo\n}");
		try{
			assertTrue(parser.isLoopStatement());
			fail();
		} catch (SyntaxException e) { }
		
		use("loop { }\n");
		try{
			assertTrue(parser.isLoopStatement());
			fail();
		} catch (SyntaxException e) { }
		
		use("loop { \n foo + bar > foo\n \n");
		try{
			assertTrue(parser.isLoopStatement());
			fail();
		} catch (SyntaxException e) { }
    }
    
    @Test
	public void testIsFunctionDefinition() {
    	Tree<Token> expected;
    	
		use("define fun1 {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		expected = tree("function", "fun1", "var", "block");
		assertStackTopEquals(expected);

		use("define fun2 using x {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		expected = tree("function", "fun2", tree("var", "x"), "block");
		assertStackTopEquals(expected);
		
		use("define fun3 using x, y {\n}\n");
		assertTrue(parser.isFunctionDefinition());
		expected = tree("function", "fun3", tree("var", "x", "y"), "block");
		assertStackTopEquals(expected);
		
		use("fun4 {\n}\n");
		assertFalse(parser.isFunctionDefinition());
		
		use("define fun5 x, y {\n}\n");
		try{
			assertTrue(parser.isFunctionDefinition());
			fail();
		} catch (SyntaxException e) { }
		
		use("define fun6 using {\n}\n");
		try{
			assertTrue(parser.isFunctionDefinition());
			fail();
		} catch (SyntaxException e) { }
		
		use("define fun7 using x, y");
		try{
			assertTrue(parser.isFunctionDefinition());
			fail();
		} catch (SyntaxException e) { }
	}
    
	@Test
	public void testIsBugDefinition() {
		Tree<Token> expected;
		
		use("Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
						"bug1", 
						"list", 
						tree("initially", "block"), 
						tree("block", tree("move", "x")), 
						"list");
		assertStackTopEquals(expected);
		
		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
				"bug1", 
				tree("list", tree("var", "foo", "bar")), 
				tree("initially", "block"), 
				tree("block", tree("move", "x")), 
				"list");
		assertStackTopEquals(expected);
		
		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"var x\n" +
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
				"bug1", 
				tree("list", tree("var", "foo", "bar"), tree("var", "x")), 
				tree("initially", "block"), 
				tree("block", tree("move", "x")), 
				"list");
		assertStackTopEquals(expected);
		
		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"var x\n" +
			"initially { \n move foo\n} \n" +
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
				"bug1", 
				tree("list", tree("var", "foo", "bar"), tree("var", "x")), 
				tree("initially", tree("block", tree("move", "foo"))), 
				tree("block", tree("move", "x")), 
				"list");
		assertStackTopEquals(expected);
		
		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"var x\n" +
			"initially { \n move foo\n} \n" +
			"move x\n" +
			"turn x\n" +
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
				"bug1", 
				tree("list", tree("var", "foo", "bar"), tree("var", "x")), 
				tree("initially", tree("block", tree("move", "foo"))), 
				tree("block", tree("move", "x"), tree("turn", "x")), 
				"list");
		assertStackTopEquals(expected);
		
		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"var x\n" +
			"initially { \n move foo\n} \n" +
			"move x\n" +
			"turn x\n" +
			"define fun3 using x, y {\n}\n" +
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
				"bug1", 
				tree("list", tree("var", "foo", "bar"), tree("var", "x")), 
				tree("initially", tree("block", tree("move", "foo"))), 
				tree("block", tree("move", "x"), tree("turn", "x")), 
				tree("list", tree("function", "fun3", tree("var", "x", "y"), "block")));
		assertStackTopEquals(expected);

		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"var x\n" +
			"initially { \n move foo\n} \n" +
			"move x\n" +
			"turn x\n" +
			"define fun3 using x, y {\n}\n" +
			"define fun1 {\n}\n" +
			"}\n\n");
		assertTrue(parser.isBugDefinition());
		expected = tree("Bug", 
				"bug1", 
				tree("list", tree("var", "foo", "bar"), tree("var", "x")), 
				tree("initially", tree("block", tree("move", "foo"))), 
				tree("block", tree("move", "x"), tree("turn", "x")), 
				tree("list", tree("function", "fun3", tree("var", "x", "y"), "block"), tree("function", "fun1", "var", "block")));
		assertStackTopEquals(expected);
		
		use("Bug bug1 { \n" +
			"var foo, bar\n\n" +
			"var x\n" +
			"initially { \n move foo\n} \n" +
			"define fun3 using x, y {\n}\n" +
			"define fun1 {\n}\n" +
			"}\n\n");
		try {
			assertTrue(parser.isBugDefinition());
			fail();
		} catch (SyntaxException e) { }
	}
	
	@Test
	public void testIsAllbugsCode() {
		Tree<Token> expected;
		
		use("Allbugs { \n" +
			"}\n");
		assertTrue(parser.isAllbugsCode());
		expected = tree("Allbugs",
						"list",
						"list");
		assertStackTopEquals(expected);
		
		use("Allbugs { \n" +
			"var foo, bar\n" +
			"}\n");
		assertTrue(parser.isAllbugsCode());
		expected = tree("Allbugs",
				tree("list", tree("var", "foo", "bar")),
				"list");
		assertStackTopEquals(expected);
		
		use("Allbugs { \n" +
			"var foo, bar\n" +
			"var x, y\n\n" +
			"}\n");
		assertTrue(parser.isAllbugsCode());
		expected = tree("Allbugs",
				tree("list", tree("var", "foo", "bar"), tree("var", "x", "y")),
				"list");
		assertStackTopEquals(expected);
		
		use("Allbugs { \n" +
			"var foo, bar\n" +
			"var x, y\n\n" +
			"define fun1 using x {\n}\n" +
			"}\n");
		assertTrue(parser.isAllbugsCode());
		expected = tree("Allbugs",
				tree("list", tree("var", "foo", "bar"), tree("var", "x", "y")),
				tree("list", tree("function", "fun1", tree("var", "x"), "block")));
		assertStackTopEquals(expected);

		use("Allbugs { \n" +
			"var foo, bar\n" +
			"var x, y\n\n" +
			"define fun1 using x {\n}\n" +
			"define fun2 {\n}\n" +
			"}\n");
		assertTrue(parser.isAllbugsCode());
		expected = tree("Allbugs",
				tree("list", tree("var", "foo", "bar"), tree("var", "x", "y")),
				tree("list", tree("function", "fun1", tree("var", "x"), "block"), tree("function", "fun2", "var", "block")));
		assertStackTopEquals(expected);
		
		use("{ \n" +
			"var foo, bar\n" +
			"var x, y\n\n" +
			"define fun1 using x {\n}\n" +
			"define fun2 {\n}\n" +
			"}\n");
		assertFalse(parser.isAllbugsCode());
		
		use("Allbugs { \n" +
			"var foo, bar\n" +
			"var x, y\n\n" +
			"define fun1 using x {\n}\n" +
			"define fun2 {\n}\n");
		try{
			assertTrue(parser.isAllbugsCode());
			fail();
		} catch (SyntaxException e) { }	
	}

	@Test
	public void testIsProgram() {
		Tree<Token> expected;
		
		use("Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isProgram());
		expected = tree("program",
						"Allbugs",
						tree("list", tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list")));
		assertStackTopEquals(expected);
		
		use("Allbugs { \n" + 
			"var foo, bar\n" +
			"}\n" +
			"Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isProgram());
		expected = tree("program",
				tree("Allbugs", tree("list", tree("var", "foo", "bar")), "list"),
				tree("list", tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list")));
		assertStackTopEquals(expected);
		
		use("Allbugs { \n" + 
			"var foo, bar\n" +
			"}\n" +
			"Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n" +
			"Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isProgram());
		expected = tree("program",
				tree("Allbugs", tree("list", tree("var", "foo", "bar")), "list"),
				tree("list", tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list"),
							 tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list")));
		assertStackTopEquals(expected);
		
		use("Allbugs { \n" + 
			"var foo, bar\n" +
			"}\n" +
			"Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n" +
			"Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n" +
			"Bug bug1 { \n" + 
			"move x\n" + 
			"}\n\n");
		assertTrue(parser.isProgram());
		expected = tree("program",
				tree("Allbugs", tree("list", tree("var", "foo", "bar")), "list"),
				tree("list", tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list"),
							 tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list"),
							 tree("Bug", "bug1", "list", tree("initially", "block"), tree("block", tree("move", "x")), "list")));
		assertStackTopEquals(expected);

		use("Allbugs { \n" + 
			"var foo, bar\n" +
			"}\n");
		try{
			assertTrue(parser.isProgram());
			fail();
		} catch (SyntaxException e) { }
	}

	@Test
	public void testIsArithmeticExpression() {
		Tree<Token> expected;
		
        use("");
        assertFalse(parser.isArithmeticExpression());
        
        use("250");
        assertTrue(parser.isArithmeticExpression());
        expected = tree("250.0");
        assertStackTopEquals(expected);
        
        use("hello");
        assertTrue(parser.isArithmeticExpression());
        expected = tree("hello");
        assertStackTopEquals(expected);
        
        use("(xyz + 3)");
        assertTrue(parser.isArithmeticExpression());
        expected = tree("+", "xyz", "3.0");
        assertStackTopEquals(expected);
        
        use("12 * 5 - 3");
        assertTrue(parser.isArithmeticExpression());
        expected = tree("-", tree("*", "12.0", "5.0"), "3.0");
        assertStackTopEquals(expected);
        
        use("12 * ((5 - 3) * 4) / 6 + (8)");
        assertTrue(parser.isArithmeticExpression());
        expected = tree("+", tree("/", tree("*", "12.0", tree("*", tree("-", "5.0", "3.0"), "4.0")), "6.0"), "8.0");
        assertStackTopEquals(expected);
        
        use("17 +");
        try {
        	assertTrue(parser.isArithmeticExpression());
        } catch (SyntaxException e) { }
        
        use("22 *");
        try {
        	assertTrue(parser.isArithmeticExpression());
        } catch (SyntaxException e) { }
        
        use("#");
        assertFalse(parser.isArithmeticExpression());
	}
	
    @Test
    public void testIsVariable() {
        use("foo 23 bar +");
        assertTrue(parser.isVariable());
        
        assertFalse(parser.isVariable());
        assertTrue(parser.isFactor());
        
        assertTrue(parser.isVariable());
        
        assertFalse(parser.isVariable());
        assertTrue(parser.isAddOperator());
    }
	
//  ----- "Helper" methods
    
    /**
     * Sets the <code>parser</code> instance to use the given string.
     * 
     * @param s The string to be parsed.
     */
    private void use(String s) {
        parser = new Parser(s);
    }
    
    /**
     * Returns the current top of the stack.
     *
     * @return The top of the stack.
     */
    private Object stackTop() {
        return parser.stack.pop();
    }
    
    /**
     * Tests whether the top element in the stack is correct.
     *
     * @return <code>true</code> if the top element of the stack is as expected.
     */
    private void assertStackTopEquals(Tree<Token> expected) {
        assertEquals(expected, stackTop());
        
        // Added this to make sure nothing unnecessary was left lingering on the stack
        assertTrue(parser.stack.isEmpty());
    }
    
    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether Tokens are pushed
     * back appropriately.
     * @param parser TODO
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Parser parser, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = parser.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(typeName(expectedType), typeName(actualType));
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
    
    private String typeName(int type) {
        switch(type) {
            case StreamTokenizer.TT_EOF: return "EOF";
            case StreamTokenizer.TT_EOL: return "EOL";
            case StreamTokenizer.TT_WORD: return "WORD";
            case StreamTokenizer.TT_NUMBER: return "NUMBER";
            default: return "'" + (char)type + "'";
        }
    }
    
    /**
     * Returns a Tree node consisting of a single leaf; the
     * node will contain a Token with a String as its value. <br>
     * Given a Tree, return the same Tree.<br>
     * Given a Token, return a Tree with the Token as its value.<br>
     * Given a String, make it into a Token, return a Tree
     * with the Token as its value.
     * 
     * @param value A Tree, Token, or String from which to
              construct the Tree node.
     * @return A Tree leaf node containing a Token whose value
     *         is the parameter.
     */
    private Tree<Token> createNode(Object value) {
        if (value instanceof Tree) {
            return (Tree) value;
        }
        if (value instanceof Token) {
            return new Tree<Token>((Token) value);
        }
        else if (value instanceof String) {
            return new Tree<Token>(new Token((String) value));
        }
        assert false: "Illegal argument: tree(" + value + ")";
        return null; 
    }
    
    /**
     * Builds a Tree that can be compared with the one the
     * Parser produces. Any String or Token arguments will be
     * converted to Tree nodes containing Tokens.
     * 
     * @param op The String value to use in the Token in the root.
     * @param children The objects to be made into children.
     * @return The resultant Tree.
     */
    private Tree<Token> tree(String op, Object... children) {
        Tree<Token> tree = new Tree<Token>(new Token(op));
        for (int i = 0; i < children.length; i++) {
            tree.addChild(createNode(children[i]));
        }
        return tree;
    }
}

