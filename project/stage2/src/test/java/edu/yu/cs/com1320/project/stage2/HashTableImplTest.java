package edu.yu.cs.com1320.project.stage2;


import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {

    private HashTableImpl hashTable;
    @BeforeEach
    public void setUp() {
        hashTable = new HashTableImpl<>();
    }

    @Test
    public void getTest() {
        hashTable.put("key1", 3);
        hashTable.put(true, "hello");
        assertEquals(3, hashTable.get("key1"));
        assertEquals("hello", hashTable.get(true));
    }

    @Test
    public void putTest() {
        assertNull(hashTable.put("key1", 56));
        assertEquals(56, hashTable.put("key1", false));
        assertEquals(false, hashTable.put("key1", null));
    }

    @Test
    public void containsKey() {
        assertThrows(NullPointerException.class, () -> {
            hashTable.containsKey(null);
        });
        hashTable.put("key1", 74.5);
        assertTrue(hashTable.containsKey("key1"));
        assertFalse(hashTable.containsKey("key2"));
    }

    @Test
    public void keySet() {
        hashTable.put("key1", 1);
        hashTable.put("key2", 2);
        Set<String> mySet = new HashSet<>();
        mySet.add("key1");
        mySet.add("key2");
        assertEquals(mySet, hashTable.keySet());
    }

    @Test
    public void values() {
        hashTable.put("key1", "value1");
        hashTable.put("key2", "value2");
        List<String> myList = new ArrayList<>();
        myList.add("value1");
        myList.add("value2");
        assertEquals(myList, hashTable.values());
    }

    @Test
    public void size() {
        hashTable.put("key1", 1);
        hashTable.put("key2", 2);
        hashTable.put("key3", 3);
        hashTable.put("key2", null);
        assertEquals(2, hashTable.size());
    }
}