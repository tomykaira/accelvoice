package io.github.tomykaira.accelvoice.pathcomp;

import io.github.tomykaira.accelvoice.selector.CandidatesCollection;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import io.github.tomykaira.accelvoice.selector.SelectionListener;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Properties;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    private static final SelectionListener listener = new SelectionListener() {
        @Override
        public void notify(String selected) {
            logger.info("Result: " + selected);
            System.out.println(selected);
        }

    };

    public static void main(String[] args) throws IOException {
        File logFile;
        if (args.length == 0) {
            logFile = new File(System.getProperty("user.home"), ".pathcomp.log");
        } else {
            logFile = new File(args[0]);
        }

        PropertyConfigurator.configure(log4jConfig(logFile));
        RecognizerLibrary.INSTANCE.start(1, new String[]{"Java"}, logFile.toString());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String [] command = line.split(":", 2);
                if (command[0].equals("start")) {
                    startCompletion(command[1]);
                } else if (command[0].equals("stop")) {
                    stopCompletion();
                } else if (command[0].equals("exit")) {
                    break;
                }
            }
        }
        RecognizerLibrary.INSTANCE.stop();
    }

    private static void startCompletion(String line) throws IOException {
        CandidatesCollection collection = new Find(Paths.get(line)).findCandidates(listener, 3);
        StringBuilder sb = new StringBuilder("Candidates: ");
        for (String s : collection.getCandidates()) {
            sb.append(s);
            sb.append(" ");
        }
        logger.info(sb.toString());
        collection.select();
    }

    private static void stopCompletion() {
        RecognizerLibrary.INSTANCE.abort_recognition();
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
}
