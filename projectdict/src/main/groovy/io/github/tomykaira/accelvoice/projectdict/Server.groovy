package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.selector.RecognizerLibrary
import org.apache.log4j.Logger

abstract class Server {
    protected final Project project
    private static final Logger logger = Logger.getLogger(Server.class)
    private final File logFile
    protected final CandidatesCollector collector;

    Server(Project project, File logFile) {
        this.project = project
        this.logFile = logFile
        this.collector = new CandidatesCollector(project.trie)
    }

    abstract void mainLoop();

    def run() {
        def start = System.nanoTime()
        project.startWatching()
        startLibrary()
        logger.info("System initialized in " + (System.nanoTime() - start) / 1000_000)

        mainLoop()

        stopLibrary()
    }

    private void startLibrary() {
        RecognizerLibrary.INSTANCE.start(1, {"java"} as String[], logFile?.toString())
    }

    private static void stopLibrary() {
        RecognizerLibrary.INSTANCE.stop()
    }
}
