
package club.hanfei.processor.advice.validate;

import javax.servlet.http.HttpServletRequest;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.json.JSONObject;

/**
 * Validates for emotion list.
@author Zephyr
 * @version 1.0.0.0, Aug 16, 2016
 * @since 1.5.0
 */
@Singleton
public class UpdateEmotionListValidation extends ProcessAdvice {

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }
    }
}
