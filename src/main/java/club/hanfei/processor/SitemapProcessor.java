
package club.hanfei.processor;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.sitemap.Sitemap;
import club.hanfei.service.SitemapQueryService;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.TextXmlRenderer;

/**
 * Sitemap processor.
 *
@version 1.0.0.0, Sep 24, 2016
 * @since 1.6.0
 */
@RequestProcessor
public class SitemapProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SitemapProcessor.class);

    /**
     * Sitemap query service.
     */
    @Inject
    private SitemapQueryService sitemapQueryService;

    /**
     * Returns the sitemap.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/sitemap.xml", method = HttpMethod.GET)
    public void sitemap(final RequestContext context) {
        final TextXmlRenderer renderer = new TextXmlRenderer();

        context.setRenderer(renderer);

        final Sitemap sitemap = new Sitemap();

        try {
            LOGGER.log(Level.INFO, "Generating sitemap....");

            sitemapQueryService.genIndex(sitemap);
            sitemapQueryService.genDomains(sitemap);
            sitemapQueryService.genArticles(sitemap);

            final String content = sitemap.toString();

            LOGGER.log(Level.INFO, "Generated sitemap");

            renderer.setContent(content);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get blog article feed error", e);

            try {
                context.getResponse().sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
