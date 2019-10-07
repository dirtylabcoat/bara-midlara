/**
 * MIT License
 *
 * Copyright (c) 2017 Daniel Löfquist <daniel@lofquist.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

/**
 *
 * @author fighterhayabusa
 */
public class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = -6154475791066619575L;
    private static final Logger LOGGER = Logger.getLogger(ApiServlet.class.getName());
    private static final String METHOD_API_DIVIDER = "_";

    private final String[] supportedHttpMethods = { "GET", "POST", "PUT", "DELETE" };
    private final Map<String, Map<String, String>> methods = new HashMap<>();
    private final Set<String> apis = new HashSet<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doResponse(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doResponse(request, response, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doResponse(request, response, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doResponse(request, response, "DELETE");
    }

    private void doResponse(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
        String api = request.getRequestURI();
        String json = this.getApiJson(method, api);
        if (!json.isEmpty()) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(json);
        } else {
            response.setStatus(this.apis.contains(api) ? HttpServletResponse.SC_METHOD_NOT_ALLOWED : HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getApiJson(String method, String api) throws IOException {
        if (this.methods.get(method).containsKey(api)) {
            return new String(Files.readAllBytes(Paths.get(this.methods.get(method).get(api))), StandardCharsets.UTF_8);
        } else {
            return "";
        }
    }

    @Override
    public void init() {
        Arrays.asList(supportedHttpMethods).stream().forEach((method) -> {
            this.methods.put(method, new HashMap<>());
        });
        String configFile = System.getProperty("api.config") == null
                ? "demo.config"
                : System.getProperty("api.config");
        Properties properties = new Properties();
        try {
            try (InputStream is = new FileInputStream(configFile)) {
                properties.load(is);
                properties.entrySet().stream().forEach((entry) -> {
                    String key = entry.getKey().toString();
                    int fc = key.indexOf(METHOD_API_DIVIDER);
                    String method = key.substring(0, fc);
                    String api = key.substring(fc + 1);
                    this.methods.get(method).put(api, entry.getValue().toString());
                    this.apis.add(api);
                });
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to read configuration file. [{0}]", e.toString());
        }
    }
}

