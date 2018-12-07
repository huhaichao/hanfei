
package club.hanfei.service;

import club.hanfei.cache.ArticleCache;
import club.hanfei.cache.DomainCache;
import club.hanfei.cache.TagCache;
import club.hanfei.model.Common;
import club.hanfei.processor.StatisticProcessor;
import club.hanfei.processor.channel.ArticleChannel;
import club.hanfei.processor.channel.ArticleListChannel;
import club.hanfei.processor.channel.ChatRoomChannel;
import club.hanfei.util.Hanfei;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.jdbc.util.Connections;
import org.b3log.latke.service.annotation.Service;
import org.json.JSONObject;

/**
 * Cache management service.
 *
@version 1.0.0.0, Dec 2, 2018
 * @since 3.4.5
 */
@Service
public class CacheMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CacheMgmtService.class);

    /**
     * Global locks
     */
    private static Cache LOCKS = CacheFactory.getCache("locks");

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Article cache.
     */
    @Inject
    private ArticleCache articleCache;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Refreshes all caches.
     */
    public void refreshCache() {
        final String lockName = "refreshCaches";
        if (!lock(lockName)) {
            return;
        }
        try {
            LOGGER.info("Refreshing cache");
            domainCache.loadDomains();
            articleCache.loadPerfectArticles();
            articleCache.loadSideHotArticles();
            articleCache.loadSideRandomArticles();
            tagCache.loadTags();
            final StatisticProcessor statisticProcessor = BeanManager.getInstance().getReference(StatisticProcessor.class);
            statisticProcessor.loadStatData();
            userQueryService.loadUserNames();
            statusReport();
            LOGGER.info("Refreshed cache");
        } finally {
            unlock(lockName);
        }
    }

    private void statusReport() {
        final JSONObject ret = new JSONObject();
        ret.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        ret.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());
        ret.put(Common.ONLINE_CHAT_CNT, ChatRoomChannel.SESSIONS.size());
        ret.put(Common.ARTICLE_CHANNEL_CNT, ArticleChannel.SESSIONS.size());
        ret.put(Common.ARTICLE_LIST_CHANNEL_CNT, ArticleListChannel.SESSIONS.size());
        ret.put(Common.THREAD_CNT, Hanfei.getActiveThreadCount() + "/" + Hanfei.getMaxThreadCount());
        ret.put(Common.DB_CONN_CNT, Connections.getActiveConnectionCount() + "/" + Connections.getTotalConnectionCount() + "/" + Connections.getMaxConnectionCount());
        ret.put(Keys.Runtime.RUNTIME_CACHE, Latkes.getRuntimeCache().name());
        ret.put(Keys.Runtime.RUNTIME_DATABASE, Latkes.getRuntimeDatabase().name());
        ret.put(Keys.Runtime.RUNTIME_MODE, Latkes.getRuntimeMode().name());
        final JSONObject memory = new JSONObject();
        ret.put("memory", memory);
        final int mb = 1024 * 1024;
        final Runtime runtime = Runtime.getRuntime();
        memory.put("total", runtime.totalMemory() / mb);
        memory.put("free", runtime.freeMemory() / mb);
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / mb);
        memory.put("max", runtime.maxMemory() / mb);

        LOGGER.info(ret.toString(4));
    }

    /**
     * Lock.
     *
     * @param lockName the specified lock name
     * @return {@code true} if lock successfully, returns {@code false} otherwise
     */
    public synchronized static boolean lock(final String lockName) {
        JSONObject lock = LOCKS.get(lockName);
        if (null == lock) {
            lock = new JSONObject();
        }

        if (lock.optBoolean(Common.LOCK)) {
            return false;
        }

        lock.put(Common.LOCK, true);
        LOCKS.put(lockName, lock);

        return true;
    }

    /**
     * Unlock.
     *
     * @param lockName the specified lock name
     */
    public synchronized static void unlock(final String lockName) {
        JSONObject lock = LOCKS.get(lockName);
        if (null == lock) {
            lock = new JSONObject();
        }

        lock.put(Common.LOCK, false);
        LOCKS.put(lockName, lock);
    }
}
