
package club.hanfei.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Article;
import club.hanfei.model.Breezemoon;
import club.hanfei.model.Comment;
import club.hanfei.model.Common;
import club.hanfei.model.Follow;
import club.hanfei.model.Notification;
import club.hanfei.model.Role;
import club.hanfei.model.UserExt;
import club.hanfei.processor.advice.AnonymousViewCheck;
import club.hanfei.processor.advice.CSRFToken;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.UserBlockCheck;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.AvatarQueryService;
import club.hanfei.service.BreezemoonQueryService;
import club.hanfei.service.CommentQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.EmotionMgmtService;
import club.hanfei.service.EmotionQueryService;
import club.hanfei.service.FollowQueryService;
import club.hanfei.service.NotificationMgmtService;
import club.hanfei.service.OptionQueryService;
import club.hanfei.service.PointtransferMgmtService;
import club.hanfei.service.PointtransferQueryService;
import club.hanfei.service.RoleQueryService;
import club.hanfei.service.UserMgmtService;
import club.hanfei.service.UserQueryService;
import club.hanfei.util.Escapes;
import club.hanfei.util.Hanfei;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.json.JSONObject;

/**
 * User processor.
 * <ul>
 * <li>User articles (/member/{userName}), GET</li>
 * <li>User comments (/member/{userName}/comments), GET</li>
 * <li>User following users (/member/{userName}/following/users), GET</li>
 * <li>User following tags (/member/{userName}/following/tags), GET</li>
 * <li>User following articles (/member/{userName}/following/articles), GET</li>
 * <li>User followers (/member/{userName}/followers), GET</li>
 * <li>User points (/member/{userName}/points), GET</li>
 * <li>User breezemoons (/member/{userName}/breezemoons), GET</li>
 * <li>Lists usernames (/users/names), GET</li>
 * <li>Lists emotions (/users/emotions), GET</li>
 * </ul>
 *
 *
 * @version 1.27.0.7, Nov 28, 2018
 * @since 0.2.0
 */
