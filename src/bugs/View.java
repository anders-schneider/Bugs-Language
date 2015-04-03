package bugs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Observable;

import javax.swing.JPanel;
import javax.swing.Timer;

public class View extends JPanel implements ActionListener {
	
	Timer timer = new Timer(40, this);
	Interpreter interpreter;
	
	/**
	 * View constructor
	 * @param interpreter
	 */
	public View(Interpreter interpreter) {
		this.interpreter = interpreter;
		timer.start();
	}
	
	/**
	 * Paints a triangle to represent each Bug and a line to represent where they have been.
	 * 
	 * @param g Where to paint this Bug.
	 */
	@Override
	public void paint(Graphics g) {
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		
		if (interpreter == null) {
			return;
		}
		
		// Draw lines
		for (int i = 0; i < interpreter.commands.size(); i++) {
			Command command = interpreter.commands.get(i);
			if (command == null) continue;
			int x1 = (int) scaleX(command.x1);
			int y1 = (int) scaleY(command.y1);
			int x2 = (int) scaleX(command.x2);
			int y2 = (int) scaleY(command.y2);
			
			g.setColor(command.color);
			g.drawLine(x1, y1, x2, y2);
		}
		
		// Draw bugs
		Iterator bugsIter = interpreter.bugs.keySet().iterator();
		
		while (bugsIter.hasNext()) {
			String next = (String) bugsIter.next();
			Bug b = interpreter.bugs.get(next);
			
			if (b == null) {
				continue;
			} else if (b.bugName == null) {
				continue;
			}
			
			if (b.bugColor == null) continue;
			g.setColor(b.bugColor);
			
			int x1 = (int) (scaleX(b.x) + computeDeltaX(12, (int)b.angle));
		    int x2 = (int) (scaleX(b.x) + computeDeltaX(6, (int)b.angle - 135));
		    int x3 = (int) (scaleX(b.x) + computeDeltaX(6, (int)b.angle + 135));
		    
		    int y1 = (int) (scaleY(b.y) + computeDeltaY(12, (int)b.angle));
		    int y2 = (int) (scaleY(b.y) + computeDeltaY(6, (int)b.angle - 135));
		    int y3 = (int) (scaleY(b.y) + computeDeltaY(6, (int)b.angle + 135));
		    g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
		}
	}

	private double scaleY(double y) {
		return this.getHeight() * (y / 100);
	}

	private double scaleX(double x) {
		return this.getWidth() * (x / 100);
	}

	/**
	 * Computes how much to move to add to this Bug's x-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees".
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the x-coordinate.
	 */
	private static double computeDeltaX(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.cos(radians);
	}

	/**
	 * Computes how much to move to add to this Bug's y-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees.
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the y-coordinate.
	 */
	private static double computeDeltaY(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.sin(-radians);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			repaint();
		}
	}
}
