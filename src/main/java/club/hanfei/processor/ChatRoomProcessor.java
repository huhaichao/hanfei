
package club.hanfei.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.model.UserExt;
import club.hanfei.processor.advice.AnonymousViewCheck;
import club.hanfei.processor.advice.LoginCheck;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.processor.advice.validate.ChatMsgAddValidation;
import club.hanfei.processor.channel.ChatRoomChannel;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.AvatarQueryService;
import club.hanfei.service.CommentMgmtService;
import club.hanfei.service.CommentQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.NotificationMgmtService;
import club.hanfei.service.NotificationQueryService;
import club.hanfei.service.ShortLinkQueryService;
import club.hanfei.service.TuringQueryService;
import club.hanfei.service.UserMgmtService;
import club.hanfei.service.UserQueryService;
import club.hanfei.util.Emotions;
import club.hanfei.util.Hanfei;
import club.hanfei.util.JSONs;
import club.hanfei.util.Markdowns;
import com.qiniu.util.Auth;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Times;
import org.json.JSONObject;

/**
 * Chat room processor.
 * <ul>
 * <li>Shows char room (/cr), GET</li>
 * <li>Sends chat message (/chat-room/send), POST</li>
 * <li>Receives <a href="https://github.com/b3log/xiaov">XiaoV</a> message (/community/push), POST</li>
 * </ul>
 *
@version 1.3.5.17, Nov 28, 2018
 * @since 1.4.0
 */
@RequestProcessor
public class ChatRoomProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ChatRoomProcessor.class);

    /**
     * Chat messages.
     */
    public static LinkedList<JSONObject> messages = new LinkedList<>();

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Turing query service.
     */
    @Inject
    private TuringQueryService turingQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Notification query service.
     */
    @Inject
    private NotificationQueryService notificationQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Comment management service.
     */
    @Inject
    private CommentMgmtService commentMgmtService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Adds a chat message.
     * <p>
     * The request json object (a chat message):
     * <pre>
     * {
     *     "content": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/chat-room/send", method = HttpMethod.POST)
    @Before({LoginCheck.class, ChatMsgAddValidation.class})
    public synchronized void addChatRoomMsg(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        String content = requestJSONObject.optString(Common.CONTENT);

        content = shortLinkQueryService.linkArticle(content);
        content = Emotions.convert(content);
        content = Markdowns.toHTML(content);
        content = Markdowns.clean(content, "");

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userName = currentUser.optString(User.USER_NAME);

        final JSONObject msg = new JSONObject();
        msg.put(User.USER_NAME, userName);
        msg.put(UserExt.USER_AVATAR_URL, currentUser.optString(UserExt.USER_AVATAR_URL));
        msg.put(Common.CONTENT, content);
        msg.put(Common.TIME, System.currentTimeMillis());

        messages.addFirst(msg);
        final int maxCnt = Hanfei.getInt("chatRoom.msgCnt");
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        final JSONObject pushMsg = JSONs.clone(msg);
        pushMsg.put(Common.TIME, Times.getTimeAgo(msg.optLong(Common.TIME), Locales.getLocale()));
        ChatRoomChannel.notifyChat(pushMsg);

        if (content.contains("@" + TuringQueryService.ROBOT_NAME + " ")) {
            content = content.replaceAll("@" + TuringQueryService.ROBOT_NAME + " ", "");
            final String xiaoVSaid = turingQueryService.chat(currentUser.optString(User.USER_NAME), content);
            if (null != xiaoVSaid) {
                final JSONObject xiaoVMsg = new JSONObject();
                xiaoVMsg.put(User.USER_NAME, TuringQueryService.ROBOT_NAME);
                xiaoVMsg.put(UserExt.USER_AVATAR_URL, TuringQueryService.ROBOT_AVATAR + "?imageView2/1/w/48/h/48/interlace/0/q");
                xiaoVMsg.put(Common.CONTENT, "<p>@" + userName + " " + xiaoVSaid + "</p>");
                xiaoVMsg.put(Common.TIME, System.currentTimeMillis());

                messages.addFirst(xiaoVMsg);
                if (messages.size() > maxCnt) {
                    messages.remove(maxCnt);
                }

                final JSONObject pushXiaoVMsg = JSONs.clone(xiaoVMsg);
                pushXiaoVMsg.put(Common.TIME, Times.getTimeAgo(System.currentTimeMillis(), Locales.getLocale()));
                ChatRoomChannel.notifyChat(pushXiaoVMsg);
            }
        }

        context.renderTrueResult();

        try {
            final String userId = currentUser.optString(Keys.OBJECT_ID);
            final JSONObject user = userQueryService.getUser(userId);
            user.put(UserExt.USER_LATEST_CMT_TIME, System.currentTimeMillis());
            userMgmtService.updateUser(userId, user);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Update user latest comment time failed", e);
        }
    }

    /**
     * Shows chat room.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/cr", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showChatRoom(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("chat-room.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> msgs = messages.stream().
                map(msg -> JSONs.clone(msg).put(Common.TIME, Times.getTimeAgo(msg.optLong(Common.TIME), Locales.getLocale()))).collect(Collectors.toList());
        dataModel.put(Common.MESSAGES, msgs);
        dataModel.put("chatRoomMsgCnt", Hanfei.getInt("chatRoom.msgCnt"));

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Hanfei.get("qiniu.accessKey"), Hanfei.get("qiniu.secretKey"));
        dataModel.put("qiniuUploadToken", auth.uploadToken(Hanfei.get("qiniu.bucket")));
        dataModel.put("qiniuDomain", Hanfei.get("qiniu.domain"));

        final long imgMaxSize = Hanfei.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Hanfei.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);
        dataModel.put(Common.ONLINE_CHAT_CNT, ChatRoomChannel.SESSIONS.size());

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
    }

    /**
     * XiaoV push API.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/community/push", method = HttpMethod.POST)
    @Before(StopwatchStartAdvice.class)
    @After(StopwatchEndAdvice.class)
    public synchronized void receiveXiaoV(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final String key = Hanfei.get("xiaov.key");
        if (!key.equals(request.getParameter("key"))) {
            context.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String msg = request.getParameter("msg");
        if (StringUtils.isBlank(msg)) {
            context.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        String user = request.getParameter("user");
        if (StringUtils.isBlank("user")) {
            user = "V";
        }

        final JSONObject ret = new JSONObject();
        context.renderJSON(ret);

        final JSONObject chatroomMsg = new JSONObject();
        chatroomMsg.put(User.USER_NAME, user);
        chatroomMsg.put(UserExt.USER_AVATAR_URL, AvatarQueryService.DEFAULT_AVATAR_URL);
        chatroomMsg.put(Common.CONTENT, msg);

        ChatRoomChannel.notifyChat(chatroomMsg);
        messages.addFirst(chatroomMsg);
        final int maxCnt = Hanfei.getInt("chatRoom.msgCnt");
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        ret.put(Keys.STATUS_CODE, true);
    }
}
