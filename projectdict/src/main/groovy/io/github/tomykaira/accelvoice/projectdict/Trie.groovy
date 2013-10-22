package io.github.tomykaira.accelvoice.projectdict

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
class Trie {
    int count
    Map<String, Trie> children

    def Trie() {
        this(0)
    }

    def Trie(int count) {
        this(count, [:])
    }

    def Trie(int count, Map<String, Trie> children) {
        this.count = count
        this.children = children
    }

    @Override
    String toString() {
        toString(0)
    }

    String toString(int indent) {
        def h = " " * indent
        count + "\n" + children.collect { p -> h + p.key + ": " + p.value.toString(indent + 2) }.join("\n")
    }

    void insert(String word, int count) {
        this.count += count
        if (word == '') {
            children[""] = new Trie(count)
            return
        }
        def coupleWith = findCorrespondingChild(word)
        if (coupleWith == null) {
            children[word] = new Trie(count, ["": new Trie(count)])
            return
        }
        def (prefix, existing, adding) = commonPrefix(coupleWith.key, word)
        if (existing == '') {
            coupleWith.value.insert(adding, count)
            return
        }
        children.remove(coupleWith.key)
        def newTrie = new Trie()
                .insertSub(existing, coupleWith.value)
                .insertSub(adding, new Trie(count))
        children.put(prefix, newTrie)
    }

    Trie find(String prefix) {
        find(prefix, "")
    }

    Trie find(String prefix, String used) {
        def target = findCorrespondingChild(prefix)
        if (target == null)
            return new Trie()
        def (common, existing, remain) = commonPrefix(target.key, prefix)
        if (remain == '') {
            new Trie().insertSub(used + target.key, target.value)
        } else if (remain != '' && existing == '') {
            target.value.find(remain, used + common)
        } else {
            new Trie() // Not found
        }
    }

    private def findCorrespondingChild(String word) {
        children.find { p ->
            !p.key.isEmpty() && p.key[0] == word[0]
        }
    }

    private Trie insertSub(String word, Trie sub) {
        count += sub.count
        children[word] = sub
        this
    }

    private static List<String> commonPrefix(String l, String r) {
        int i = 0;
        int max = Math.min(l.length(), r.length())
        while (i < max && l[i] == r[i])
            i++
        [l.substring(0, i), l.substring(i), r.substring(i)]
    }

    List<String> toWordList() {
        if (children.isEmpty())
            return [""]
        def result = []
        children.each { child ->
            result.addAll(child.value.toWordList().collect { child.key + it })
        }
        result
    }
}
