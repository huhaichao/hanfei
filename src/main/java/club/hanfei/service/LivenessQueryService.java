
package club.hanfei.service;

import java.util.Date;

import club.hanfei.model.Liveness;
import club.hanfei.repository.LivenessRepository;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Stopwatchs;
import org.json.JSONObject;

/**
 * Liveness query service.
 *
@version 1.1.0.0, Mar 23, 2016
 * @since 1.4.0
 */
@Service
public class LivenessQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LivenessQueryService.class);

    /**
     * Liveness repository.
     */
    @Inject
    private LivenessRepository livenessRepository;

    /**
     * Gets point of current liveness.
     *
     * @param userId the specified user id
     * @return point
     */
    public int getCurrentLivenessPoint(final String userId) {
        Stopwatchs.start("Gets liveness");
        try {
            final String date = DateFormatUtils.format(new Date(), "yyyyMMdd");

            try {
                final JSONObject liveness = livenessRepository.getByUserAndDate(userId, date);
                if (null == liveness) {
                    return 0;
                }

                return Liveness.calcPoint(liveness);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Gets current liveness point failed", e);

                return 0;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the yesterday's liveness.
     *
     * @param userId the specified user id
     * @return yesterday's liveness, returns {@code null} if not found
     */
    public JSONObject getYesterdayLiveness(final String userId) {
        final Date yesterday = DateUtils.addDays(new Date(), -1);
        final String date = DateFormatUtils.format(yesterday, "yyyyMMdd");

        try {
            return livenessRepository.getByUserAndDate(userId, date);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets yesterday's liveness failed", e);

            return null;
        }
    }
}
