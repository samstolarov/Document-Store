package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

public class DocumentImpl implements Document {
    private final URI uri;
    private String contents;
    private byte[] binaryData;
    private final HashMap<String, String> metaData;
    public DocumentImpl(URI uri, String txt){
        if (uri == null || uri.getPath().isEmpty() || uri.toString() == null || uri.toString().isEmpty() || txt == null || txt.isEmpty()){
            throw new IllegalArgumentException("the uri or txt was empty");
        }
        this.uri = uri;
        this.contents = txt;
        this.metaData = new HashMap<>();
    }
    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("the uri or txt was empty");
        }
        this.uri = uri;
        this.binaryData = binaryData;
        this.metaData = new HashMap<>();
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
        return (HashMap<String, String>) metaData.clone();
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
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (contents != null ? contents.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return result;
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
