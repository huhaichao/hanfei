
package club.hanfei.processor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.DataModelService;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Locales;

/**
 * Error processor.
 *
@version 1.2.0.10, Jun 2, 2018
 * @since 0.2.0
 */
@RequestProcessor
public class ErrorProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ErrorProcessor.class);

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Handles the error.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/error/{statusCode}", method = {HttpMethod.GET, HttpMethod.POST})
    @Before(StopwatchStartAdvice.class)
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void handleErrorPage(final RequestContext context) {
        final String statusCode = context.pathVar("statusCode");
        if (StringUtils.equals("GET", context.method())) {
            final String requestURI = context.requestURI();
            final String templateName = statusCode + ".ftl";
            LOGGER.log(Level.TRACE, "Shows error page[requestURI={0}, templateName={1}]", requestURI, templateName);

            final HttpServletRequest request = context.getRequest();
            final HttpServletResponse response = context.getResponse();
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
            renderer.setTemplateName("error/" + templateName);
            context.setRenderer(renderer);

            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            dataModelService.fillSideHotArticles(dataModel);
            dataModelService.fillRandomArticles(dataModel);
            dataModelService.fillSideTags(dataModel);
        } else {
            context.renderJSON().renderMsg(statusCode);
        }
    }
}
