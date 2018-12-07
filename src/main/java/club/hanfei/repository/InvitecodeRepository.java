
package club.hanfei.repository;

import club.hanfei.model.Invitecode;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Invitecode repository.
 *
@version 1.0.0.0, Jul 2, 2016
 * @since 1.4.0
 */
@Repository
public class InvitecodeRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public InvitecodeRepository() {
        super(Invitecode.INVITECODE);
    }
}
