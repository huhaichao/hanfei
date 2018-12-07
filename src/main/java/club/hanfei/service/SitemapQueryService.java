
package club.hanfei.service;

import java.util.Date;
import java.util.List;

import club.hanfei.cache.DomainCache;
import club.hanfei.model.Article;
import club.hanfei.model.Domain;
import club.hanfei.model.sitemap.Sitemap;
import club.hanfei.repository.ArticleRepository;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.annotation.Service;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Sitemap query service.
 *
@version 1.0.0.1, Mar 31, 2018
 * @since 1.6.0
 */
@Service
public class SitemapQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SitemapQueryService.class);

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Generates index for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genIndex(final Sitemap sitemap) {
        final Sitemap.URL url = new Sitemap.URL();
        url.setLoc(Latkes.getServePath());
        url.setChangeFreq("always");
        url.setPriority("1.0");

        sitemap.addURL(url);
    }

    /**
     * Generates domains for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genDomains(final Sitemap sitemap) {
        final List<JSONObject> domains = domainCache.getDomains(Integer.MAX_VALUE);

        for (final JSONObject domain : domains) {
            final String permalink = Latkes.getServePath() + "/domain/" + domain.optString(Domain.DOMAIN_URI);

            final Sitemap.URL url = new Sitemap.URL();
            url.setLoc(permalink);
            url.setChangeFreq("always");
            url.setPriority("0.9");

            sitemap.addURL(url);
        }
    }

    /**
     * Generates articles for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genArticles(final Sitemap sitemap) {
        final Query query = new Query().setCurrentPageNum(1).setPageCount(Integer.MAX_VALUE).
                addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                setFilter(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONArray articles = articleRepository.get(query).getJSONArray(Keys.RESULTS);

            for (int i = 0; i < articles.length(); i++) {
                final JSONObject article = articles.getJSONObject(i);
                final long id = article.getLong(Keys.OBJECT_ID);
                final String permalink = Latkes.getServePath() + "/article/" + id;

                final Sitemap.URL url = new Sitemap.URL();
                url.setLoc(permalink);
                final Date updateDate = new Date(id);
                final String lastMod = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(updateDate);
                url.setLastMod(lastMod);

                sitemap.addURL(url);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets sitemap articles failed", e);
        }
    }
}
