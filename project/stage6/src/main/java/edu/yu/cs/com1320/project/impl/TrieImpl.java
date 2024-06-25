package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int alphabetSize = 256;
    private Node<Value> root;

    public TrieImpl() {
        this.root = new Node<>();
    }

    private class Node<Value> {
        private final List<Value> values;
        private final Node<Value>[] links;

        private Node() {
            this.values = new ArrayList<>();
            this.links = new Node[alphabetSize];
        }
    }

    @Override
    public void put(String key, Value val) {
        if (val == null) {
            return;
        } else {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node<Value> put(Node x, String key, Value val, int d) {
        if (x == null) {
            x = new Node<>();
        }
        if (d == key.length()) {
            if (!x.values.contains(val)) {
                x.values.add(val);
                return x;
            }
             return x;
        }
        char c = key.charAt(d);
        if (Character.isLetterOrDigit(c)) {
            x.links[c] = this.put(x.links[c], key, val, d + 1);
            return x;
        } else {
            return put(x, key, val, d + 1);
        }

    }

    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        if(key == null){
            throw new IllegalArgumentException("key was null");
        }
        Node<Value> node = this.get(this.root, key, 0);
        if (node == null || key.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(node.values, comparator);
        return node.values;
    }

    @Override
    public Set<Value> get(String key) {
        if(key == null){
            throw new IllegalArgumentException("key was null");
        }
        Set<Value> theSet = new HashSet<>();
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null) {
            return Collections.emptySet();
        }
        for (Value v : x.values) {
            theSet.add(v);
        }
        return theSet;
    }

    private Node get(Node x, String key, int d) {
        if (x == null) {
            return null;
        }

        if (d == key.length()) {
            return x;
        }
        char c = key.charAt(d);
        if(Character.isLetterOrDigit(c)){
            return this.get(x.links[c], key, d + 1);
        }
        else{
            return this.get(x, key, d + 1);
        }


//        return x;
    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if(prefix == null){
            throw new IllegalArgumentException("prefix can't be null");
        }
        List<Value> valueList = new ArrayList<>();
        Node<Value> starterNode = get(this.root, prefix, 0);
        getAllWithPrefix(starterNode, valueList);
        Collections.sort(valueList, comparator);
        return valueList;

        //return valueList;
        //call get to get to the Node containing last letter of the prefix
        //add that node's values to the list
        //traverse further down the nodes (checking if the links of that node are not null)
        //add any values to the list
        //sort and return
    }

    private List<Value> getAllWithPrefix(Node<Value> node, List<Value> valueList) {
        if (node == null) {
            return Collections.emptyList();
        }
        if (node.values != null) {
            valueList.addAll(node.values);
        }
        for (Node<Value> link : node.links) {
            getAllWithPrefix(link, valueList);
        }

        return valueList;
    }
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        List<Value> deletedValues = new ArrayList<>();
        this.root = deleteAllWithPrefix(prefix, this.root, 0, deletedValues);
        Set<Value> deleteSet = new HashSet<>();
        for(Value v : deletedValues){
            deleteSet.add(v);
        }
        return deleteSet;
    }

    private Node<Value> deleteAllWithPrefix(String prefix, Node<Value> node, int d, List<Value> deletedValues) {
        if (node == null) {
            return null;
        }
        if (d == prefix.length()) {
            deletedValues.addAll(getAllWithPrefix(node, deletedValues));
            node = null;
//            if (allLinksNull(node.links)) {
//                return null;
//            }
            return node;
        } else {
            char c = prefix.charAt(d);
            node.links[c] = this.deleteAllWithPrefix(prefix, node.links[c], d + 1, deletedValues);
//            if (allLinksNull(node.links)) {
//                return null;
//            }
            return node;
        }
    }

    private boolean allLinksNull(Node<Value>[] links) {
        for (Node<Value> link : links) {
            if (link != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<Value> deleteAll(String key) {
        Set<Value> deleteSet;
        deleteSet = get(key);
        this.root = deleteAll(this.root, key, 0);
        return deleteSet;
    }
    private Node<Value> deleteAll(Node<Value> x, String key, int d) {
        if (x == null) {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()) {
            x.values.clear();
            return x;
        }
        //continue down the trie to the target node
        else {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
            //return x;
        }
        //this node has a val – do nothing, return the node
        if (x.values != null) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c < alphabetSize; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }
    @Override
    public Value delete(String key, Value val) {
        if(key == null){
            throw new IllegalArgumentException("key was null");
        }
        Node<Value> deleteNode = this.get(this.root, key, 0);
        if(deleteNode == null){
            return null;
        }
        Value deleteValue = null;
        if(deleteNode.values.contains(val)){
            int theValue = deleteNode.values.indexOf(val);
            deleteValue = deleteNode.values.get(theValue);
        }else{
            return null;
        }
        this.delete(this.root, key, 0, deleteValue);
        return deleteValue;
    }

    private Node<Value> delete(Node<Value> node, String key, int d, Value value){
        if(node == null){
            return null;
        }
        if(d == key.length()){
            node.values.remove(value);
            return node;
        }else{
            char c = key.charAt(d);
            if(Character.isLetterOrDigit(c)) {
                node.links[c] = delete(node.links[c], key, d + 1, value);
            }else{
                return this.get(node, key, d + 1);
            }
            //return node;
        }
        if(node.values != null){
            return node;
        }
        for(int i = 0; i < alphabetSize; i++){
            if(node.links[i] != null){
                return node;
            }
        }
        return null;

    }
}
