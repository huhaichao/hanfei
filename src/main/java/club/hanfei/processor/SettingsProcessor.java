
package club.hanfei.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.model.Emotion;
import club.hanfei.model.Invitecode;
import club.hanfei.model.Notification;
import club.hanfei.model.Pointtransfer;
import club.hanfei.model.UserExt;
import club.hanfei.model.Verifycode;
import club.hanfei.processor.advice.CSRFCheck;
import club.hanfei.processor.advice.CSRFToken;
import club.hanfei.processor.advice.LoginCheck;
import club.hanfei.processor.advice.PermissionCheck;
import club.hanfei.processor.advice.PermissionGrant;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.processor.advice.validate.PointTransferValidation;
import club.hanfei.processor.advice.validate.UpdateEmotionListValidation;
import club.hanfei.processor.advice.validate.UpdatePasswordValidation;
import club.hanfei.processor.advice.validate.UpdateProfilesValidation;
import club.hanfei.service.AvatarQueryService;
import club.hanfei.service.DataModelService;
import club.hanfei.service.EmotionMgmtService;
import club.hanfei.service.EmotionQueryService;
import club.hanfei.service.InvitecodeMgmtService;
import club.hanfei.service.InvitecodeQueryService;
import club.hanfei.service.NotificationMgmtService;
import club.hanfei.service.OptionQueryService;
import club.hanfei.service.PointtransferMgmtService;
import club.hanfei.service.PostExportService;
import club.hanfei.service.RoleQueryService;
import club.hanfei.service.UserMgmtService;
import club.hanfei.service.UserQueryService;
import club.hanfei.service.VerifycodeMgmtService;
import club.hanfei.service.VerifycodeQueryService;
import club.hanfei.util.Hanfei;
import club.hanfei.util.Languages;
import club.hanfei.util.Results;
import club.hanfei.util.Sessions;
import com.qiniu.util.Auth;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.TimeZones;
import org.json.JSONObject;

/**
 * Settings processor.
 * <ul>
 * <li>Shows settings (/settings), GET</li>
 * <li>Shows settings pages (/settings/*), GET</li>
 * <li>Updates profiles (/settings/profiles), POST</li>
 * <li>Updates user avatar (/settings/avatar), POST</li>
 * <li>Geo status (/settings/geo/status), POST</li>
 * <li>Privacy (/settings/privacy), POST</li>
 * <li>Function (/settings/function), POST</li>
 * <li>Transfer point (/point/transfer), POST</li>
 * <li>Queries invitecode state (/invitecode/state), GET</li>
 * <li>Point buy invitecode (/point/buy-invitecode), POST</li>
 * <li>Exports posts(article/comment) to a file (/export/posts), POST</li>
 * <li>Updates emotions (/settings/emotionList), POST</li>
 * <li>Password (/settings/password), POST</li>
 * <li>Updates i18n (/settings/i18n), POST</li>
 * <li>Sends email verify code (/settings/email/vc), POST</li>
 * <li>Updates email (/settings/email), POST</li>
 * <li>Updates username (/settings/username), POST</li>
 * <li>Deactivates user (/settings/deactivate), POST</li>
 * </ul>
 *
@version 1.3.1.1, Nov 5, 2018
 * @since 2.4.0
 */
