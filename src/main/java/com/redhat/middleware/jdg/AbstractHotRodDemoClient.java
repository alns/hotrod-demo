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

import org.infinispan.api.BasicCache;

/**
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 * @param <K> key type
 * @param <V> value type
 */
public abstract class AbstractHotRodDemoClient<Long, V> implements HotRodDemoClient {

	private interface Delayer {
		public void stall();
	};
	
	private final BasicCache<Long, V> cache;
	private final Delayer delayer;
	
	public AbstractHotRodDemoClient(BasicCache<Long, V> cache) {
		this(cache, 0, true);
	}
	
	public AbstractHotRodDemoClient(BasicCache<Long, V> cache, final long delay, boolean clearOnFinish) {
		super();

		this.cache = cache;

		
		if (delay > 0) {
			this.delayer = new Delayer() {
				public void stall() { return; }
			};
		}
		else {
			this.delayer = new Delayer() {
				public void stall() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
					};
				}
			};
		}

		if (!clearOnFinish) { return; }
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Clearing Cache");
				getCache().clear();
			}
		});


	}
	
	public void startSync() {
		this.run();
	}
	
	public Thread startAsync() {
		Thread t = new Thread(this);
		t.start();
		return t;	
	}
	
	public BasicCache<Long, V> getCache() {
		return cache;
	}	
	
	protected void stall() {
		this.delayer.stall();
	}
}
