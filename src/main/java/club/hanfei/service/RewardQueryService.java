
package club.hanfei.service;

import java.util.ArrayList;
import java.util.List;

import club.hanfei.model.Reward;
import club.hanfei.repository.RewardRepository;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;

/**
 * Reward query service.
 *
@version 1.1.0.1, Oct 17, 2015
 * @since 1.3.0
 */
@Service
public class RewardQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RewardQueryService.class);

    /**
     * Reward repository.
     */
    @Inject
    private RewardRepository rewardRepository;

    /**
     * Gets rewarded count.
     *
     * @param dataId the specified data id
     * @param type   the specified type
     * @return rewarded count
     */
    public long rewardedCount(final String dataId, final int type) {
        final Query query = new Query();
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Reward.DATA_ID, FilterOperator.EQUAL, dataId));
        filters.add(new PropertyFilter(Reward.TYPE, FilterOperator.EQUAL, type));

        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            return rewardRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Rewarded count error", e);

            return 0;
        }
    }

    /**
     * Determines the user specified by the given user id has rewarded the data (article/comment/user) or not.
     *
     * @param userId the specified user id
     * @param dataId the specified data id
     * @param type   the specified type
     * @return {@code true} if has rewared
     */
    public boolean isRewarded(final String userId, final String dataId, final int type) {
        final Query query = new Query();
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Reward.SENDER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Reward.DATA_ID, FilterOperator.EQUAL, dataId));
        filters.add(new PropertyFilter(Reward.TYPE, FilterOperator.EQUAL, type));

        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            return 0 != rewardRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Determines reward error", e);

            return false;
        }
    }
}
