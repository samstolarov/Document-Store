package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.undo.Command;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Consumer;


public class DocumentStoreImpl implements DocumentStore {
    private final HashTable<URI, Document> document;
    private final Stack<Command> commandStack;
    public DocumentStoreImpl() {
        this.document = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
    }

    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || this.get(uri) == null || key.isEmpty() || key == null) {
            throw new IllegalArgumentException("something here was null or empty when SETTING the metadata");
        }
        String previousValue = get(uri).getMetadataValue(key);
        Command metadataCommand  = new Command(uri, (URI thisURI) -> {
                get(uri).setMetadataValue(key, previousValue);
        });
        commandStack.push(metadataCommand);
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
            undoPutCommand(uri, null);
            putStuff(format, uri, bytes);
            return 0;
        } else {
            Document previousDocument = document.get(uri);
            int hashCode = document.get(uri).hashCode();
            undoPutCommand(uri, previousDocument);
            putStuff(format, uri, bytes);
            return hashCode;
        }
        //convert input to byte array
        //instantiate Document
        //add it to document HashMap
    }
    private void undoPutCommand(URI uri, Document previousDocument){
        Command putCommand  = new Command(uri, (URI thisUri) -> {
            if(previousDocument != null){
                document.put(uri, previousDocument);
            }else{
                document.put(uri, null);
            }
        });
        commandStack.push(putCommand);
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
        Document previousDocument = document.get(url);
        Command deleteCommand  = new Command(url, (URI thisURI) -> {
            document.put(url, previousDocument);
        });
        commandStack.push(deleteCommand);

        if(document.get(url) == null){
            return false;
        }
        document.put(url, null);
        return true;
    }
    @Override
    public void undo() throws IllegalStateException{
        if(commandStack.size() <= 0){
            throw new IllegalStateException("command stack is empty");
        }
        Command command = commandStack.pop();
        command.undo();
    }
    @Override
    public void undo(URI url) throws IllegalStateException{
        Stack<Command> tempStack = new StackImpl<>();
        boolean commandFound = false;

        while(commandStack.size() > 0){
            Command currentCommand = commandStack.pop();
            tempStack.push(currentCommand);
            if(currentCommand.getUri().equals(url)){
                currentCommand.undo();
                commandFound = true;
                break;
            }
        }
        while(tempStack.size() > 0){
            commandStack.push(tempStack.pop());
        }
        if(!commandFound){
            throw new IllegalStateException("no command with that url found");
        }
    }
}
