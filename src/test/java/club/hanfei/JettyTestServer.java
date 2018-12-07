
package club.hanfei;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author trydofor
 * @since 2017-09-25
 */
public class JettyTestServer {

    public void start(int port, boolean useMain) throws Exception {

        URLClassLoader classLoader = (URLClassLoader) JettyTestServer.class.getClassLoader();
        String projectPath = null;
        String testClassDir = "target/test-classes";
        URL[] urlTest = classLoader.getURLs();
        for (URL url : urlTest) {
            String u = url.getPath();
            int p = u.lastIndexOf(testClassDir);
            if (p > 0) {
                projectPath = u.substring(0, p);
                break;
            }
        }

        // useMain : delete test resources, eg hanfei.properties
        if (useMain && projectPath != null) {
            File[] mainRes = new File(projectPath + "src/main/resources").listFiles();
            if (mainRes != null) {
                File testClzDir = new File(projectPath + testClassDir);
                for (File res : mainRes) {
                    if (res.isDirectory()) continue;
                    File t = new File(testClzDir, res.getName());
                    if (t.isFile()) t.delete();
                }
            }
        }

        if(projectPath == null) projectPath = "./";

        WebAppContext webapp = new WebAppContext();
        // useFileMappedBuffer=false
        webapp.setDefaultsDescriptor(projectPath + "src/test/resources/webdefault.xml");
        webapp.setContextPath("/");
        webapp.setResourceBase(projectPath + "src/main/webapp");
        webapp.setClassLoader(classLoader);

        Server server = new Server(port);
        server.setHandler(webapp);

        server.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(LifeCycle lifeCycle) {
                System.err.println("\n");
                System.err.println("================================================================");
                System.err.println("====\t\t Symphony Local is started ! PORT : " + port + "\t\t====");
                System.err.println("================================================================");
                System.err.println("\n");
            }
        });

        //
        server.start();
        server.join();
    }

    public static void main(String[] args) throws Exception {
        JettyTestServer jettyTestServer = new JettyTestServer();
        jettyTestServer.start(8080, true);
    }
}
