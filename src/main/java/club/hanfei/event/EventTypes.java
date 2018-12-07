
package club.hanfei.event;

/**
 * Event types.
 *
@version 1.1.0.1, May 6, 2017
 * @since 0.2.0
 */
public final class EventTypes {

    /**
     * Indicates a add article event.
     */
    public static final String ADD_ARTICLE = "Add Article";

    /**
     * Indicates a update article event.
     */
    public static final String UPDATE_ARTICLE = "Update Article";

    /**
     * Indicates an add comment to article event.
     */
    public static final String ADD_COMMENT_TO_ARTICLE = "Add Comment";

    /**
     * Indicates a update article event.
     */
    public static final String UPDATE_COMMENT = "Update Comment";

    /**
     * Private constructor.
     */
    private EventTypes() {
    }
}
