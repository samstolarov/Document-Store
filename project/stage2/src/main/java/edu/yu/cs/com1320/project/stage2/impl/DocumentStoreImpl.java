package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


public class DocumentStoreImpl implements DocumentStore {
    private final HashTable<URI, Document> document;
    public DocumentStoreImpl() {
        this.document = new HashTableImpl<>();
    }

    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || this.get(uri) == null || key.isEmpty() || key == null) {
            throw new IllegalArgumentException("something here was null or empty when SETTING the metadata");
        }
        return get(uri).setMetadataValue(key, value);
    }

    @Override
    public String getMetadata(URI uri, String key) {
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || this.get(uri) == null || key.isEmpty() || key == null) {
            throw new IllegalArgumentException("something here was null or empty when GETTING the metadata");
        }
        return get(uri).getMetadataValue(key);
    }

    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        byte[] bytes = null;
        if (uri == null || uri.getPath() == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || format == null) {
            throw new IllegalArgumentException("something was null or empty when PUTTING the document");
        }
        if (input == null) {
            if (document.get(uri) == null) {
                return 0;
            } else {
                int hashCode =  document.get(uri).hashCode();
                delete(uri);
                return hashCode;
            }
        }
        try {
            bytes = input.readAllBytes();
        } catch (Exception e) {
            throw new IOException("there was an issue reading the input");
        }

        if (document.get(uri) == null) {
            putStuff(format, uri, bytes);
            return 0;
        } else {
            int hashCode = document.get(uri).hashCode();
            putStuff(format, uri, bytes);
            return hashCode;
        }
        //convert input to byte array
        //instantiate Document
        //add it to document HashMap
    }
    private void putStuff(DocumentFormat format, URI uri, byte[] bytes){
        if (format == DocumentFormat.BINARY) {
            DocumentImpl documentBinary = new DocumentImpl(uri, bytes);
            document.put(uri, documentBinary);
        }
        if (format == DocumentStore.DocumentFormat.TXT) {
            String txt = new String(bytes);
            DocumentImpl documentText = new DocumentImpl(uri, txt);
            document.put(uri, documentText);
        }
    }

    @Override
    public Document get(URI url) {
        return document.get(url);
    }

    @Override
    public boolean delete(URI url) {
        if(document.get(url) == null){
            return false;
        }
        document.put(url, null);
        return true;
    }
}
