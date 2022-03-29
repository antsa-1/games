package com.tauhka.games.pool.debug;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
		JComponent tableComponent = new ServerGUIComponent("table.png", poolTable, 1200, 677);
		tableComponent.setBounds(0, 0, 1200, 677);
		canvasPanel.setBounds(new Rectangle(0, 0, 1200, 677));
		for (Ball ball : poolTable.getRemainingBalls()) {
			ServerGUIComponent l = new ServerGUIComponent(ball.getNumber() + ".png", ball, 34, 34);
			l.setBounds((int) ball.getPosition().x, (int) ball.getPosition().y, 141, 141);
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

	public void updateSwingComponentPositions() {
		int componentCount = this.canvasPanel.getComponentCount();
		for (int i = 0; i < componentCount - 1; i++) {
			ServerGUIComponent ballPanel = (ServerGUIComponent) canvasPanel.getComponent(i);
			System.out.println("ServerGUI -> " + ballPanel.getPoolComponent().getNumber() + " panel old position x:" + ballPanel.getX() + " y:" + ballPanel.getY());
			ballPanel.setBounds((int) ballPanel.getPoolComponent().getPosition().x, (int) ballPanel.getPoolComponent().getPosition().y, 141, 141);
			System.out.println("ServerGUI -> panel new position x:" + ballPanel.getX() + " y:" + ballPanel.getY());
		}
	}

	// Updates component positions every time thread wakes up.
	@Override
	public void run() {
		try {
			synchronized (poolTable) {
				while (true) {
					System.out.println("ServerGui starts to wait");
					poolTable.wait();
					System.out.println("ServerGui continues after waiting");
					updateSwingComponentPositions();
					System.out.println("ServerGui updated component positions, now notifies");
					canvasPanel.revalidate();
					canvasPanel.repaint();
					poolTable.notify();
				}
			}

		} catch (InterruptedException e) {
			System.out.println("ServerGui interrupted");
		}
	}

}
