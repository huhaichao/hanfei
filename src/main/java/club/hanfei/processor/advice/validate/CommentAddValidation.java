
package club.hanfei.processor.advice.validate;

import javax.servlet.http.HttpServletRequest;

import club.hanfei.model.Article;
import club.hanfei.model.Comment;
import club.hanfei.model.UserExt;
import club.hanfei.service.ArticleQueryService;
import club.hanfei.service.CommentQueryService;
import club.hanfei.service.OptionQueryService;
import club.hanfei.util.StatusCodes;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.json.JSONObject;

/**
 * Validates for comment adding locally.
 *
@version 1.3.0.2, Mar 9, 2017
 * @since 0.2.0
 */
@Singleton
public class CommentAddValidation extends ProcessAdvice {

    /**
     * Max comment content length.
     */
    public static final int MAX_COMMENT_CONTENT_LENGTH = 2000;
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;
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
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Validates comment fields.
     *
     * @param requestJSONObject the specified request object
     * @throws RequestProcessAdviceException if validate failed
     */
    public static void validateCommentFields(final JSONObject requestJSONObject) throws RequestProcessAdviceException {
        final BeanManager beanManager = BeanManager.getInstance();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);
        final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);
        final ArticleQueryService articleQueryService = beanManager.getReference(ArticleQueryService.class);
        final CommentQueryService commentQueryService = beanManager.getReference(CommentQueryService.class);

        final JSONObject exception = new JSONObject();
        exception.put(Keys.STATUS_CODE, StatusCodes.ERR);

        final String commentContent = StringUtils.trim(requestJSONObject.optString(Comment.COMMENT_CONTENT));
        if (StringUtils.isBlank(commentContent) || commentContent.length() > MAX_COMMENT_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentErrorLabel")));
        }

        if (optionQueryService.containReservedWord(commentContent)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
        }

        final JSONObject article = articleQueryService.getArticleById(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId);
        if (null == article) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
        }

        if (!article.optBoolean(Article.ARTICLE_COMMENTABLE)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("notAllowCmtLabel")));
        }

        final String originalCommentId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
        if (StringUtils.isNotBlank(originalCommentId)) {
            final JSONObject originalCmt = commentQueryService.getComment(originalCommentId);
            if (null == originalCmt) {
                throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            }
        }
    }

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()).
                    put(Keys.STATUS_CODE, StatusCodes.ERR));
        }

        validateCommentFields(requestJSONObject);
    }
}
