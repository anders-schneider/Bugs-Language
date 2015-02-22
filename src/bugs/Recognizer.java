package bugs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * This class consists of a number of methods that "recognize" strings
 * composed of Tokens that follow the indicated grammar rules for each
 * method.
 * <p>Each method may have one of three outcomes:
 * <ul>
 *   <li>The method may succeed, returning <code>true</code> and
 *      consuming the tokens that make up that particular nonterminal.</li>
 *   <li>The method may fail, returning <code>false</code> and not
 *       consuming any tokens.</li>
 *   <li>(Some methods only) The method may determine that an
 *       unrecoverable error has occurred and throw a
 *       <code>SyntaxException</code></li>.
 * </ul>
 * @author Anders Schneider
 * @version February 2015
 */
public class Recognizer {
    StreamTokenizer tokenizer = null;
    int lineNumber;
    
    /**
     * Constructs a Recognizer for the given string.
     * @param text The string to be recognized.
     */
    public Recognizer(String text) {
        Reader reader = new StringReader(text);
        tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();
        tokenizer.eolIsSignificant(true);
        tokenizer.slashStarComments(true);
        tokenizer.slashSlashComments(true);
        tokenizer.lowerCaseMode(false);
        tokenizer.ordinaryChars(33, 47);
        tokenizer.ordinaryChars(58, 64);
        tokenizer.ordinaryChars(91, 96);
        tokenizer.ordinaryChars(123, 126);
        tokenizer.quoteChar('\"');
        lineNumber = 1;
    }

    /** 
     * Tries to recognize an &lt;expression&gt;. 
     * <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; { &lt;comparator&gt; &lt;arithmetic expression&gt; }</pre> 
     * A <code>SyntaxException</code> will be thrown if the comparator
     * is present but not followed by a valid &lt;arithmetic expression&gt;. 
     * @return <code>true</code> if an expression is recognized. 
     */
    public boolean isExpression() {
    	if (!isArithmeticExpression()) {return false;}
    	
    	while (isComparator()) {
    		if (!isArithmeticExpression()) {
    			error("Comparator not followed by arithmetic expression");
    		}
    	}
    	return true;
    }

    /** 
      * Tries to recognize an &lt;arithmetic expression&gt;. 
      * <pre>&lt;arithmetic expression&gt; ::= [ &lt;add operator&gt; ] &lt;term&gt; { &lt;add_operator&gt; &lt;term&gt; }</pre> 
      * A <code>SyntaxException</code> will be thrown if the add_operator 
      * is present but not followed by a valid &lt;term&gt;. 
      * @return <code>true</code> if an arithmetic expression is recognized. 
      */
    public boolean isArithmeticExpression() {
    	boolean startsWithUnary = isAddOperator();
    	if (!isTerm()) {
    		if (!startsWithUnary) {return false;}
    		error("Add operator not followed by a valid term");
    	}
    	while (isAddOperator()) { 
    	if (!isTerm()) error("Error in expression after '+' or '-'"); 
        } 
        return true; 
    } 


    /**
     * Tries to recognize a &lt;term&gt;.
     * <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt;}</pre>
     * A <code>SyntaxException</code> will be thrown if the multiply_operator
     * is present but not followed by a valid &lt;term&gt;.
     * @return <code>true</code> if a term is recognized.
     */
    public boolean isTerm() {
        if (!isFactor()) return false;
        while (isMultiplyOperator()) {
            if (!isTerm()) error("No term after '*' or '/'");
        }
        return true;
    }

    /**
     * Tries to recognize a &lt;factor&gt;.
     * <pre>&lt;factor&gt; ::= &lt;name&gt; "." &lt;name&gt;
     *           | &lt;name&gt; "(" &lt;parameter list&gt; ")"
     *           | &lt;name&gt;
     *           | &lt;number&gt;
     *           | "(" &lt;expression&gt; ")"</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is recognized.
     */
    public boolean isFactor() {
        if (isVariable()) {
            if (symbol(".")) {              // reference to another Bug
                if (name()) return true;
                error("Incorrect use of dot notation");
            }
            else if (isParameterList()) return true; // function call
            else return true;                        // just a variable
        }
        if (number()) return true;
        if (symbol("(")) {
            if (!isExpression()) error("Error in parenthesized expression");
            if (!symbol(")")) error("Unclosed parenthetical expression");
            return true;
       }
       return false;
    }

