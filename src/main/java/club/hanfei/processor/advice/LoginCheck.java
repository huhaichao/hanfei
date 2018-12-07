
package club.hanfei.processor.advice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.service.UserMgmtService;
import club.hanfei.service.UserQueryService;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.json.JSONObject;

/**
 * Login check. Gets user from request attribute named "user" if logged in.
 *
@version 1.2.0.3, Jun 2, 2018
 * @since 0.2.5
 */
@Singleton
public class LoginCheck extends ProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LoginCheck.class);

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_UNAUTHORIZED + ", " + request.getRequestURI());
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            throw new RequestProcessAdviceException(exception);
        }

        final int point = currentUser.optInt(UserExt.USER_POINT);
        final int appRole = currentUser.optInt(UserExt.USER_APP_ROLE);
        if (UserExt.USER_APP_ROLE_C_HACKER == appRole) {
            currentUser.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(point));
        } else {
            currentUser.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(point));
        }

        request.setAttribute(User.USER, currentUser);
    }
}
