package bugs;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;


public class RecognizerTest2 {
    Recognizer r;
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void testIsProgram() {
        use("Bug sally { \n x = y \n } \n");
        assertTrue(r.isProgram());
        use("Allbugs { \n  } \n Bug sally { \n x = y \n } \n");
        assertTrue(r.isProgram());
        use("Allbugs { \n  } \n Bug sally { \n x = y \n } \n Bug Willy { \n x = y \n } \n");
        assertTrue(r.isProgram());
    }

    @Test
    public void testIsAllbugsCode() {
        use("Allbugs { \n  } \n");
        assertTrue(r.isAllbugsCode());
        use("Allbugs { \n var a, b, c \n } \n");
        assertTrue(r.isAllbugsCode());
        use("Allbugs { \n" +
            "   define foo using c { \n" +
            "      move c \n" +
            "   } \n" +
            "} \n");
        use("Allbugs { \n" +
          "   var a, b, c \n" +
          "   define foo using c { \n" +
          "      move c \n" +
          "   } \n" +
          "} \n");
        assertTrue(r.isAllbugsCode());
    }

    @Test
    public void testIsBugDefinition() {
        use("Bug sally { \n x = y \n } \n");
        assertTrue(r.isBugDefinition());
        use("Bug sally { \n" +
            "   var a, b, c \n" +
            "   initially { \n" +
            "      a = 5 \n" +
            "   } \n" +
            "   b = a \n" +
            "   c = b \n" +
            "   define foo using c { \n" +
            "      move c \n" +
            "   } \n" +
            "} \n");
        assertTrue(r.isBugDefinition());
    }

    @Test
    public void testIsVarDeclaration() {
        use("var a\n var a, b\n");
        assertTrue(r.isVarDeclaration());
        assertTrue(r.isVarDeclaration());
        atEnd();
    }
    
    @Test
    public void testIsInitializationBlock() {
        use("initially {\n }\n");
        assertTrue(r.isInitializationBlock());
        use("initially {\n move 5\n x = y \n}\n");
        assertTrue(r.isInitializationBlock());
    }

    @Test
    public void testIsCommand() {
        use("x = y \n" +
            "loop { \n exit if 2 = 2 \n } \n" +
            "switch { \n case 2 = 2 \n x = y \n } \n" +
            "return 5 \n" +
            "do foo() \n" +
            "color red \n");
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        use("move 5 \n" +
            "moveto x, y \n" +
            "turn 90 \n" +
            "turnto 90 \n" +
            "line 0, 0, 100, 100 \n");
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
        assertTrue(r.isCommand());
    }

    @Test
    public void testIsStatement() {
        use("x = y \n" +
            "loop { \n exit if 2 = 2 \n } \n" +
            "switch { \n case 2 = 2 \n x = y \n } \n" +
            "return 5 \n" +
            "do foo() \n" +
            "color red \n");
        assertTrue(r.isStatement());
        assertTrue(r.isStatement());
        assertTrue(r.isStatement());
        assertTrue(r.isStatement());
        assertTrue(r.isStatement());
        assertTrue(r.isStatement());
    }

    @Test
    public void testIsAction() {
        use("move 5 \n" +
            "moveto x, y \n" +
            "turn 90 \n" +
            "turnto 90 \n" +
            "line 0, 0, 100, 100 \n");
        assertTrue(r.isAction());
        assertTrue(r.isAction());
        assertTrue(r.isAction());
        assertTrue(r.isAction());
        assertTrue(r.isAction());
    }

    @Test
    public void testIsMoveAction() {
        use("move 90\n");
        assertTrue(r.isMoveAction());
        use("move (90)\n");
        assertTrue(r.isMoveAction());
        use("move 9 * x\n");
        assertTrue(r.isMoveAction());
    }

    @Test(expected=SyntaxException.class)
    public void testIsMoveActionException() {
        use("move\n");
        r.isMoveAction();
    }

    @Test(expected=SyntaxException.class)
    public void testIsMoveActionException2() {
        use("move 19, 20\n");
        r.isMoveAction();
    }
    
    @Test
    public void testIsMoveToAction() {
        use("moveto 20, 25\n");
        assertTrue(r.isMoveToAction());
        use("moveto (90), a<b\n");
        assertTrue(r.isMoveToAction());
        use("moveto 9 * x, foo(bar)\n");
        assertTrue(r.isMoveToAction());
    }

