
package club.hanfei.repository;

import java.util.ArrayList;
import java.util.List;

import club.hanfei.model.Vote;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Vote repository.
 *
@version 1.1.0.0, Sep 15, 2018
 * @since 1.3.0
 */
@Repository
public class VoteRepository extends AbstractRepository {

    /**
     * Removes votes by the specified data id.
     *
     * @param dataId the specified data id
     * @throws RepositoryException repository exception
     */
    public void removeByDataId(final String dataId) throws RepositoryException {
        remove(new Query().setFilter(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId)).
                setPageCount(1));
    }

    /**
     * Removes vote if it exists.
     *
     * @param userId   the specified user id
     * @param dataId   the specified data entity id
     * @param dataType the specified data type
     * @return the removed vote type, returns {@code -1} if removed nothing
     * @throws RepositoryException repository exception
     */
    public int removeIfExists(final String userId, final String dataId, final int dataType) throws RepositoryException {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Vote.USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId));
        filters.add(new PropertyFilter(Vote.DATA_TYPE, FilterOperator.EQUAL, dataType));
        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);
        if (0 == array.length()) {
            return -1;
        }

        final JSONObject voteToRemove = array.optJSONObject(0);
        remove(voteToRemove.optString(Keys.OBJECT_ID));

        return voteToRemove.optInt(Vote.TYPE);
    }

    /**
     * Public constructor.
     */
    public VoteRepository() {
        super(Vote.VOTE);
    }
}
