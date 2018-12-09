
package club.hanfei.processor;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Article;
import club.hanfei.model.Common;
import club.hanfei.model.Domain;
import club.hanfei.model.Option;
import club.hanfei.model.Tag;
import club.hanfei.model.UserExt;
import club.hanfei.processor.advice.AnonymousViewCheck;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.DomainQueryService;
import club.hanfei.service.OptionQueryService;
import club.hanfei.service.UserQueryService;
import club.hanfei.util.Hanfei;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.json.JSONObject;

@RequestProcessor
public class DomainProcessor {

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

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
     * Shows domain articles.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/domain/{domainURI}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showDomainArticles(final RequestContext context) {
        final String domainURI = context.pathVar("domainURI");
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("domain-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        int pageSize = Hanfei.getInt("indexArticlesCnt");

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                context.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }
        }

        final JSONObject domain = domainQueryService.getByURI(domainURI);
        if (null == domain) {
            context.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final List<JSONObject> tags = domainQueryService.getTags(domain.optString(Keys.OBJECT_ID));
        domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);

        dataModel.put(Domain.DOMAIN, domain);
        dataModel.put(Common.SELECTED, domain.optString(Domain.DOMAIN_URI));

        final String domainId = domain.optString(Keys.OBJECT_ID);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = articleQueryService.getDomainArticles(avatarViewMode, domainId, pageNum, pageSize);
        final List<JSONObject> latestArticles = (List<JSONObject>) result.opt(Article.ARTICLES);
        dataModel.put(Common.LATEST_ARTICLES, latestArticles);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

        final List<Integer> pageNums = (List<Integer>) pagination.opt(Pagination.PAGINATION_PAGE_NUMS);
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
    }

    /**
     * Shows domains.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/domains", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showDomains(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("domains.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject statistic = optionQueryService.getStatistic();
        final int tagCnt = statistic.optInt(Option.ID_C_STATISTIC_TAG_COUNT);
        dataModel.put(Tag.TAG_T_COUNT, tagCnt);

        final int domainCnt = statistic.optInt(Option.ID_C_STATISTIC_DOMAIN_COUNT);
        dataModel.put(Domain.DOMAIN_T_COUNT, domainCnt);

        final List<JSONObject> domains = domainQueryService.getAllDomains();
        dataModel.put(Common.ALL_DOMAINS, domains);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }
}
