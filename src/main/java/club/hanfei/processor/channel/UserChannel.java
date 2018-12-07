
package club.hanfei.processor.channel;

import java.util.Collections;
import java.util.Map;
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
import club.hanfei.model.UserExt;
import club.hanfei.service.UserMgmtService;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.json.JSONObject;

/**
 * User channel.
 *
@version 1.0.1.2, Sep 3, 2018
 * @since 1.4.0
 */
@ServerEndpoint(value = "/user-channel", configurator = Channels.WebSocketConfigurator.class)
public class UserChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserChannel.class);

    /**
     * Session set.
     */
    public static final Map<String, Set<Session>> SESSIONS = new ConcurrentHashMap();

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);

        Set<Session> userSessions = SESSIONS.get(userId);
        if (null == userSessions) {
            userSessions = Collections.newSetFromMap(new ConcurrentHashMap());
        }
        userSessions.add(session);

        SESSIONS.put(userId, userSessions);

        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        final String ip = (String) Channels.getHttpSessionAttribute(session, Common.IP);
        userMgmtService.updateOnlineStatus(userId, ip, true, true);
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
     * @param session session
     */
    @OnMessage
    public void onMessage(final String message, final Session session) {
        JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        final String ip = (String) Channels.getHttpSessionAttribute(session, Common.IP);
        userMgmtService.updateOnlineStatus(userId, ip, true, true);
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
     * Sends command to browsers.
     *
     * @param message the specified message, for example,
     *                "userId": "",
     *                "cmd": ""
     */
    public static void sendCmd(final JSONObject message) {
        final String recvUserId = message.optString(UserExt.USER_T_ID);
        if (StringUtils.isBlank(recvUserId)) {
            return;
        }

        final String msgStr = message.toString();

        for (final String userId : SESSIONS.keySet()) {
            if (userId.equals(recvUserId)) {
                final Set<Session> sessions = SESSIONS.get(userId);

                for (final Session session : sessions) {
                    if (session.isOpen()) {
                        session.getAsyncRemote().sendText(msgStr);
                    }
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
        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        final String ip = (String) Channels.getHttpSessionAttribute(session, Common.IP);

        Set<Session> userSessions = SESSIONS.get(userId);
        if (null == userSessions) {
            userMgmtService.updateOnlineStatus(userId, ip, false, false);

            return;
        }

        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            userMgmtService.updateOnlineStatus(userId, ip, false, false);

            return;
        }
    }
}
