
package club.hanfei.processor;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.processor.advice.LoginCheck;
import club.hanfei.processor.advice.stopwatch.StopwatchEndAdvice;
import club.hanfei.processor.advice.stopwatch.StopwatchStartAdvice;
import club.hanfei.service.OptionQueryService;
import club.hanfei.util.Hanfei;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.net.MimeTypes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.json.JSONObject;

/**
 * Fetch file and upload processor.
 * <p>
 * <ul>
 * <li>Fetches the remote file and upload it (/fetch-upload), POST</li>
 * </ul>
 * </p>
 *
@version 1.0.0.1, Apr 5, 2018
 * @since 1.5.0
 */
@RequestProcessor
public class FetchUploadProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FetchUploadProcessor.class);

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Fetches the remote file and upload it.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = "/fetch-upload", method = HttpMethod.POST)
    @Before({StopwatchStartAdvice.class, LoginCheck.class})
    @After({StopwatchEndAdvice.class})
    public void fetchUpload(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        JSONObject requestJSONObject;
        try {
            requestJSONObject = context.requestJSON();
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            return;
        }

        final String originalURL = requestJSONObject.optString(Common.URL);

        HttpResponse res = null;
        byte[] data;
        String contentType;
        try {
            final HttpRequest req = HttpRequest.get(originalURL);
            res = req.send();

            if (HttpServletResponse.SC_OK != res.statusCode()) {
                return;
            }

            data = res.bodyBytes();
            contentType = res.contentType();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Fetch file [url=" + originalURL + "] failed", e);

            return;
        } finally {
            if (null != res) {
                try {
                    res.close();
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Close response failed", e);
                }
            }
        }

        String suffix;
        String[] exts = MimeTypes.findExtensionsByMimeTypes(contentType, false);
        if (null != exts && 0 < exts.length) {
            suffix = exts[0];
        } else {
            suffix = StringUtils.substringAfter(contentType, "/");
        }

        final String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;

        if (Hanfei.getBoolean("qiniu.enabled")) {
            final Auth auth = Auth.create(Hanfei.get("qiniu.accessKey"), Hanfei.get("qiniu.secretKey"));
            final UploadManager uploadManager = new UploadManager(new Configuration());

            try {
                uploadManager.put(data, "e/" + fileName, auth.uploadToken(Hanfei.get("qiniu.bucket")),
                        null, contentType, false);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Uploads to qiniu failed", e);
            }

            context.renderJSONValue(Common.URL, Hanfei.get("qiniu.domain") + "/e/" + fileName);
            context.renderJSONValue("originalURL", originalURL);
        } else {
            try (final OutputStream output = new FileOutputStream(Hanfei.get("upload.dir") + fileName)) {
                IOUtils.write(data, output);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Writes output stream failed", e);
            }

            context.renderJSONValue(Common.URL, Latkes.getServePath() + "/upload/" + fileName);
            context.renderJSONValue("originalURL", originalURL);
        }

        context.renderTrueResult();
    }
}
