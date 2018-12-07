
package club.hanfei.service;

import club.hanfei.util.Hanfei;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Turing query service.
 *
@version 1.0.0.3, Aug 2, 2018
 * @since 1.4.0
 */
@Service
public class TuringQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TuringQueryService.class);

    /**
     * Enabled Turing Robot or not.
     */
    private static final boolean TURING_ENABLED = Hanfei.getBoolean("turing.enabled");

    /**
     * Turing Robot API.
     */
    private static final String TURING_API = Hanfei.get("turing.api");

    /**
     * Turing Robot Key.
     */
    private static final String TURING_KEY = Hanfei.get("turing.key");

    /**
     * Robot name.
     */
    public static final String ROBOT_NAME = Hanfei.get("turing.name");

    /**
     * Robot avatar.
     */
    public static final String ROBOT_AVATAR = Hanfei.get("turing.avatar");

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Chat with Turing Robot.
     *
     * @param userName the specified user name
     * @param msg      the specified message
     * @return robot returned message, return {@code null} if not found
     */
    public String chat(final String userName, final String msg) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(msg) || !TURING_ENABLED) {
            return null;
        }

        try {
            final JSONObject reqData = new JSONObject();
            reqData.put("reqType", 0);
            final JSONObject perception = new JSONObject();
            final JSONObject inputText = new JSONObject();
            inputText.put("text", msg);
            perception.put("inputText", inputText);
            reqData.put("perception", perception);
            final JSONObject userInfo = new JSONObject();
            userInfo.put("apiKey", TURING_KEY);
            userInfo.put("userId", userName);
            userInfo.put("userIdName", userName);
            reqData.put("userInfo", userInfo);

            final HttpResponse response = HttpRequest.post(TURING_API).bodyText(reqData.toString()).contentTypeJson().timeout(5000).send();
            response.charset("UTF-8");
            final JSONObject data = new JSONObject(response.bodyText());
            final JSONObject intent = data.optJSONObject("intent");
            final int code = intent.optInt("code");
            final JSONArray results = data.optJSONArray("results");
            switch (code) {
                case 5000:
                case 6000:
                case 4000:
                case 4001:
                case 4002:
                case 4003:
                case 4005:
                case 4007:
                case 4100:
                case 4200:
                case 4300:
                case 4400:
                case 4500:
                case 4600:
                case 4602:
                case 7002:
                case 8008:
                    LOGGER.log(Level.ERROR, "Turing query failed with code [" + code + "]");

                    return langPropsService.get("turingQuotaExceedLabel");
                case 10004:
                case 10019:
                case 10014:
                case 10013:
                case 10008:
                case 10011:
                    final StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < results.length(); i++) {
                        final JSONObject result = results.optJSONObject(i);
                        final String resultType = result.optString("resultType");
                        String values = result.optJSONObject("values").optString(resultType);
                        if (StringUtils.endsWithAny(values, new String[]{"jpg", "png", "gif"})) {
                            values = "![](" + values + ")";
                        }

                        builder.append(values).append("\n");
                    }
                    String ret = builder.toString();
                    ret = StringUtils.trim(ret);

                    return ret;
                default:
                    LOGGER.log(Level.WARN, "Turing Robot default return [" + data.toString(4) + "]");

                    return langPropsService.get("turingQuotaExceedLabel");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Chat with Turing Robot failed", e);
        }

        return null;
    }
}
