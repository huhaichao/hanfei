
package club.hanfei.repository;

import club.hanfei.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * User-Role repository.
 *
@version 1.0.0.0, Dec 3, 2016
 * @since 1.8.0
 */
@Repository
public class UserRoleRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public UserRoleRepository() {
        super(User.USER + "_" + Role.ROLE);
    }
}
