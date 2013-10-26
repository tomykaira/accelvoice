package io.github.tomykaira.accelvoice.projectdict

import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

class App {
    private final static logger = Logger.getLogger(App.class)

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Specify project")
            System.exit(1)
        }
        def logFile = args.length >= 2 ? new File(args[1]) : null
        PropertyConfigurator.configure(log4jConfig(logFile))

        logger.info("Starting projectdict server for " + args[0]);
        def server = new HttpServer(new Project(args[0]), logFile)
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
