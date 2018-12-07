
package club.hanfei.util;

/**
 * Status code constants and utilities.
 * <p>
 * 因为一些历史原因，所以目前仅有 API2 在使用该类，其他的响应还是使用 "sc": boolean 来实现。
 * </p>
 *
@version 1.0.0.2, Apr 3, 2017
 * @since 2.0.0
 */
public final class StatusCodes {

    /**
     * Indicates success.
     */
    public static final int SUCC = 0;

    /**
     * Indicates not found.
     */
    public static final int NOT_FOUND = 1;

    /**
     * Indicates an error occurred.
     */
    public static final int ERR = -1;

}
