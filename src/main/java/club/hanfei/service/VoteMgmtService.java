
package club.hanfei.service;

import java.util.List;

import club.hanfei.model.Article;
import club.hanfei.model.Comment;
import club.hanfei.model.Liveness;
import club.hanfei.model.Vote;
import club.hanfei.repository.ArticleRepository;
import club.hanfei.repository.CommentRepository;
import club.hanfei.repository.TagArticleRepository;
import club.hanfei.repository.VoteRepository;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.annotation.Service;
import org.json.JSONObject;

/**
 * Vote management service.
 *
@version 1.1.1.0, Jul 31, 2016
 * @since 1.3.0
 */
@Service
public class VoteMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VoteMgmtService.class);

    /**
     * Vote repository.
     */
    @Inject
    private VoteRepository voteRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Tag-Article repository.
     */
    @Inject
    private TagArticleRepository tagArticleRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Liveness management service.
     */
    @Inject
    private LivenessMgmtService livenessMgmtService;

    /**
     * Gets Reddit article score.
     *
     * @param ups   the specified vote up count
     * @param downs the specified vote down count
     * @param t     time (epoch seconds)
     * @return reddit score
     */
    private static double redditArticleScore(final int ups, final int downs, final long t) {
        final int x = ups - downs;
        final double z = Math.max(Math.abs(x), 1);
        int y = 0;
        if (x > 0) {
            y = 1;
        } else if (x < 0) {
            y = -1;
        }

        return Math.log10(z) + y * (t - 1353745196) / 45000;
    }

    private static double redditCommentScore(final int ups, final int downs) {
        final int n = ups + downs;
        if (0 == n) {
            return 0;
        }

        final double z = 1.281551565545; // 1.0: 85%, 1.6: 95%, 1.281551565545: 80%
        final double p = (double) ups / n;

        return (p + z * z / (2 * n) - z * Math.sqrt((p * (1 - p) + z * z / (4 * n)) / n)) / (1 + z * z / n);
    }

    /**
     * Cancels the vote.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data id
     * @param dataType the specified data type
     */
    @Transactional
    public void voteCancel(final String userId, final String dataId, final int dataType) {
        try {
            final int oldType = voteRepository.removeIfExists(userId, dataId, dataType);

            if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
                final JSONObject article = articleRepository.get(dataId);
                if (null == article) {
                    LOGGER.log(Level.ERROR, "Not found article [id={0}] to vote cancel", dataId);

                    return;
                }

                if (Vote.TYPE_C_UP == oldType) {
                    article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) - 1);
                } else if (Vote.TYPE_C_DOWN == oldType) {
                    article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) - 1);
                }

                final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
                final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
                final long t = article.optLong(Keys.OBJECT_ID) / 1000;

                final double redditScore = redditArticleScore(ups, downs, t);
                article.put(Article.REDDIT_SCORE, redditScore);

                updateTagArticleScore(article);

                articleRepository.update(dataId, article);
            } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
                final JSONObject comment = commentRepository.get(dataId);
                if (null == comment) {
                    LOGGER.log(Level.ERROR, "Not found comment [id={0}] to vote cancel", dataId);

                    return;
                }

                if (Vote.TYPE_C_UP == oldType) {
                    comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) - 1);
                } else if (Vote.TYPE_C_DOWN == oldType) {
                    comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) - 1);
                }

                final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
                final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);

                final double redditScore = redditCommentScore(ups, downs);
                comment.put(Comment.COMMENT_SCORE, redditScore);

                commentRepository.update(dataId, comment);
            } else {
                LOGGER.warn("Wrong data type [" + dataType + "]");
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, e.getMessage());
        }
    }

    /**
     * The specified user vote up the specified article/comment.
     *
     * @param userId   the specified user id
     * @param dataId   the specified article/comment id
     * @param dataType the specified data type
     */
    @Transactional
    public void voteUp(final String userId, final String dataId, final int dataType) {
        try {
            up(userId, dataId, dataType);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + userId + "] vote up an [" + dataType + "][id=" + dataId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_VOTE);
    }

    /**
     * The specified user vote down the specified article、comment.
     *
     * @param userId the specified user id
     * @param dataId the specified article id
     */
    @Transactional
    public void voteDown(final String userId, final String dataId, final int dataType) {
        try {
            down(userId, dataId, dataType);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + userId + "] vote down an [" + dataType + "][id=" + dataId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }

        livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_VOTE);
    }

    /**
     * The specified user vote up the specified data entity with the specified data type.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @throws RepositoryException repository exception
     */
    private void up(final String userId, final String dataId, final int dataType) throws RepositoryException {
        final int oldType = voteRepository.removeIfExists(userId, dataId, dataType);

        if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
            final JSONObject article = articleRepository.get(dataId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) + 1);
            } else if (Vote.TYPE_C_DOWN == oldType) {
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) - 1);
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) + 1);
            }

            final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
            final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
            final long t = article.optLong(Keys.OBJECT_ID) / 1000;

            final double redditScore = redditArticleScore(ups, downs, t);
            article.put(Article.REDDIT_SCORE, redditScore);

            updateTagArticleScore(article);

            articleRepository.update(dataId, article);
        } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
            final JSONObject comment = commentRepository.get(dataId);
            if (null == comment) {
                LOGGER.log(Level.ERROR, "Not found comment [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) + 1);
            } else if (Vote.TYPE_C_DOWN == oldType) {
                comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) - 1);
                comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) + 1);
            }

            final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
            final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);

            final double redditScore = redditCommentScore(ups, downs);
            comment.put(Comment.COMMENT_SCORE, redditScore);

            commentRepository.update(dataId, comment);
        } else {
            LOGGER.warn("Wrong data type [" + dataType + "]");
        }

        final JSONObject vote = new JSONObject();
        vote.put(Vote.USER_ID, userId);
        vote.put(Vote.DATA_ID, dataId);
        vote.put(Vote.TYPE, Vote.TYPE_C_UP);
        vote.put(Vote.DATA_TYPE, dataType);

        voteRepository.add(vote);
    }

    /**
     * The specified user vote down the specified data entity with the specified data type.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @throws RepositoryException repository exception
     */
    private void down(final String userId, final String dataId, final int dataType) throws RepositoryException {
        final int oldType = voteRepository.removeIfExists(userId, dataId, dataType);

        if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
            final JSONObject article = articleRepository.get(dataId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to vote down", dataId);

                return;
            }

            if (-1 == oldType) {
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) + 1);
            } else if (Vote.TYPE_C_UP == oldType) {
                article.put(Article.ARTICLE_GOOD_CNT, article.optInt(Article.ARTICLE_GOOD_CNT) - 1);
                article.put(Article.ARTICLE_BAD_CNT, article.optInt(Article.ARTICLE_BAD_CNT) + 1);
            }

            final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
            final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
            final long t = article.optLong(Keys.OBJECT_ID) / 1000;

            final double redditScore = redditArticleScore(ups, downs, t);
            article.put(Article.REDDIT_SCORE, redditScore);

            updateTagArticleScore(article);

            articleRepository.update(dataId, article);
        } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
            final JSONObject comment = commentRepository.get(dataId);
            if (null == comment) {
                LOGGER.log(Level.ERROR, "Not found comment [id={0}] to vote up", dataId);

                return;
            }

            if (-1 == oldType) {
                comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) + 1);
            } else if (Vote.TYPE_C_UP == oldType) {
                comment.put(Comment.COMMENT_GOOD_CNT, comment.optInt(Comment.COMMENT_GOOD_CNT) - 1);
                comment.put(Comment.COMMENT_BAD_CNT, comment.optInt(Comment.COMMENT_BAD_CNT) + 1);
            }

            final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
            final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);

            final double redditScore = redditCommentScore(ups, downs);
            comment.put(Comment.COMMENT_SCORE, redditScore);

            commentRepository.update(dataId, comment);
        } else {
            LOGGER.warn("Wrong data type [" + dataType + "]");
        }

        final JSONObject vote = new JSONObject();
        vote.put(Vote.USER_ID, userId);
        vote.put(Vote.DATA_ID, dataId);
        vote.put(Vote.TYPE, Vote.TYPE_C_DOWN);
        vote.put(Vote.DATA_TYPE, dataType);

        voteRepository.add(vote);
    }

    private void updateTagArticleScore(final JSONObject article) throws RepositoryException {
        final List<JSONObject> tagArticleRels = tagArticleRepository.getByArticleId(article.optString(Keys.OBJECT_ID));
        for (final JSONObject tagArticleRel : tagArticleRels) {
            tagArticleRel.put(Article.REDDIT_SCORE, article.optDouble(Article.REDDIT_SCORE, 0D));

            tagArticleRepository.update(tagArticleRel.optString(Keys.OBJECT_ID), tagArticleRel);
        }
    }
}
