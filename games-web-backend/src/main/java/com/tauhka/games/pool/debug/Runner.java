package com.tauhka.games.pool.debug;

import com.tauhka.games.core.User;
import com.tauhka.games.pool.PoolTable;

/**
 * @author antsa-1 from GitHub 28 Mar 2022
 **/

public class Runner {

	public static void main(String[] args) {
		User u = new User("test");
		new ServerGUI(new PoolTable(u, null, false)).start();
	}
}
