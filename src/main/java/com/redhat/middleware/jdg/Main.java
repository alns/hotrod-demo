/*
* JBoss, Home of Professional Open Source
* Copyright 2011 Red Hat Inc. and/or its affiliates and other
* contributors as indicated by the @author tags. All rights reserved.
* See the copyright.txt in the distribution for a full listing of
* individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package com.redhat.middleware.jdg;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCache;

/**
 * Main class.  Configure accordingly!  Pay attention to
 * <code>INITIAL_LIST</code> and <code>CACHE_NAME</code>
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 */
public class Main {
	/**
	 * Initial hotrod server list
	 */
	private static String LOCALHOST = "127.0.0.1";
	private static String SERVER_IP = System.getProperty("hotrodServer", LOCALHOST);
	/**
	 * Name of the cache to use for demo
	 */
	private static final String DEFAULT_CACHE_NAME = "___defaultcache";
	private static String CACHE_NAME = System.getProperty("cacheName", DEFAULT_CACHE_NAME);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RemoteCacheManager cm = new RemoteCacheManager(SERVER_IP);
		
		RemoteCache<Long, Object> cache = cm.getCache(CACHE_NAME);
		
		CountDemoClient countDemo = new CountDemoClient(cache);
		countDemo.startSync();
		
		//RemoteCache<Long, Status> cache = cm.getCache(CACHE_NAME);
		//TwitterDemoClient twitterDemo = new TwitterDemoClient( (RemoteCache<Long, Status>)cache);//, "CONSUMER KEY", "CONSUMER SECRET");
		//Thread t = twitterDemo.startAsync();
		//t.join();
	}
}
