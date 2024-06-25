package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.util.*;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    private Entry<Key, Value>[] array;
    private int size;
    public HashTableImpl(){
        this.array = new Entry[5];
        this.size = 0;
    }
   private class Entry<K, V>{
        private final K key;
        private V value;
        private Entry<K, V> next;
        public Entry(K key, V value){
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
    @Override
    public Value get(Key k) {
       for(Entry<Key, Value> entry : array){
           while(entry != null){
               if(entry.key.equals(k)){
                   return entry.value;
               }
               entry = entry.next;
           }
       }

       return null;
    }

    @Override
    public Value put(Key k, Value v) {
        if (v == null){
            return delete(k);
        }
        if (k == null){
            throw new IllegalArgumentException("key was null");
        }
        double loadFactor = (double) size / array.length;
        if(loadFactor > 0.99){
            resizeArray();
        }
        int index = hashFunction(k, array.length);
        Entry<Key, Value> entry = array[index];
        while(entry != null){
            if(entry.key.equals(k)){
                Value oldValue = entry.value;
                entry.value = v;
                return oldValue;
            }
            entry = entry.next;
        }
        Entry<Key, Value> newEntry = new Entry<>(k, v);
        newEntry.next = array[index];
        array[index] = newEntry;
        size++;
        return null;
    }

    @Override
    public boolean containsKey(Key key) {
        if (key == null){
            throw new NullPointerException("the key was null");
        }
        int newIndex = hashFunction(key, array.length);
        Entry<Key, Value> entry = array[newIndex];
        //for(Entry<Key, Value> entry : array){
            while(entry != null){
                if(entry.key.equals(key)){
                    return true;
                }
                entry = entry.next;
            }
        //}

        return false;
    }

    @Override
    public Set<Key> keySet() {
        Set<Key> theSet = new HashSet<>();
        for(Entry<Key, Value> entry : array){
            while(entry != null){
                theSet.add(entry.key);
                entry = entry.next;
            }
        }
        return Collections.unmodifiableSet(theSet);
    }

    @Override
    public Collection<Value> values() {
        List<Value> theList = new ArrayList<>();
        for(Entry<Key, Value> entry : array){
            while(entry != null){
                theList.add(entry.value);
                entry = entry.next;
            }
        }
        return Collections.unmodifiableList(theList);
    }

    @Override
    public int size() {
        return size;
    }
    private int hashFunction(Key key, int arrayLength){
        int mod = (key.hashCode() & 0x7fffffff);
        return mod % arrayLength;
    }
    private Value delete(Key k){
        int index = hashFunction(k, array.length);
        Entry<Key, Value> currentEntry = array[index];
        Entry<Key, Value> previous = null;
        while(currentEntry != null){
            if(currentEntry.key.equals(k)){
                if(previous == null){
                    array[index] = currentEntry.next;
                }else{
                    previous.next = currentEntry.next;
                }
                size--;
                return currentEntry.value;
            }
            previous = currentEntry;
            currentEntry = currentEntry.next;
        }
        return null;
    }
    private void resizeArray(){
        int newCapacity = array.length * 2;
        Entry<Key, Value>[] newArray = new Entry[newCapacity];
        for(Entry<Key, Value> entry : array){
            while(entry != null){
                Entry<Key, Value> next = entry.next;
                int newIndex = hashFunction(entry.key, newCapacity);
                entry.next = newArray[newIndex];
                newArray[newIndex] = entry;
                entry = next;
            }
        }
        array = newArray;
    }
}
