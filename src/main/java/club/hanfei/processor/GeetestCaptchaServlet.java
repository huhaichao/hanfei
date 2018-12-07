
package club.hanfei.processor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import club.hanfei.model.Common;
import club.hanfei.util.GeetestLib;
import club.hanfei.util.Sessions;
import club.hanfei.util.Hanfei;
import org.b3log.latke.Keys;
import org.json.JSONObject;

/**
 * <a href="http://www.geetest.com">极验验证</a>.
 *
@version 1.0.0.0, Mar 23, 2016
 * @since 1.4.0
 */
@WebServlet(urlPatterns = "/geetest-captcha", loadOnStartup = 2)
public class GeetestCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final GeetestLib gtSdk = new GeetestLib(Hanfei.get("geetest.id"), Hanfei.get("geetest.key"));
        String resStr = "{}";
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int gtServerStatus = gtSdk.preProcess(userId);
        final JSONObject status = new JSONObject();
        status.put(Common.DATA, gtServerStatus);
        Sessions.put(userId + GeetestLib.gtServerStatusSessionKey, status);
        resStr = gtSdk.getResponseStr();

        final PrintWriter out = response.getWriter();
        out.println(resStr);
    }
}
