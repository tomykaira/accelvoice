package io.github.tomykaira.accelvoice.pathcomp;

import io.github.tomykaira.accelvoice.selector.CandidatesCollection;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import io.github.tomykaira.accelvoice.selector.SelectionListener;
import org.apache.log4j.Logger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Properties;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        File logFile;
        if (args.length == 0) {
            logFile = new File(System.getProperty("user.home"), ".pathcomp.log");
        } else {
            logFile = new File(args[0]);
        }

        log4jConfig(logFile);

        final SelectionListener listener = new SelectionListener() {
            @Override
            public void notify(String selected) {
                logger.info("Result: " + selected);
                System.out.println(selected);
            }

        };

        registerSignalHandler();

        try (BufferedReader stream = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = stream.readLine()) != null) {
                CandidatesCollection collection = new Find(Paths.get(line)).findCandidates(listener);
                StringBuilder sb = new StringBuilder("Candidates: ");
                for (String s : collection.getCandidates()) {
                    sb.append(s);
                    sb.append(" ");
                }
                logger.info(sb.toString());
                collection.select();
            }
        }
    }

    private static Properties log4jConfig(File logFile) {
        Properties properties = new Properties();
        properties.setProperty("log4j.rootLogger", "INFO, ROOT");
        if (logFile == null) {
            properties.setProperty("log4j.appender.ROOT", "org.apache.log4j.ConsoleAppender");
        } else {
            properties.setProperty("log4j.appender.ROOT", "org.apache.log4j.FileAppender");
            properties.setProperty("log4j.appender.ROOT.File", logFile.getAbsolutePath());
            properties.setProperty("log4j.appender.ROOT.Append", "true");
        }

        properties.setProperty("log4j.appender.ROOT.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.ROOT.layout.ConversionPattern", "%d [%t] %-5p %c - %m%n");
        return properties;
    }

    private static void registerSignalHandler() {
        Signal usr1 = new Signal("USR1");
        Signal.handle(usr1, new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                RecognizerLibrary.INSTANCE.abort_recognition();
            }
        });
    }
}
