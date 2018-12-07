
package club.hanfei.util;

import java.util.Iterator;

import org.json.JSONObject;
import org.owasp.encoder.Encode;

/**
 * Escape utilities.
 *
@version 1.0.0.0, Nov 18, 2017
 * @since 2.3.0
 */
public final class Escapes {

    /**
     * Private constructor.
     */
    private Escapes() {
    }

    /**
     * Escapes the specified string.
     *
     * @param str the specified string
     */
    public static String escapeHTML(final String str) {
        return Encode.forHtml(str);
    }

    /**
     * Escapes string property in the specified JSON object.
     *
     * @param jsonObject the specified JSON object
     */
    public static void escapeHTML(final JSONObject jsonObject) {
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (jsonObject.opt(key) instanceof String) {
                jsonObject.put(key, Encode.forHtml(jsonObject.optString(key)));
            }
        }
    }
}
