
package club.hanfei.repository;

import club.hanfei.model.Reward;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;

/**
 * Reward repository.
 *
@version 1.1.0.0, Sep 15, 2018
 * @since 1.3.0
 */
@Repository
public class RewardRepository extends AbstractRepository {

    /**
     * Removes rewards by the specified data id.
     *
     * @param dataId the specified data id
     * @throws RepositoryException repository exception
     */
    public void removeByDataId(final String dataId) throws RepositoryException {
        remove(new Query().setFilter(new PropertyFilter(Reward.DATA_ID, FilterOperator.EQUAL, dataId)).
                setPageCount(1));
    }

    /**
     * Public constructor.
     */
    public RewardRepository() {
        super(Reward.REWARD);
    }
}
