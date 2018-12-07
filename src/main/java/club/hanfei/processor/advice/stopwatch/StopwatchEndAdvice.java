
package club.hanfei.processor.advice.stopwatch;

import java.util.Map;

import club.hanfei.model.Common;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.renderer.AbstractResponseRenderer;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;

/**
 * Stopwatch end advice for request processors.
 *
@version 1.1.0.0, Aug 2, 2015
 * @since 0.2.0
 */
@Service
public class StopwatchEndAdvice extends ProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StopwatchEndAdvice.class);

    @Override
    public void doAdvice(final RequestContext context) {
        Stopwatchs.end();

        final AbstractResponseRenderer renderer = context.getRenderer();
        if (null != renderer) {
            final Map<String, Object> dataModel = renderer.getRenderDataModel();
            final String requestURI = context.getRequest().getRequestURI();

            final long elapsed = Stopwatchs.getElapsed("Request URI [" + requestURI + ']');
            dataModel.put(Common.ELAPSED, elapsed);
        }

        LOGGER.log(Level.TRACE, "Stopwatch: {0}    {1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
    }
}