@RequestProcessor
public class UserProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserProcessor.class);

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Article management service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Emotion query service.
     */
    @Inject
    private EmotionQueryService emotionQueryService;

    /**
     * Emotion management service.
     */
    @Inject
    private EmotionMgmtService emotionMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Breezemoon query service.
     */
    @Inject
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Shows user home breezemoons page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = {"/member/{userName}/breezemoons", "/member/{userName}/breezemoons/{breezemoonId}"}, method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeBreezemoons(final RequestContext context) {
        final String breezemoonId = context.pathVar("breezemoonId");
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/breezemoons.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeBreezemoonsCnt");
        final int windowSize = Hanfei.getInt("userHomeBreezemoonsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser;
        String currentUserId = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            currentUserId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(currentUserId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final JSONObject result = breezemoonQueryService.getBreezemoons(avatarViewMode, currentUserId, followingId, pageNum, pageSize, windowSize);
        List<JSONObject> bms = (List<JSONObject>) result.opt(Breezemoon.BREEZEMOONS);
        dataModel.put(Common.USER_HOME_BREEZEMOONS, bms);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int recordCount = pagination.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(recordCount / (double) pageSize);
        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, Breezemoon.BREEZEMOONS);

        if (StringUtils.isNotBlank(breezemoonId)) {
            dataModel.put(Common.IS_SINGLE_BREEZEMOON_URL, true);
            final JSONObject breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);
            bms = Arrays.asList(breezemoon);
            breezemoonQueryService.organizeBreezemoons(avatarViewMode, "admin", bms);
            dataModel.put(Common.USER_HOME_BREEZEMOONS, bms);
        } else {
            dataModel.put(Common.IS_SINGLE_BREEZEMOON_URL, false);
        }
    }

    /**
     * Shows user home anonymous comments page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/comments/anonymous", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeAnonymousComments(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        if (null == currentUser || (!currentUser.optString(Keys.OBJECT_ID).equals(user.optString(Keys.OBJECT_ID)))
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            context.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeCmtsCnt");
        final int windowSize = Hanfei.getInt("userHomeCmtsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final List<JSONObject> userComments = commentQueryService.getUserComments(
                avatarViewMode, user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_ANONYMOUS,
                pageNum, pageSize, currentUser);
        dataModel.put(Common.USER_HOME_COMMENTS, userComments);

        int recordCount = 0;
        int pageCount = 0;
        if (!userComments.isEmpty()) {
            final JSONObject first = userComments.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, "commentsAnonymous");
    }

    /**
     * Shows user home anonymous articles page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/articles/anonymous", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showAnonymousArticles(final RequestContext context) {
        final String userName = context.pathVar("userName");
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/home.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        if (null == currentUser || (!currentUser.optString(Keys.OBJECT_ID).equals(user.optString(Keys.OBJECT_ID)))
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            context.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final int pageNum = Paginator.getPage(request);
        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        if (isLoggedIn) {
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final int pageSize = Hanfei.getInt("userHomeArticlesCnt");
        final int windowSize = Hanfei.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_ANONYMOUS, pageNum, pageSize);
        dataModel.put(Common.USER_HOME_ARTICLES, userArticles);

        int recordCount = 0;
        int pageCount = 0;
        if (!userArticles.isEmpty()) {
            final JSONObject first = userArticles.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));

        dataModel.put(Common.TYPE, "articlesAnonymous");
    }

    /**
     * Shows user home page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHome(final RequestContext context) {
        final String userName = context.pathVar("userName");
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final int pageNum = Paginator.getPage(request);
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        renderer.setTemplateName("home/home.ftl");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final int pageSize = Hanfei.getInt("userHomeArticlesCnt");
        final int windowSize = Hanfei.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_PUBLIC, pageNum, pageSize);
        dataModel.put(Common.USER_HOME_ARTICLES, userArticles);

        int recordCount = 0;
        int pageCount = 0;
        if (!userArticles.isEmpty()) {
            final JSONObject first = userArticles.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            dataModel.put(Common.IS_MY_ARTICLE, false);
        } else {
            dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));
        }

        dataModel.put(Common.TYPE, "home");
    }

    /**
     * Shows user home comments page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/comments", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeComments(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeCmtsCnt");
        final int windowSize = Hanfei.getInt("userHomeCmtsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final List<JSONObject> userComments = commentQueryService.getUserComments(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_PUBLIC, pageNum, pageSize, currentUser);
        dataModel.put(Common.USER_HOME_COMMENTS, userComments);

        int recordCount = 0;
        int pageCount = 0;
        if (!userComments.isEmpty()) {
            final JSONObject first = userComments.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, "comments");
    }

    /**
     * Shows user home following users page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/following/users", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowingUsers(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/following-users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeFollowingUsersCnt");
        final int windowSize = Hanfei.getInt("userHomeFollowingUsersWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingUsersResult = followQueryService.getFollowingUsers(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingUsers = (List<JSONObject>) followingUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_USERS, followingUsers);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingUser : followingUsers) {
                final String homeUserFollowingUserId = followingUser.optString(Keys.OBJECT_ID);

                followingUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingUserId, Follow.FOLLOWING_TYPE_C_USER));
            }
        }

        final int followingUserCnt = followingUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) followingUserCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingUserCnt);

        dataModel.put(Common.TYPE, "followingUsers");
    }

    /**
     * Shows user home following tags page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/following/tags", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowingTags(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/following-tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeFollowingTagsCnt");
        final int windowSize = Hanfei.getInt("userHomeFollowingTagsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingTagsResult = followQueryService.getFollowingTags(followingId, pageNum, pageSize);
        final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_TAGS, followingTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingTag : followingTags) {
                final String homeUserFollowingTagId = followingTag.optString(Keys.OBJECT_ID);

                followingTag.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingTagId, Follow.FOLLOWING_TYPE_C_TAG));
            }
        }

        final int followingTagCnt = followingTagsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingTagCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingTagCnt);

        dataModel.put(Common.TYPE, "followingTags");
    }

    /**
     * Shows user home following articles page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/following/articles", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowingArticles(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/following-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeFollowingArticlesCnt");
        final int windowSize = Hanfei.getInt("userHomeFollowingArticlesWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingArticlesResult = followQueryService.getFollowingArticles(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE));
            }
        }

        final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingArticleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingArticleCnt);

        dataModel.put(Common.TYPE, "followingArticles");
    }

    /**
     * Shows user home watching articles page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/watching/articles", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeWatchingArticles(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/watching-articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeFollowingArticlesCnt");
        final int windowSize = Hanfei.getInt("userHomeFollowingArticlesWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingArticlesResult = followQueryService.getWatchingArticles(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH));
            }
        }

        final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingArticleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingArticleCnt);

        dataModel.put(Common.TYPE, "watchingArticles");
    }

    /**
     * Shows user home follower users page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/followers", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomeFollowers(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/followers.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomeFollowersCnt");
        final int windowSize = Hanfei.getInt("userHomeFollowersWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject followerUsersResult = followQueryService.getFollowerUsers(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followerUsers = (List) followerUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWER_USERS, followerUsers);

        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followerUser : followerUsers) {
                final String homeUserFollowerUserId = followerUser.optString(Keys.OBJECT_ID);

                followerUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowerUserId, Follow.FOLLOWING_TYPE_C_USER));
            }

            if (followerId.equals(followingId)) {
                notificationMgmtService.makeRead(followingId, Notification.DATA_TYPE_C_NEW_FOLLOWER);
            }
        }

        final int followerUserCnt = followerUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) followerUserCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followerUserCnt);

        dataModel.put(Common.TYPE, "followers");

        notificationMgmtService.makeRead(followingId, Notification.DATA_TYPE_C_NEW_FOLLOWER);
    }

    /**
     * Shows user home points page.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/member/{userName}/points", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showHomePoints(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/points.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Hanfei.getInt("userHomePointsCnt");
        final int windowSize = Hanfei.getInt("userHomePointsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final JSONObject userPointsResult
                = pointtransferQueryService.getUserPoints(user.optString(Keys.OBJECT_ID), pageNum, pageSize);
        final List<JSONObject> userPoints = CollectionUtils.jsonArrayToList(userPointsResult.optJSONArray(Keys.RESULTS));
        dataModel.put(Common.USER_HOME_POINTS, userPoints);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, user.optString(Keys.OBJECT_ID), Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final int pointsCnt = userPointsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) pointsCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModel.put(Common.TYPE, "points");
    }

    /**
     * Lists usernames.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/users/names", method = HttpMethod.GET)
    public void listNames(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();

        context.renderJSON().renderTrueResult();

        final String namePrefix = request.getParameter("name");
        if (StringUtils.isBlank(namePrefix)) {
            final List<JSONObject> admins = userQueryService.getAdmins();
            final List<JSONObject> userNames = new ArrayList<>();
            for (final JSONObject admin : admins) {
                final JSONObject userName = new JSONObject();
                userName.put(User.USER_NAME, admin.optString(User.USER_NAME));
                final String avatar = avatarQueryService.getAvatarURLByUser(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, admin, "20");
                userName.put(UserExt.USER_AVATAR_URL, avatar);

                userNames.add(userName);
            }

            context.renderJSONValue(Common.USER_NAMES, userNames);

            return;
        }

        final List<JSONObject> userNames = userQueryService.getUserNamesByPrefix(namePrefix);
        context.renderJSONValue(Common.USER_NAMES, userNames);
    }

    /**
     * Lists emotions.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/users/emotions", method = HttpMethod.GET)
    public void getEmotions(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            context.renderJSONValue("emotions", "");

            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String emotions = emotionQueryService.getEmojis(userId);

        context.renderJSONValue("emotions", emotions);
    }

    /**
     * Fills home user.
     *
     * @param dataModel the specified data model
     * @param user      the specified user
     */
    static void fillHomeUser(final Map<String, Object> dataModel, final JSONObject user, final RoleQueryService roleQueryService) {
        Escapes.escapeHTML(user);
        dataModel.put(User.USER, user);

        final String roleId = user.optString(User.USER_ROLE);
        final JSONObject role = roleQueryService.getRole(roleId);
        user.put(Role.ROLE_NAME, role.optString(Role.ROLE_NAME));
        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.optLong(Keys.OBJECT_ID)));
    }
}
