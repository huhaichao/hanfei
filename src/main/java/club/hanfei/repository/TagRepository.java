
package club.hanfei.repository;

import java.util.ArrayList;
import java.util.List;

import club.hanfei.cache.TagCache;
import club.hanfei.model.Tag;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.latke.util.URLs;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tag repository.
 *
@version 1.2.0.2, Aug 27, 2018
 * @since 0.2.0
 */
@Repository
public class TagRepository extends AbstractRepository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagRepository.class);

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Tag-Article relation repository.
     */
    @Inject
    private TagArticleRepository tagArticleRepository;

    /**
     * Public constructor.
     */
    public TagRepository() {
        super(Tag.TAG);
    }

    @Override
    public String add(final JSONObject tag) throws RepositoryException {
        final String ret = super.add(tag);

        tagCache.putTag(tag);

        return ret;
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        super.remove(id);

        tagCache.removeTag(id);
    }

    @Override
    public void update(final String id, final JSONObject article) throws RepositoryException {
        super.update(id, article);

        article.put(Keys.OBJECT_ID, id);
        tagCache.putTag(article);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = tagCache.getTag(id);
        if (null != ret) {
            return ret;
        }

        ret = super.get(id);

        if (null == ret) {
            return null;
        }

        tagCache.putTag(ret);

        return ret;
    }

    /**
     * Gets a tag URI with the specified tag title.
     *
     * @param title the specified tag title
     * @return tag URI, returns {@code null} if not found
     */
    public String getURIByTitle(final String title) {
        return tagCache.getURIByTitle(title);
    }

    /**
     * Gets a tag by the specified tag URI.
     *
     * @param tagURI the specified tag URI
     * @return a tag, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByURI(final String tagURI) throws RepositoryException {
        final String uri = URLs.encode(tagURI);
        final Query query = new Query().setFilter(new PropertyFilter(Tag.TAG_URI, FilterOperator.EQUAL, uri))
                .addSort(Tag.TAG_REFERENCE_CNT, SortDirection.DESCENDING)
                .setPageCount(1);
        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Gets a tag by the specified tag title.
     *
     * @param tagTitle the specified tag title
     * @return a tag, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTitle(final String tagTitle) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, tagTitle)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }

    /**
     * Gets most used tags with the specified number.
     *
     * @param num the specified number
     * @return a list of most used tags, returns an empty list if not found
     * @throws RepositoryException repository exception
     */
    public List<JSONObject> getMostUsedTags(final int num) throws RepositoryException {
        final Query query = new Query().addSort(Tag.TAG_REFERENCE_CNT, SortDirection.DESCENDING).
                setCurrentPageNum(1).setPageSize(num).setPageCount(1);

        return getList(query);
    }

    /**
     * Gets tags of an article specified by the article id.
     *
     * @param articleId the specified article id
     * @return a list of tags of the specified article, returns an empty list if not found
     * @throws RepositoryException repository exception
     */
    public List<JSONObject> getByArticleId(final String articleId) throws RepositoryException {
        final List<JSONObject> ret = new ArrayList<>();

        final List<JSONObject> tagArticleRelations = tagArticleRepository.getByArticleId(articleId);
        for (final JSONObject tagArticleRelation : tagArticleRelations) {
            final String tagId = tagArticleRelation.optString(Tag.TAG + "_" + Keys.OBJECT_ID);
            final JSONObject tag = get(tagId);

            ret.add(tag);
        }

        return ret;
    }
}
