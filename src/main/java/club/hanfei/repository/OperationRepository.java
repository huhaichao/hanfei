
package club.hanfei.repository;

import club.hanfei.model.Operation;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Operation repository.
 *
@version 1.0.0.0, Nov 19, 2018
 * @since 3.4.4
 */
@Repository
public class OperationRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public OperationRepository() {
        super(Operation.OPERATION);
    }
}
