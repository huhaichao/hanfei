
package club.hanfei.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * XSS test case.
 *
@version 1.0.0.1, Sep 14, 2017
 * @since 0.3.0
 */
public class XSSTestCase {

    @Test
    public void xss() {
        String src = "http://error\"  onerror=\"this.src='http://7u2fje.com1.z0.glb.clouddn.com/girl.jpg';this.removeAttribute('onerror');if(!window.a){console.log('Where am I ?');window.a=1}";
        assertFalse(Jsoup.isValid("<img src=\"" + src + "\"/>", Whitelist.basicWithImages()));

        src = "http://7u2fje.com1.z0.glb.clouddn.com/girl.jpg";
        assertTrue(Jsoup.isValid("<img src=\"" + src + "\"/>", Whitelist.basicWithImages()));

        src = "1\" onmouseover=alert(111);\"&p=10";
        assertTrue(Jsoup.isValid(src, Whitelist.none()));

        src = src.replace("\"", "");
        src = "<div onmouseover=alert(111);></div>";
        assertFalse(Jsoup.isValid(src, Whitelist.none()));
    }
}
