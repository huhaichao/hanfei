
package club.hanfei.service;

import club.hanfei.model.Article;
import club.hanfei.model.Follow;
import club.hanfei.model.Tag;
import club.hanfei.repository.ArticleRepository;
import club.hanfei.repository.FollowRepository;
import club.hanfei.repository.TagRepository;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.annotation.Service;
import org.json.JSONObject;

/**
 * Follow management service.
 *
@version 1.3.1.2, Jan 18, 2017
 * @since 0.2.5
 */
@Service
public class FollowMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowMgmtService.class);

    /**
     * Follow repository.
     */
    @Inject
    private FollowRepository followRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * The specified follower follows the specified following tag.
     *
     * @param followerId     the specified follower id
     * @param followingTagId the specified following tag id
     */
    @Transactional
    public void followTag(final String followerId, final String followingTagId) {
        try {
            follow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower follows the specified following user.
     *
     * @param followerId      the specified follower id
     * @param followingUserId the specified following user id
     */
    @Transactional
    public void followUser(final String followerId, final String followingUserId) {
        try {
            follow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower follows the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void followArticle(final String followerId, final String followingArticleId) {
        try {
            follow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower watches the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void watchArticle(final String followerId, final String followingArticleId) {
        try {
            follow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] watches an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower unfollows the specified following tag.
     *
     * @param followerId     the specified follower id
     * @param followingTagId the specified following tag id
     */
    @Transactional
    public void unfollowTag(final String followerId, final String followingTagId) {
        try {
            unfollow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower unfollows the specified following user.
     *
     * @param followerId      the specified follower id
     * @param followingUserId the specified following user id
     */
    @Transactional
    public void unfollowUser(final String followerId, final String followingUserId) {
        try {
            unfollow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows a user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower unfollows the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void unfollowArticle(final String followerId, final String followingArticleId) {
        try {
            unfollow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }


    /**
     * The specified follower unwatches the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void unwatchArticle(final String followerId, final String followingArticleId) {
        try {
            unfollow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unwatches an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower follows the specified following entity with the specified following type.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    private synchronized void follow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        if (followRepository.exists(followerId, followingId, followingType)) {
            return;
        }

        if (Follow.FOLLOWING_TYPE_C_TAG == followingType) {
            final JSONObject tag = tagRepository.get(followingId);
            if (null == tag) {
                LOGGER.log(Level.ERROR, "Not found tag [id={0}] to follow", followingId);

                return;
            }

            tag.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT) + 1);
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagRepository.update(followingId, tag);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to follow", followingId);

                return;
            }

            article.put(Article.ARTICLE_COLLECT_CNT, article.optInt(Article.ARTICLE_COLLECT_CNT) + 1);

            articleRepository.update(followingId, article);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to watch", followingId);

                return;
            }

            article.put(Article.ARTICLE_WATCH_CNT, article.optInt(Article.ARTICLE_WATCH_CNT) + 1);

            articleRepository.update(followingId, article);
        }

        final JSONObject follow = new JSONObject();
        follow.put(Follow.FOLLOWER_ID, followerId);
        follow.put(Follow.FOLLOWING_ID, followingId);
        follow.put(Follow.FOLLOWING_TYPE, followingType);

        followRepository.add(follow);
    }

    /**
     * Removes a follow relationship.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    public synchronized void unfollow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        followRepository.removeByFollowerIdAndFollowingId(followerId, followingId, followingType);

        if (Follow.FOLLOWING_TYPE_C_TAG == followingType) {
            final JSONObject tag = tagRepository.get(followingId);
            if (null == tag) {
                LOGGER.log(Level.ERROR, "Not found tag [id={0}] to unfollow", followingId);

                return;
            }

            tag.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT) - 1);
            if (tag.optInt(Tag.TAG_FOLLOWER_CNT) < 0) {
                tag.put(Tag.TAG_FOLLOWER_CNT, 0);
            }

            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagRepository.update(followingId, tag);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to unfollow", followingId);

                return;
            }

            article.put(Article.ARTICLE_COLLECT_CNT, article.optInt(Article.ARTICLE_COLLECT_CNT) - 1);
            if (article.optInt(Article.ARTICLE_COLLECT_CNT) < 0) {
                article.put(Article.ARTICLE_COLLECT_CNT, 0);
            }

            articleRepository.update(followingId, article);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to unwatch", followingId);

                return;
            }

            article.put(Article.ARTICLE_WATCH_CNT, article.optInt(Article.ARTICLE_WATCH_CNT) - 1);
            if (article.optInt(Article.ARTICLE_WATCH_CNT) < 0) {
                article.put(Article.ARTICLE_WATCH_CNT, 0);
            }

            articleRepository.update(followingId, article);
        }
    }
}
