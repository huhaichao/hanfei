
package club.hanfei.processor.channel;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;

/**
 * Channel utilities.
 *
@version 2.0.2.3, Sep 27, 2018
 * @since 1.4.0
 */
public final class Channels {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Channels.class);

    /**
     * WebSocket configurator.
     *
     *
     * @version 1.0.0.1, Sep 27, 2018
     * @since 1.4.0
     */
    public static class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

        @Override
        public void modifyHandshake(final ServerEndpointConfig config, final HandshakeRequest request, final HandshakeResponse response) {
            final HttpSession httpSession = (HttpSession) request.getHttpSession();
            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
            final String skin = (String) httpSession.getAttribute(Keys.TEMAPLTE_DIR_NAME);
            config.getUserProperties().put(Keys.TEMAPLTE_DIR_NAME, skin);
        }
    }

    /**
     * Gets a parameter of the specified HTTP session by the given session.
     *
     * @param session       the given session
     * @param parameterName the specified parameter name
     * @return parameter value, returns {@code null} if the parameter does not exist
     */
    public static String getHttpParameter(final Session session, final String parameterName) {
        final Map<String, List<String>> parameterMap = session.getRequestParameterMap();
        for (final String key : parameterMap.keySet()) {
            if (!key.equals(parameterName)) {
                continue;
            }

            final List<String> values = parameterMap.get(key);
            if (null != values && !values.isEmpty()) {
                return values.get(0);
            }
        }

        return null;
    }

    /**
     * Gets an attribute of the specified HTTP session by the given session.
     *
     * @param session       the given session
     * @param attributeName the specified attribute name
     * @return attribute, returns {@code null} if not found or occurred exception
     */
    public static Object getHttpSessionAttribute(final Session session, final String attributeName) {
        final HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
        if (null == httpSession) {
            return null;
        }

        try {
            return httpSession.getAttribute(attributeName);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Private constructor.
     */
    private Channels() {
    }

}
