package edu.yu.cs.com1320.project.stage6.impl;


import edu.yu.cs.com1320.project.stage6.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private final URI uri;
    private String contents;
    private byte[] binaryData;
    private Map<String, String> metaData;
    private Map<String, Integer> wordMap;
    private long lastUseTime;

    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        if (uri == null || uri.getPath().isEmpty() || uri.toString() == null || uri.toString().isEmpty() || text == null || text.isEmpty()){
            throw new IllegalArgumentException("the uri or txt was empty");
        }
        this.uri = uri;
        this.contents = text;
        this.metaData = new HashMap<>();
        this.wordMap = wordCountMap;
        if(this.wordMap == null){
            countWords(text);
        }
        this.lastUseTime = System.nanoTime();
    }
    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("the uri or txt was empty");
        }
        this.uri = uri;
        this.binaryData = binaryData;
        this.metaData = new HashMap<>();
        this.wordMap = new HashMap<>();
        this.lastUseTime = System.nanoTime();
    }
    private void countWords(String txt){
        this.wordMap = new HashMap<>();
        String[] words = txt.split(" ");
        for(String word : words){
            if(wordMap.get(word) == null){
                wordMap.put(word, 1);
            }else{
               wordMap.put(word, wordMap.get(word) + 1);
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
    public HashMap<String, String> getMetadata() {
        HashMap<String, String> hashTable = new HashMap<>();
        Set<String> keys = metaData.keySet();
        for(String key : keys){
            String value = metaData.get(key);
            hashTable.put(key, value);
        }
        return hashTable;
    }
   public void setMetadata(HashMap<String, String> metadata){
        this.metaData = metadata;
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
        if(wordMap.get(word) != null){
            return wordMap.get(word);
        }
        return 0;
    }

    @Override
    public Set<String> getWords() {
        return wordMap.keySet();
    }

    @Override
    public long getLastUseTime() {
        return lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }

    @Override
    public HashMap<String, Integer> getWordMap() {
        return (HashMap<String, Integer>) wordMap;
    }

    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.wordMap = wordMap;
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

    @Override
    public int compareTo(Document o) {
        return Long.compare(this.lastUseTime, o.getLastUseTime());
    }
}
