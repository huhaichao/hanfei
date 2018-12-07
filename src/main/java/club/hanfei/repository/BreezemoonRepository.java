
package club.hanfei.repository;

import club.hanfei.model.Breezemoon;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Breezemoon repository.
 *
@version 1.0.0.0, May 21, 2018
 * @since 2.8.0
 */
@Repository
public class BreezemoonRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public BreezemoonRepository() {
        super(Breezemoon.BREEZEMOON);
    }
}
