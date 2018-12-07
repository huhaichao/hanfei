
package club.hanfei.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Article;
import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.processor.advice.AnonymousViewCheck;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.SearchQueryService;
import club.hanfei.service.UserQueryService;
import club.hanfei.util.Escapes;
import club.hanfei.util.Hanfei;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Search processor.
 * <ul>
 * <li>Searches keyword (/search), GET</li>
 * </ul>
 *
@version 1.1.4.1, Dec 8, 2017
 * @since 1.4.0
 */
@RequestProcessor
public class SearchProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SearchProcessor.class);

    /**
     * Search query service.
     */
    @Inject
    private SearchQueryService searchQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Searches.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/search", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void search(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("search-articles.ftl");

        if (!Hanfei.getBoolean("es.enabled") && !Hanfei.getBoolean("algolia.enabled")) {
            context.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final Map<String, Object> dataModel = renderer.getDataModel();
        String keyword = request.getParameter("key");
        if (StringUtils.isBlank(keyword)) {
            keyword = "";
        }
        dataModel.put(Common.KEY, Escapes.escapeHTML(keyword));

        final int pageNum = Paginator.getPage(request);
        int pageSize = Hanfei.getInt("indexArticlesCnt");
        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);
        }
        final List<JSONObject> articles = new ArrayList<>();
        int total = 0;

        if (Hanfei.getBoolean("es.enabled")) {
            final JSONObject result = searchQueryService.searchElasticsearch(Article.ARTICLE, keyword, pageNum, pageSize);
            if (null == result || 0 != result.optInt("status")) {
                context.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            final JSONObject hitsResult = result.optJSONObject("hits");
            final JSONArray hits = hitsResult.optJSONArray("hits");

            for (int i = 0; i < hits.length(); i++) {
                final JSONObject article = hits.optJSONObject(i).optJSONObject("_source");
                articles.add(article);
            }

            total = result.optInt("total");
        }

        if (Hanfei.getBoolean("algolia.enabled")) {
            final JSONObject result = searchQueryService.searchAlgolia(keyword, pageNum, pageSize);
            if (null == result) {
                context.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            final JSONArray hits = result.optJSONArray("hits");

            for (int i = 0; i < hits.length(); i++) {
                final JSONObject article = hits.optJSONObject(i);
                articles.add(article);
            }

            total = result.optInt("nbHits");
            if (total > 1000) {
                total = 1000; // Algolia limits the maximum number of search results to 1000
            }
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        articleQueryService.organizeArticles(avatarViewMode, articles);
        final Integer participantsCnt = Hanfei.getInt("latestArticleParticipantsCnt");
        articleQueryService.genParticipants(avatarViewMode, articles, participantsCnt);

        dataModel.put(Article.ARTICLES, articles);

        final int pageCount = (int) Math.ceil(total / (double) pageSize);
        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, Hanfei.getInt("defaultPaginationWindowSize"));
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        String searchEmptyLabel = langPropsService.get("searchEmptyLabel");
        searchEmptyLabel = searchEmptyLabel.replace("${key}", keyword);
        dataModel.put("searchEmptyLabel", searchEmptyLabel);
    }
}
