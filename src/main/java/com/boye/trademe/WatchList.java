package com.boye.trademe;

// Import required java libraries

import twitter4j.TwitterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WatchList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TrademeTemplate authorization = TrademeTemplate.getInstance();

	/**
	 * Step 2: redirect to authorization URL
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.sendRedirect(authorization.getAuthorizationURL()); // trade me login page
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
}