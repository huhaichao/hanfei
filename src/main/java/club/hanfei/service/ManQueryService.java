
package club.hanfei.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import club.hanfei.model.Common;
import club.hanfei.util.Hanfei;
import club.hanfei.util.Markdowns;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.json.JSONObject;

/**
 * Manual query service.
 * <p>
 * Sees <a href="https://github.com/tldr-pages/tldr">tldr</a> for more details.
 * </p>
 *
@version 1.0.1.5, Nov 3, 2018
 * @since 1.8.0
 */
@Service
public class ManQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ManQueryService.class);

    /**
     * All command manuals.
     */
    private static final List<JSONObject> CMD_MANS = new ArrayList<>();

    /**
     * Whether tldr is enabled.
     */
    public static boolean TLDR_ENABLED;

    static {
        Hanfei.EXECUTOR_SERVICE.submit(ManQueryService::init);
    }

    /**
     * Initializes manuals.
     */
    private static void init() {
        // http://stackoverflow.com/questions/585534/what-is-the-best-way-to-find-the-users-home-directory-in-java
        final String userHome = System.getProperty("user.home");
        if (StringUtils.isBlank(userHome)) {
            return;
        }

        try {
            Thread.sleep(5 * 1000);

            final String tldrPagesPath = userHome + File.separator + "tldr" + File.separator + "pages" + File.separator;
            final Collection<File> mans = FileUtils.listFiles(new File(tldrPagesPath), new String[]{"md"}, true);
            for (final File manFile : mans) {
                try (final InputStream is = new FileInputStream(manFile)) {
                    final String md = IOUtils.toString(is, "UTF-8");
                    String html = Markdowns.toHTML(md);

                    final JSONObject cmdMan = new JSONObject();
                    cmdMan.put(Common.MAN_CMD, StringUtils.substringBeforeLast(manFile.getName(), "."));

                    html = html.replace("\n", "").replace("\r", "");
                    cmdMan.put(Common.MAN_HTML, html);

                    CMD_MANS.add(cmdMan);
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Loads man [" + manFile.getPath() + "] failed", e);
                }
            }
        } catch (final Exception e) {
            return;
        }

        TLDR_ENABLED = !CMD_MANS.isEmpty();

        Collections.sort(CMD_MANS, (o1, o2) -> {
            final String c1 = o1.optString(Common.MAN_CMD);
            final String c2 = o2.optString(Common.MAN_CMD);

            return c1.compareToIgnoreCase(c2);
        });
    }

    /**
     * Gets manuals by the specified command prefix.
     *
     * @param cmdPrefix the specified comman prefix
     * @return a list of manuals, for example, <pre>
     * [
     *     {
     *         "manCmd": "find",
     *         "manHTML": "...."
     *     }, ....
     * ]
     * </pre>, returns an empty list if not found
     */
    public List<JSONObject> getMansByCmdPrefix(final String cmdPrefix) {
        final JSONObject toSearch = new JSONObject();
        toSearch.put(Common.MAN_CMD, cmdPrefix);

        final int index = Collections.binarySearch(CMD_MANS, toSearch, (o1, o2) -> {
            String c1 = o1.optString(Common.MAN_CMD);
            final String c2 = o2.optString(Common.MAN_CMD);

            if (c1.length() < c2.length()) {
                return c1.compareToIgnoreCase(c2);
            }

            c1 = c1.substring(0, c2.length());

            return c1.compareToIgnoreCase(c2);
        });

        final List<JSONObject> ret = new ArrayList<>();

        if (index < 0) {
            return ret;
        }

        int start = index;
        int end = index;

        while (start > -1 && CMD_MANS.get(start).optString(Common.MAN_CMD).startsWith(cmdPrefix.toLowerCase())) {
            start--;
        }

        start++;

        final int WINDOW_SIZE = 8;

        if (start < index - WINDOW_SIZE) {
            end = start + WINDOW_SIZE;
        } else {
            while (end < CMD_MANS.size() && end < index + 5 && CMD_MANS.get(end).optString(Common.MAN_CMD).startsWith(cmdPrefix.toLowerCase())) {
                end++;

                if (end >= start + WINDOW_SIZE) {
                    break;
                }
            }
        }

        return CMD_MANS.subList(start, end);
    }
}
