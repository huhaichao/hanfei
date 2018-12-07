
package club.hanfei.processor.advice;

import java.util.Map;

import club.hanfei.model.Common;
import club.hanfei.util.Sessions;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.renderer.AbstractResponseRenderer;

/**
 * Fills CSRF token.
 *
@version 1.0.0.0, Aug 27, 2015
 * @since 1.3.0
 */
@Singleton
public class CSRFToken extends ProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CSRFToken.class);

    @Override
    public void doAdvice(final RequestContext context) {
        final AbstractResponseRenderer renderer = context.getRenderer();
        if (null != renderer) {
            final Map<String, Object> dataModel = renderer.getRenderDataModel();

            dataModel.put(Common.CSRF_TOKEN, Sessions.getCSRFToken(context.getRequest()));
        }
    }
}