    @Test(expected=SyntaxException.class)
    public void testIsMoveToActionException() {
        use("moveto 25\n");
        r.isMoveToAction();
    }

    @Test(expected=SyntaxException.class)
    public void testIsMoveToActionException2() {
        use("moveto 19, 20, 21\n");
        r.isMoveToAction();
    }

    @Test
    public void testIsTurnAction() {
        use("turn 90\n");
        assertTrue(r.isTurnAction());
        use("turn (90)\n");
        assertTrue(r.isTurnAction());
        use("turn 9 * x\n");
        assertTrue(r.isTurnAction());
    }

    @Test(expected=SyntaxException.class)
    public void testIsTurnActionException() {
        use("turn\n");
        r.isTurnAction();
    }

    @Test(expected=SyntaxException.class)
    public void testIsTurnActionException2() {
        use("turn 19, 20\n");
        r.isTurnAction();
    }

    @Test
    public void testIsTurnToAction() {
        use("turnto 90\n");
        assertTrue(r.isTurnToAction());
        use("turnto (90)\n");
        assertTrue(r.isTurnToAction());
        use("turnto 9 * x\n");
        assertTrue(r.isTurnToAction());
    }

    @Test(expected=SyntaxException.class)
    public void testIsTurnToActionException() {
        use("turnto\n");
        r.isTurnToAction();
    }

    @Test(expected=SyntaxException.class)
    public void testIsTurnToActionException2() {
        use("turnto 19, 20\n");
        r.isTurnToAction();
    }

