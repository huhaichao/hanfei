
package club.hanfei.processor.advice.validate;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.service.ActivityQueryService;
import club.hanfei.service.LivenessQueryService;
import club.hanfei.util.Hanfei;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.json.JSONObject;

/**
 * Validates for activity 1A0001.
 *
@version 1.0.0.4, Jun 2, 2018
 * @since 1.3.0
 */
@Singleton
public class Activity1A0001Validation extends ProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    /**
     * Liveness query service.
     */
    @Inject
    private LivenessQueryService livenessQueryService;

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int currentLiveness = livenessQueryService.getCurrentLivenessPoint(userId);
        final int livenessMax = Hanfei.getInt("activitYesterdayLivenessReward.maxPoint");
        final float liveness = (float) currentLiveness / livenessMax * 100;
        final float livenessThreshold = Hanfei.getFloat("activity1A0001LivenessThreshold");
        if (liveness < livenessThreshold) {
            String msg = langPropsService.get("activityNeedLivenessLabel");
            msg = msg.replace("${liveness}", String.valueOf(livenessThreshold) + "%");
            msg = msg.replace("${current}", String.format("%.2f", liveness) + "%");
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, msg));
        }

        if (Hanfei.getBoolean("activity1A0001Closed")) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityClosedLabel")));
        }

        final Calendar calendar = Calendar.getInstance();

        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activity1A0001CloseLabel")));
        }

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        if (hour > 14 || (hour == 14 && minute > 55)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityEndLabel")));
        }

        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        if (200 != amount && 300 != amount && 400 != amount && 500 != amount) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityBetFailLabel")));
        }

        final int smallOrLarge = requestJSONObject.optInt(Common.SMALL_OR_LARGE);
        if (0 != smallOrLarge && 1 != smallOrLarge) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityBetFailLabel")));
        }

        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        if (activityQueryService.is1A0001Today(userId)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityParticipatedLabel")));
        }

        final int balance = currentUser.optInt(UserExt.USER_POINT);
        if (balance - amount < 0) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("insufficientBalanceLabel")));
        }
    }
}
