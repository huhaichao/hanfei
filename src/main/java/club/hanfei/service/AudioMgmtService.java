
package club.hanfei.service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import club.hanfei.util.Hanfei;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.URLs;
import org.json.JSONObject;

/**
 * Audio management service.
 *
@version 1.0.2.3, May 6, 2018
 * @since 2.1.0
 */
@Service
public class AudioMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AudioMgmtService.class);

    private static final String BAIDU_API_KEY = Hanfei.get("baidu.yuyin.apiKey");
    private static final String BAIDU_SECRET_KEY = Hanfei.get("baidu.yuyin.secretKey");
    private static String BAIDU_ACCESS_TOKEN;
    private static long BAIDU_ACCESS_TOKEN_TIME;

    private static void refreshBaiduAccessToken() {
        if (StringUtils.isBlank(BAIDU_API_KEY) || StringUtils.isBlank(BAIDU_SECRET_KEY)) {
            return;
        }

        final long now = System.currentTimeMillis();
        if (StringUtils.isNotBlank(BAIDU_ACCESS_TOKEN) && now - 1000 * 60 * 60 * 6 < BAIDU_ACCESS_TOKEN_TIME) {
            return;
        }

        try {
            final URL url = new URL("https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
                    + "&client_id=" + BAIDU_API_KEY + "&client_secret=" + BAIDU_SECRET_KEY);

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            try (final InputStream inputStream = conn.getInputStream()) {
                final String content = IOUtils.toString(inputStream, "UTF-8");

                conn.disconnect();

                final JSONObject result = new JSONObject(content);
                BAIDU_ACCESS_TOKEN = result.optString("access_token", null);
                BAIDU_ACCESS_TOKEN_TIME = now;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Requires Baidu Yuyin access token failed", e);
        }
    }

    private static byte[] baiduTTS(final String text, final String uid) {
        if (StringUtils.isBlank(BAIDU_API_KEY) || StringUtils.isBlank(BAIDU_SECRET_KEY)) {
            return null;
        }

        if (null == BAIDU_ACCESS_TOKEN) {
            refreshBaiduAccessToken();
        }

        if (null == BAIDU_ACCESS_TOKEN) {
            LOGGER.warn("Please configure [baidu.yuyin.*] in hanfei.properties");

            return null;
        }

        try {
            final URL url = new URL("http://tsn.baidu.com/text2audio?grant_type=client_credentials"
                    + "&client_id=" + BAIDU_API_KEY + "&client_secret=" + BAIDU_SECRET_KEY);

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (final OutputStream outputStream = conn.getOutputStream()) {
                IOUtils.write("tex=" + URLs.encode(StringUtils.substring(text, 0, 1024))
                        + "&lan=zh&cuid=" + uid + "&spd=6&pit=6&ctp=1&tok=" + BAIDU_ACCESS_TOKEN, outputStream, "UTF-8");
            }

            byte[] ret;
            try (final InputStream inputStream = conn.getInputStream()) {
                final int responseCode = conn.getResponseCode();
                final String contentType = conn.getContentType();

                if (HttpServletResponse.SC_OK != responseCode || !"audio/mp3".equals(contentType)) {
                    final String msg = IOUtils.toString(inputStream, "UTF-8");
                    LOGGER.warn("Baidu Yuyin TTS failed: " + msg);
                    conn.disconnect();

                    return null;
                }

                ret = IOUtils.toByteArray(inputStream);
            }

            conn.disconnect();

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Requires Baidu Yuyin access token failed", e);
        }

        return null;
    }

    /**
     * Text to speech.
     *
     * @param text   the specified text
     * @param type   specified type, "article"/"comment"
     * @param textId the specified id of text, article id or comment id
     * @param uid    the specified user id
     * @return speech URL, returns {@code ""} if TTS failed
     */
    public String tts(final String text, final String type, final String textId, final String uid) {
        final byte[] bytes = baiduTTS(text, uid);
        if (null == bytes) {
            return "";
        }

        String ret;
        try {
            if (Hanfei.getBoolean("qiniu.enabled")) {
                final Auth auth = Auth.create(Hanfei.get("qiniu.accessKey"), Hanfei.get("qiniu.secretKey"));
                final UploadManager uploadManager = new UploadManager(new Configuration());
                final BucketManager bucketManager = new BucketManager(auth, new Configuration());
                final int seq = RandomUtils.nextInt(10);
                final String fileKey = "audio/" + type + "/" + textId + seq + ".mp3";
                try {
                    bucketManager.delete(Hanfei.get("qiniu.bucket"), fileKey);
                } catch (final Exception e) {
                    // ignore
                }
                uploadManager.put(bytes, fileKey, auth.uploadToken(Hanfei.get("qiniu.bucket")), null, "audio/mp3", false);
                ret = Hanfei.get("qiniu.domain") + "/audio/" + type + "/" + textId + seq + ".mp3";
            } else {
                final String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp3";
                try (final OutputStream output = new FileOutputStream(Hanfei.get("upload.dir") + fileName)) {
                    IOUtils.write(bytes, output);
                }

                ret = Latkes.getServePath() + "/upload/" + fileName;
            }

            return ret;
        } catch (final Exception e) {
            if (e instanceof QiniuException) {
                try {
                    LOGGER.log(Level.ERROR, "Uploads audio failed [type=" + type + ", textId=" + textId + "], Qiniu exception body [" +
                            ((QiniuException) e).response.bodyString() + "]");
                } catch (final Exception qe) {
                    LOGGER.log(Level.ERROR, "Uploads audio and parse result exception", qe);
                }
            } else {
                LOGGER.log(Level.ERROR, "Uploads audio failed [type=" + type + ", textId=" + textId + "]", e);
            }
        }

        return "";
    }
}