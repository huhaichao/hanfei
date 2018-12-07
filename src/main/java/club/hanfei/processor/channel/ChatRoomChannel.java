
package club.hanfei.processor.channel;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import club.hanfei.model.Common;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

/**
 * Char room channel.
 *
@version 1.0.1.1, Apr 25, 2016
 * @since 1.4.0
 */
@ServerEndpoint(value = "/chat-room-channel", configurator = Channels.WebSocketConfigurator.class)
public class ChatRoomChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ChatRoomChannel.class);

    /**
     * Session set.
     */
    public static final Set<Session> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap());

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        SESSIONS.add(session);

        synchronized (SESSIONS) {
            final Iterator<Session> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final Session s = i.next();

                if (s.isOpen()) {
                    final String msgStr = new JSONObject().put(Common.ONLINE_CHAT_CNT, SESSIONS.size()).put(Common.TYPE, "online").toString();
                    s.getAsyncRemote().sendText(msgStr);
                }
            }
        }
    }

    /**
     * Called when the connection closed.
     *
     * @param session     session
     * @param closeReason close reason
     */
    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        removeSession(session);
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
        removeSession(session);
    }

    /**
     * Notifies the specified chat message to browsers.
     *
     * @param message the specified message, for example      <pre>
     *                               {
     *                                   "userName": "",
     *                                   "content": ""
     *                               }
     *                               </pre>
     */
    public static void notifyChat(final JSONObject message) {
        message.put(Common.TYPE, "msg");
        final String msgStr = message.toString();

        synchronized (SESSIONS) {
            final Iterator<Session> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final Session session = i.next();

                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(msgStr);
                }
            }
        }
    }

    /**
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final Session session) {
        SESSIONS.remove(session);

        synchronized (SESSIONS) {
            final Iterator<Session> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final Session s = i.next();

                if (s.isOpen()) {
                    final String msgStr = new JSONObject().put(Common.ONLINE_CHAT_CNT, SESSIONS.size()).put(Common.TYPE, "online").toString();
                    s.getAsyncRemote().sendText(msgStr);
                }
            }
        }
    }
}
