
package club.hanfei.service;

import java.util.Collections;
import java.util.List;

import club.hanfei.model.Article;
import club.hanfei.model.Comment;
import club.hanfei.model.Revision;
import club.hanfei.repository.CommentRepository;
import club.hanfei.repository.RevisionRepository;
import club.hanfei.util.Escapes;
import club.hanfei.util.Markdowns;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Stopwatchs;
import org.json.JSONObject;

/**
 * Revision query service.
 *
@version 1.0.1.1, Nov 30, 2018
 * @since 2.1.0
 */
@Service
public class RevisionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RevisionQueryService.class);

    /**
     * Revision repository.
     */
    @Inject
    private RevisionRepository revisionRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Gets a comment's revisions.
     *
     * @param commentId the specified comment id
     * @return comment revisions, returns an empty list if not found
     */
    public List<JSONObject> getCommentRevisions(final String commentId) {
        try {
            final JSONObject comment = commentRepository.get(commentId);
            if (null == comment || Comment.COMMENT_STATUS_C_VALID != comment.optInt(Comment.COMMENT_STATUS)) {
                return Collections.emptyList();
            }

            final Query query = new Query().setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, commentId),
                    new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_COMMENT)
            )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(revisionRepository.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                String commentContent = data.optString(Comment.COMMENT_CONTENT);
                commentContent = commentContent.replace("\n", "_esc_br_");
                commentContent = Markdowns.clean(commentContent, "");
                commentContent = commentContent.replace("_esc_br_", "\n");
                data.put(Comment.COMMENT_CONTENT, commentContent);

                rev.put(Revision.REVISION_DATA, data);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets comment revisions failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets an article's revisions.
     *
     * @param articleId the specified article id
     * @return article revisions, returns an empty list if not found
     */
    public List<JSONObject> getArticleRevisions(final String articleId) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, articleId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_ARTICLE)
        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(revisionRepository.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject rev : ret) {
                final JSONObject data = new JSONObject(rev.optString(Revision.REVISION_DATA));
                final String articleTitle = Escapes.escapeHTML(data.optString(Article.ARTICLE_TITLE));
                data.put(Article.ARTICLE_TITLE, articleTitle);

                String articleContent = data.optString(Article.ARTICLE_CONTENT);
                // articleContent = Markdowns.toHTML(articleContent); https://hacpai.com/article/1490233597586
                articleContent = articleContent.replace("\n", "_esc_br_");
                articleContent = Markdowns.clean(articleContent, "");
                articleContent = articleContent.replace("_esc_br_", "\n");
                data.put(Article.ARTICLE_CONTENT, articleContent);

                rev.put(Revision.REVISION_DATA, data);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets article revisions failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Counts revision specified by the given data id and data type.
     *
     * @param dataId   the given data id
     * @param dataType the given data type
     * @return count result
     */
    public int count(final String dataId, final int dataType) {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, dataId),
                new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, dataType)
        ));

        Stopwatchs.start("Revision count");
        try {
            return (int) revisionRepository.count(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Counts revisions failed", e);

            return 0;
        } finally {
            Stopwatchs.end();
        }
    }
}
