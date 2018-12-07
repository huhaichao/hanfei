
package club.hanfei.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

import org.json.JSONObject;

/**
 * This class defines all character model relevant keys.
 *
@version 1.0.1.0, Jul 8, 2016
 * @since 1.4.0
 */
public final class Character {

    /**
     * Character.
     */
    public static final String CHARACTER = "character";

    /**
     * Characters.
     */
    public static final String CHARACTERS = "characters";

    /**
     * Key of character user id.
     */
    public static final String CHARACTER_USER_ID = "characterUserId";

    /**
     * Key of character image.
     */
    public static final String CHARACTER_IMG = "characterImg";

    /**
     * Key of character content.
     */
    public static final String CHARACTER_CONTENT = "characterContent";

    /**
     * Character font.
     */
    private static final Font FONT = new Font("宋体", Font.PLAIN, 40);

    /**
     * Gets a character by the specified character content in the specified characters.
     *
     * @param content    the specified character content
     * @param characters the specified characters
     * @return character, returns {@code null} if not found
     */
    public static JSONObject getCharacter(final String content, final Set<JSONObject> characters) {
        for (final JSONObject character : characters) {
            if (character.optString(CHARACTER_CONTENT).equals(content)) {
                return character;
            }
        }

        return null;
    }

    /**
     * Creates an image with the specified content (a character).
     *
     * @param content the specified content
     * @return image
     */
    public static BufferedImage createImage(final String content) {
        final BufferedImage ret = new BufferedImage(500, 500, Transparency.TRANSLUCENT);
        final Graphics g = ret.getGraphics();
        g.setClip(0, 0, 50, 50);
        g.fillRect(0, 0, 50, 50);
        g.setFont(new Font(null, Font.PLAIN, 40));
        g.setColor(Color.BLACK);
        g.drawString(content, 5, 40);
        g.dispose();

        return ret;
    }
}
