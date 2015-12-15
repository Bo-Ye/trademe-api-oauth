package twitter4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 4.0.0
 */
public final class TwitterObjectFactory {
    private TwitterObjectFactory() {
        throw new AssertionError("not intended to be instantiated.");
    }

    private static final ThreadLocal<Map> rawJsonMap = new ThreadLocal<Map>() {
        @Override
        protected Map initialValue() {
            return new HashMap();
        }
    };

    /**
     * Returns a raw JSON form of the provided object.<br>
     * Note that raw JSON forms can be retrieved only from the same thread invoked the last method call and will become inaccessible once another method call
     *
     * @param obj target object to retrieve JSON
     * @return raw JSON
     * @since Twitter4J 2.1.7
     */
    public static String getRawJSON(Object obj) {
        if (!registeredAtleastOnce) {
            throw new IllegalStateException("Apparently jsonStoreEnabled is not set to true.");
        }
        Object json = rawJsonMap.get().get(obj);
        if (json instanceof String) {
            return (String) json;
        } else if (json != null) {
            // object must be instance of JSONObject
            return json.toString();
        } else {
            return null;
        }
    }


















    /**
     * Constructs a RateLimitStatus object from rawJSON string.
     *
     * @param rawJSON raw JSON form as String
     * @return RateLimitStatus
     * @throws TwitterException when provided string is not a valid JSON string.
     * @since Twitter4J 2.1.7
     */
    public static Map<String, RateLimitStatus> createRateLimitStatus(String rawJSON) throws TwitterException {
        try {
            return RateLimitStatusJSONImpl.createRateLimitStatuses(new JSONObject(rawJSON));
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }







    /**
     * Constructs an OEmbed object from rawJSON string.
     *
     * @param rawJSON raw JSON form as String
     * @return OEmbed
     * @throws TwitterException when provided string is not a valid JSON string.
     * @since Twitter4J 3.0.2
     */
    public static OEmbed createOEmbed(String rawJSON) throws TwitterException {
        try {
            return new OEmbedJSONImpl(new JSONObject(rawJSON));
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }



    /**
     * clear raw JSON forms associated with the current thread.<br>
     *
     * @since Twitter4J 2.1.7
     */
    static void clearThreadLocalMap() {
        rawJsonMap.get().clear();
    }

    private static boolean registeredAtleastOnce = false;

    /**
     * associate a raw JSON form to the current thread<br>
     *
     * @since Twitter4J 2.1.7
     */
    static <T> T registerJSONObject(T key, Object json) {
        registeredAtleastOnce = true;
        rawJsonMap.get().put(key, json);
        return key;
    }
}