
package club.hanfei.repository;

import club.hanfei.model.Revision;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Revision repository.
 *
@version 1.0.0.0, Apr 20, 2016
 * @since 1.4.0
 */
@Repository
public class RevisionRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public RevisionRepository() {
        super(Revision.REVISION);
    }
}
