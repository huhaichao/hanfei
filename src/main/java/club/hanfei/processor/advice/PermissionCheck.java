
package club.hanfei.processor.advice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.model.Permission;
import club.hanfei.model.Role;
import club.hanfei.service.RoleQueryService;
import club.hanfei.util.Hanfei;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.servlet.handler.MatchResult;
import org.b3log.latke.servlet.handler.RouteHandler;
import org.b3log.latke.util.Stopwatchs;
import org.json.JSONObject;

/**
 * Permission check.
 *
@version 1.0.1.1, Oct 16, 2018
 * @since 1.8.0
 */
@Singleton
public class PermissionCheck extends ProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PermissionCheck.class);

    /**
     * URL permission rules.
     * <p>
     * &lt;"url:method", permissions&gt;
     * </p>
     */
    private static final Map<String, Set<String>> URL_PERMISSION_RULES = new HashMap<>();

    static {
        // Loads permission URL rules
        final String prefix = "permission.rule.url.";

        final Set<String> keys = Hanfei.CFG.stringPropertyNames();
        for (final String key : keys) {
            if (key.startsWith(prefix)) {
                final String value = Hanfei.CFG.getProperty(key);
                final Set<String> permissions = new HashSet<>(Arrays.asList(value.split(",")));

                URL_PERMISSION_RULES.put(key, permissions);
            }
        }
    }

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;
    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        Stopwatchs.start("Check Permissions");

        try {
            final HttpServletRequest request = context.getRequest();

            final JSONObject exception = new JSONObject();
            exception.put(Keys.MSG, langPropsService.get("noPermissionLabel"));
            exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

            final String prefix = "permission.rule.url.";
            final String requestURI = request.getRequestURI();
            final String method = request.getMethod();
            String rule = prefix;

            try {
                final MatchResult matchResult = RouteHandler.doMatch(requestURI, method);
                rule += matchResult.getMatchedPattern() + "." + method;
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Match method failed", e);

                throw new RequestProcessAdviceException(exception);
            }

            final Set<String> requisitePermissions = URL_PERMISSION_RULES.get(rule);
            if (null == requisitePermissions) {
                return;
            }

            final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final Set<String> grantPermissions = roleQueryService.getPermissions(roleId);

            if (!Permission.hasPermission(requisitePermissions, grantPermissions)) {
                throw new RequestProcessAdviceException(exception);
            }
        } finally {
            Stopwatchs.end();
        }
    }
}
