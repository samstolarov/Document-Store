package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;


public class DocumentStoreImpl implements DocumentStore {
    private final HashTable<URI, Document> documentTable;
    public final Stack<Undoable> commandStack;
    private final Trie<Document> documentTrie;
    public DocumentStoreImpl() {
        this.documentTable = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.documentTrie = new TrieImpl<>();
    }

    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || this.get(uri) == null || key.isEmpty() || key == null) {
            throw new IllegalArgumentException("something here was null or empty when SETTING the metadata");
        }
        String previousValue = get(uri).getMetadataValue(key);
        Undoable metadataCommand  = new GenericCommand(uri, (thisURI) -> {
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
            if (documentTable.get(uri) == null) {
                return 0;
            } else {
                int hashCode =  documentTable.get(uri).hashCode();
                delete(uri);
                return hashCode;
            }
        }
        try {
            bytes = input.readAllBytes();
        } catch (Exception e) {
            throw new IOException("there was an issue reading the input");
        }
        if (documentTable.get(uri) == null) {
            undoPutCommand(uri, null);
            putStuff(format, uri, bytes);
            return 0;
        } else {
            Document previousDocument = documentTable.get(uri);
            int hashCode = documentTable.get(uri).hashCode();
            undoPutCommand(uri, previousDocument);
            putStuff(format, uri, bytes);
            return hashCode;
        }
        //convert input to byte array
        //instantiate Document
        //add it to document HashMap
    }
    private void undoPutCommand(URI uri, Document previousDocument){
        Undoable putCommand;
        //= new GenericCommand(uri, (thisUri) -> {
            if(previousDocument != null){
                putCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                    documentTable.put(uri, previousDocument);
                });
            }else{
                putCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                    documentTable.put(uri, null);
                });
            }
        //});
        commandStack.push(putCommand);
    }
    private void putStuff(DocumentFormat format, URI uri, byte[] bytes){
        if (format == DocumentFormat.BINARY) {
            DocumentImpl documentBinary = new DocumentImpl(uri, bytes);
            documentTable.put(uri, documentBinary);
        }
        if (format == DocumentStore.DocumentFormat.TXT) {
            String txt = new String(bytes);
            String[] words = txt.split(" ");
            DocumentImpl documentText = new DocumentImpl(uri, txt);
            documentTable.put(uri, documentText);
            for(String word : words){
                Set<Document> set = documentTrie.get(word);
                if(!set.contains(documentText)){
                    documentTrie.put(word, documentText);
                }

            }
        }
    }
    @Override
    public Document get(URI url) {
        return documentTable.get(url);
    }

    @Override
    public boolean delete(URI url) {
        Document previousDocument = documentTable.get(url);
        if(previousDocument != null){
            Set<String> words = previousDocument.getWords();
            if(words != null && !words.isEmpty()){
                for(String word : previousDocument.getWords()){
                    documentTrie.delete(word, previousDocument);
                }
            }
        }
        Undoable deleteCommand  = new GenericCommand<>(url, (URI thisURI) -> {
            documentTable.put(url, previousDocument);
        });
        commandStack.push(deleteCommand);

        if(documentTable.get(url) == null){
            return false;
        }
        documentTable.put(url, null);
        return true;
    }
    @Override
    public void undo() throws IllegalStateException{
        if(commandStack.size() <= 0){
            throw new IllegalStateException("command stack is empty");
        }
        Undoable command = commandStack.pop();
        if(command instanceof CommandSet){
            ((CommandSet<URI>) command).undoAll();
        }
        if(command instanceof GenericCommand<?>){
            command.undo();
        }
    }
    @Override
    public void undo(URI url) throws IllegalStateException{
        Stack<Undoable> tempStack = new StackImpl<>();
        boolean commandFound = false;
        while(commandStack.size() > 0){
            Undoable currentCommand = commandStack.pop();
            tempStack.push(currentCommand);
            if(currentCommand instanceof GenericCommand){
                if((((GenericCommand<URI>) currentCommand).getTarget().equals(url))){
                    currentCommand.undo();
                    commandFound = true;
                    break;
                }
            }
            else if(currentCommand instanceof CommandSet){
                if(((CommandSet<URI>)currentCommand).containsTarget(url)){
                    CommandSet<URI> newCommand = (CommandSet<URI>)currentCommand;
                    newCommand.undo(url);
                    commandFound = true;
                    if(newCommand.isEmpty()) {
                        continue;
                    }
                }
            }
            if(commandFound){
                break;
            }
            tempStack.push(currentCommand);
        }
        pushFromTempStack(tempStack, commandFound);
    }

    private void pushFromTempStack(Stack<Undoable> tempStack, boolean commandFound) {
        while(tempStack.size() > 0){
            commandStack.push(tempStack.pop());
        }
        if(!commandFound){
            throw new IllegalStateException("no command with that url found");
        }
    }

    @Override
    public List<Document> search(String keyword) {
        Set<Document> searchSet = documentTrie.get(keyword);
        List<Document> searchList = new ArrayList<>();
        Comparator<Document> comparator = wordComparator(keyword);
        for(Document doc : searchSet){
            searchList.add(doc);
        }
        searchList.sort(comparator);
        return searchList;
    }
    private Comparator<Document> wordComparator(String keyword){
        Comparator<Document> comparator = (doc1, doc2) -> {
            if(doc1.wordCount(keyword) > doc2.wordCount(keyword)){
                return -1;
            }
            else if(doc1.wordCount(keyword) < doc2.wordCount(keyword)){
                return 1;
            }
            else{
                return 0;
            }
        };
            return comparator;
    }
    private Comparator<Document> prefixComparator(String prefix){
        Comparator<Document> comparator = (doc1, doc2) ->{
          if(prefixCount(doc1, prefix) > prefixCount(doc2, prefix)){
              return -1;
          }else if(prefixCount(doc1, prefix) < prefixCount(doc2, prefix)){
              return 1;
          }else{
              return 0;
          }
        };
        return comparator;
    }
    private int prefixCount (Document doc, String prefix){
        int count = 0;
        Set<String> words = doc.getWords();
        for(String word : words){
            if(word.startsWith(prefix)){
                count += doc.wordCount(word);
            }
        }
        return count;
    }
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        Comparator<Document> comparator = prefixComparator(keywordPrefix);
        List<Document> prefixList = documentTrie.getAllWithPrefixSorted(keywordPrefix, comparator);
        prefixList.sort(comparator);
        if(!prefixList.isEmpty()){
            return prefixList;
        }else{
            return Collections.emptyList();
        }
    //sorts by prefixes as individual words, have to sort by prefixes as part of other words
        //maybe I fixed it? depends if the method being private in DocStore as opposed to DocImpl makes a difference
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deleteSet = new HashSet<>();
        Set<Document> documentsDeleted = documentTrie.deleteAll(keyword);
        for(Document doc : documentsDeleted){
            Set<String> docWords = doc.getWords();
            for(String word : docWords){
                documentTrie.delete(word, doc);
            }
            documentTable.put(doc.getKey(), null);
        }

        for(Document doc : documentsDeleted){
            deleteSet.add(doc.getKey());
        }
        this.undoDeleteAll(deleteSet, documentsDeleted);
        return deleteSet;
    }
    private void undoDeleteAll(Set<URI> deleteSet, Set<Document> documentsDeleted) {
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : documentsDeleted) {
            GenericCommand<URI> deleteCommand = null;
            for (URI uri : deleteSet) {
                if (uri == doc.getKey()) {
                    deleteCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                        documentTable.put(doc.getKey(), doc);
                        Set<String> words = doc.getWords();
                            for (String word : words) {
                                documentTrie.put(word, doc);
                            }
                    });
                }
            }
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> deletePrefixSet = new HashSet<>();
        Set<Document> documentsPrefixDeleted = documentTrie.deleteAllWithPrefix(keywordPrefix);
        for(Document doc : documentsPrefixDeleted){
            Set<String> words = doc.getWords();
            for(String word : words){
                if(word.startsWith(keywordPrefix)){
                    documentTrie.delete(word, doc);
                    //maybe delete() maybe deleteAll()? Idk if it accomplishes the same thing in this scenario
                }
            }
            documentTable.put(doc.getKey(), null);
        }
        for(Document doc : documentsPrefixDeleted){
            deletePrefixSet.add(doc.getKey());
        }
        this.undoDeleteAllWithPrefix(deletePrefixSet, documentsPrefixDeleted);
        return deletePrefixSet;
    }
    private void undoDeleteAllWithPrefix(Set<URI> deletePrefixSet, Set<Document> documentsPrefixDeleted){
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : documentsPrefixDeleted) {
            GenericCommand<URI> deleteCommand = null;
            for (URI uri : deletePrefixSet) {
                if (uri == doc.getKey()) {
                    deleteCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                        documentTable.put(doc.getKey(), doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc);
                        }
                    });
                }
            }
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        List<Document> documentList = new ArrayList<>();
        for(Document doc : documentTable.values()){
            boolean matches = true;
            for(Map.Entry<String, String>  entry : keysValues.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                if(!doc.getMetadata().containsKey(key) || !doc.getMetadata().get(key).equals(value)){
                    matches = false;
                    break;
                }
            }
            if(matches){
                documentList.add(doc);
            }
        }
        return documentList;
    }

    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> keywordList = this.search(keyword);
        List<Document> metadataList = this.searchByMetadata(keysValues);
        List<Document> bothList = new ArrayList<>();
        for(Document keyDoc : keywordList){
            for(Document metadataDoc : metadataList){
                if(keyDoc.equals(metadataDoc)){
                    bothList.add(keyDoc);
                }
            }
        }
        bothList.sort(wordComparator(keyword));
        return bothList;
    }

    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> prefixList = this.searchByPrefix(keywordPrefix);
        List<Document> metadaList = this.searchByMetadata(keysValues);
        List<Document> bothList = new ArrayList<>();
        for(Document keyDoc : prefixList){
            for(Document metadataDoc : metadaList){
                if(keyDoc.equals(metadataDoc)){
                    bothList.add(keyDoc);
                }
            }
        }
        bothList.sort(prefixComparator(keywordPrefix));
        return bothList;
    }

    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        List<Document> metadataDocuments = this.searchByMetadata(keysValues);
        for(Document doc : metadataDocuments){
            deletedDocuments.add(doc.getKey());
            Set<String> docWords = doc.getWords();
            for(String word : docWords){
                documentTrie.delete(word, doc);
            }
            documentTable.put(doc.getKey(), null);
        }
        this.undoDeleteAllWithMetadata(deletedDocuments, metadataDocuments);
        return deletedDocuments;
    }

    private void undoDeleteAllWithMetadata(Set<URI> deletedDocuments, List<Document> metadataDocuments){
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : metadataDocuments) {
            GenericCommand<URI> deleteCommand = null;
            for (URI uri : deletedDocuments) {
                if (uri == doc.getKey()) {
                    deleteCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                        documentTable.put(doc.getKey(), doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc);
                        }
                    });
                }
            }
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }

    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> bothDocs = this.searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> deletedURIs = new HashSet<>();
        for(Document doc : bothDocs){
            deletedURIs.add(doc.getKey());
            Set<String> docWords = doc.getWords();
            for(String word : docWords){
                documentTrie.delete(word, doc);
            }
            documentTable.put(doc.getKey(), null);

        }
        this.undoDeleteAllWithKeywordAndMetadata(deletedURIs, bothDocs);
        return deletedURIs;
    }

    private void undoDeleteAllWithKeywordAndMetadata(Set<URI> deletedURIs, List<Document> bothDocs){
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : bothDocs) {
            GenericCommand<URI> deleteCommand = null;
            for (URI uri : deletedURIs) {
                if (uri == doc.getKey()) {
                    deleteCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                        documentTable.put(doc.getKey(), doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc);
                        }
                    });
                }
            }
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> bothDocs = this.searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> deletedURIs = new HashSet<>();
        for(Document doc : bothDocs){
            deletedURIs.add(doc.getKey());
            Set<String> docWords = doc.getWords();
            for(String word : docWords){
                documentTrie.delete(word, doc);
            }
            documentTable.put(doc.getKey(), null);
        }
        this.undoDeleteAllWithPrefixAndMetadata(deletedURIs, bothDocs);
        return deletedURIs;
    }
    private void undoDeleteAllWithPrefixAndMetadata(Set<URI> deletedURIs, List<Document> bothDocs){
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : bothDocs) {
            GenericCommand<URI> deleteCommand = null;
            for (URI uri : deletedURIs) {
                if (uri == doc.getKey()) {
                    deleteCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                        documentTable.put(doc.getKey(), doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc);
                        }
                    });
                }
            }
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }
}
