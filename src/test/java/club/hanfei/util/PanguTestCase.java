
package club.hanfei.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <a href="https://github.com/vinta/pangu.java">Pangu</a> utilities test case.
 *
@version 1.0.0.0, Aug 31, 2016
 * @since 1.6.0
 */
public class PanguTestCase {

    @Test
    public void test() {
        final String text = Pangu.spacingText("Sym是一个用Java写的实时论坛，欢迎来体验！");

        Assert.assertEquals(text, "Sym 是一个用 Java 写的实时论坛，欢迎来体验！");
    }
}
