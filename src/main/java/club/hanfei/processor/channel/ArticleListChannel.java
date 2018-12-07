
package club.hanfei.processor.channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import club.hanfei.model.Article;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

/**
 * Article list channel.
 *
@version 2.0.3.1, Apr 25, 2016
 * @since 1.3.0
 */
@ServerEndpoint(value = "/article-list-channel", configurator = Channels.WebSocketConfigurator.class)
public class ArticleListChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleListChannel.class);

    /**
     * Session articles &lt;session, "articleId1,articleId2"&gt;.
     */
    public static final Map<Session, String> SESSIONS = new ConcurrentHashMap<>();

    /**
     * Notifies the specified article heat message to browsers.
     *
     * @param message the specified message, for example
     *                {
     *                "articleId": "",
     *                "operation": "" // "+"/"-"
     *                }
     */
    public static void notifyHeat(final JSONObject message) {
        final String articleId = message.optString(Article.ARTICLE_T_ID);
        final String msgStr = message.toString();

        for (final Map.Entry<Session, String> entry : SESSIONS.entrySet()) {
            final Session session = entry.getKey();
            final String articleIds = entry.getValue();

            if (!StringUtils.contains(articleIds, articleId)) {
                continue;
            }

            if (session.isOpen()) {
                session.getAsyncRemote().sendText(msgStr);
            }
        }
    }

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        final String articleIds = Channels.getHttpParameter(session, Article.ARTICLE_T_IDS);
        if (StringUtils.isBlank(articleIds)) {
            return;
        }

        SESSIONS.put(session, articleIds);
    }

    /**
     * Called when the connection closed.
     *
     * @param session     session
     * @param closeReason close reason
     */
    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        SESSIONS.remove(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @OnMessage
    public void onMessage(final String message) {
    }

    /**
     * Called in case of an error.
     *
     * @param session session
     * @param error   error
     */
    @OnError
    public void onError(final Session session, final Throwable error) {
        SESSIONS.remove(session);
    }
}
