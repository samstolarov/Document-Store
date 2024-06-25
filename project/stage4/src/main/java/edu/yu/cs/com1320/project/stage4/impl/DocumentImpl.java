package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage4.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DocumentImpl implements Document {
    private final URI uri;
    private String contents;
    private byte[] binaryData;
    private final HashTable<String, String> metaData;
    private final HashMap<String, Integer> wordCount;
    public DocumentImpl(URI uri, String txt){
        if (uri == null || uri.getPath().isEmpty() || uri.toString() == null || uri.toString().isEmpty() || txt == null || txt.isEmpty()){
            throw new IllegalArgumentException("the uri or txt was empty");
        }
        this.uri = uri;
        this.contents = txt;
        this.metaData = new HashTableImpl<>();
        this.wordCount = new HashMap<>();
        countWords(txt);
    }
    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("the uri or txt was empty");
        }
        this.uri = uri;
        this.binaryData = binaryData;
        this.metaData = new HashTableImpl<>();
        this.wordCount = new HashMap<>();
    }
    private void countWords(String txt){
        int count = 0;
        String[] words = txt.split(" ");
        for(String word : words){
            if(wordCount.get(word) == null){
                wordCount.put(word, 1);
            }else{
               wordCount.put(word, wordCount.get(word) + 1);
                }
            }
        }
    @Override
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isEmpty()){
            throw new IllegalArgumentException("key was null or empty");
        }
        return metaData.put(key, value);
    }

    @Override
    public String getMetadataValue(String key) {
        if(key == null || key.isEmpty()){
            throw new IllegalArgumentException("key was null or empty");
        }
        return metaData.get(key);
    }

    @Override
    public HashTable<String, String> getMetadata() {
        HashTable<String, String> hashTable = new HashTableImpl<>();
        Set<String> keys = metaData.keySet();
        for(String key : keys){
            String value = metaData.get(key);
            hashTable.put(key, value);
        }
        return hashTable;
    }

    @Override
    public String getDocumentTxt() {
        return this.contents;
    }

    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public int wordCount(String word) {
        if(wordCount.get(word) != null){
            return wordCount.get(word);
        }
        return 0;
    }

    @Override
    public Set<String> getWords() {
//        Set<String> theSet = new HashSet<>();
//        String[] txt = this.contents.split(" ");
//        for(String word : txt){
//            theSet.add(word);
//        }
//        return theSet;
        return wordCount.keySet();
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (contents != null ? contents.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }
    @Override
    public boolean equals(Object e){
        if(this == e){
            return true;
        }
        if(e == null){
            return false;
        }
        if(!(e instanceof DocumentImpl)){
            return false;
        }
        if(getClass() != e.getClass()){
            return false;
        }
        Document thisBrokerageAccount = (Document) e;
        return thisBrokerageAccount.hashCode() == this.hashCode();
        //hey
    }
}
