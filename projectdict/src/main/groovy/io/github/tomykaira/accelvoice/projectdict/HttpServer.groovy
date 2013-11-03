package io.github.tomykaira.accelvoice.projectdict

import java.util.concurrent.Executors

class HttpServer extends Server {
    def HttpServer(Project project, File logFile) {
        super(project, logFile)
    }

    @Override
    void mainLoop() {
        def server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8302), 0);
        server.createContext("/", new HttpCompletionHandler(collector))
        server.setExecutor(Executors.newCachedThreadPool())
        server.start()
        sleepWait()
    }

    private static void sleepWait() {
        Object lock = new Object()
        synchronized(lock) {
            try {
                lock.wait()
            } catch (InterruptedException ignored) {
            }
        }
    }
}
