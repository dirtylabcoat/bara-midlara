package org.dirtylabcoat.baramidlara;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class App {
    public static void main(String[] args) throws Exception {

        Server server = new Server(8080);
	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        context.addServlet(new ServletHolder(new ApiServlet()), "/*");

        server.start();

    }

    public static class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = -6154475791066619575L;
        private static final Logger LOGGER = Logger.getLogger(ApiServlet.class.getName());

        private final Map<String, String> apis = new HashMap<>();

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,	IOException {
            String requestedApi = request.getRequestURI();
            if (apis.containsKey(requestedApi)) {
                String json = new String(Files.readAllBytes(Paths.get(apis.get(requestedApi))), StandardCharsets.UTF_8);
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(json);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        @Override
        public void init() {
            String configFile = System.getProperty("api.config");
            Properties properties = new Properties();
            try {
                try (InputStream is = new FileInputStream(configFile)) {
                    properties.load(is);
                    properties.entrySet().stream().forEach((entry) -> {
                        apis.put(entry.getKey().toString(), entry.getValue().toString());
                    });
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to read configuration file. [{0}]", e.toString());
            }
        }

    }

}
