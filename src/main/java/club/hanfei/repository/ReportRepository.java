
package club.hanfei.repository;

import club.hanfei.model.Report;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Report repository.
 *
@version 1.0.0.0, Jun 25, 2018
 * @since 3.1.0
 */
@Repository
public class ReportRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public ReportRepository() {
        super(Report.REPORT);
    }
}
