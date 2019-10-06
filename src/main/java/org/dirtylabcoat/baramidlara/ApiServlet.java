/**
 * MIT License
 *
 * Copyright (c) 2017-2019 Daniel Löfquist <daniel@lofquist.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
    private static final String EMPTY_JSON = "{}";

    private ApiConfig config;

    @Override
    public void init() {
        config = new ApiConfig();
        String configFile = System.getProperty("api.config") == null
                ? "config.json"
                : System.getProperty("api.config");
        try {
            // Read config into POJOs
            String jsonString = stringFromFile(configFile);
            JsonReader reader = Json.createReader(new StringReader(jsonString));
            JsonObject jsonObject = reader.readObject();
            config.setPort(jsonObject.getInt("port"));
            config.setApis(
                    jsonObject.getJsonArray("apis").stream().map(a -> a.asJsonObject()).map(a -> {
                        Api api = new Api();
                        api.setPath(a.getString("path"));
                        api.setMethod(a.getString("method"));
                        api.setResponse(a.getString("response"));
                        return api;
                    }).collect(Collectors.toList())
            );
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to read configuration file. [{0}]", e.toString());
        }
    }

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

    public ApiConfig getConfig() {
        return config;
    }

    private void doResponse(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
        String path = request.getRequestURI();
        String json = this.getApiJson(path, method);
        if (!json.equals(EMPTY_JSON)) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(json);
        } else {
            response.setStatus(
                    config.getApis().stream().filter(a -> a.getPath().equals(path)).findFirst().isPresent()
                            ? HttpServletResponse.SC_METHOD_NOT_ALLOWED
                            : HttpServletResponse.SC_NOT_FOUND
            );
        }
    }

    private String getApiJson(String path, String method) throws IOException {
        String response = findResponseByPathAndMethod(path, method);
        return response != null
                ? stringFromFile(response)
                : EMPTY_JSON;
    }

    private String stringFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }

    private String findResponseByPathAndMethod(String path, String method) {
        for (Api api : config.getApis()) {
            if (api.existsAndIsAllowed(path, method)) {
                return api.getResponse();
            }
        }
        return null;
    }

}
