
package club.hanfei.repository;

import java.util.List;

import club.hanfei.cache.UserCache;
import club.hanfei.model.Role;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.annotation.Repository;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User repository.
 *
@version 2.1.2.2, Aug 27, 2018
 * @since 0.2.0
 */
@Repository
public class UserRepository extends AbstractRepository {

    /**
     * User cache.
     */
    @Inject
    private UserCache userCache;

    /**
     * Public constructor.
     */
    public UserRepository() {
        super(User.USER);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = userCache.getUser(id);
        if (null != ret) {
            return ret;
        }

        ret = super.get(id);

        if (null == ret) {
            return null;
        }

        userCache.putUser(ret);

        return ret;
    }

    @Override
    public void update(final String id, final JSONObject user) throws RepositoryException {
        final JSONObject old = get(id);
        if (null == old) {
            return;
        }

        userCache.removeUser(old);
        super.update(id, user);
        user.put(Keys.OBJECT_ID, id);
        userCache.putUser(user);
    }

    /**
     * Gets a user by the specified name.
     *
     * @param name the specified name
     * @return user, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByName(final String name) throws RepositoryException {
        JSONObject ret = userCache.getUserByName(name);
        if (null != ret) {
            return ret;
        }

        final Query query = new Query().setPageCount(1);
        query.setFilter(new PropertyFilter(User.USER_NAME, FilterOperator.EQUAL, name));

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        ret = array.optJSONObject(0);

        userCache.putUser(ret);

        return ret;
    }

    /**
     * Gets a user by the specified email.
     *
     * @param email the specified email
     * @return user, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByEmail(final String email) throws RepositoryException {
        final Query query = new Query().setPageCount(1);
        query.setFilter(new PropertyFilter(User.USER_EMAIL, FilterOperator.EQUAL, email.toLowerCase().trim()));

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Gets the administrators.
     *
     * @return administrators, returns an empty list if not found or error
     * @throws RepositoryException repository exception
     */
    public List<JSONObject> getAdmins() throws RepositoryException {
        List<JSONObject> ret = userCache.getAdmins();
        if (ret.isEmpty()) {
            final Query query = new Query().setFilter(
                    new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, Role.ROLE_ID_C_ADMIN)).setPageCount(1)
                    .addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
            ret = getList(query);
            userCache.putAdmins(ret);
        }

        return ret;
    }
}