    @Test
    public void testIsLineAction() {
        use("line 10, 20, 30, 40\n");
        assertTrue(r.isLineAction());
        use("line (90), a<b, foo(bar), (x + y)*z\n");
        assertTrue(r.isLineAction());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsLineActionException() {
        use("line 10, 20, 30\n");
        r.isLineAction();
    }
    
    @Test
    public void testIsAssignmentStatement() {
        use("a = b\n");
        assertTrue(r.isAssignmentStatement());
        use("a = 3 + foo(5)\n");
        assertTrue(r.isAssignmentStatement());
        use("a = b = c\n");
        assertTrue(r.isAssignmentStatement());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsAssignmentStatementException() {
        use("moveTo x, y\n"); // note misspelling
        r.isAssignmentStatement();
    }

    @Test
    public void testIsLoopStatement() {
        use("loop {\n }\n loop {\n x = y \n }\n");
        assertTrue(r.isLoopStatement());
        assertTrue(r.isLoopStatement());
        atEnd();
    }

    @Test
    public void testIsExitIfStatement() {
        use("exit if x\n");
        assertTrue(r.isExitIfStatement());
        use("exit if x > y\n");
        assertTrue(r.isExitIfStatement());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsExitIfStatementException() {
        use("exit x < y\n");
        r.isExitIfStatement();
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsExitIfStatementException2() {
        use("exit if x < y and y < z\n");
        r.isExitIfStatement();
    }

    @Test
    public void testIsSwitchStatement() {
        use("switch {\n }\n");
        assertTrue(r.isSwitchStatement());
        use("switch {\n case x = y \n }\n");
        assertTrue(r.isSwitchStatement());
        use("switch {\n case x = y \n x = y \n }\n");
        assertTrue(r.isSwitchStatement());
        atEnd();
    }

    @Test
    public void testIsReturnStatement() {
        use("return 55\n");
        assertTrue(r.isReturnStatement());
        use("return 20+5\n");
        assertTrue(r.isReturnStatement());
        use("return x <= y\n");
        assertTrue(r.isReturnStatement());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsReturnStatementException() {
        use("return red\n");
        assertFalse(r.isReturnStatement());
    }

    @Test
    public void testIsDoStatement() {
        use("do foo\n");
        assertTrue(r.isDoStatement());
        use("do foo ()\n");
        assertTrue(r.isDoStatement());
        use("do foo(bar)\n");
        assertTrue(r.isDoStatement());
        use("do foo(bar, 6+7)\n");
        assertTrue(r.isDoStatement());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsDoStatementException() {
        use("do foo 5 \n");
        r.isDoStatement();
    }

    @Test
    public void testIsColorStatement() {
        use("color red\n");
        assertTrue(r.isColorStatement());
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsColorStatementException() {
        use("color elephant\n");
        r.isColorStatement();
    }

    @Test
    public void testIsBlock() {
        use("{ \n } \n { \n x = y \n } \n { \n x = y  \n x = y \n } \n\n");
        assertTrue(r.isBlock());
        assertTrue(r.isBlock());
        assertTrue(r.isBlock());
        atEnd();
    }
    
    @Test(expected=SyntaxException.class)
    public void testIsBlockException() {
        use("{ \n \n");
        r.isBlock();
    }
    

    @Test
    public void testIsComparator() {
        use("< <= = & != >= >");
        assertTrue(r.isComparator());
        assertTrue(r.isComparator());
        assertTrue(r.isComparator());
        and();
        assertTrue(r.isComparator());
        assertTrue(r.isComparator());
        assertTrue(r.isComparator());
        assertFalse(r.isComparator());
    }

    @Test
    public void testIsFunctionDefinition() {
        use("define foo {\n }\n");
        assertTrue(r.isFunctionDefinition());
        use("define foo using a, b {\n }\n");
        assertTrue(r.isFunctionDefinition());
        use("define foo {\n a = b \n }\n");
        assertTrue(r.isFunctionDefinition());
        use("define foo using a, b {\n a = b \n }\n");
        assertTrue(r.isFunctionDefinition());
    }

    @Test
    public void testIsFunctionCall() {
        use("foo()");
        assertTrue(r.isFunctionCall());
        use("foo(5)");
        assertTrue(r.isFunctionCall());
        use("foo(5, 2+3)");
        assertTrue(r.isFunctionCall());
    }
    
    @Test
    public void testIsParameterList() {
        use("() &");
        assertTrue(r.isParameterList()); and();
        use("(5) &");
        assertTrue(r.isParameterList()); and();
        use("(bar, x+3) &");
        assertTrue(r.isParameterList()); and();
    }

    @Test
    public void testIsEol() {
        use("\n & \n");
        assertTrue(r.isEol());
        assertFalse(r.isEol());
        and();
        assertTrue(r.isEol());
    }

    @Test
    public void testIsExpression() {
        use("5");
        assertTrue(r.isExpression());
        use("abc");
        assertTrue(r.isExpression());
        use("5 + abc");
        assertTrue(r.isExpression()); atEnd();
        use("5 * (b - 24)");
        assertTrue(r.isExpression()); atEnd();
        use("foo(b)");
        assertTrue(r.isExpression()); atEnd();
        use("5 * foo(b)");
        assertTrue(r.isExpression()); atEnd();
        use("5 * foo(b, c - 24)");
        assertTrue(r.isExpression()); atEnd();
        use("5 * (b - 24)");
        assertTrue(r.isExpression()); atEnd();
        use("x < y");
        assertTrue(r.isExpression()); atEnd();
        use("x <= y");
        assertTrue(r.isExpression()); atEnd();
        use("2 < 3 <= 4 = 5 != 6 >= 7 > 8");
        assertTrue(r.isExpression()); atEnd();
        use("(3 + 4)");
        assertTrue(r.isExpression()); atEnd();
        use("3 <= 4");
        assertTrue(r.isExpression()); atEnd();
        use("(3 <= 4)");
        assertTrue(r.isExpression()); atEnd();
        use("2 < (3 <= 4)");
        assertTrue(r.isExpression()); atEnd();
        use("2 < (3 <= 4) = 5 != 6 >= 7 > (8 * 9)");
        assertTrue(r.isExpression()); atEnd();
        use("foo(bar)");
        assertTrue(r.isExpression()); atEnd();
        use("-5");
        assertTrue(r.isExpression()); atEnd();
        use("(-5 * 3)");
        assertTrue(r.isExpression()); atEnd();
        use("+5");
        assertTrue(r.isExpression()); atEnd();
        use("-(-5 * (+3))");
        assertTrue(r.isExpression()); atEnd();
    }
    
    private void use(String s) {
        r = new Recognizer(s);
    }

    private void and() {
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "&"));
        
    }
    
    private void atEnd() {
        assertTrue(r.nextTokenMatches(Token.Type.EOF));
    }

}