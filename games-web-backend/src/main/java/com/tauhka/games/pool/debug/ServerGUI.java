package com.tauhka.games.pool.debug;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;

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

public class ServerGUI implements Runnable {
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
			l.setBounds((int) ball.getPosition().x - ball.getRadius().intValue(), (int) ball.getPosition().y - ball.getRadius().intValue(), 141, 141);
			canvasPanel.add(l);
		}
		canvasPanel.add(tableComponent);
		this.canvasPanel = canvasPanel;
		frame = new JFrame();
		frame.getContentPane().add(canvasPanel);
		frame.setLayout(null);
		frame.setSize(1400, 800);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		canvasPanel.setVisible(true);
		frame.setVisible(true);

	}

	public void updateSwingComponentPositions() {
		JPanel canvasPanel = this.canvasPanel;
		System.out.println("ServerGui updateSwingComponentPositions");
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					int componentCount = canvasPanel.getComponentCount();
					for (int i = 0; i < componentCount - 1; i++) {
						ServerGUIComponent ballPanel = (ServerGUIComponent) canvasPanel.getComponent(i);
						Ball ball = (Ball) ballPanel.getPoolComponent();
						System.out.println("ServerGUI -> " + ballPanel.getPoolComponent().getNumber() + " panel old position x:" + ballPanel.getX() + " y:" + ballPanel.getY());
						ballPanel.setBounds((int) ballPanel.getPoolComponent().getPosition().x - ball.getRadius().intValue(), (int) ballPanel.getPoolComponent().getPosition().y - ball.getRadius().intValue(), 141, 141);
						System.out.println("ServerGUI -> ball real position:" + ball.getPosition() + "  Panel positionx:" + ballPanel.getX() + " y:" + ballPanel.getY());
					}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
