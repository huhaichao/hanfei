
package club.hanfei.processor;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Common;
import club.hanfei.model.Report;
import club.hanfei.processor.advice.LoginCheck;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.ReportMgmtService;
import club.hanfei.util.StatusCodes;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.json.JSONObject;

/**
 * Report processor.
 * <ul>
 * <li>Reports content or users (/report), POST</li>
 * </ul>
 *
@version 1.0.0.0, Jun 26, 2018
 * @since 3.1.0
 */
@RequestProcessor
public class ReportProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ReportProcessor.class);

    /**
     * Report management service.
     */
    @Inject
    private ReportMgmtService reportMgmtService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Reports content or users.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/report", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, LoginCheck.class})
    @After(StopwatchEndAdvice.class)
    public void report(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String dataId = requestJSONObject.optString(Report.REPORT_DATA_ID);
        final int dataType = requestJSONObject.optInt(Report.REPORT_DATA_TYPE);
        final int type = requestJSONObject.optInt(Report.REPORT_TYPE);
        final String memo = StringUtils.trim(requestJSONObject.optString(Report.REPORT_MEMO));

        final JSONObject report = new JSONObject();
        report.put(Report.REPORT_USER_ID, userId);
        report.put(Report.REPORT_DATA_ID, dataId);
        report.put(Report.REPORT_DATA_TYPE, dataType);
        report.put(Report.REPORT_TYPE, type);
        report.put(Report.REPORT_MEMO, memo);

        try {
            reportMgmtService.addReport(report);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Adds a report failed", e);

            context.renderMsg(langPropsService.get("systemErrLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

}
