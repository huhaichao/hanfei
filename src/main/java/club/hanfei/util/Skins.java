
package club.hanfei.util;

import java.util.TimeZone;

import javax.servlet.ServletContext;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.AbstractServletListener;

/**
 * Skin utilities.
 *
@version 1.1.0.2, Sep 27, 2018
 * @since 1.3.0
 */
public final class Skins {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Skins.class);

    /**
     * FreeMarker template configurations for skins (skins/).
     */
    public static final Configuration SKIN;

    /**
     * Freemarker version.
     */
    public static final Version FREEMARKER_VER = Configuration.VERSION_2_3_28;

    static {
        final ServletContext servletContext = AbstractServletListener.getServletContext();
        SKIN = new Configuration(FREEMARKER_VER);
        SKIN.setDefaultEncoding("UTF-8");
        SKIN.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        SKIN.setServletContextForTemplateLoading(servletContext, "skins");
        SKIN.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        SKIN.setLogTemplateExceptions(false);
    }

    /**
     * Private constructor.
     */
    private Skins() {
    }
}
