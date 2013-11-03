package io.github.tomykaira.accelvoice.projectdict

import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonBuilder

import java.nio.charset.Charset

class JsonProtocol {

    public static Map<String, String> parseJsonRequest(HttpExchange t) {
        def result = [:]
        t.getRequestBody().withReader {
            it.readLines().join("\n").split("&").collect {
                def pair = it.split("=", 2)
                result[pair[0]] = pair[1]
            }
        }
        result
    }

    public static void respondJson(HttpExchange t, Map value) {
        JsonBuilder builder = new JsonBuilder()
        builder(value)
        respond(t, 200, builder.toString())
    }

    private static void respond(HttpExchange t, int statusCode, String response) {
        t.sendResponseHeaders(statusCode, response.length())
        OutputStream os = t.getResponseBody()
        os.write(response.getBytes(Charset.defaultCharset()))
        os.close()
    }
}
