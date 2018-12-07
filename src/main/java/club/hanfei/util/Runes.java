
package club.hanfei.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Rune utilities.
 *
@version 1.0.0.0, Mar 19, 2017
 * @since 2.1.0
 */
public final class Runes {


    private Runes() {
    }

//    public static void main(final String[] args) {
//        System.out.println(getChinesePercent("123abc这个中文cde123abc也要提取123ab"));
//    }

    /**
     * Gets chinese percentage of the specified string.
     *
     * @param str the specified string
     * @return percentage
     */
    public static int getChinesePercent(final String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }

        final Pattern p = Pattern.compile("([\u4e00-\u9fa5]+)");
        final Matcher m = p.matcher(str);
        final StringBuilder chineseBuilder = new StringBuilder();
        while (m.find()) {
            chineseBuilder.append(m.group(0));
        }

        return (int) Math.floor(StringUtils.length(chineseBuilder.toString()) / (double) StringUtils.length(str) * 100);
    }
}
