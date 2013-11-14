package io.github.tomykaira.accelvoice.projectdict

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.github.tomykaira.accelvoice.selector.SelectionListener
import org.apache.log4j.Logger

@Mixin(JsonProtocol)
class HttpCompletionHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(HttpCompletionHandler.class)

    private final Map<Integer, String> results = new HashMap<>();

    private int activeTag;

    private final CandidatesCollector collector

    HttpCompletionHandler(CandidatesCollector collector) {
        this.collector = collector
    }

    @Override
    void handle(HttpExchange httpExchange) throws IOException {
        try {
            def data = parseJsonRequest(httpExchange)
            Map result = null
            switch (httpExchange.getRequestURI().getPath()) {
                case "/start-completion":
                    result = handleStartCompletion(data)
                    break
                case "/result":
                    result = handleResult(data)
                    break
            }
            if (result == null) {
                respond(httpExchange, 404, "Not supported command")
            } else {
                respondJson(httpExchange, result)
            }
        } catch(Exception e) {
            logger.error("Error in CompleteHandler", e)
            respondException(httpExchange, e)
        }
    }

    private static void respondException(HttpExchange httpExchange, Exception e) {
        StringWriter writer = new StringWriter()
        writer.withPrintWriter { w ->
            w.println("Internal server error")
            w.println("")
            e.printStackTrace(w)
        }
        respond(httpExchange, 500, writer.toString())
    }

    // TODO: select candidates with the same language (for polyglot projects)
    private Map handleStartCompletion(Map<String, String> data) {
        String prefix = data["prefix"]
        final int tag = prefix.hashCode() * 53 + (System.currentTimeMillis() % 2592000000L) as int

        def listener = listenerForTag(tag)
        def collection = collector.completeByPrefix(prefix, listener)

        if (collection.candidates.isEmpty()) {
            logger.info("$tag: No candidate found for $prefix")
        } else {
            logger.info("$tag: complete $prefix from ${collection.candidates.collect { "\"$it\"" }.join(" ")}")
            collection.select()
        }

        activeTag = tag

        [
                tag: tag.toString(),
                candidate_count: collection.candidates.size()
        ]
    }

    private SelectionListener listenerForTag(tag) {
        new SelectionListener() {
            @Override
            void notify(String selected) {
                results.put(tag, selected)
                logger.info("$tag: result found $selected")
            }
        }
    }

    private Map handleResult(Map<String, String> data) {
        int tag = Integer.parseInt(data["tag"])
        while (results.get(tag) == null && activeTag == tag)
            sleep(10)
        [
                tag: tag,
                result: results.remove(tag)
        ]
    }
}
