package io.github.tomykaira.accelvoice.projectdict

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
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

    void insert(String word, int count) {
        this.count += count
        def coupleWith = children.find { p ->
            p.key[0] == word[0]
        }
        if (coupleWith == null) {
            children[word] = new Trie(count)
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
        def target = children.find { it.key[0] == prefix[0] }
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
}
