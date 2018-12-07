
package club.hanfei.repository;

import club.hanfei.model.Visit;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Visit repository.
 *
@version 1.0.0.0, Jul 27, 2018
 * @since 3.2.0
 */
@Repository
public class VisitRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public VisitRepository() {
        super(Visit.VISIT);
    }
}
