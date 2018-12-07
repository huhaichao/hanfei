
package club.hanfei.processor;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Article;
import club.hanfei.model.Common;
import club.hanfei.model.Follow;
import club.hanfei.model.Notification;
import club.hanfei.processor.advice.LoginCheck;
import club.hanfei.processor.advice.PermissionCheck;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.FollowMgmtService;
import club.hanfei.service.NotificationMgmtService;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.json.JSONObject;

/**
 * Follow processor.
 * <ul>
 * <li>Follows a user (/follow/user), POST</li>
 * <li>Unfollows a user (/follow/user), POST</li>
 * <li>Follows a tag (/follow/tag), POST</li>
 * <li>Unfollows a tag (/follow/tag), POST</li>
 * <li>Follows an article (/follow/article), POST</li>
 * <li>Unfollows an article (/follow/article), POST</li>
 * <li>Watches an article (/follow/article-watch), POST</li>
 * <li>Unwatches an article (/follow/article-watch), POST</li>
 * </ul>
 *
@version 1.3.0.5, Jul 22, 2018
 * @since 0.2.5
 */
@RequestProcessor
public class FollowProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowProcessor.class);
    /**
     * Holds follows.
     */
    private static final Set<String> FOLLOWS = new HashSet<>();
    /**
     * Follow management service.
     */
    @Inject
    private FollowMgmtService followMgmtService;
    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;
    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Follows a user.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/follow/user", method = HttpMethod.POST)
    @Before(LoginCheck.class)
    public void followUser(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followUser(followerUserId, followingUserId);

        if (!FOLLOWS.contains(followingUserId + followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, followingUserId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followerUserId);

            notificationMgmtService.addNewFollowerNotification(notification);
        }

        FOLLOWS.add(followingUserId + followerUserId);

        context.renderTrueResult();
    }

    /**
     * Unfollows a user.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/unfollow/user", method = HttpMethod.POST)
    @Before(LoginCheck.class)
    public void unfollowUser(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowUser(followerUserId, followingUserId);

        context.renderTrueResult();
    }

    /**
     * Follows a tag.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/follow/tag", method = HttpMethod.POST)
    @Before(LoginCheck.class)
    public void followTag(final RequestContext context) {
        context.renderJSON();
        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followTag(followerUserId, followingTagId);

        context.renderTrueResult();
    }

    /**
     * Unfollows a tag.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/unfollow/tag", method = HttpMethod.POST)
    @Before(LoginCheck.class)
    public void unfollowTag(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowTag(followerUserId, followingTagId);

        context.renderTrueResult();
    }

    /**
     * Follows an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/follow/article", method = HttpMethod.POST)
    @Before({LoginCheck.class, PermissionCheck.class})
    public void followArticle(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followArticle(followerUserId, followingArticleId);

        final JSONObject article = articleQueryService.getArticle(followingArticleId);
        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        if (!FOLLOWS.contains(articleAuthorId + followingArticleId + "-" + followerUserId) &&
                !articleAuthorId.equals(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followingArticleId + "-" + followerUserId);

            notificationMgmtService.addArticleNewFollowerNotification(notification);
        }

        FOLLOWS.add(articleAuthorId + followingArticleId + "-" + followerUserId);

        context.renderTrueResult();
    }

    /**
     * Unfollows an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/unfollow/article", method = HttpMethod.POST)
    @Before(LoginCheck.class)
    public void unfollowArticle(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }

    /**
     * Watches an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/follow/article-watch", method = HttpMethod.POST)
    @Before({LoginCheck.class, PermissionCheck.class})
    public void watchArticle(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.watchArticle(followerUserId, followingArticleId);

        final JSONObject article = articleQueryService.getArticle(followingArticleId);
        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        if (!FOLLOWS.contains(articleAuthorId + followingArticleId + "-" + followerUserId) &&
                !articleAuthorId.equals(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followingArticleId + "-" + followerUserId);

            notificationMgmtService.addArticleNewWatcherNotification(notification);
        }

        FOLLOWS.add(articleAuthorId + followingArticleId + "-" + followerUserId);

        context.renderTrueResult();
    }

    /**
     * Unwatches an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/unfollow/article-watch", method = HttpMethod.POST)
    @Before(LoginCheck.class)
    public void unwatchArticle(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unwatchArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }
}
