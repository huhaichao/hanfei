
package club.hanfei.repository;

import java.util.ArrayList;
import java.util.List;

import club.hanfei.model.Follow;
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
 * Follow repository.
 *
@version 1.0.0.1, Jan 18, 2018
 * @since 0.2.5
 */
@Repository
public class FollowRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public FollowRepository() {
        super(Follow.FOLLOW);
    }

    /**
     * Removes a follow relationship by the specified follower id and the specified following entity id.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    public void removeByFollowerIdAndFollowingId(final String followerId, final String followingId, final int followingType)
            throws RepositoryException {
        final JSONObject toRemove = getByFollowerIdAndFollowingId(followerId, followingId, followingType);

        if (null == toRemove) {
            return;
        }

        remove(toRemove.optString(Keys.OBJECT_ID));
    }

    /**
     * Gets a follow relationship by the specified follower id and the specified following entity id.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @return follow relationship, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByFollowerIdAndFollowingId(final String followerId, final String followingId, final int followingType)
            throws RepositoryException {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Follow.FOLLOWER_ID, FilterOperator.EQUAL, followerId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_ID, FilterOperator.EQUAL, followingId));
        filters.add(new PropertyFilter(Follow.FOLLOWING_TYPE, FilterOperator.EQUAL, followingType));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Determines whether exists a follow relationship for the specified follower and the specified following entity.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @return {@code true} if exists, returns {@code false} otherwise
     * @throws RepositoryException repository exception
     */
    public boolean exists(final String followerId, final String followingId, final int followingType)
            throws RepositoryException {
        return null != getByFollowerIdAndFollowingId(followerId, followingId, followingType);
    }
}
