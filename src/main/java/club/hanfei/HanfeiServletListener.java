
package club.hanfei;

import java.util.Locale;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import club.hanfei.cache.DomainCache;
import club.hanfei.cache.TagCache;
import club.hanfei.event.ArticleAddAudioHandler;
import club.hanfei.event.ArticleAddNotifier;
import club.hanfei.event.ArticleBaiduSender;
import club.hanfei.event.ArticleQQSender;
import club.hanfei.event.ArticleSearchAdder;
import club.hanfei.event.ArticleSearchUpdater;
import club.hanfei.event.ArticleUpdateAudioHandler;
import club.hanfei.event.ArticleUpdateNotifier;
import club.hanfei.event.CommentNotifier;
import club.hanfei.event.CommentUpdateNotifier;
import club.hanfei.model.Common;
import club.hanfei.model.Option;
import club.hanfei.model.UserExt;
import club.hanfei.repository.OptionRepository;
import club.hanfei.repository.UserRepository;
import club.hanfei.service.CronMgmtService;
import club.hanfei.service.InitMgmtService;
import club.hanfei.service.UserQueryService;
import club.hanfei.util.Hanfei;
import club.hanfei.util.Sessions;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.Crypts;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.StaticResources;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.json.JSONObject;

/**
 *
 */
