package com.tauhka.games.pool.debug;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import com.tauhka.games.pool.PoolComponent;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

public class PoolGUIComponent extends JComponent {

	private static final long serialVersionUID = 1L;
	private PoolComponent poolComponent;
	private Image scaledImage;

	public PoolGUIComponent(String imageName, PoolComponent p, int width, int height) {
		try {
			// The path where images are found should be replaced here
			BufferedImage image = ImageIO.read(new File("C:\\Users\\test\\workspaces\\games\\games-web-backend\\target\\games\\pool\\" + imageName));
			scaledImage = image.getScaledInstance(width, height, 0);
			poolComponent = p;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PoolComponent getPoolComponent() {
		return poolComponent;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(scaledImage, 0, 0, this);
	}
}
