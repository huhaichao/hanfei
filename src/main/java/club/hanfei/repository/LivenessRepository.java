
package club.hanfei.repository;

import club.hanfei.model.Liveness;
import org.b3log.latke.Keys;
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
 * Liveness repository.
 *
@version 1.0.0.0, Mar 22, 2016
 * @since 1.4.0
 */
@Repository
public class LivenessRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public LivenessRepository() {
        super(Liveness.LIVENESS);
    }

    /**
     * Gets a liveness by the specified user id and date.
     *
     * @param userId the specified user id
     * @param date   the specified date
     * @return a liveness, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByUserAndDate(final String userId, final String date) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Liveness.LIVENESS_USER_ID, FilterOperator.EQUAL, userId),
                new PropertyFilter(Liveness.LIVENESS_DATE, FilterOperator.EQUAL, date))).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }
}