    /**
     * Tries to recognize a &lt;parameter list&gt;.
     * <pre>&ltparameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"
     * @return <code>true</code> if a parameter list is recognized.
     */
    public boolean isParameterList() {
        if (!symbol("(")) return false;
        if (isExpression()) {
            while (symbol(",")) {
                if (!isExpression()) error("No expression after ','");
            }
        }
        if (!symbol(")")) error("Parameter list doesn't end with ')'");
        return true;
    }

    /**
     * Tries to recognize an &lt;add_operator&gt;.
     * <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
     * @return <code>true</code> if an addop is recognized.
     */
    public boolean isAddOperator() {
        return symbol("+") || symbol("-");
    }

    /**
     * Tries to recognize a &lt;multiply_operator&gt;.
     * <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
     * @return <code>true</code> if a multiply_operator is recognized.
     */
    public boolean isMultiplyOperator() {
        return symbol("*") || symbol("/");
    }

    /**
     * Tries to recognize a &lt;variable&gt;.
     * <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
     * @return <code>true</code> if a variable is recognized.
     */
    public boolean isVariable() {
        return name();
    }
    
    /**
     * Tries to recognize an &lt;action&gt;.
     * <pre>&lt;action&gt; ::= &lt;move action&gt;
     *           | &lt;moveto action&gt;
     *           | &lt;turn action&gt;
     *           | &lt;turnto action&gt;
     *           | &lt;line action&gt;</pre>
     * @return <code>true</code> if an action is recognized.
     */
    public boolean isAction() {
    	if (isMoveAction()) {return true;}
    	if (isMoveToAction()) {return true;}
    	if (isTurnAction()) {return true;}
    	if (isTurnToAction()) {return true;}
    	if (isLineAction()) {return true;}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;allbugs code&gt;.
     * <pre>&lt;allbugs code&gt; ::= "Allbugs" "{" &lt;eol&gt;
     * 		       { &lt;var declaration&gt; }
     * 		       { &lt;function definition&gt; }
     * 		  "}" &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the 'Allbugs' is not followed
     * by a "{", or if there is no eol
     * after the first "{", or if the final "}" is not present, 
     * or if there is an error with the
     * final eol of the allbugs code.
     * @return <code>true</code> if an allbugs code is recognized.
     */
    public boolean isAllbugsCode() {
    	if (!keyword("Allbugs")) {return false;}
    	if (!symbol("{")) {error("No '{' following 'Allbugs'");}
    	if (!isEol()) {error("Invalid eol following '{'");}
    	while (isVarDeclaration()) {}
    	while (isFunctionDefinition()) {}
    	if (!symbol("}")) {error("Error in closing '}'");}
    	if (!isEol()) {error("Error in final eol");}
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;assignment statement&gt;.
     * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the variable is
     * is present but not followed by a "=", or if the "=" is not followed
     * by a valid &lt;expression&gt;, or if the &lt;expression&gt; is not
     * followed by a valid &lt;eol&gt;.
     * @return <code>true</code> if an assignment statement is recognized.
     */
    public boolean isAssignmentStatement() {
    	if (!isVariable()) {return false;}
    	
    	if (!symbol("=")) {error("Missing '=' after variable");}
    	
    	if (!isExpression()) {error("Error in expression");}
    	
    	if (!isEol()) {error("Error in eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;block&gt;.
     * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt; } "}" &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the '{' is not followed
     * by an eol, or if a "}" or appropriate eol is not included.
     * @return <code>true</code> if a block is recognized.
     */
    public boolean isBlock() {
    	if (!symbol("{")) {return false;}
    	if (!isEol()) {error("No eol following the '{'");}
    	while (isCommand()) {}
    	if (!symbol("}")) {error("No closing '}'");}
    	if (!isEol()) {error("No final eol");}
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;bug definition&gt;.
     * <pre>&lt;bug definition&gt; ::= "Bug" &lt;NAME&gt; "{" &lt;eol&gt;
     * 		       { &lt;var declaration&gt; }
     * 		       [ &lt;initialization block&gt; ]
     * 		       &lt;command&gt;
     * 		       { &lt;command&gt; }
     * 		       { &lt;function definition&gt; }
     * 		  "}" &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the 'Bug' is not followed
     * by a valid name, or if the first "{" is not present, or if there is no eol
     * after the first "{", or if the necessary command does not appear where it
     * should, or if the final "}" is not present, or if there is an error with the
     * final eol of the definition.
     * @return <code>true</code> if a Bug definition is recognized.
     */
    public boolean isBugDefinition() {
    	if (!keyword("Bug")) {return false;}
    	if (!nextTokenMatches(Token.Type.NAME)) {error("Error in Bug definition name");}
    	if (!symbol("{")) {error("'{' not present");}
    	if (!isEol()) {error("Error with eol after '{'");}
    	while (isVarDeclaration()) {}
    	if (isInitializationBlock()) {}
    	if (!isCommand()) {error("Error in first command of Bug definition");}
    	while (isCommand()) {}
    	while (isFunctionDefinition()) {}
    	if (!symbol("}")) {error("Error with final '}'");}
    	if (!isEol()) {error("Error with final eol of Bug Definition");}
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;color statement&gt;.
     * <pre>&lt;color statement&gt; ::= "color" &lt;KEYWORD&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the 'color' is not followed
     * by a keyword, or if the keyword is not followed by an eol.
     * @return <code>true</code> if a color statement is recognized.
     */
    public boolean isColorStatement() {
    	if (keyword("color")) {
    		if (nextTokenMatches(Token.Type.KEYWORD)) {
    			if (isEol()) {return true;}
    			error("No eol at the end of the statement");
    		}
    		error("Token following 'color' is not a keyword");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;command&gt;.
     * <pre>&lt;command&gt; ::= &lt;action&gt; | &lt;statement&gt;</pre>
     * @return <code>true</code> if a command is recognized.
     */
    public boolean isCommand() {
    	if (isAction()) {return true;}
    	if (isStatement()) {return true;}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;comparator&gt;.
     * <pre>&lt;comparator&gt; ::= &lt;"&lt;"&gt; | &lt;"&lt;="&gt; | &lt;"="&gt; | &lt;"!="&gt; | &lt;"&gt;="&gt; | &lt;"&gt;"&gt; </pre>
     * A <code>SyntaxException</code> will be thrown if the "!"
     * is present but not followed by "=".
     * @return <code>true</code> if a comparator is recognized.
     */
    public boolean isComparator() {
    	if (symbol("=")) { return true;}
    	
    	if (symbol("!")) {
    		if (symbol("=")) {
    			return true;
    		} else {
    			error("! not followed by =");
    		}
    	}
    	
    	if (symbol("<") || symbol(">")) {
    		if (symbol("=")) return true;
    		
    		return true;
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;do statement&gt;.
     * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [ &lt;parameter list&gt; ] &lt;eol&gt;</pre>
     * @return <code>true</code> if a do statement is recognized.
     */
    public boolean isDoStatement() {
    	if (keyword("do")) {
    		if (isVariable()) {
    			if (isParameterList()) {}
    			if (isEol()) {
    				return true;
    			} else { error("No eol at the end");}
    		} else { error("'do' not followed by a variable");}
    	}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;eol&gt;.
     * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; }</pre>
     * @return <code>true</code> if an eol is recognized.
     */
    public boolean isEol() {
    	if (nextTokenMatches(Token.Type.EOL)) {
    		while (nextTokenMatches(Token.Type.EOL)) {}
    		return true;
    	}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;exit if statement&gt;.
     * <pre>&lt;exit if statement&gt; ::= "exit" "if" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "exit" is
     * is present but not followed by an "if", or if the "if" is not followed
     * by a valid &lt;expression&gt;, or if the &lt;expression&gt; is not
     * followed by a valid &lt;eol&gt;.
     * @return <code>true</code> if an exit if statement is recognized.
     */
    public boolean isExitIfStatement() {
    	if (!keyword("exit")) {return false;}
    	
    	if (!keyword("if")) {error("'exit' not followed by 'if'");}
    	
    	if (!isExpression()) {error("'exit if' not followed by valid expression");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;function call&gt;.
     * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
     * @return <code>true</code> if a do statement is recognized.
     */
    public boolean isFunctionCall() {
    	if (nextTokenMatches(Token.Type.NAME)) {
    		if (isParameterList()) { return true;}
    		error("No parameter list following the name token");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;function definition&gt;.
     * <pre>&lt;function definition&gt; ::= "define" &lt;NAME&gt; [ "using" &lt;variable&gt; { "," &lt;variable&gt; } ] &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "define"
     * is present but not followed by a valid &lt;NAME&gt; or if one of the
     * variables has an error.
     * @return <code>true</code> if a function definition is recognized.
     */
    public boolean isFunctionDefinition() {
    	if (!keyword("define")) {return false;}
    	if (!nextTokenMatches(Token.Type.NAME)) {error("Error in function name");}
    	if (keyword("using")) {
    		if (!isVariable()) {error("Error in variable after 'using'");}
    		while (symbol(",")) {
    			if (!isVariable()) {error("Error in variable");}
    		}
    	}
    	if (!isBlock()) {error("Error in block after function definition");}
    	return true;
    }
    
    /**
     * Tries to recognize an &lt;initialization block&gt;.
     * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "initially"
     * is present but not followed by a valid &lt;block&gt;
     * @return <code>true</code> if an initialization block is recognized.
     */
    public boolean isInitializationBlock() {
    	if (!keyword("initially")) {return false;}
    	return isBlock();
    }
    
    /**
     * Tries to recognize a &lt;line action&gt;.
     * <pre>&lt;line action&gt; ::= "line" &lt;expression&gt; "," &lt;expression&gt; "," &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "line"
     * is present but any of the following &lt;expression&gt;s or ","s
     * are not valid.
     * @return <code>true</code> if a line action is recognized.
     */
    public boolean isLineAction() {
    	if (!keyword("line")) {return false;}
    	
    	if (!isExpression()) {error("Invalid expression following 'line'");}
    	
    	if (!symbol(",")) {error("Missing ',' symbol");}
    	
    	if (!isExpression()) {error("Invalid expression after ',' symbol");}
    	
    	if (!symbol(",")) {error("Missing ',' symbol");}
    	
    	if (!isExpression()) {error("Invalid expression after ',' symbol");}
    	
    	if (!symbol(",")) {error("Missing ',' symbol");}
    	
    	if (!isExpression()) {error("Invalid expression after ',' symbol");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;loop statement&gt;.
     * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "loop"
     * is present but not followed by a valid &lt;block&gt;
     * @return <code>true</code> if a loop statement is recognized.
     */
    public boolean isLoopStatement() {
    	if (!keyword("loop")) {return false;}
    	return isBlock();
    }
    
    /**
     * Tries to recognize a &lt;move action&gt;.
     * <pre>&lt;move action&gt; ::= "move" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "move" is
     * present but not followed by a valid &lt;expression&gt;, or the 
     * &lt;expression&gt; is not followed by an eol.
     * @return <code>true</code> if a move action is recognized.
     */
    public boolean isMoveAction() {
    	if (!keyword("move")) {return false;}
    	
    	if (!isExpression()) {error("Invalid expression following 'move'");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;moveto action&gt;.
     * <pre>&lt;move action&gt; ::= "moveto" &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "moveto" is
     * present but not followed by a valid &lt;expression&gt;, or the 
     * &lt;expression&gt; is not followed by a ",", valid &lt;expression&gt;,
     * and then a valid &lt;eol&gt;.
     * @return <code>true</code> if a move action is recognized.
     */
    public boolean isMoveToAction() {
    	if (!keyword("moveto")) {return false;}
    	
    	if (!isExpression()) {error("Invalid expression following 'moveto'");}
    	
    	if (!symbol(",")) {error("No comma following expression");}
    	
    	if (!isExpression()) {error("Invalid expression following ','");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;program&gt;.
     * <pre>&lt;program&gt; ::= [ &lt;allbugs code&gt; ] &lt;bug definition&gt; { &lt;bug definition&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if there is no valid
     * &lt;bug definition&gt;.
     * @return <code>true</code> if a program is recognized.
     */
    public boolean isProgram() {
    	if (isAllbugsCode()) {}
    	if (!isBugDefinition()) {error("Error in first bug definition");}
    	while (isBugDefinition()) {}
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;return statement&gt;.
     * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "return" is
     * is present but not followed by a valid &lt;expression&gt;, or if 
     * the &lt;expression&gt; is not followed by a valid &lt;eol&gt;.
     * @return <code>true</code> if a return statement is recognized.
     */
    public boolean isReturnStatement() {
    	if (!keyword("return")) {return false;}
    	
    	if (!isExpression()) {error("'return' not followed by valid expression");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize as &lt;statement&gt;.
     * <pre>&lt;action&gt; ::= &lt;assignment statement&gt;
     *           | &lt;loop statement&gt;
     *           | &lt;exit if statement&gt;
     *           | &lt;switch statement&gt;
     *           | &lt;return statement&gt;
     *           | &lt;do statement&gt;
     *           | &lt;color statement&gt;</pre>
     * @return <code>true</code> if a statement is recognized.
     */
    public boolean isStatement() {
    	if (isAssignmentStatement()) {return true;}
    	if (isLoopStatement()) {return true;}
    	if (isExitIfStatement()) {return true;}
    	if (isSwitchStatement()) {return true;}
    	if (isReturnStatement()) {return true;}
    	if (isDoStatement()) {return true;}
    	if (isColorStatement()) {return true;}
    	return false;
    }
    
    /**
     * 
     */
    public boolean isSwitchStatement() {
    	if (!keyword("switch")) {return false;}
    	if (!symbol("{")) {error("No '{' following 'switch'");}
    	if (!isEol()) {error("No eol following '{'");}
    	
    	while (keyword("case")) {
    		if (!isExpression()) {error("Invalid expression following 'case'");}
    		if (!isEol()) {error("No vali eol following expression");}
    		while (isCommand()) {}
    	}
    	
    	if (!symbol("}")) {error("Error with closing ')'");}
    	if (!isEol()) {error("Error with final eol");}
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;turn action&gt;.
     * <pre>&lt;turn action&gt; ::= "turn" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "turn" is
     * present but not followed by a valid &lt;expression&gt;, or the 
     * &lt;expression&gt; is not followed by an eol.
     * @return <code>true</code> if a turn action is recognized.
     */
    public boolean isTurnAction() {
    	if (!keyword("turn")) {return false;}
    	
    	if (!isExpression()) {error("Invalid expression following 'move'");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;turnto action&gt;.
     * <pre>&lt;turn action&gt; ::= "turnto" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "turnto" is
     * present but not followed by a valid &lt;expression&gt;, or the 
     * &lt;expression&gt; is not followed by an eol.
     * @return <code>true</code> if a turnto action is recognized.
     */
    public boolean isTurnToAction() {
    	if (!keyword("turnto")) {return false;}
    	
    	if (!isExpression()) {error("Invalid expression following 'move'");}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;var declaration&gt;.
     * <pre>&lt;var declaration&gt; ::= "var" &lt;NAME&gt; { "," &lt;NAME&gt; } &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the "var" is
     * present but not followed by a valid &lt;NAME&gt;, or a "," is present but 
     * not followed by a valid &lt;NAME&gt;, or there is no valid eol at the end.
     * @return <code>true</code> if a var declaration is recognized.
     */
    public boolean isVarDeclaration() {
    	if (!keyword("var")) {return false;}
    	
    	if (!nextTokenMatches(Token.Type.NAME)) {error("Invalid name following 'var'");}
    	
    	while (symbol(",")) {
    		if (!nextTokenMatches(Token.Type.NAME)) {error("Invalid name following ','");}
    	}
    	
    	if (!isEol()) {error("Error with eol");}
    	
    	return true;
    }

//----- Private "helper" methods

    /**
   * Tests whether the next token is a number. If it is, the token
   * is consumed, otherwise it is not.
   *
   * @return <code>true</code> if the next token is a number.
   */
    private boolean number() {
        return nextTokenMatches(Token.Type.NUMBER);
    }

    /**
     * Tests whether the next token is a name. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a name.
     */
    private boolean name() {
        return nextTokenMatches(Token.Type.NAME);
    }

    /**
     * Tests whether the next token is the expected name. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @param expectedName The String value of the expected next token.
     * @return <code>true</code> if the next token is a name with the expected value.
     */
    private boolean name(String expectedName) {
        return nextTokenMatches(Token.Type.NAME, expectedName);
    }

    /**
     * Tests whether the next token is the expected keyword. If it is, the token
     * is moved to the stack, otherwise it is not.
     *
     * @param expectedKeyword The String value of the expected next token.
     * @return <code>true</code> if the next token is a keyword with the expected value.
     */
    private boolean keyword(String expectedKeyword) {
        return nextTokenMatches(Token.Type.KEYWORD, expectedKeyword);
    }

    /**
     * Tests whether the next token is the expected symbol. If it is,
     * the token is consumed, otherwise it is not.
     *
     * @param expectedSymbol The String value of the token we expect
     *    to encounter next.
     * @return <code>true</code> if the next token is the expected symbol.
     */
    boolean symbol(String expectedSymbol) {
        return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
    }

    /**
     * Tests whether the next token has the expected type. If it does,
     * the token is consumed, otherwise it is not. This method would
     * normally be used only when the token's value is not relevant.
     *
     * @param type The expected type of the next token.
     * @return <code>true</code> if the next token has the expected type.
     */
    boolean nextTokenMatches(Token.Type type) {
        Token t = nextToken();
        if (t.type == type) return true;
        pushBack();
        return false;
    }

    /**
     * Tests whether the next token has the expected type and value.
     * If it does, the token is consumed, otherwise it is not. This
     * method would normally be used when the token's value is
     * important.
     *
     * @param type The expected type of the next token.
     * @param value The expected value of the next token; must
     *              not be <code>null</code>.
     * @return <code>true</code> if the next token has the expected type.
     */
    boolean nextTokenMatches(Token.Type type, String value) {
        Token t = nextToken();
        if (type == t.type && value.equals(t.value)) return true;
        pushBack();
        return false;
    }

    /**
     * Returns the next Token.
     * @return The next Token.
     */
    Token nextToken() {
        int code;
        try { code = tokenizer.nextToken(); }
        catch (IOException e) { throw new Error(e); } // Should never happen
        switch (code) {
            case StreamTokenizer.TT_WORD:
                if (Token.KEYWORDS.contains(tokenizer.sval)) {
                    return new Token(Token.Type.KEYWORD, tokenizer.sval);
                }
                return new Token(Token.Type.NAME, tokenizer.sval);
            case StreamTokenizer.TT_NUMBER:
                return new Token(Token.Type.NUMBER, tokenizer.nval + "");
            case StreamTokenizer.TT_EOL:
                return new Token(Token.Type.EOL, "\n");
            case StreamTokenizer.TT_EOF:
                return new Token(Token.Type.EOF, "EOF");
            default:
                return new Token(Token.Type.SYMBOL, ((char) code) + "");
        }
    }

    /**
     * Returns the most recent Token to the tokenizer.
     */
    void pushBack() {
        tokenizer.pushBack();
    }

    /**
     * Utility routine to throw a <code>SyntaxException</code> with the
     * given message.
     * @param message The text to put in the <code>SyntaxException</code>.
     */
    private void error(String message) {
        throw new SyntaxException("Line " + lineNumber + ": " + message);
    }
}
