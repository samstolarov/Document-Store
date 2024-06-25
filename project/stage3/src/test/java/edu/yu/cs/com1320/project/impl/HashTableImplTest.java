package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {

   private HashTable hashTable;
    @BeforeEach
    public void setUp() {
        this.hashTable = new HashTableImpl();
    }
    record Person(int age, String name){}
    //record Car(String model){
//        @Override
//        public int hashCode(){
//            return 3;
//        }
//    }
    @Test
    public void getTest() {
//        Person sam = new Person(20, "sam");
//        Person yonatan = new Person(22, "yonatan");
//        Car honda = new Car("civic");
//        Car toyota = new Car("corolla");
       // hashTable.put(sam, yonatan);
        hashTable.put("honda", "sam");
        hashTable.put("toyota", "yonatan");
        for(int i=0; i<100; i++){
            hashTable.put("dummy"+i,i);
        }
        assertEquals("yonatan", hashTable.get("toyota"));
        assertEquals("sam", hashTable.get("honda"));

        //hashTable.put(true, "hello");
       // assertEquals(yonatan, hashTable.get(sam));
        //assertEquals("hello", hashTable.get(true));
    }

    @Test
    public void putTest() {
        assertNull(hashTable.put("key1", 56));
        assertEquals(56, hashTable.put("key1", 57));
        assertEquals(57, hashTable.put("key1", 58));
        assertEquals(58, hashTable.put("key1", 59));
        assertEquals(59, hashTable.put("key1", false));
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

//    @Test
//    public void keySet() {
//        hashTable.put("key1", 1);
//        hashTable.put("key2", 2);
//        Set<String> mySet = new HashSet<>();
//        mySet.add("key1");
//        mySet.add("key2");
//        assertEquals(mySet, hashTable.keySet());
//    }

//    @Test
//    public void values() {
//        hashTable.put("key1", "value1");
//        hashTable.put("key2", "value2");
//        List<String> myList = new ArrayList<>();
//        myList.add("value1");
//        myList.add("value2");
//        assertEquals(myList, hashTable.values());
//    }

//    @Test
//    public void size() {
//        hashTable.put("key1", 1);
//        hashTable.put("key2", 2);
//        hashTable.put("key3", 3);
//        hashTable.put("key2", null);
//        assertEquals(2, hashTable.size());
//    }
    @Test
    public void resizeArrayTest(){
        for(int i = 0; i < 9238; i++){
            hashTable.put(i, "value" + i);
        }
        for(int i = 0; i < 9238; i++){
            assertTrue(hashTable.containsKey(i));
            assertEquals("value" + i, hashTable.get(i));
        }
        //assertEquals(139, hashTable.size());
    }
}