@RequestProcessor
public class SettingsProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SettingsProcessor.class);

    /**
     * Post export service.
     */
    @Inject
    private PostExportService postExportService;

    /**
     * Invitecode query service.
     */
    @Inject
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * Invitecode management service.
     */
    @Inject
    private InvitecodeMgmtService invitecodeMgmtService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Emotion query service.
     */
    @Inject
    private EmotionQueryService emotionQueryService;

    /**
     * Emotion management service.
     */
    @Inject
    private EmotionMgmtService emotionMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Verifycode query service.
     */
    @Inject
    private VerifycodeQueryService verifycodeQueryService;

    /**
     * Verifycode management service.
     */
    @Inject
    private VerifycodeMgmtService verifycodeMgmtService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Deactivates user.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/deactivate", method = HttpMethod.POST)
    @Before({LoginCheck.class})
    public void deactivateUser(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        try {
            userMgmtService.deactivateUser(currentUser.optString(Keys.OBJECT_ID));
            Sessions.logout(currentUser.optString(Keys.OBJECT_ID), response);

            context.renderTrueResult();
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates username.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/username", method = HttpMethod.POST)
    @Before({LoginCheck.class})
    public void updateUserName(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        try {
            if (currentUser.optInt(UserExt.USER_POINT) < Pointtransfer.TRANSFER_SUM_C_CHANGE_USERNAME) {
                throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
            }

            final JSONObject user = userQueryService.getUser(userId);
            final String oldName = user.optString(User.USER_NAME);
            final String newName = requestJSONObject.optString(User.USER_NAME);
            user.put(User.USER_NAME, newName);

            userMgmtService.updateUserName(userId, user);

            pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_CHANGE_USERNAME, Pointtransfer.TRANSFER_SUM_C_CHANGE_USERNAME,
                    oldName + "-" + newName, System.currentTimeMillis(), "");

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Sends email verify code.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/email/vc", method = HttpMethod.POST)
    @Before({LoginCheck.class})
    public void sendEmailVC(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String email = StringUtils.lowerCase(StringUtils.trim(requestJSONObject.optString(User.USER_EMAIL)));
        if (!Strings.isEmail(email)) {
            final String msg = langPropsService.get("sendFailedLabel") + " - " + langPropsService.get("invalidEmailLabel");
            context.renderMsg(msg);

            return;
        }

        final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
        if (CaptchaProcessor.invalidCaptcha(captcha)) {
            final String msg = langPropsService.get("sendFailedLabel") + " - " + langPropsService.get("captchaErrorLabel");
            context.renderMsg(msg);

            return;
        }

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (email.equalsIgnoreCase(user.optString(User.USER_EMAIL))) {
            final String msg = langPropsService.get("sendFailedLabel") + " - " + langPropsService.get("bindedLabel");
            context.renderMsg(msg);

            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        try {
            JSONObject verifycode = verifycodeQueryService.getVerifycodeByUserId(Verifycode.TYPE_C_EMAIL, Verifycode.BIZ_TYPE_C_BIND_EMAIL, userId);
            if (null != verifycode) {
                context.renderTrueResult().renderMsg(langPropsService.get("vcSentLabel"));

                return;
            }

            if (null != userQueryService.getUserByEmail(email)) {
                context.renderMsg(langPropsService.get("duplicatedEmailLabel"));

                return;
            }

            final String code = RandomStringUtils.randomNumeric(6);
            verifycode = new JSONObject();
            verifycode.put(Verifycode.USER_ID, userId);
            verifycode.put(Verifycode.BIZ_TYPE, Verifycode.BIZ_TYPE_C_BIND_EMAIL);
            verifycode.put(Verifycode.TYPE, Verifycode.TYPE_C_EMAIL);
            verifycode.put(Verifycode.CODE, code);
            verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_UNSENT);
            verifycode.put(Verifycode.EXPIRED, DateUtils.addMinutes(new Date(), 10).getTime());
            verifycode.put(Verifycode.RECEIVER, email);
            verifycodeMgmtService.addVerifycode(verifycode);

            context.renderTrueResult().renderMsg(langPropsService.get("verifycodeSentLabel"));
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates email.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/email", method = HttpMethod.POST)
    @Before({LoginCheck.class})
    public void updateEmail(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        try {
            final JSONObject verifycode = verifycodeQueryService.getVerifycodeByUserId(Verifycode.TYPE_C_EMAIL, Verifycode.BIZ_TYPE_C_BIND_EMAIL, userId);
            if (null == verifycode) {
                final String msg = langPropsService.get("updateFailLabel") + " - " + langPropsService.get("captchaErrorLabel");
                context.renderMsg(msg);
                context.renderJSONValue(Common.CODE, 2);

                return;
            }

            if (!StringUtils.equals(verifycode.optString(Verifycode.CODE), captcha)) {
                final String msg = langPropsService.get("updateFailLabel") + " - " + langPropsService.get("captchaErrorLabel");
                context.renderMsg(msg);
                context.renderJSONValue(Common.CODE, 2);

                return;
            }

            final JSONObject user = userQueryService.getUser(userId);
            final String email = verifycode.optString(Verifycode.RECEIVER);
            user.put(User.USER_EMAIL, email);
            userMgmtService.updateUserEmail(userId, user);
            verifycodeMgmtService.removeByCode(captcha);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user i18n.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/i18n", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class})
    public void updateI18n(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        String userLanguage = requestJSONObject.optString(UserExt.USER_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toString());
        if (!Languages.getAvailableLanguages().contains(userLanguage)) {
            userLanguage = Locale.US.toString();
        }

        String userTimezone = requestJSONObject.optString(UserExt.USER_TIMEZONE, TimeZone.getDefault().getID());
        if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(userTimezone)) {
            userTimezone = TimeZone.getDefault().getID();
        }

        try {
            JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
            final String userId = user.optString(Keys.OBJECT_ID);
            user = userQueryService.getUser(userId);

            user.put(UserExt.USER_LANGUAGE, userLanguage);
            user.put(UserExt.USER_TIMEZONE, userTimezone);

            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Shows settings pages.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = {"/settings", "/settings/*"}, method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class, LoginCheck.class})
    @After({CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showSettings(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        final String requestURI = request.getRequestURI();
        String page = StringUtils.substringAfter(requestURI, "/settings/");
        if (StringUtils.isBlank(page)) {
            page = "profile";
        }
        page += ".ftl";
        renderer.setTemplateName("home/settings/" + page);
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        UserProcessor.fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String userId = user.optString(Keys.OBJECT_ID);

        final int invitedUserCount = userQueryService.getInvitedUserCount(userId);
        dataModel.put(Common.INVITED_USER_COUNT, invitedUserCount);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Hanfei.get("qiniu.accessKey"), Hanfei.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Hanfei.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Hanfei.get("qiniu.domain"));

        if (!Hanfei.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Hanfei.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Hanfei.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String inviteTipLabel = (String) dataModel.get("inviteTipLabel");
        inviteTipLabel = inviteTipLabel.replace("{point}", String.valueOf(Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER));
        dataModel.put("inviteTipLabel", inviteTipLabel);

        String pointTransferTipLabel = (String) dataModel.get("pointTransferTipLabel");
        pointTransferTipLabel = pointTransferTipLabel.replace("{point}", Hanfei.get("pointTransferMin"));
        dataModel.put("pointTransferTipLabel", pointTransferTipLabel);

        String dataExportTipLabel = (String) dataModel.get("dataExportTipLabel");
        dataExportTipLabel = dataExportTipLabel.replace("{point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_DATA_EXPORT));
        dataModel.put("dataExportTipLabel", dataExportTipLabel);

        final String allowRegister = optionQueryService.getAllowRegister();
        dataModel.put("allowRegister", allowRegister);

        String buyInvitecodeLabel = langPropsService.get("buyInvitecodeLabel");
        buyInvitecodeLabel = buyInvitecodeLabel.replace("${point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_BUY_INVITECODE));
        buyInvitecodeLabel = buyInvitecodeLabel.replace("${point2}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_INVITECODE_USED));
        dataModel.put("buyInvitecodeLabel", buyInvitecodeLabel);

        String updateNameTipLabel = (String) dataModel.get("updateNameTipLabel");
        updateNameTipLabel = updateNameTipLabel.replace("{point}", Hanfei.get("pointChangeUsername"));
        dataModel.put("updateNameTipLabel", updateNameTipLabel);

        final List<JSONObject> invitecodes = invitecodeQueryService.getValidInvitecodes(userId);
        for (final JSONObject invitecode : invitecodes) {
            String msg = langPropsService.get("expireTipLabel");
            msg = msg.replace("${time}", DateFormatUtils.format(invitecode.optLong(Keys.OBJECT_ID)
                    + Hanfei.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));
            invitecode.put(Common.MEMO, msg);
        }

        dataModel.put(Invitecode.INVITECODES, invitecodes);

        if (requestURI.contains("function")) {
            dataModel.put(Emotion.EMOTIONS, emotionQueryService.getEmojis(userId));
            dataModel.put(Emotion.SHORT_T_LIST, emojiLists);
        }

        if (requestURI.contains("i18n")) {
            dataModel.put(Common.LANGUAGES, Languages.getAvailableLanguages());

            final List<JSONObject> timezones = new ArrayList<>();
            final List<TimeZones.TimeZoneWithDisplayNames> timeZones = TimeZones.getInstance().getTimeZones();
            for (final TimeZones.TimeZoneWithDisplayNames timeZone : timeZones) {
                final JSONObject timezone = new JSONObject();

                timezone.put(Common.ID, timeZone.getTimeZone().getID());
                timezone.put(Common.NAME, timeZone.getDisplayName());

                timezones.add(timezone);
            }
            dataModel.put(Common.TIMEZONES, timezones);
        }

        dataModel.put(Common.TYPE, "settings");
    }

    /**
     * Updates user geo status.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/geo/status", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class})
    public void updateGeoStatus(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        int geoStatus = requestJSONObject.optInt(UserExt.USER_GEO_STATUS);
        if (UserExt.USER_GEO_STATUS_C_PRIVATE != geoStatus && UserExt.USER_GEO_STATUS_C_PUBLIC != geoStatus) {
            geoStatus = UserExt.USER_GEO_STATUS_C_PUBLIC;
        }

        try {
            JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
            final String userId = user.optString(Keys.OBJECT_ID);
            user = userQueryService.getUser(userId);
            user.put(UserExt.USER_GEO_STATUS, geoStatus);

            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user privacy.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/privacy", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class})
    public void updatePrivacy(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        final boolean articleStatus = requestJSONObject.optBoolean(UserExt.USER_ARTICLE_STATUS);
        final boolean commentStatus = requestJSONObject.optBoolean(UserExt.USER_COMMENT_STATUS);
        final boolean followingUserStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWING_USER_STATUS);
        final boolean followingTagStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWING_TAG_STATUS);
        final boolean followingArticleStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWING_ARTICLE_STATUS);
        final boolean watchingArticleStatus = requestJSONObject.optBoolean(UserExt.USER_WATCHING_ARTICLE_STATUS);
        final boolean followerStatus = requestJSONObject.optBoolean(UserExt.USER_FOLLOWER_STATUS);
        final boolean breezemoonStatus = requestJSONObject.optBoolean(UserExt.USER_BREEZEMOON_STATUS);
        final boolean pointStatus = requestJSONObject.optBoolean(UserExt.USER_POINT_STATUS);
        final boolean onlineStatus = requestJSONObject.optBoolean(UserExt.USER_ONLINE_STATUS);
        final boolean uaStatus = requestJSONObject.optBoolean(UserExt.USER_UA_STATUS);
        final boolean userJoinPointRank = requestJSONObject.optBoolean(UserExt.USER_JOIN_POINT_RANK);
        final boolean userJoinUsedPointRank = requestJSONObject.optBoolean(UserExt.USER_JOIN_USED_POINT_RANK);

        JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = user.optString(Keys.OBJECT_ID);
        user = userQueryService.getUser(userId);

        user.put(UserExt.USER_ONLINE_STATUS, onlineStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_ARTICLE_STATUS, articleStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_COMMENT_STATUS, commentStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWING_USER_STATUS, followingUserStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWING_TAG_STATUS, followingTagStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWING_ARTICLE_STATUS, followingArticleStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_WATCHING_ARTICLE_STATUS, watchingArticleStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_FOLLOWER_STATUS, followerStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_BREEZEMOON_STATUS, breezemoonStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_POINT_STATUS, pointStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_UA_STATUS, uaStatus
                ? UserExt.USER_XXX_STATUS_C_PUBLIC : UserExt.USER_XXX_STATUS_C_PRIVATE);
        user.put(UserExt.USER_JOIN_POINT_RANK, userJoinPointRank
                ? UserExt.USER_JOIN_POINT_RANK_C_JOIN : UserExt.USER_JOIN_POINT_RANK_C_NOT_JOIN);
        user.put(UserExt.USER_JOIN_USED_POINT_RANK, userJoinUsedPointRank
                ? UserExt.USER_JOIN_USED_POINT_RANK_C_JOIN : UserExt.USER_JOIN_USED_POINT_RANK_C_NOT_JOIN);

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user function.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/function", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class})
    public void updateFunction(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        String userListPageSizeStr = requestJSONObject.optString(UserExt.USER_LIST_PAGE_SIZE);
        final int userCommentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        final int userAvatarViewMode = requestJSONObject.optInt(UserExt.USER_AVATAR_VIEW_MODE);
        final int userListViewMode = requestJSONObject.optInt(UserExt.USER_LIST_VIEW_MODE);
        final boolean notifyStatus = requestJSONObject.optBoolean(UserExt.USER_NOTIFY_STATUS);
        final boolean subMailStatus = requestJSONObject.optBoolean(UserExt.USER_SUB_MAIL_STATUS);
        final boolean keyboardShortcutsStatus = requestJSONObject.optBoolean(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS);
        final boolean userReplyWatchArticleStatus = requestJSONObject.optBoolean(UserExt.USER_REPLY_WATCH_ARTICLE_STATUS);
        final boolean forwardStatus = requestJSONObject.optBoolean(UserExt.USER_FORWARD_PAGE_STATUS);
        String indexRedirectURL = requestJSONObject.optString(UserExt.USER_INDEX_REDIRECT_URL);
        if (!Strings.isURL(indexRedirectURL)) {
            indexRedirectURL = "";
        }
        if (StringUtils.isNotBlank(indexRedirectURL) && !StringUtils.startsWith(indexRedirectURL, Latkes.getServePath())) {
            context.renderMsg(langPropsService.get("onlyInternalURLLabel"));

            return;
        }
        if (StringUtils.isNotBlank(indexRedirectURL)) {
            String tmp = StringUtils.substringBefore(indexRedirectURL, "?");
            if (StringUtils.endsWith(tmp, "/")) {
                tmp = StringUtils.substringBeforeLast(tmp, "/");
            }
            if (StringUtils.equalsIgnoreCase(tmp, Latkes.getServePath())) {
                indexRedirectURL = "";
            }
        }

        int userListPageSize;
        try {
            userListPageSize = Integer.valueOf(userListPageSizeStr);
            if (10 > userListPageSize) {
                userListPageSize = 10;
            }
            if (userListPageSize > 96) {
                userListPageSize = 96;
            }
        } catch (final Exception e) {
            userListPageSize = Hanfei.getInt("indexArticlesCnt");
        }

        JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = user.optString(Keys.OBJECT_ID);
        user = userQueryService.getUser(userId);

        user.put(UserExt.USER_LIST_PAGE_SIZE, userListPageSize);
        user.put(UserExt.USER_COMMENT_VIEW_MODE, userCommentViewMode);
        user.put(UserExt.USER_AVATAR_VIEW_MODE, userAvatarViewMode);
        user.put(UserExt.USER_LIST_VIEW_MODE, userListViewMode);
        user.put(UserExt.USER_NOTIFY_STATUS, notifyStatus ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_SUB_MAIL_STATUS, subMailStatus ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_KEYBOARD_SHORTCUTS_STATUS, keyboardShortcutsStatus ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_REPLY_WATCH_ARTICLE_STATUS, userReplyWatchArticleStatus ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_FORWARD_PAGE_STATUS, forwardStatus ? UserExt.USER_XXX_STATUS_C_ENABLED : UserExt.USER_XXX_STATUS_C_DISABLED);
        user.put(UserExt.USER_INDEX_REDIRECT_URL, indexRedirectURL);

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user profiles.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/profiles", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class, UpdateProfilesValidation.class})
    public void updateProfiles(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String userTags = requestJSONObject.optString(UserExt.USER_TAGS);
        final String userURL = requestJSONObject.optString(User.USER_URL);
        final String userQQ = requestJSONObject.optString(UserExt.USER_QQ);
        final String userIntro = requestJSONObject.optString(UserExt.USER_INTRO);
        final String userNickname = requestJSONObject.optString(UserExt.USER_NICKNAME);

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        user.put(UserExt.USER_TAGS, userTags);
        user.put(User.USER_URL, userURL);
        user.put(UserExt.USER_QQ, userQQ);
        user.put(UserExt.USER_INTRO, userIntro);
        user.put(UserExt.USER_NICKNAME, userNickname);
        user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);

        try {
            userMgmtService.updateProfiles(user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user avatar.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/avatar", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class, UpdateProfilesValidation.class})
    public void updateAvatar(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String userAvatarURL = requestJSONObject.optString(UserExt.USER_AVATAR_URL);

        JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = user.optString(Keys.OBJECT_ID);
        user = userQueryService.getUser(userId);
        user.put(UserExt.USER_AVATAR_TYPE, UserExt.USER_AVATAR_TYPE_C_UPLOAD);
        user.put(UserExt.USER_UPDATE_TIME, System.currentTimeMillis());

        if (Strings.contains(userAvatarURL, new String[]{"<", ">", "\"", "'"})) {
            user.put(UserExt.USER_AVATAR_URL, AvatarQueryService.DEFAULT_AVATAR_URL);
        } else {
            if (Hanfei.getBoolean("qiniu.enabled")) {
                final String qiniuDomain = Hanfei.get("qiniu.domain");

                if (!StringUtils.startsWith(userAvatarURL, qiniuDomain)) {
                    user.put(UserExt.USER_AVATAR_URL, AvatarQueryService.DEFAULT_AVATAR_URL);
                } else {
                    user.put(UserExt.USER_AVATAR_URL, userAvatarURL);
                }
            } else {
                user.put(UserExt.USER_AVATAR_URL, userAvatarURL);
            }
        }

        try {
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates user password.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/password", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class, UpdatePasswordValidation.class})
    public void updatePassword(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String newPassword = requestJSONObject.optString(User.USER_NEW_PASSWORD);

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (!password.equals(user.optString(User.USER_PASSWORD))) {
            context.renderMsg(langPropsService.get("invalidOldPwdLabel"));

            return;
        }

        user.put(User.USER_PASSWORD, newPassword);

        try {
            userMgmtService.updatePassword(user);
            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }
    }

    /**
     * Updates user emotions.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/settings/emotionList", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class, UpdateEmotionListValidation.class})
    public void updateEmoji(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String emotionList = requestJSONObject.optString(Emotion.EMOTIONS);

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        try {
            emotionMgmtService.setEmotionList(user.optString(Keys.OBJECT_ID), emotionList);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }
    }

    /**
     * Point transfer.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/point/transfer", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class, PointTransferValidation.class})
    public void pointTransfer(final RequestContext context) {
        final JSONObject ret = Results.falseResult();
        context.renderJSON(ret);

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        final JSONObject toUser = (JSONObject) request.getAttribute(Common.TO_USER);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        String memo = (String) request.getAttribute(Pointtransfer.MEMO);
        if (StringUtils.isBlank(memo)) {
            memo = "";
        }

        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final String toId = toUser.optString(Keys.OBJECT_ID);

        final String transferId = pointtransferMgmtService.transfer(fromId, toId,
                Pointtransfer.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT, amount, toId, System.currentTimeMillis(), memo);
        final boolean succ = null != transferId;
        ret.put(Keys.STATUS_CODE, succ);
        if (!succ) {
            ret.put(Keys.MSG, langPropsService.get("transferFailLabel"));
        } else {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, toId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointTransferNotification(notification);
        }
    }

    /**
     * Queries invitecode state.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/invitecode/state", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class})
    public void queryInvitecode(final RequestContext context) {
        final JSONObject ret = Results.falseResult();
        context.renderJSON(ret);

        final JSONObject requestJSONObject = context.requestJSON();
        String invitecode = requestJSONObject.optString(Invitecode.INVITECODE);
        if (StringUtils.isBlank(invitecode)) {
            ret.put(Keys.STATUS_CODE, -1);
            ret.put(Keys.MSG, invitecode + " " + langPropsService.get("notFoundInvitecodeLabel"));

            return;
        }

        invitecode = invitecode.trim();

        final JSONObject result = invitecodeQueryService.getInvitecode(invitecode);

        if (null == result) {
            ret.put(Keys.STATUS_CODE, -1);
            ret.put(Keys.MSG, langPropsService.get("notFoundInvitecodeLabel"));
        } else {
            final int status = result.optInt(Invitecode.STATUS);
            ret.put(Keys.STATUS_CODE, status);

            switch (status) {
                case Invitecode.STATUS_C_USED:
                    ret.put(Keys.MSG, langPropsService.get("invitecodeUsedLabel"));

                    break;
                case Invitecode.STATUS_C_UNUSED:
                    String msg = langPropsService.get("invitecodeOkLabel");
                    msg = msg.replace("${time}", DateFormatUtils.format(result.optLong(Keys.OBJECT_ID)
                            + Hanfei.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));

                    ret.put(Keys.MSG, msg);

                    break;
                case Invitecode.STATUS_C_STOPUSE:
                    ret.put(Keys.MSG, langPropsService.get("invitecodeStopLabel"));

                    break;
                default:
                    ret.put(Keys.MSG, langPropsService.get("notFoundInvitecodeLabel"));
            }
        }
    }

    /**
     * Point buy invitecode.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/point/buy-invitecode", method = HttpMethod.POST)
    @Before({LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    public void pointBuy(final RequestContext context) {
        final JSONObject ret = Results.falseResult();
        context.renderJSON(ret);

        final HttpServletRequest request = context.getRequest();
        final String allowRegister = optionQueryService.getAllowRegister();
        if (!"2".equals(allowRegister)) {
            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final String userName = currentUser.optString(User.USER_NAME);

        // 故意先生成后返回校验，所以即使积分不够也是可以兑换成功的
        // 这是为了让积分不够的用户可以通过这个后门兑换、分发邀请码以实现积分“自充”
        // 后期可能会关掉这个【特性】
        final String invitecode = invitecodeMgmtService.userGenInvitecode(fromId, userName);

        final String transferId = pointtransferMgmtService.transfer(fromId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_BUY_INVITECODE, Pointtransfer.TRANSFER_SUM_C_BUY_INVITECODE,
                invitecode, System.currentTimeMillis(), "");
        final boolean succ = null != transferId;
        ret.put(Keys.STATUS_CODE, succ);
        if (!succ) {
            ret.put(Keys.MSG, langPropsService.get("exchangeFailedLabel"));
        } else {
            String msg = langPropsService.get("expireTipLabel");
            msg = msg.replace("${time}", DateFormatUtils.format(System.currentTimeMillis()
                    + Hanfei.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));
            ret.put(Keys.MSG, invitecode + " " + msg);
        }
    }

    /**
     * Exports posts(article/comment) to a file.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/export/posts", method = HttpMethod.POST)
    @Before({LoginCheck.class})
    public void exportPosts(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        final String downloadURL = postExportService.exportPosts(userId);
        if ("-1".equals(downloadURL)) {
            context.renderJSONValue(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));

        } else if (StringUtils.isBlank(downloadURL)) {
            return;
        }

        context.renderJSON(true).renderJSONValue("url", downloadURL);
    }

    private static final String[][] emojiLists = {{
            "smile",
            "laughing",
            "smirk",
            "heart_eyes",
            "kissing_heart",
            "flushed",
            "grin",
            "stuck_out_tongue_closed_eyes",
            "kissing",
            "sleeping",
            "anguished",
            "open_mouth",
            "expressionless",
            "unamused",
            "sweat_smile",
            "weary",
            "sob",
            "joy",
            "astonished",
            "scream"
    }, {
            "tired_face",
            "rage",
            "triumph",
            "yum",
            "mask",
            "sunglasses",
            "dizzy_face",
            "imp",
            "smiling_imp",
            "innocent",
            "alien",
            "yellow_heart",
            "blue_heart",
            "purple_heart",
            "heart",
            "green_heart",
            "broken_heart",
            "dizzy",
            "anger",
            "exclamation"
    }, {
            "question",
            "zzz",
            "notes",
            "poop",
            "+1",
            "-1",
            "ok_hand",
            "punch",
            "v",
            "hand",
            "point_up",
            "point_down",
            "pray",
            "clap",
            "muscle",
            "ok_woman",
            "no_good",
            "raising_hand",
            "massage",
            "haircut"
    }, {
            "nail_care",
            "see_no_evil",
            "feet",
            "kiss",
            "eyes",
            "trollface",
            "snowman",
            "zap",
            "cat",
            "dog",
            "mouse",
            "hamster",
            "rabbit",
            "frog",
            "koala",
            "pig",
            "monkey",
            "racehorse",
            "camel",
            "sheep"
    }, {
            "elephant",
            "panda_face",
            "snake",
            "hatched_chick",
            "hatching_chick",
            "turtle",
            "bug",
            "honeybee",
            "beetle",
            "snail",
            "octopus",
            "whale",
            "dolphin",
            "dragon",
            "goat",
            "paw_prints",
            "tulip",
            "four_leaf_clover",
            "rose",
            "mushroom"
    }, {
            "seedling",
            "shell",
            "crescent_moon",
            "partly_sunny",
            "octocat",
            "jack_o_lantern",
            "ghost",
            "santa",
            "tada",
            "camera",
            "loudspeaker",
            "hourglass",
            "lock",
            "key",
            "bulb",
            "hammer",
            "moneybag",
            "smoking",
            "bomb",
            "gun"
    }, {
            "hocho",
            "pill",
            "syringe",
            "scissors",
            "swimmer",
            "black_joker",
            "coffee",
            "tea",
            "sake",
            "beer",
            "wine_glass",
            "pizza",
            "hamburger",
            "poultry_leg",
            "meat_on_bone",
            "dango",
            "doughnut",
            "icecream",
            "shaved_ice",
            "cake"
    }, {
            "cookie",
            "lollipop",
            "apple",
            "green_apple",
            "tangerine",
            "lemon",
            "cherries",
            "grapes",
            "watermelon",
            "strawberry",
            "peach",
            "melon",
            "banana",
            "pear",
            "pineapple",
            "sweet_potato",
            "eggplant",
            "tomato",
            Emotion.EOF_EMOJI // 标记结束以便在function.ftl中处理
    }};
}
