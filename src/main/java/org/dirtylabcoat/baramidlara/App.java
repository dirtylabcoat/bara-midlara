package org.dirtylabcoat.baramidlara;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        private static final String METHOD_API_DIVIDER = "_";

        private final String[] supportedHttpMethods = { "GET", "POST", "PUT", "DELETE" };
        private final Map<String, Map<String, String>> methods = new HashMap<>();
        private final Set<String> apis = new HashSet<>();

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            doResponse(request, response, "GET");
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
            doResponse(request, response, "POST");
        }

        @Override
        protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
            doResponse(request, response, "PUT");
        }

        @Override
        protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
            doResponse(request, response, "DELETE");
        }

        private void doResponse(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
            String api = request.getRequestURI();
            String json = getApiJson(method, api);
            if (!json.isEmpty()) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(json);
            } else {
                response.setStatus(apis.contains(api) ? HttpServletResponse.SC_METHOD_NOT_ALLOWED : HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private String getApiJson(String method, String api) throws IOException {
            if (methods.get(method).containsKey(api)) {
                String json = new String(Files.readAllBytes(Paths.get(methods.get(method).get(api))), StandardCharsets.UTF_8);
                return json;
            } else {
                return "";
            }
        }

        @Override
        public void init() {
            Arrays.asList(supportedHttpMethods).stream().forEach((method) -> {
                this.methods.put(method, new HashMap<>());
            });
            String configFile = System.getProperty("api.config");
            Properties properties = new Properties();
            try {
                try (InputStream is = new FileInputStream(configFile)) {
                    properties.load(is);
                    properties.entrySet().stream().forEach((entry) -> {
                        String key = entry.getKey().toString();
                        int fc = key.indexOf(METHOD_API_DIVIDER);
                        String method = key.substring(0, fc);
                        String api = key.substring(fc + 1);
                        methods.get(method).put(api, entry.getValue().toString());
                        apis.add(api);
                    });
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to read configuration file. [{0}]", e.toString());
            }
        }
    }

}
