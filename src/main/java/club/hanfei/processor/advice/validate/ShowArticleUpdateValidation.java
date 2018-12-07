
package club.hanfei.processor.advice.validate;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Article;
import club.hanfei.model.UserExt;
import club.hanfei.service.ArticleQueryService;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.json.JSONObject;

/**
 * Validates for show article update.
 *
@version 1.0.0.0, Mar 11, 2013
 * @since 0.2.0
 */
@Singleton
public class ShowArticleUpdateValidation extends ProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject article;
        try {
            final String articleId = request.getParameter("id");
            if (StringUtils.isBlank(articleId)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("updateArticleNotFoundLabel")));
            }

            article = articleQueryService.getArticleById(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId);
            if (null == article) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("updateArticleNotFoundLabel")));
            }
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        request.setAttribute(Article.ARTICLE, article);
    }
}
