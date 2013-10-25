package io.github.tomykaira.accelvoice.selector

class Demo {
    public static void main(String[] args) {
        def done = false
        def listener = new SelectionListener() {
            @Override
            void notify(String selected) {
                println(selected)
                done = true
            }
        }
        def collection = new CandidatesCollection(["addAll", "asBoolean", "asImmutable", "asList", "asSynchronized",
                "asType", "collect", "collectAll", "collectEntries", "collectMany", "collectNested", "combinations",
                "containsAll", "count", "countBy", "disjoint", "eachPermutation", "find", "findAll", "findResult",
                "findResults", "flatten", "getAt", "grep", "groupBy", "inject", "intersect", "isCase", "join",
                "leftShift", "max", "min", "multiply", "plus", "removeAll", "retainAll", "sort", "split", "sum",
                "toList", "toListString", "toSet", "unique"], listener)

        RecognizerLibrary.INSTANCE.start(args.length, args, null)

        collection.select()

        while (!done) {
            sleep(1000)
        }

        RecognizerLibrary.INSTANCE.stop()
    }
}
