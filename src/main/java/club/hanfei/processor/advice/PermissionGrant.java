
package club.hanfei.processor.advice;

import java.util.Map;

import club.hanfei.model.Common;
import club.hanfei.model.Permission;
import club.hanfei.model.Role;
import club.hanfei.service.RoleQueryService;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.renderer.AbstractResponseRenderer;
import org.b3log.latke.util.Stopwatchs;
import org.json.JSONObject;

/**
 * Permission grant.
 *
@version 1.0.3.2, Jan 7, 2017
 * @since 1.8.0
 */
@Singleton
public class PermissionGrant extends ProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PermissionGrant.class);

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    @Override
    public void doAdvice(final RequestContext context) {
        final AbstractResponseRenderer renderer = context.getRenderer();
        if (null == renderer) {
            return;
        }

        Stopwatchs.start("Grant permissions");
        try {
            final Map<String, Object> dataModel = context.getRenderer().getRenderDataModel();

            final JSONObject user = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final Map<String, JSONObject> permissionsGrant = roleQueryService.getPermissionsGrantMap(roleId);
            dataModel.put(Permission.PERMISSIONS, permissionsGrant);

            final JSONObject role = roleQueryService.getRole(roleId);

            String noPermissionLabel = langPropsService.get("noPermissionLabel");
            noPermissionLabel = noPermissionLabel.replace("{roleName}", role.optString(Role.ROLE_NAME));
            dataModel.put("noPermissionLabel", noPermissionLabel);
        } finally {
            Stopwatchs.end();
        }
    }
}
