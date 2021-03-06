
package club.hanfei.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import club.hanfei.model.Article;
import club.hanfei.model.Notification;
import club.hanfei.model.Permission;
import club.hanfei.model.UserExt;
import club.hanfei.repository.NotificationRepository;
import club.hanfei.service.FollowQueryService;
import club.hanfei.service.NotificationMgmtService;
import club.hanfei.service.RoleQueryService;
import club.hanfei.service.UserQueryService;
import org.b3log.latke.Keys;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.json.JSONObject;

/**
 * Sends article update related notifications.
 *
@version 1.0.0.4, Nov 17, 2018
 * @since 2.0.0
 */
@Singleton
public class ArticleUpdateNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleUpdateNotifier.class);

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    @Override
    public void action(final Event<JSONObject> event) {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event [type={0}, data={1}]", event.getType(), data);

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final String articleId = originalArticle.optString(Keys.OBJECT_ID);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
            atUserNames.remove(articleAuthorName); // Do not notify the author itself

            final Set<String> requisiteAtUserPermissions = new HashSet<>();
            requisiteAtUserPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_USER);
            final boolean hasAtUserPerm = roleQueryService.userHasPermissions(articleAuthorId, requisiteAtUserPermissions);
            final Set<String> atedUserIds = new HashSet<>();
            if (hasAtUserPerm) {
                // 'At' Notification
                for (final String userName : atUserNames) {
                    final JSONObject user = userQueryService.getUserByName(userName);
                    final JSONObject requestJSONObject = new JSONObject();
                    final String atedUserId = user.optString(Keys.OBJECT_ID);
                    if (!notificationRepository.hasSentByDataIdAndType(atedUserId, articleId, Notification.DATA_TYPE_C_AT)) {
                        requestJSONObject.put(Notification.NOTIFICATION_USER_ID, atedUserId);
                        requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);
                        notificationMgmtService.addAtNotification(requestJSONObject);
                    }

                    atedUserIds.add(atedUserId);
                }
            }

            // 'following - article update' Notification
            final JSONObject followerUsersResult =
                    followQueryService.getArticleWatchers(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL,
                            articleId, 1, Integer.MAX_VALUE);

            final List<JSONObject> watcherUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
            for (final JSONObject watcherUser : watcherUsers) {
                final String watcherName = watcherUser.optString(User.USER_NAME);
                if ((isDiscussion && !atUserNames.contains(watcherName)) || articleAuthorName.equals(watcherName)) {
                    continue;
                }

                final JSONObject requestJSONObject = new JSONObject();
                final String watcherUserId = watcherUser.optString(Keys.OBJECT_ID);

                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, watcherUserId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);

                notificationMgmtService.addFollowingArticleUpdateNotification(requestJSONObject);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article update notification failed", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#UPDATE_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.UPDATE_ARTICLE;
    }
}
