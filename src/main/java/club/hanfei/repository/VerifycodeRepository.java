
package club.hanfei.repository;

import club.hanfei.model.Verifycode;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Verifycode repository.
 *
@version 1.0.0.0, Jul 3, 2015
 * @since 1.3.0
 */
@Repository
public class VerifycodeRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public VerifycodeRepository() {
        super(Verifycode.VERIFYCODE);
    }
}
