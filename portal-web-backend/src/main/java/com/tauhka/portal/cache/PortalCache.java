package com.tauhka.portal.cache;

import com.tauhka.portal.highscore.TopLists;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author antsa-1 from GitHub 19 Feb 2022
 **/

@ApplicationScoped
public class PortalCache {

	private TopLists topListsCache;

	public TopLists getTopLists() {
		return topListsCache;
	}

	public void setTopLists(TopLists topListsCache) {
		this.topListsCache = topListsCache;
	}

}
