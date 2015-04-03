package bugs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFileChooser;

import tree.Tree;

public class Interpreter extends Thread {
	
	String program;
	Tree<Token> allbugs;
	Tree<Token> bugsList;
	HashMap<String, Bug> bugs;
	HashMap<String, Double> variables;
	HashMap<String, Tree<Token>> functions;
	ArrayList<Command> commands;
	boolean paused;
	boolean doneEvaluating;
	int delay;
		
	/**
	 * Constructs a new Interpreter and initializes all of its data structures.
	 */
	public Interpreter() {
		variables = new HashMap<String, Double>();
		functions = new HashMap<String, Tree<Token>>();
		bugs = new HashMap<String, Bug>();
		commands = new ArrayList<Command>();
		paused = false;
		doneEvaluating = false;
		delay = 600;
	}
	
	/**
	 * Makes the stored program string into an AST
	 */
	public void parseProgram() {
		Parser p = new Parser(program);
		if (!p.isProgram()) {
			throw new RuntimeException("Input program is not a valid program");
		}
		
		Tree<Token> programTree = p.stack.pop();
		
		allbugs = programTree.getChild(0);
		bugsList = programTree.getChild(1);
	}
	
	/**
	 * Interprets the parts of the program tree corresponding to the initialization of Bugs
	 */
	public void initializeBugs() {
		
		for (int i = 0; i < bugsList.getNumberOfChildren(); i++) {
			Tree<Token> bugTree = bugsList.getChild(i);
			Bug b = new Bug(this, bugTree);
			
			if (bugs.containsKey(b.bugName)) {
				throw new IllegalArgumentException("Bugs must have distinct names! Two bugs "
													+ "were found to be named " + b.bugName);
			}
			
			bugs.put(b.bugName, b);
			b.setBlocked(true);
		}
		
		Iterator bugsIter = bugs.keySet().iterator();
		while (bugsIter.hasNext()) {
			Bug b = bugs.get(bugsIter.next());
			b.start();
		}
	}
	
	@Override
	public void run() {
		runContinuously();
	}
		
	/**
	 * Sets the delay between rounds of action for the Bugs
	 * 
	 * @param newDelay
	 */
	public void setDelay(int newDelay) {
		delay = newDelay;
	}
	
	/**
	 * Continuously grants permission to Bugs to perform rounds of action, sleeping for the
	 * determined amount of time between rounds.
	 */
	public void runContinuously() {
		while (!bugs.isEmpty()) {
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (paused) {
				continue;
			}
			
			roundOfActions();
		}
	}
	
	/**
	 * Permits the Bugs to each execute one action
	 */
	public void runSingleStep() {
		roundOfActions();
	}
		
	/**
	 * Waits until all Bugs are blocked, then unblocks all bugs and allows them
	 * to each perform one action
	 */
	synchronized private void roundOfActions() {
		while (countBlockedBugs() < bugs.size()) {
			try {
				wait();
			} catch (InterruptedException e) { }
		}
		
		Iterator bugsIter = bugs.keySet().iterator();
		while(bugsIter.hasNext()) {
			bugs.get(bugsIter.next()).setBlocked(false);
		}
		
		notifyAll();
	}
	
	/**
	 * Blocks the Bug until all Bugs are unblocked
	 * @param b
	 */
	synchronized void getPermissionToAct(Bug b) {
		while (b.isBlocked()) {
			try {
				wait();
			} catch (InterruptedException e) { }
		}
	}
	
	/**
	 * Blocks the Bug after it completes its action
	 * @param b
	 */
	synchronized void completeAction(Bug b) {
		b.setBlocked(true);
		notifyAll();
	}
	
	/**
	 * Removes the Bug from the pool of Bugs still executing
	 * @param b
	 */
	synchronized void terminateBug(Bug b) {
		bugs.remove(b.bugName);
		if (bugs.isEmpty()) {
			doneEvaluating = true;
		}
	}

	private int countBlockedBugs() {
		int result = 0;
		Iterator bugsIter = bugs.keySet().iterator();
		while(bugsIter.hasNext()) {
			if (bugs.get(bugsIter.next()).isBlocked()) {
				result++;
			}
		}
		return result;
	}

	public void interpretAllbugs() {
		Tree<Token> varList = allbugs.getChild(0);
		for (int i = 0; i < varList.getNumberOfChildren(); i++) {
			interpretVariables(varList.getChild(i));
		}
		
		Tree<Token> funcList = allbugs.getChild(1);
		for (int i = 0; i < funcList.getNumberOfChildren(); i++) {
			interpretFunction(funcList.getChild(i));
		}
	}
	
	void interpretFunction(Tree<Token> tree) {
		String funcName = tree.getChild(0).getValue().value;
		
		functions.put(funcName, tree);
	}

	void interpretVariables(Tree<Token> tree) {
		int numChildren = tree.getNumberOfChildren();
		
		for (int i = 0; i < numChildren; i++) {
			String varName = tree.getChild(i).getValue().value;
			variables.put(varName, 0.0);
		}
	}
	
	public void load() throws IOException {
		StringBuilder sb = new StringBuilder();
        BufferedReader reader;
        String fileName;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load which file?");
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                fileName = file.getCanonicalPath();
                reader =
                    new BufferedReader(new FileReader(fileName));
                String line;
                while ((line = reader.readLine()) != null) {
                	sb.append(line);
                	sb.append('\n');
                }
                reader.close();
                program = sb.toString();
            }
        }
    }
}
