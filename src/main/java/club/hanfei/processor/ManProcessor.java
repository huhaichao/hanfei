
package club.hanfei.processor;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.DataModelService;
import club.hanfei.service.ManQueryService;
import org.apache.commons.lang.StringUtils;
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
 * Man processor.
 *
  @version 1.0.0.4, Jul 3, 2017
 * @since 1.8.0
 */
@RequestProcessor
public class ManProcessor {

    /**
     * TLDR query service.
     */
    @Inject
    private ManQueryService manQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Shows man.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/man", method = HttpMethod.GET)
    @Before(StopwatchStartAdvice.class)
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showMan(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();
        if (!ManQueryService.TLDR_ENABLED) {
            context.sendRedirect("https://hacpai.com/man");

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("other/man.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        String cmd = request.getParameter(Common.CMD);
        if (StringUtils.isBlank(cmd)) {
            cmd = "man";
        }

        List<JSONObject> mans = manQueryService.getMansByCmdPrefix(cmd);
        if (mans.isEmpty()) {
            mans = manQueryService.getMansByCmdPrefix("man");
        }

        dataModel.put(Common.MANS, mans);
    }


    /**
     * Lists mans.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/man/cmd", method = HttpMethod.GET)
    public void listMans(final RequestContext context) {
        context.renderJSON().renderTrueResult();
        final HttpServletRequest request = context.getRequest();
        final String cmdPrefix = request.getParameter(Common.NAME);
        if (StringUtils.isBlank(cmdPrefix)) {
            return;
        }

        final List<JSONObject> mans = manQueryService.getMansByCmdPrefix(cmdPrefix);

        context.renderJSONValue(Common.MANS, mans);
    }
}
