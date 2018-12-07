
package club.hanfei.event;

import club.hanfei.model.Article;
import club.hanfei.service.ArticleMgmtService;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

/**
 * Article update audio handler.
 *
@version 1.0.0.1, Nov 3, 2018
 * @since 2.1.0
 */
@Singleton
public class ArticleUpdateAudioHandler extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleUpdateAudioHandler.class);

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Gets the event type {@linkplain EventTypes#UPDATE_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.UPDATE_ARTICLE;
    }

    @Override
    public void action(final Event<JSONObject> event) {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event [type={0}, data={1}]", event.getType(), data);

        final JSONObject originalArticle = data.optJSONObject(Article.ARTICLE);
        articleMgmtService.genArticleAudio(originalArticle);
    }
}
