package bugs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * GUI for Bugs language.
 * @author Anders Schneider
 * @version 2015
 */
public class BugsGui extends JFrame {
    private static final long serialVersionUID = 1L;
    View display;
    JSlider speedControl;
    int speed;
    JButton stepButton;
    JButton runButton;
    JButton pauseButton;
    JButton resetButton;
    
    private Interpreter interpreter;
    
    /**
     * GUI constructor.
     */
    public BugsGui() {
    	
        super();
        
    	interpreter = new Interpreter();
        setSize(600, 600);
        setLayout(new BorderLayout());
        createAndInstallMenus();
        createDisplayPanel();
        createControlPanel();
        initializeButtons();
        setVisible(true);
    }

    private void createAndInstallMenus() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        JMenuItem loadMenuItem = new JMenuItem("Load...");
        JMenuItem helpMenuItem = new JMenuItem("Help");
        
        menuBar.add(fileMenu);
        fileMenu.add(quitMenuItem);
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quit();
            }});
        fileMenu.add(loadMenuItem);
        loadMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				load();
			}
        });
        
        menuBar.add(helpMenu);
        helpMenu.add(helpMenuItem);
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                help();
            }});
        
        this.setJMenuBar(menuBar);
    }

    private void createDisplayPanel() {
        display = new View(interpreter);
        add(display, BorderLayout.CENTER);
    }


    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        
        addSpeedLabel(controlPanel);       
        addSpeedControl(controlPanel);
        addStepButton(controlPanel);
        addRunButton(controlPanel);
        addPauseButton(controlPanel);
        addResetButton(controlPanel);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void addSpeedLabel(JPanel controlPanel) {
        controlPanel.add(new JLabel("Speed:"));
    }

    private void addSpeedControl(JPanel controlPanel) {
        speedControl = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
        speed = 50;
        speedControl.setMajorTickSpacing(10);
        speedControl.setMinorTickSpacing(5);
        speedControl.setPaintTicks(true);
        speedControl.setPaintLabels(true);
        speedControl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                resetSpeed(speedControl.getValue());
            }
        });
        controlPanel.add(speedControl);
    }

    private void addStepButton(JPanel controlPanel) {
        stepButton = new JButton("Step");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAnimation();
            }
        });
        controlPanel.add(stepButton);
    }

    private void addRunButton(JPanel controlPanel) {
        runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAnimation();
            }
        });
        controlPanel.add(runButton);
    }

    private void addPauseButton(JPanel controlPanel) {
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseAnimation();
            }
        });
        controlPanel.add(pauseButton);
    }

    private void addResetButton(JPanel controlPanel) {
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAnimation();
            }
        });
        controlPanel.add(resetButton);
    }
    
    private void initializeButtons() {
        stepButton.setEnabled(false);
        runButton.setEnabled(false);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void resetSpeed(int value) {
        speed = value;
        interpreter.setDelay(1100 - speed * 10);
    }
    
    protected void stepAnimation() {
    	interpreter.paused = true;
    	
        runButton.setEnabled(true);
        interpreter.runSingleStep();
                
        display.repaint();
        
        if (!interpreter.doneEvaluating) {
        	runButton.setEnabled(true);
            stepButton.setEnabled(true);
        } else {
        	runButton.setEnabled(false);
            stepButton.setEnabled(false);
        }
        pauseButton.setEnabled(false);
        resetButton.setEnabled(true);
    }
    
    protected void runAnimation() {
    	interpreter.paused = false;
        stepButton.setEnabled(true);
        runButton.setEnabled(false);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
        
        if (!interpreter.isAlive()) {
        	interpreter.start();
        }
    }
    
    protected void pauseAnimation() {
    	interpreter.paused = true;
        if (!interpreter.doneEvaluating) {
        	runButton.setEnabled(true);
        	stepButton.setEnabled(true);
        } else {
        	runButton.setEnabled(false);
        	stepButton.setEnabled(false);
        }
        pauseButton.setEnabled(false);
        resetButton.setEnabled(true);
    }
    
    protected void resetAnimation() {    	
    	String programString = interpreter.program;
    	
    	interpreter = new Interpreter();
    	interpreter.program = programString;
    	interpreter.parseProgram();
    	interpreter.interpretAllbugs();
    	interpreter.initializeBugs();
    	
    	display.interpreter = this.interpreter;
    	
    	interpreter.setDelay(1100 - 10 * speed);
    	
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(false);
        
        display.repaint();
    }

    protected void help() {
    	
    }

    protected void quit() {
        System.exit(0);
    }
    
    protected void load() {    	
    	try {
    		interpreter.load();
    	} catch (IOException e) {
    		System.out.println("Couldn't load that file!");
    		return;
    	}
    	
    	try {
    		interpreter.parseProgram();
    	} catch (RuntimeException e) {
    		System.out.println("Couldn't parse that file, error: " + e.getMessage());
    		return;
    	}
    	
    	try {
    		interpreter.interpretAllbugs();
    	} catch (RuntimeException e) {
    		System.out.println("Encountered an error in the Allbugs code: " + e.getMessage());
    		return;
    	}
    	
    	try {
    		interpreter.initializeBugs();
    	} catch (RuntimeException e) {
    		System.out.println("Encountered an error in the Bug initialization code: " + e.getMessage());
    		return;
    	}
    	
    	stepButton.setEnabled(true);
    	runButton.setEnabled(true);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new BugsGui();
    }
}