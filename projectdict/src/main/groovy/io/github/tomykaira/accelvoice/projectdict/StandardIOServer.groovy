package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.selector.SelectionListener
import org.apache.log4j.Logger

public class StandardIOServer extends Server {
    private static final Logger logger = Logger.getLogger(StandardIOServer.class)

    def StandardIOServer(Project project, File logFile) {
        super(project, logFile);
    }

    void mainLoop() {
        def listener = new SelectionListener() {
            @Override
            void notify(String selected) {
                logger.info("Result: " + selected)
                println(selected)
            }
        }

        System.in.eachLine { line ->
            def collection = collector.completeByPrefix(line, listener)
            logger.info(collection.candidates.join(" "))
            collection.select()
        }
    }
}
