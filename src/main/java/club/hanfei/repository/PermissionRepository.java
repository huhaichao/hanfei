
package club.hanfei.repository;

import club.hanfei.model.Permission;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Permission repository.
 *
@version 1.0.0.0, Dec 3, 2016
 * @since 1.8.0
 */
@Repository
public class PermissionRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public PermissionRepository() {
        super(Permission.PERMISSION);
    }
}
