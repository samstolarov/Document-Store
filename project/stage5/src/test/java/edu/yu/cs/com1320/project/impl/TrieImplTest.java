package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TrieImplTest {

    private Trie<String> trie;
    @BeforeEach
    public void setUp() {
        trie = new TrieImpl<>();
    }

    @Test
    public void putAndGetTest() {
        trie.put("apple", "red");
        trie.put("banana", "yellow");
        trie.put("apple", "green");

        assertEquals(Set.of("red", "green"), trie.get("apple"));
        assertEquals(Set.of("yellow"), trie.get("banana"));
        assertTrue(trie.get("pear").isEmpty());
    }

    @Test
    public void getSortedTest() {
        trie.put("apple", "red");
        trie.put("banana", "yellow");

        List<String> sortedValues = trie.getSorted("banana", Comparator.naturalOrder());
        assertEquals(List.of("yellow"), sortedValues);

    }

    @Test
    public void getAllWithPrefixSortedTest() {
        trie.put("apple", "red");
        trie.put("banana", "yellow");
        trie.put("app", "blue");

        assertEquals(Set.of("red") ,trie.get("apple"));
        assertEquals(Set.of("yellow"), trie.get("banana"));
        assertEquals(Set.of("blue"), trie.get("app"));

        List<String> sortedValues = trie.getAllWithPrefixSorted("app", Comparator.naturalOrder());
        assertEquals(List.of("blue", "red"), sortedValues);
    }

    @Test
    public void deleteAllWithPrefixTest() {
        trie.put("apple", "red");
        trie.put("banana", "yellow");
        trie.put("app", "blue");
        trie.put("ap", "green");
        Set deletes;
        deletes = trie.deleteAllWithPrefix("ap");

        assertEquals(Set.of("red", "blue", "green"), deletes);
        assertTrue(trie.get("apple").isEmpty());
        assertTrue(trie.get("app").isEmpty());
        assertTrue(trie.get("ap").isEmpty());
        assertFalse(trie.get("banana").isEmpty());

    }

    @Test
    public void deleteAll() {
        trie.put("apple", "red");
        trie.put("banana", "yellow");
        trie.put("apple", "green");

        trie.put("apple", "blue");
        assertEquals(Set.of("red", "green", "blue"), trie.deleteAll("apple"));
        assertTrue(trie.get("apple").isEmpty());
        assertFalse(trie.get("banana").isEmpty());
    }

    @Test
    public void deleteTest() {
        trie.put("apple#", "red");
        trie.put("banana", "yellow");
        trie.put("apple", "green");
        trie.put("banana", "orange");

        assertEquals("red", trie.delete("apple", "red"));
        assertFalse(trie.get("apple").isEmpty());
        assertEquals(Set.of("green"), trie.get("apple"));
        assertEquals("yellow", trie.delete("banana", "yellow"));
        assertFalse(trie.get("banana").isEmpty());
        assertEquals(Set.of("orange"), trie.get("banana"));
    }
}