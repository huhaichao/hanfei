
package club.hanfei.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.model.Option;
import club.hanfei.processor.advice.AnonymousViewCheck;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.CommentQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.OptionQueryService;
import club.hanfei.service.UserQueryService;
import club.hanfei.service.VisitMgmtService;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Times;
import org.json.JSONObject;

/**
 * Data statistic processor.
 * <ul>
 * <li>Shows data statistic (/statistic), GET</li>
 * </ul>
 *
  @version 1.2.1.2, Dec 2, 2018
 * @since 1.4.0
 */
@RequestProcessor
public class StatisticProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StatisticProcessor.class);

    /**
     * Month days.
     */
    private final List<String> monthDays = new ArrayList<>();

    /**
     * User counts.
     */
    private final List<Integer> userCnts = new ArrayList<>();

    /**
     * Article counts.
     */
    private final List<Integer> articleCnts = new ArrayList<>();

    /**
     * Comment counts.
     */
    private final List<Integer> commentCnts = new ArrayList<>();

    /**
     * History months.
     */
    private final List<String> months = new ArrayList<>();

    /**
     * History user counts.
     */
    private final List<Integer> historyUserCnts = new ArrayList<>();

    /**
     * History article counts.
     */
    private final List<Integer> historyArticleCnts = new ArrayList<>();

    /**
     * History comment counts.
     */
    private final List<Integer> historyCommentCnts = new ArrayList<>();

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Visit management service.
     */
    @Inject
    private VisitMgmtService visitMgmtService;

    /**
     * Loads statistic data.
     */
    public void loadStatData() {
        try {
            final Date end = new Date();
            final Date dayStart = DateUtils.addDays(end, -30);

            monthDays.clear();
            userCnts.clear();
            articleCnts.clear();
            commentCnts.clear();
            months.clear();
            historyArticleCnts.clear();
            historyCommentCnts.clear();
            historyUserCnts.clear();

            for (int i = 0; i < 31; i++) {
                final Date day = DateUtils.addDays(dayStart, i);
                monthDays.add(DateFormatUtils.format(day, "yyyy-MM-dd"));

                final int userCnt = userQueryService.getUserCntInDay(day);
                userCnts.add(userCnt);

                final int articleCnt = articleQueryService.getArticleCntInDay(day);
                articleCnts.add(articleCnt);

                final int commentCnt = commentQueryService.getCommentCntInDay(day);
                commentCnts.add(commentCnt);
            }

            final JSONObject firstAdmin = userQueryService.getAdmins().get(0);
            final long monthStartTime = Times.getMonthStartTime(firstAdmin.optLong(Keys.OBJECT_ID));
            final Date monthStart = new Date(monthStartTime);

            int i = 1;
            while (true) {
                final Date month = DateUtils.addMonths(monthStart, i);

                if (month.after(end)) {
                    break;
                }

                i++;

                months.add(DateFormatUtils.format(month, "yyyy-MM"));

                final int userCnt = userQueryService.getUserCntInMonth(month);
                historyUserCnts.add(userCnt);

                final int articleCnt = articleQueryService.getArticleCntInMonth(month);
                historyArticleCnts.add(articleCnt);

                final int commentCnt = commentQueryService.getCommentCntInMonth(month);
                historyCommentCnts.add(commentCnt);
            }

            visitMgmtService.expire();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Loads stat data failed", e);
        }
    }

    /**
     * Shows data statistic.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/statistic", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showStatistic(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("statistic.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put("monthDays", monthDays);
        dataModel.put("userCnts", userCnts);
        dataModel.put("articleCnts", articleCnts);
        dataModel.put("commentCnts", commentCnts);

        dataModel.put("months", months);
        dataModel.put("historyUserCnts", historyUserCnts);
        dataModel.put("historyArticleCnts", historyArticleCnts);
        dataModel.put("historyCommentCnts", historyCommentCnts);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        dataModel.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());

        final JSONObject statistic = optionQueryService.getStatistic();
        dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);
    }
}