public final class HanfeiServletListener extends AbstractServletListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(HanfeiServletListener.class);

    /**
     * Symphony version.
     */
    public static final String VERSION = "3.4.4";

    /**
     * Bean manager.
     */
    private BeanManager beanManager;

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        LOGGER.log(Level.INFO, "Sym process [pid=" + Hanfei.currentPID() + "]");
        Stopwatchs.start("Context Initialized");
        Latkes.USER_AGENT = Hanfei.USER_AGENT_BOT;
        Latkes.setScanPath("club.hanfei");
        super.contextInitialized(servletContextEvent);

        beanManager = BeanManager.getInstance();

        final InitMgmtService initMgmtService = beanManager.getReference(InitMgmtService.class);
        initMgmtService.initSym();

        // Register event listeners
        final EventManager eventManager = beanManager.getReference(EventManager.class);

        final ArticleAddNotifier articleAddNotifier = beanManager.getReference(ArticleAddNotifier.class);
        eventManager.registerListener(articleAddNotifier);

        final ArticleUpdateNotifier articleUpdateNotifier = beanManager.getReference(ArticleUpdateNotifier.class);
        eventManager.registerListener(articleUpdateNotifier);

        final ArticleBaiduSender articleBaiduSender = beanManager.getReference(ArticleBaiduSender.class);
        eventManager.registerListener(articleBaiduSender);

        final ArticleQQSender articleQQSender = beanManager.getReference(ArticleQQSender.class);
        eventManager.registerListener(articleQQSender);

        final CommentNotifier commentNotifier = beanManager.getReference(CommentNotifier.class);
        eventManager.registerListener(commentNotifier);

        final CommentUpdateNotifier commentUpdateNotifier = beanManager.getReference(CommentUpdateNotifier.class);
        eventManager.registerListener(commentUpdateNotifier);

        final ArticleSearchAdder articleSearchAdder = beanManager.getReference(ArticleSearchAdder.class);
        eventManager.registerListener(articleSearchAdder);

        final ArticleSearchUpdater articleSearchUpdater = beanManager.getReference(ArticleSearchUpdater.class);
        eventManager.registerListener(articleSearchUpdater);

        final ArticleAddAudioHandler articleAddAudioHandler = beanManager.getReference(ArticleAddAudioHandler.class);
        eventManager.registerListener(articleAddAudioHandler);

        final ArticleUpdateAudioHandler articleUpdateAudioHandler = beanManager.getReference(ArticleUpdateAudioHandler.class);
        eventManager.registerListener(articleUpdateAudioHandler);

        final TagCache tagCache = beanManager.getReference(TagCache.class);
        tagCache.loadTags();

        final DomainCache domainCache = beanManager.getReference(DomainCache.class);
        domainCache.loadDomains();

        final CronMgmtService cronMgmtService = beanManager.getReference(CronMgmtService.class);
        cronMgmtService.start();

        LOGGER.info("Initialized the context");

        Stopwatchs.end();
        LOGGER.log(Level.DEBUG, "Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
        Stopwatchs.release();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        Hanfei.SCHEDULED_EXECUTOR_SERVICE.shutdown();
        Hanfei.EXECUTOR_SERVICE.shutdown();

        LOGGER.info("Destroyed the context");
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
        super.sessionDestroyed(httpSessionEvent);
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        Locales.setLocale(Latkes.getLocale());

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequestEvent.getServletRequest();

        httpServletRequest.setAttribute(Keys.TEMAPLTE_DIR_NAME, Hanfei.get("skinDirName"));
        httpServletRequest.setAttribute(Common.IS_MOBILE, false);

        httpServletRequest.setAttribute(UserExt.USER_AVATAR_VIEW_MODE, UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL);

        final String userAgentStr = httpServletRequest.getHeader(Common.USER_AGENT);

        final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
        BrowserType browserType = userAgent.getBrowser().getBrowserType();

        if (StringUtils.containsIgnoreCase(userAgentStr, "mobile")
                || StringUtils.containsIgnoreCase(userAgentStr, "MQQBrowser")
                || StringUtils.containsIgnoreCase(userAgentStr, "iphone")
                || StringUtils.containsIgnoreCase(userAgentStr, "MicroMessenger")
                || StringUtils.containsIgnoreCase(userAgentStr, "CFNetwork")
                || StringUtils.containsIgnoreCase(userAgentStr, "Android")) {
            browserType = BrowserType.MOBILE_BROWSER;
        } else if (StringUtils.containsIgnoreCase(userAgentStr, "Iframely")
                || StringUtils.containsIgnoreCase(userAgentStr, "Google")
                || StringUtils.containsIgnoreCase(userAgentStr, "BUbiNG")
                || StringUtils.containsIgnoreCase(userAgentStr, "ltx71")) {
            browserType = BrowserType.ROBOT;
        } else if (BrowserType.UNKNOWN == browserType) {
            if (!StringUtils.containsIgnoreCase(userAgentStr, "Java")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "MetaURI")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "Feed")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "okhttp")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "Sym")) {
                LOGGER.log(Level.WARN, "Unknown client [UA=" + userAgentStr + ", remoteAddr="
                        + Requests.getRemoteAddr(httpServletRequest) + ", URI=" + httpServletRequest.getRequestURI() + "]");
            }
        }

        if (BrowserType.ROBOT == browserType) {
            LOGGER.log(Level.DEBUG, "Request made from a search engine [User-Agent={0}]",
                    httpServletRequest.getHeader(Common.USER_AGENT));
            httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, true);

            return;
        }

        httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, false);

        if (StaticResources.isStatic(httpServletRequest)) {
            return;
        }

        Stopwatchs.start("Request initialized [" + httpServletRequest.getRequestURI() + "]");

        httpServletRequest.setAttribute(Common.IS_MOBILE, BrowserType.MOBILE_BROWSER == browserType);

        resolveSkinDir(httpServletRequest);
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        Locales.setLocale(null);

        try {
            super.requestDestroyed(servletRequestEvent);

            final HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
            final Object isStaticObj = request.getAttribute(Keys.HttpRequest.IS_REQUEST_STATIC_RESOURCE);
            if (null != isStaticObj && !(Boolean) isStaticObj) {
                Stopwatchs.end();

                final int threshold = Hanfei.getInt("performance.threshold");
                if (0 < threshold) {
                    final long elapsed = Stopwatchs.getElapsed("Request initialized [" + request.getRequestURI() + "]");
                    if (elapsed >= threshold) {
                        LOGGER.log(Level.INFO, "Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
                    }
                }
            }
        } finally {
            Stopwatchs.release();
        }
    }


    /**
     * Resolve skin (template) for the specified HTTP servlet request.
     *
     * @param request the specified HTTP servlet request
     */
    private void resolveSkinDir(final HttpServletRequest request) {
        Stopwatchs.start("Resolve skin");

        request.setAttribute(Keys.TEMAPLTE_DIR_NAME, (Boolean) request.getAttribute(Common.IS_MOBILE)
                ? "mobile" : "classic");
        String templateDirName = (Boolean) request.getAttribute(Common.IS_MOBILE) ? "mobile" : "classic";
        request.setAttribute(Keys.TEMAPLTE_DIR_NAME, templateDirName);

        final HttpSession httpSession = request.getSession();
        httpSession.setAttribute(Keys.TEMAPLTE_DIR_NAME, templateDirName);

        try {
            final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
            final UserRepository userRepository = beanManager.getReference(UserRepository.class);
            final OptionRepository optionRepository = beanManager.getReference(OptionRepository.class);

            final JSONObject optionLang = optionRepository.get(Option.ID_C_MISC_LANGUAGE);
            final String optionLangValue = optionLang.optString(Option.OPTION_VALUE);
            if ("0".equals(optionLangValue)) {
                Locales.setLocale(request.getLocale());
            } else {
                Locales.setLocale(Locales.getLocale(optionLangValue));
            }

            JSONObject user = userQueryService.getCurrentUser(request);
            if (null == user) {
                final Cookie[] cookies = request.getCookies();
                if (null == cookies || 0 == cookies.length) {
                    return;
                }

                try {
                    for (final Cookie cookie : cookies) {
                        if (!Sessions.COOKIE_NAME.equals(cookie.getName())) {
                            continue;
                        }

                        final String value = Crypts.decryptByAES(cookie.getValue(), Hanfei.get("cookie.secret"));
                        if (StringUtils.isBlank(value)) {
                            break;
                        }

                        final JSONObject cookieJSONObject = new JSONObject(value);

                        final String userId = cookieJSONObject.optString(Keys.OBJECT_ID);
                        if (StringUtils.isBlank(userId)) {
                            break;
                        }

                        user = userRepository.get(userId);
                        if (null == user) {
                            return;
                        } else {
                            break;
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Read cookie failed", e);
                }

                if (null == user) {
                    return;
                }
            }

            final String skin = (Boolean) request.getAttribute(Common.IS_MOBILE)
                    ? user.optString(UserExt.USER_MOBILE_SKIN) : user.optString(UserExt.USER_SKIN);

            request.setAttribute(Keys.TEMAPLTE_DIR_NAME, skin);
            httpSession.setAttribute(Keys.TEMAPLTE_DIR_NAME, skin);
            request.setAttribute(UserExt.USER_AVATAR_VIEW_MODE, user.optInt(UserExt.USER_AVATAR_VIEW_MODE));

            request.setAttribute(Common.CURRENT_USER, user);

            final Locale locale = Locales.getLocale(user.optString(UserExt.USER_LANGUAGE));
            Locales.setLocale(locale);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Resolves skin failed", e);
        } finally {
            Stopwatchs.end();
        }
    }
}
