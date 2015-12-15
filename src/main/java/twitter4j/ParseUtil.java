/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A tiny parse utility class.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
final class ParseUtil {
    private ParseUtil() {
        // should never be instantiated
        throw new AssertionError();
    }





    public static Date parseTrendsDate(String asOfStr) throws TwitterException {
        Date parsed;
        switch (asOfStr.length()) {
            case 10:
                parsed = new Date(Long.parseLong(asOfStr) * 1000);
                break;
            case 20:
                parsed = getDate(asOfStr, "yyyy-MM-dd'T'HH:mm:ss'Z'");
                break;
            default:
                parsed = getDate(asOfStr, "EEE, d MMM yyyy HH:mm:ss z");
        }
        return parsed;
    }




    private final static Map<String, LinkedBlockingQueue<SimpleDateFormat>> formatMapQueue = new HashMap<String,
            LinkedBlockingQueue<SimpleDateFormat>>();

    public static Date getDate(String dateString, String format) throws TwitterException {
        LinkedBlockingQueue<SimpleDateFormat> simpleDateFormats = formatMapQueue.get(format);
        if (simpleDateFormats == null) {
            simpleDateFormats = new LinkedBlockingQueue<SimpleDateFormat>();
            formatMapQueue.put(format, simpleDateFormats);
        }
        SimpleDateFormat sdf = simpleDateFormats.poll();
        if (null == sdf) {
            sdf = new SimpleDateFormat(format, Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        }
        try {
            return sdf.parse(dateString);
        } catch (ParseException pe) {
            throw new TwitterException("Unexpected date format(" + dateString + ") returned from twitter.com", pe);
        } finally {
            try {
                simpleDateFormats.put(sdf);
            } catch (InterruptedException ignore) {
                // the size of LinkedBlockingQueue is Integer.MAX by default.
                // there is no need to concern about this situation
            }
        }
    }



    public static int getInt(String str) {
        if (null == str || "".equals(str) || "null".equals(str)) {
            return -1;
        } else {
            try {
                return Integer.valueOf(str);
            } catch (NumberFormatException nfe) {
                // workaround for the API side issue http://issue.twitter4j.org/youtrack/issue/TFJ-484
                return -1;
            }
        }
    }



    public static long getLong(String str) {
        if (null == str || "".equals(str) || "null".equals(str)) {
            return -1;
        } else {
            // some count over 100 will be expressed as "100+"
            if (str.endsWith("+")) {
                str = str.substring(0, str.length() - 1);
                return Long.valueOf(str) + 1;
            }
            return Long.valueOf(str);
        }
    }






    public static int toAccessLevel(HttpResponse res) {
        if (null == res) {
            return -1;
        }
        String xAccessLevel = res.getResponseHeader("X-Access-Level");
        int accessLevel;
        if (null == xAccessLevel) {
            accessLevel = TwitterResponse.NONE;
        } else {
            // https://dev.twitter.com/pages/application-permission-model-faq#how-do-we-know-what-the-access-level-of-a-user-token-is
            switch (xAccessLevel.length()) {
                // “read” (Read-only)
                case 4:
                    accessLevel = TwitterResponse.READ;
                    break;
                case 10:
                    // “read-write” (Read & Write)
                    accessLevel = TwitterResponse.READ_WRITE;
                    break;
                case 25:
                    // “read-write-directmessages” (Read, Write, & Direct Message)
                    accessLevel = TwitterResponse.READ_WRITE_DIRECTMESSAGES;
                    break;
                case 26:
                    // “read-write-privatemessages” (Read, Write, & Direct Message)
                    accessLevel = TwitterResponse.READ_WRITE_DIRECTMESSAGES;
                    break;
                default:
                    accessLevel = TwitterResponse.NONE;
                    // unknown access level;
            }
        }
        return accessLevel;
    }
}
