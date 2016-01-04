package com.boye.trademe;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by boy on 11/12/15.
 */
public class WatchlistCallback extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String WATCH_LIST_URL = "https://api.tmsandbox.co.nz/v1/MyTradeMe/Watchlist/All.xml";
    private final TrademeTemplate trademeTemplate = TrademeTemplate.getInstance();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String oauthVerifier = request.getParameter("oauth_verifier");
        trademeTemplate.setUpAccessToken(oauthVerifier);
        String result = trademeTemplate.call(WATCH_LIST_URL);
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(result);
    }
}
