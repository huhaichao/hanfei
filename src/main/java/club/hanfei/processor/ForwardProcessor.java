
package club.hanfei.processor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.DataModelService;
import club.hanfei.service.LinkMgmtService;
import club.hanfei.util.Hanfei;
import club.hanfei.util.Headers;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.json.JSONObject;

/**
 * Forward processor.
 * <ul>
 * <li>Shows forward page (/forward), GET</li>
 * </ul>
 *
@version 1.0.0.3, Nov 21, 2018
 * @since 2.3.0
 */
@RequestProcessor
public class ForwardProcessor {

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Link management service.
     */
    @Inject
    private LinkMgmtService linkMgmtService;

    /**
     * Shows jump page.
     *
     * @param context the specified context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/forward", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showForward(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        String to = request.getParameter(Common.GOTO);
        if (StringUtils.isBlank(to)) {
            to = Latkes.getServePath();
        }

        final String referer = Headers.getHeader(request, "referer", "");
        if (!StringUtils.startsWith(referer, Latkes.getServePath())) {
            context.sendRedirect(Latkes.getServePath());

            return;
        }

        final String url = to;
        Hanfei.EXECUTOR_SERVICE.submit(() -> {
            linkMgmtService.addLink(url);
        });

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null != user && UserExt.USER_XXX_STATUS_C_DISABLED == user.optInt(UserExt.USER_FORWARD_PAGE_STATUS)) {
            context.sendRedirect(to);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("forward.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put("forwardURL", to);
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }
}
