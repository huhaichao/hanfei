
package club.hanfei.repository;

import java.util.List;

import club.hanfei.model.Permission;
import club.hanfei.model.Role;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.json.JSONObject;

/**
 * Role-Permission repository.
 *
@version 1.0.0.1, Aug 27, 2018
 * @since 1.8.0
 */
@Repository
public class RolePermissionRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public RolePermissionRepository() {
        super(Role.ROLE + "_" + Permission.PERMISSION);
    }

    /**
     * Removes role-permission relations by the specified role id.
     *
     * @param roleId the specified role id
     * @throws RepositoryException repository exception
     */
    public void removeByRoleId(final String roleId) throws RepositoryException {
        final List<JSONObject> toRemoves = getByRoleId(roleId);
        for (final JSONObject toRemove : toRemoves) {
            remove(toRemove.optString(Keys.OBJECT_ID));
        }
    }

    /**
     * Gets role-permission relations by the specified role id.
     *
     * @param roleId the specified role id
     * @return for example      <pre>
     * [{
     *         "oId": "",
     *         "roleId": roleId,
     *         "permissionId": ""
     * }, ....], returns an empty list if not found
     * </pre>
     * @throws RepositoryException repository exception
     */
    public List<JSONObject> getByRoleId(final String roleId) throws RepositoryException {
        final Query query = new Query().setFilter(
                new PropertyFilter(Role.ROLE_ID, FilterOperator.EQUAL, roleId)).
                setPageCount(1);

        return getList(query);
    }
}
