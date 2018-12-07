
package club.hanfei.repository;

import club.hanfei.model.Common;
import club.hanfei.model.Tag;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User-Tag relation repository.
 *
@version 1.1.0.0, Apr 17, 2017
 * @since 0.2.0
 */
@Repository
public class UserTagRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public UserTagRepository() {
        super(User.USER + "_" + Tag.TAG);
    }

    /**
     * Removes user-tag relations by the specified user id and tag id.
     *
     * @param userId the specified user id
     * @param tagId  the specified tag id
     * @param type   the specified type
     * @throws RepositoryException repository exception
     */
    public void removeByUserIdAndTagId(final String userId, final String tagId, final int type) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(User.USER + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId),
                new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, type)
        )).setCurrentPageNum(1).setPageSize(Integer.MAX_VALUE).setPageCount(1);

        final JSONArray rels = get(query).optJSONArray(Keys.RESULTS);
        for (int i = 0; i < rels.length(); i++) {
            final String id = rels.optJSONObject(i).optString(Keys.OBJECT_ID);
            remove(id);
        }
    }

    /**
     * Gets user-tag relations by the specified user id.
     *
     * @param userId         the specified user id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "tag_oId": "",
     *         "user_oId": userId,
     *         "type": "" // "creator"/"article"/"comment", a tag 'creator' is also an 'article' quoter
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByUserId(final String userId, final int currentPageNum, final int pageSize) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(User.USER + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, userId)).
                setCurrentPageNum(currentPageNum).setPageSize(pageSize).setPageCount(1);

        return get(query);
    }

    /**
     * Gets user-tag relations by the specified tag id.
     *
     * @param tagId          the specified tag id
     * @param currentPageNum the specified current page number, MUST greater then {@code 0}
     * @param pageSize       the specified page size(count of a page contains objects), MUST greater then {@code 0}
     * @return for example      <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "",
     *         "tag_oId": "",
     *         "user_oId": userId,
     *         "type": "" // "creator"/"article"/"comment", a tag 'creator' is also an 'article' quoter
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTagId(final String tagId, final int currentPageNum, final int pageSize) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId)).
                setCurrentPageNum(currentPageNum).setPageSize(pageSize).setPageCount(1);

        return get(query);
    }
}
