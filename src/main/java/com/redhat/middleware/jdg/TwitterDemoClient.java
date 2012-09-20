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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.api.BasicCache;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * A hotrod client that populates the cache w/ Twitter stream.
 * 
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 *
 */
public class TwitterDemoClient extends AbstractHotRodDemoClient<Long, Status> {
	
	private Logger logger = Logger.getLogger(TwitterDemoClient.class.getName());

	/**
	 * Twitter API Consumer Key, you should create your own.
	 */
	private final String consumerKey = System.getProperty("twitConsumerKey");

	/**
	 * Twitter API Consumer Secret, you should create your own.
	 */
	private final String consumerSecret = System.getProperty("twitConsumerSecret");
	
	private String accessToken;
	private String accessTokenSecret;

	public TwitterDemoClient(BasicCache<Long, Status> cache) {
		super(cache);
	}
	
	protected void authorize() throws RuntimeException {
		Twitter twitter = new TwitterFactory().getInstance();
		if (consumerKey == null || consumerSecret == null) 
			throw new RuntimeException("Missing consumer secret and/or key");
		
		try {
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
			RequestToken requestToken = twitter.getOAuthRequestToken();
			
			AccessToken accessToken = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (null == accessToken) {
				System.out.println("Open the following URL and grant access to the account:");
				System.out.println(requestToken.getAuthorizationURL());
				System.out.print("Enter PIN(if aviailable) or just enter:");
				
				try {
					String pin = br.readLine();
	
					if (pin.length() > 0) {
						accessToken = twitter.getOAuthAccessToken(requestToken, pin);
					} else {
						accessToken = twitter.getOAuthAccessToken();
					}
				} catch (IOException e) {
					throw new RuntimeException("Unable to parse PIN input.", e);
				} finally {
					try { br.close(); } catch (Exception ex) {}
				}
				this.accessToken = accessToken.getToken();
				this.accessTokenSecret = accessToken.getTokenSecret();
			}
		} catch (TwitterException e) {
			throw new RuntimeException("Fatal error with twitter stream.", e);
		} 
		
	}
		
	public void run() {
		ConfigurationBuilder cb = new ConfigurationBuilder();

		if (accessTokenSecret == null || accessToken == null) {
			try {
				this.authorize();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error occured while authorizing", e);
			}
		}

		cb.setDebugEnabled(false).setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessTokenSecret);

		final TwitterStream stream = new TwitterStreamFactory(cb.build())
				.getInstance();
		
		stream.addListener(new  StatusListener() {
			
			public void onStatus(Status status) {
				getCache().put(status.getId(), status);
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {	}

			public void onException(Exception ex) { ex.printStackTrace(); }

			public void onScrubGeo(long lat, long lng) { }
		});
				

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				stream.shutdown();
			}
		});

		stream.sample();
	}
}
