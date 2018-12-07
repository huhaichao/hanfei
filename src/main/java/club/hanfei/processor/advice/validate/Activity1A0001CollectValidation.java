
package club.hanfei.processor.advice.validate;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.service.ActivityQueryService;
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
 * Validates for activity 1A0001 collect.
 *
@version 1.1.0.1, Jun 2, 2018
 * @since 1.3.0
 */
@Singleton
public class Activity1A0001CollectValidation extends ProcessAdvice {

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

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        if (Hanfei.getBoolean("activity1A0001Closed")) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityClosedLabel")));
        }

        final Calendar calendar = Calendar.getInstance();

        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activity1A0001CloseLabel")));
        }

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 16) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityCollectNotOpenLabel")));
        }

        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        if (!activityQueryService.is1A0001Today(currentUser.optString(Keys.OBJECT_ID))) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityNotParticipatedLabel")));
        }
    }
}
