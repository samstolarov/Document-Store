package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import java.io.IOException;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {

    private static final int MAX = 4;
    private Node root;
    private int height;
    private PersistenceManager<Key, Value> pm;
    public BTreeImpl(){
        this.root = new Node(0);
        this.height = 0;
    }

    private static class Node <Key extends Comparable<Key>, Value> {
        private int entryCount;
        private Entry<Key, Value>[] entries;

        private Node(int k){
            this.entries = (Entry<Key, Value>[]) new Entry[MAX];
            this.entryCount = k;
        }
    }
    private static class Entry <Key extends Comparable<Key>, Value>{
        private Comparable key;
        private Value value;
        private Node<Key, Value> child;
        private boolean isInDisk;
        private Entry(Comparable key, Value value, Node child){
            this.key = key;
            this.value = value;
            this.child = child;
            this.isInDisk = false;
        }
    }
    @Override
    public Value get(Key k) {
        Entry<Key, Value> theEntry = this.get(this.root, k, this.height);
        if(theEntry != null && isInDisk(theEntry)){
            try {
                Value theValue = pm.deserialize(k);
                theEntry.isInDisk = false;
                pm.delete(k);
                theEntry.value = theValue;
                return theValue;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if(theEntry == null){
            return null;
        }else{
            return theEntry.value;
        }
    }

    private boolean isInDisk(Entry<Key,Value> theEntry) {
        if(theEntry.isInDisk){
            return true;
        }
        return false;
    }

    private Entry<Key, Value> get(Node currentNode, Key key, int height){
        Entry[] entries = currentNode.entries;
        if(height == 0){
            for(int i = 0; i < currentNode.entryCount; i++){
                if(isEqual(key, entries[i].key)){
                    return entries[i];
                }
            }
            return null;
        }else{
            for(int i = 0; i < currentNode.entryCount; i++){
                if(i + 1 == currentNode.entryCount || isLess(key, entries[i + 1].key)){
                    return this.get(entries[i].child, key, height - 1);
                }
            }
            return null;
        }
    }

    private boolean isLess(Comparable key1, Comparable key2) {
        return key1.compareTo(key2) < 0;
    }

    @Override
    public Value put(Key k, Value v) {
        Entry<Key, Value> alreadyThere = this.get(this.root, k, this.height);
        //calling get already handles the deserialization, so I don't need to call it again here (I think)
        if(alreadyThere != null) {
            Value oldValue = alreadyThere.value;
            alreadyThere.value = v;
            return oldValue;
        }
        Node<Key, Value> newNode = this.put(this.root, k, v, this.height);

        if (newNode == null){//no split of root, we’re done
            return null;
        }else{
            Node newRoot = new Node(2);
            newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
            newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
            this.root = newRoot;
            this.height++;
            return null;
        }
    }
    private Node<Key, Value> put(Node currentNode, Key key, Value value, int height){
        int j;
        Entry newEntry = new Entry(key, value, null);
        if (height == 0) {
            for (j = 0; j < currentNode.entryCount; j++) {
                if (isLess(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        }

        // internal node
        else {
            for (j = 0; j < currentNode.entryCount; j++) {
                if ((j + 1 == currentNode.entryCount) || isLess(key, currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, value, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.value = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < MAX) {
            return null;
        }
        else
        {
            return this.split(currentNode);
        }
    }
    private Node split(Node currentNode) {
        Node newNode = new Node(MAX / 2);
        for (int j = 0; j < MAX / 2; j++) {
            newNode.entries[j] = currentNode.entries[MAX / 2 + j];
            currentNode.entries[MAX/2 + j] = null;
        }
        currentNode.entryCount = MAX / 2;

        //external node

        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws IOException {
        if(this.pm == null){
            throw new IllegalStateException("persistence manager hasn't been set");
        }
        Value val  = this.get(k);
        Entry<Key, Value> theEntry = this.get(this.root, k, this.height);
        if (theEntry != null) {
            theEntry.isInDisk = true;
            theEntry.value = null;
        }
        pm.serialize(k, val);
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.pm = pm;
    }
    private boolean isEqual(Comparable key1, Comparable key2){
        return key1.compareTo(key2) == 0;
    }
}

