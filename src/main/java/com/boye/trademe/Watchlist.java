package com.boye.trademe;

// Import required java libraries

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Watchlist extends HttpServlet {

    private static final long serialVersionUID = 1L;
    //watchlist callback
    private static final String CALLBACK_URL = "http://localhost:8080/trademe-api-oauth/watchlist-callback";
    private final TrademeTemplate trademeTemplate = TrademeTemplate.getInstance();

    /**
     * Step 1: a user accesses the client
     * Step 4: redirect to the authorization page.
     * Step 5: the user grants permission.
     *
     * @param request
     * @param response
     * @throws java.io.IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(trademeTemplate.getAuthorizationURL(CALLBACK_URL)); // trade me login page
    }
}
