
package club.hanfei.event;

import club.hanfei.model.Article;
import club.hanfei.model.Common;
import club.hanfei.model.Tag;
import club.hanfei.util.Hanfei;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.net.MimeTypes;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

/**
 * Sends an article URL to Baidu.
 *
@version 1.1.3.3, Nov 3, 2018
 * @since 1.3.0
 */
@Singleton
public class ArticleBaiduSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleBaiduSender.class);

    /**
     * Baidu data token.
     */
    private static final String TOKEN = Hanfei.get("baidu.data.token");

    /**
     * Sends the specified URLs to Baidu.
     *
     * @param urls the specified URLs
     */
    public static void sendToBaidu(final String... urls) {
        if (ArrayUtils.isEmpty(urls)) {
            return;
        }

        Hanfei.EXECUTOR_SERVICE.submit(() -> {
            try {
                final String urlsStr = StringUtils.join(urls, "\n");
                final HttpResponse response = HttpRequest.post("http://data.zz.baidu.com/urls?site=" + Latkes.getServerHost() + "&token=" + TOKEN).
                        header(Common.USER_AGENT, "curl/7.12.1").
                        header("Host", "data.zz.baidu.com").
                        header("Content-Type", "text/plain").
                        header("Connection", "close").body(urlsStr.getBytes(), MimeTypes.MIME_TEXT_PLAIN).timeout(30000).send();
                response.charset("UTF-8");
                LOGGER.info("Sent [" + urlsStr + "] to Baidu [response=" + response.bodyText() + "]");
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Ping Baidu spider failed", e);
            }
        });
    }

    @Override
    public void action(final Event<JSONObject> event) {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event [type={0}, data={1}]", event.getType(), data);

        if (Latkes.RuntimeMode.PRODUCTION != Latkes.getRuntimeMode() || StringUtils.isBlank(TOKEN)) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String tags = article.optString(Article.ARTICLE_TAGS);
            if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
                return;
            }

            final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);

            sendToBaidu(articlePermalink);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article to Baidu error", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#ADD_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.ADD_ARTICLE;
    }
}
