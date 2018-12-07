
package club.hanfei.cache;

import club.hanfei.model.Comment;
import club.hanfei.util.Hanfei;
import club.hanfei.util.JSONs;
import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.ioc.Singleton;
import org.json.JSONObject;

/**
 * Comment cache.
 *
@version 1.0.0.0, Sep 1, 2016
 * @since 1.6.0
 */
@Singleton
public class CommentCache {

    /**
     * Comment cache.
     */
    private static final Cache cache = CacheFactory.getCache(Comment.COMMENTS);

    static {
        cache.setMaxCount(Hanfei.getInt("cache.commentCnt"));
    }

    /**
     * Gets a comment by the specified comment id.
     *
     * @param id the specified comment id
     * @return comment, returns {@code null} if not found
     */
    public JSONObject getComment(final String id) {
        final JSONObject comment = cache.get(id);
        if (null == comment) {
            return null;
        }

        return JSONs.clone(comment);
    }

    /**
     * Adds or updates the specified comment.
     *
     * @param comment the specified comment
     */
    public void putComment(final JSONObject comment) {
        cache.put(comment.optString(Keys.OBJECT_ID), JSONs.clone(comment));
    }

    /**
     * Removes a comment by the specified comment id.
     *
     * @param id the specified comment id
     */
    public void removeComment(final String id) {
        cache.remove(id);
    }
}
