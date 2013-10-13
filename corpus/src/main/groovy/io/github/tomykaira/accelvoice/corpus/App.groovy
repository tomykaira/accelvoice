package io.github.tomykaira.accelvoice.corpus

class App {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Project root is not specified")
            System.exit(1)
        }
        def root = new File(args[0])
        def project = new JavaProject(root)
        println(project.reportAbnormalTokens())
    }
}
