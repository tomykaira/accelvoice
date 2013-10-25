package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.selector.RecognizerLibrary
import io.github.tomykaira.accelvoice.selector.SelectionListener
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

class Server {
    private static final Logger logger = Logger.getLogger(Server.class)
    private final Project project
    private final File logFile

    def Server(Project project, File logFile) {
        this.project = project
        this.logFile = logFile
    }

    def run() {
        def start = System.nanoTime()
        project.startWatching()

        def collector = new CandidatesCollector(project.trie)
        def listener = new SelectionListener() {
            @Override
            void notify(String selected) {
                logger.info("Result: " + selected)
                println(selected)
            }
        }

        RecognizerLibrary.INSTANCE.start(1, {"java"} as String[], logFile?.toString())

        logger.info("System initialized in " + (System.nanoTime() - start) / 1000_000)

        System.in.eachLine { line ->
            def collection = collector.completeByPrefix(line, listener)
            logger.info(collection.candidates.join(" "))
            collection.select()
        }

        RecognizerLibrary.INSTANCE.stop()
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Specify project")
            System.exit(1)
        }
        def logFile = args.length >= 2 ? new File(args[1]) : null
        PropertyConfigurator.configure(log4jConfig(logFile))

        logger.info("Starting projectdict server for " + args[0]);
        def server = new Server(new Project(args[0]), logFile)
        server.run()
        System.exit(0) // to stop threads forcefully
    }

    private static Properties log4jConfig(File logFile) {
        Properties properties = new Properties()
        properties.setProperty("log4j.rootLogger", "INFO, ROOT")
        if (logFile == null) {
            properties.setProperty("log4j.appender.ROOT", "org.apache.log4j.ConsoleAppender")
        } else {
            properties.setProperty("log4j.appender.ROOT", "org.apache.log4j.FileAppender")
            properties.setProperty("log4j.appender.ROOT.File", logFile.absolutePath)
            properties.setProperty("log4j.appender.ROOT.Append", "true")
        }
        properties.setProperty("log4j.appender.ROOT.layout", "org.apache.log4j.PatternLayout")
        properties.setProperty("log4j.appender.ROOT.layout.ConversionPattern", "%d [%t] %-5p %c - %m%n")
        properties
    }
}
