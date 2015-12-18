package com.boye.trademe;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by boy on 11/12/15.
 */
public class WatchListCallback extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String WATCH_LIST_URL = "https://api.tmsandbox.co.nz/v1/MyTradeMe/Watchlist/All.xml";
    private TrademeTemplate authorization = TrademeTemplate.getInstance();


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String oauthVerifier = request.getParameter("oauth_verifier");
            System.out.println("oauth_verifier: " + oauthVerifier);
            authorization.setUpAccessToken(oauthVerifier);
            String result = authorization.call(WATCH_LIST_URL);
            response.setContentType("application/xml");
            PrintWriter out = response.getWriter();
            out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
