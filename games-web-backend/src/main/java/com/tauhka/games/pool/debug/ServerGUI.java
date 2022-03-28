package com.tauhka.games.pool.debug;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tauhka.games.core.Vector2d;
import com.tauhka.games.pool.Ball;
import com.tauhka.games.pool.PoolTable;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 * 
 * Class that visualizes server side calculations in a JPanel. Can be used during development to compare UI calculations.
 **/

public class ServerGUI extends Thread {
	private PoolTable poolTable;
	private JFrame frame;
	private JPanel canvasPanel;

	public ServerGUI(PoolTable poolTable) {
		this.poolTable = poolTable;
		JPanel canvasPanel = new JPanel(true);
		canvasPanel.setLayout(null);
		JComponent tableComponent = new PoolGUIComponent("table.png", poolTable, 1200, 677);
		tableComponent.setBounds(0, 0, 1200, 677);
		canvasPanel.setBounds(new Rectangle(0, 0, 1200, 677));
		for (Ball ball : poolTable.getRemainingBalls()) {
			PoolGUIComponent l = new PoolGUIComponent(ball.getNumber() + ".png", ball, 34, 34);
			l.setBounds(ball.getPosition().getX().intValue(), ball.getPosition().getY().intValue(), 141, 141);
			canvasPanel.add(l);
		}
		canvasPanel.add(tableComponent);
		this.canvasPanel = canvasPanel;
		JFrame frame = new JFrame();
		frame.getContentPane().add(canvasPanel);
		frame.setLayout(null);
		frame.setSize(1400, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvasPanel.setVisible(true);
		frame.setVisible(true);
		this.frame = frame;
	}

	public void updateBallPositions() {
		int componentCount = this.canvasPanel.getComponentCount();
		for (int i = 0; i < componentCount - 1; i++) {
			PoolGUIComponent p = (PoolGUIComponent) canvasPanel.getComponent(i);
			p.setBounds(p.getPoolComponent().getPosition().getX().intValue(), p.getPoolComponent().getPosition().getY().intValue(), 141, 141);
		}
	}

	// Updates component positions every time thread wakes up.
	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("REPAINT");
				Thread.sleep(2000);
				canvasPanel.repaint();
				Ball b = poolTable.getRemainingBalls().get(0);
				b.getPosition().add(new Vector2d(10d, 10d));
				updateBallPositions();
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
