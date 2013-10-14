package io.github.tomykaira.accelvoice.selector

class Demo {
    public static void main(String[] args) {
        def collection = new CandidatesCollection(["addAll", "asBoolean", "asImmutable", "asList", "asSynchronized",
                "asType", "collect", "collectAll", "collectEntries", "collectMany", "collectNested", "combinations",
                "containsAll", "count", "countBy", "disjoint", "eachPermutation", "find", "findAll", "findResult",
                "findResults", "flatten", "getAt", "grep", "groupBy", "inject", "intersect", "isCase", "join",
                "leftShift", "max", "min", "multiply", "plus", "removeAll", "retainAll", "sort", "split", "sum",
                "toList", "toListString", "toSet", "unique"])

        RecognizerLibrary.INSTANCE.start(args.length, args)

        println(collection.select())

        RecognizerLibrary.INSTANCE.stop()
    }
}
