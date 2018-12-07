
package club.hanfei.processor;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Breezemoon;
import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.processor.advice.AnonymousViewCheck;
import club.hanfei.processor.advice.CSRFCheck;
import club.hanfei.processor.advice.CSRFToken;
import club.hanfei.processor.advice.LoginCheck;
import club.hanfei.processor.advice.PermissionCheck;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.BreezemoonMgmtService;
import club.hanfei.service.BreezemoonQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.OptionQueryService;
import club.hanfei.util.Geos;
import club.hanfei.util.Headers;
import club.hanfei.util.StatusCodes;
import club.hanfei.util.Hanfei;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.json.JSONObject;

/**
 * Breezemoon processor. https://github.com/b3log/symphony/issues/507
<ul>
 * <li>Shows watch breezemoons (/watch/breezemoons), GET</li>
 * <li>Adds a breezemoon (/breezemoon), POST</li>
 * <li>Updates a breezemoon (/breezemoon/{id}), PUT</li>
 * <li>Removes a breezemoon (/breezemoon/{id}), DELETE</li>
 * <li>Shows a breezemoon (/breezemoon/{id}), GET</li>
 * </ul>
 *
@version 1.0.1.1, Sep 4, 2018
 * @since 2.8.0
 */
@RequestProcessor
public class BreezemoonProcessor {

    /**
     * Breezemoon query service.
     */
    @Inject
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Breezemoon management service.
     */
    @Inject
    private BreezemoonMgmtService breezemoonMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Optiona query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Shows breezemoon page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/watch/breezemoons", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showWatchBreezemoon(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("breezemoon.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        int pageSize = Hanfei.getInt("indexArticlesCnt");
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        String currentUserId = null;
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                context.sendRedirect(Latkes.getServePath() + "/guide");

                return;
            }

            currentUserId = user.optString(Keys.OBJECT_ID);
        }

        final int windowSize = Hanfei.getInt("latestArticlesWindowSize");
        final JSONObject result = breezemoonQueryService.getFollowingUserBreezemoons(avatarViewMode, currentUserId, pageNum, pageSize, windowSize);
        final List<JSONObject> bms = (List<JSONObject>) result.opt(Breezemoon.BREEZEMOONS);
        dataModel.put(Common.WATCHING_BREEZEMOONS, bms);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.SELECTED, Common.WATCH);
        dataModel.put(Common.CURRENT, StringUtils.substringAfter(request.getRequestURI(), "/watch"));
    }

    /**
     * Adds a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/breezemoon", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void addBreezemoon(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        if (isInvalid(context, requestJSONObject)) {
            return;
        }

        final JSONObject breezemoon = new JSONObject();
        final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String authorId = user.optString(Keys.OBJECT_ID);
        breezemoon.put(Breezemoon.BREEZEMOON_AUTHOR_ID, authorId);
        final String ip = Requests.getRemoteAddr(request);
        breezemoon.put(Breezemoon.BREEZEMOON_IP, ip);
        final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
        breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);
        final JSONObject address = Geos.getAddress(ip);
        if (null != address) {
            breezemoon.put(Breezemoon.BREEZEMOON_CITY, address.optString(Common.CITY));
        }

        try {
            breezemoonMgmtService.addBreezemoon(breezemoon);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Updates a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/breezemoon/{id}", method = HttpMethod.PUT)
    @Before({StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void updateBreezemoon(final RequestContext context) {
        final String id = context.pathVar("id");
        context.renderJSON();
        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        if (isInvalid(context, requestJSONObject)) {
            return;
        }

        try {
            final JSONObject old = breezemoonQueryService.getBreezemoon(id);
            if (null == old) {
                throw new ServiceException(langPropsService.get("queryFailedLabel"));
            }

            final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
            if (!old.optString(Breezemoon.BREEZEMOON_AUTHOR_ID).equals(user.optString(Keys.OBJECT_ID))) {
                throw new ServiceException(langPropsService.get("sc403Label"));
            }

            final JSONObject breezemoon = new JSONObject();
            breezemoon.put(Keys.OBJECT_ID, id);
            final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
            breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
            final String ip = Requests.getRemoteAddr(request);
            breezemoon.put(Breezemoon.BREEZEMOON_IP, ip);
            final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
            breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);

            breezemoonMgmtService.updateBreezemoon(breezemoon);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Removes a breezemoon.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/breezemoon/{id}", method = HttpMethod.DELETE)
    @Before({StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(StopwatchEndAdvice.class)
    public void removeBreezemoon(final RequestContext context) {
        final String id = context.pathVar("id");
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        try {
            final JSONObject breezemoon = breezemoonQueryService.getBreezemoon(id);
            if (null == breezemoon) {
                throw new ServiceException(langPropsService.get("queryFailedLabel"));
            }

            final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
            if (!breezemoon.optString(Breezemoon.BREEZEMOON_AUTHOR_ID).equals(user.optString(Keys.OBJECT_ID))) {
                throw new ServiceException(langPropsService.get("sc403Label"));
            }

            breezemoonMgmtService.removeBreezemoon(id);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    private boolean isInvalid(final RequestContext context, final JSONObject requestJSONObject) {
        String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoonContent = StringUtils.trim(breezemoonContent);
        final long length = StringUtils.length(breezemoonContent);
        if (1 > length || 512 < length) {
            context.renderMsg(langPropsService.get("breezemoonLengthLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

            return true;
        }

        if (optionQueryService.containReservedWord(breezemoonContent)) {
            context.renderMsg(langPropsService.get("contentContainReservedWordLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

            return true;
        }

        requestJSONObject.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);

        return false;
    }
}
