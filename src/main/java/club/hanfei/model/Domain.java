
package club.hanfei.model;

/**
 * This class defines domain model relevant keys.
 *
@version 1.1.0.0, Mar 30, 2018
 * @since 1.4.0
 */
public final class Domain {

    /**
     * Domain.
     */
    public static final String DOMAIN = "domain";

    /**
     * Domains.
     */
    public static final String DOMAINS = "domains";

    /**
     * Key of domain title.
     */
    public static final String DOMAIN_TITLE = "domainTitle";

    /**
     * Key of domain URI.
     */
    public static final String DOMAIN_URI = "domainURI";

    /**
     * Key of domain description.
     */
    public static final String DOMAIN_DESCRIPTION = "domainDescription";

    /**
     * Key of domain type.
     */
    public static final String DOMAIN_TYPE = "domainType";

    /**
     * Key of domain sort.
     */
    public static final String DOMAIN_SORT = "domainSort";

    /**
     * Key of domain navigation.
     */
    public static final String DOMAIN_NAV = "domainNav";

    /**
     * Key of domain tag count.
     */
    public static final String DOMAIN_TAG_COUNT = "domainTagCnt";

    /**
     * Key of domain icon path.
     */
    public static final String DOMAIN_ICON_PATH = "domainIconPath";

    /**
     * Key of domain CSS.
     */
    public static final String DOMAIN_CSS = "domainCSS";

    /**
     * Key of domain status.
     */
    public static final String DOMAIN_STATUS = "domainStatus";

    /**
     * Key of domain seo title.
     */
    public static final String DOMAIN_SEO_TITLE = "domainSeoTitle";

    /**
     * Key of domain seo keywords.
     */
    public static final String DOMAIN_SEO_KEYWORDS = "domainSeoKeywords";

    /**
     * Key of domain seo description.
     */
    public static final String DOMAIN_SEO_DESC = "domainSeoDesc";

    //// Transient ////
    /**
     * Key of domain count.
     */
    public static final String DOMAIN_T_COUNT = "domainCnt";

    /**
     * Key of domain tags.
     */
    public static final String DOMAIN_T_TAGS = "domainTags";

    /**
     * Key of domain id.
     */
    public static final String DOMAIN_T_ID = "domainId";

    //// Status constants
    /**
     * Domain status - valid.
     */
    public static final int DOMAIN_STATUS_C_VALID = 0;

    /**
     * Domain status - invalid.
     */
    public static final int DOMAIN_STATUS_C_INVALID = 1;

    //// Navigation constants
    /**
     * Domain navigation - enabled.
     */
    public static final int DOMAIN_NAV_C_ENABLED = 0;

    /**
     * Domain navigation - disabled.
     */
    public static final int DOMAIN_NAV_C_DISABLED = 1;

    /**
     * Private constructor.
     */
    private Domain() {
    }
}
