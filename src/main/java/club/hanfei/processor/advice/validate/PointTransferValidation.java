
package club.hanfei.processor.advice.validate;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Common;
import club.hanfei.model.Pointtransfer;
import club.hanfei.model.UserExt;
import club.hanfei.service.UserQueryService;
import club.hanfei.util.Hanfei;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Validates for user point transfer.
 *
@version 1.0.1.4, Oct 1, 2018
 * @since 1.3.0
 */
@Singleton
public class PointTransferValidation extends ProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final String userName = requestJSONObject.optString(User.USER_NAME);
        if (StringUtils.isBlank(userName)
                || UserExt.COM_BOT_NAME.equals(userName) || UserExt.NULL_USER_NAME.equals(userName)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
        }

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        if (amount < 1 || amount > 5000) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("amountInvalidLabel")));
        }

        JSONObject toUser = userQueryService.getUserByName(userName);
        if (null == toUser) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
        }

        if (UserExt.USER_STATUS_C_VALID != toUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        request.setAttribute(Common.TO_USER, toUser);

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        if (currentUser.optString(User.USER_NAME).equals(toUser.optString(User.USER_NAME))) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("cannotTransferSelfLabel")));
        }

        final int balanceMinLimit = Hanfei.getInt("pointTransferMin");
        final int balance = currentUser.optInt(UserExt.USER_POINT);
        if (balance - amount < balanceMinLimit) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("insufficientBalanceLabel")));
        }

        String memo = StringUtils.trim(requestJSONObject.optString(Pointtransfer.MEMO));
        if (128 < StringUtils.length(memo)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("memoTooLargeLabel")));
        }
        memo = Jsoup.clean(memo, Whitelist.none());
        request.setAttribute(Pointtransfer.MEMO, memo);
    }
}
