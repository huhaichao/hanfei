
package club.hanfei.repository;

import club.hanfei.model.Role;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Role repository.
 *
@version 1.0.0.0, Dec 3, 2016
 * @since 1.8.0
 */
@Repository
public class RoleRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public RoleRepository() {
        super(Role.ROLE);
    }
}
