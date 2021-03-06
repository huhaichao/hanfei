
package club.hanfei.util;

import java.io.FileReader;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Discoverer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Markdown utilities test case.
 *

 * @version 2.4.1.9, Oct 23, 2018
 * @since 0.1.6
 */
public class MarkdownsTestCase {

    static {
        Latkes.init();
        try {
            final Collection<Class<?>> classes = Discoverer.discover("org.b3log.symphony");
            BeanManager.start(classes);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tag test.
     */
    @Test
    public void tag() {
        if (Markdowns.MARKED_AVAILABLE) {
            final String kbd = "<kbd>DV</kbd>";

            String html = Markdowns.toHTML(kbd);
            html = Markdowns.clean(html, "");

            Assert.assertTrue(html.contains("<kbd>") && html.contains("</kbd>"));
        }
    }

    /**
     * XSS test.
     */
    @Test
    public void clean() {
        if (Markdowns.MARKED_AVAILABLE) {
            final String md = "<a href='data:text/html;base64,PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4K'>a link</a>";

            final String html = Markdowns.toHTML(md);
            final String securedHTML = Markdowns.clean(html, "");

            Assert.assertTrue(securedHTML.contains("href"));
        }
    }

    /**
     * Auto space test.
     */
    @Test
    public void space() {
        if (Markdowns.MARKED_AVAILABLE) {
            final String md = "Sym是一个用Java写的实时论坛，欢迎来[体验Sym](https://hacpai.com)！";
            final String html = Markdowns.toHTML(md);

            Assert.assertEquals(html, "<p>Sym 是一个用 Java 写的实时论坛，欢迎来<a href=\"http://localhost:8080/forward?goto=https%3A%2F%2Fhacpai.com\" target=\"_blank\" rel=\"nofollow\">体验 Sym</a>！</p>");
        }
    }

    /**
     * Link test.
     */
    @Test
    public void toHTML() {
        if (Markdowns.MARKED_AVAILABLE) {
            String md = "[b3log](https://b3log.org)";
            String html = Markdowns.toHTML(md);
            Assert.assertTrue(html.contains("href"));

            md = "[b3log](b3log.org)";
            html = Markdowns.toHTML(md);
            Assert.assertTrue(html.contains("href"));
        }
    }

    /**
     * Standard syntax test.
     *
     * @throws java.lang.Exception exception
     */
    @Test
    public void toHtml0() throws Exception {
        if (Markdowns.MARKED_AVAILABLE) {
            final URL mdResource = MarkdownsTestCase.class.getResource("/markdown_syntax.text");
            final String md = IOUtils.toString(new FileReader(mdResource.getPath()));
            final String html = Markdowns.toHTML(md);

            //System.out.println(html);
        }
    }
}
