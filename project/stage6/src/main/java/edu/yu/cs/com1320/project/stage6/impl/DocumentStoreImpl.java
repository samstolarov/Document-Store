package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;


public class DocumentStoreImpl implements DocumentStore {
    private final BTree<URI, Document> bTree;
    private final Stack<Undoable> commandStack;
    private final Trie<URI> documentTrie;
    private final Trie<Map<URI, String>> metadataTrie;
    private int maxDocCount;
    private int maxDocBytes;
    private int docCount;
    private int docBytes;
    private final MinHeap<newMinHeap> minHeap;
    private final Set<URI> uriSet;

    public DocumentStoreImpl() {
       this(null);
    }
    public DocumentStoreImpl(File baseDir){
        this.bTree = new BTreeImpl<>();
        this.commandStack = new StackImpl<>();
        this.documentTrie = new TrieImpl<>();
        this.metadataTrie = new TrieImpl<>();
        this.maxDocBytes = -1;
        this.maxDocCount = -1;
        this.docCount = 0;
        this.docBytes = 0;
        this.minHeap = new MinHeapImpl<>();
        this.uriSet = new HashSet<>();
        DocumentPersistenceManager pm = new DocumentPersistenceManager(baseDir);
        bTree.setPersistenceManager(pm);
    }
    private static class newMinHeap implements Comparable<newMinHeap> {
        private BTree<URI, Document> bTree;
        private URI uri;

        private newMinHeap(URI uri, BTree<URI, Document> bTree) {
            this.bTree = bTree;
            this.uri = uri;
        }

        @Override
        public int compareTo(newMinHeap e) {
            if (bTree.get(this.uri) == null && bTree.get(e.uri) != null) {
                return 1;
            } else if (bTree.get(this.uri) != null && bTree.get(e.uri) == null) {
                return -1;
            } else {
                return bTree.get(this.uri).compareTo(bTree.get(e.uri));
            }
        }

        @Override
        public boolean equals(Object e) {
            if (this == e) {
                return true;
            }
            if (e == null) {
                return false;
            }
            if (this.getClass() != e.getClass()) {
                return false;
            }
            newMinHeap theMinHeap = (newMinHeap) e;
            return this.uri == theMinHeap.uri;
        }

        @Override
        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + (bTree.get(uri).getDocumentTxt() != null ? bTree.get(uri).getDocumentTxt().hashCode() : 0);
            result = 31 * result + Arrays.hashCode(bTree.get(uri).getDocumentBinaryData());
            return Math.abs(result);
        }
    }

    @Override
    public String setMetadata(URI uri, String key, String value) throws IOException{
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || this.get(uri) == null || key.isEmpty() || key == null) {
            throw new IllegalArgumentException("something here was null or empty when SETTING the metadata");
        }
        Map<URI, String> metadataMap = new HashMap<>();
        metadataMap.put(uri, value);
        Document doc = get(uri);
        String previousValue = doc.getMetadataValue(key);
        Undoable metadataCommand = new GenericCommand(uri, (thisURI) -> {
           //Document document =  this.get(uri);
           doc.setMetadataValue(key, previousValue);
           metadataTrie.delete(key, metadataMap);
        });
        commandStack.push(metadataCommand);
        long newTime = System.nanoTime();
        setNewTime(doc, newTime);
        String theValue =  get(uri).setMetadataValue(key, value);
        metadataTrie.put(key, metadataMap);
        return theValue;
    }

    @Override
    public String getMetadata(URI uri, String key) throws IOException{
        if (uri == null || uri.getPath().isEmpty() || uri.toString().isEmpty() || this.get(uri) == null || key.isEmpty() || key == null) {
            throw new IllegalArgumentException("something here was null or empty when GETTING the metadata");
        }
        long newTime = System.nanoTime();
        Document doc = get(uri);
        setNewTime(doc, newTime);
        if(!metadataTrie.get(key).equals(Collections.emptySet())){
            Set<Map<URI, String>> maps =  metadataTrie.get(key);
            for(Map<URI, String> map : maps){
                if(map.containsKey(uri)){
                    return map.get(uri);
                }
            }
        }
        return null;
    }

    @Override
    public int put(InputStream input, URI url, DocumentStore.DocumentFormat format) throws IOException {
        /*
        Have to have some kind of variable to store the docCount and docBytes, and then check to see if those variables
        are above the maxDocCount or above the maxDocBytes. If it is, remove as many last used docs from the heap as necessary,
        (remove it from the trie and table as well) and call reHeapify to make everything nice and ordered again.
        Otherwise, just put it in and add it to the heap. Got to do this for undo as well.
        */

        byte[] bytes;
        if (url == null || url.getPath() == null || url.getPath().isEmpty() || url.toString().isEmpty() || format == null) {
            throw new IllegalArgumentException("something was null or empty when PUTTING the document");
        }
        if (input == null) {
            if (bTree.get(url) == null) {
                return 0;
            } else {
                int hashCode = bTree.get(url).hashCode();
                delete(url);
                return hashCode;
            }
        }
        try {
            bytes = input.readAllBytes();
        } catch (Exception e) {
            throw new IOException("there was an issue reading the input");
        }
        if(bytes.length > maxDocBytes && maxDocBytes != -1){
            throw new IllegalArgumentException("Trying to put in a document larger than allowed size");
        }
        if (get(url) == null) {
            undoPutCommand(url, null);
            putStuff(format, url, bytes);
            return 0;
        } else {
            Document previousDocument = get(url);
            int hashCode = previousDocument.hashCode();
            undoPutCommand(url, previousDocument);
            putStuff(format, url, bytes);
            return hashCode;
        }
        //convert input to byte array
        //instantiate Document
        //add it to document HashMap
    }

    private void undoPutCommand(URI uri, Document previousDocument) {
        Undoable putCommand;
        long lastTime = System.nanoTime();
        if (previousDocument != null) {
            putCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                this.delete(uri);
                bTree.put(uri, previousDocument);
            });
        } else {
            putCommand = new GenericCommand<>(uri, (URI thisURI) -> {
                Document nextPreviousDocument = bTree.get(uri);
                MinHeap<Document> foundDoc = new MinHeapImpl<>();
                if (nextPreviousDocument != null) {
                    MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
                    removeFromMinHeap(nextPreviousDocument.getKey(), foundDoc, tempHeap, nextPreviousDocument);
                    Set<String> words = nextPreviousDocument.getWords();
                    if (words != null && !words.isEmpty()) {
                        for (String word : nextPreviousDocument.getWords()) {
                            documentTrie.delete(word, nextPreviousDocument.getKey());
                        }
                    }
                }
                bTree.put(uri, null);
            });
        }
        commandStack.push(putCommand);
    }

    private void putStuff(DocumentFormat format, URI uri, byte[] bytes) {
        if (format == DocumentFormat.BINARY) {
            binaryDocumentPut(uri, bytes);
        } else if (format == DocumentFormat.TXT) {
            textDocumentPut(uri, bytes);
        }
        if (overTheLimit()) {
            try {
                removeFromEverything();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void textDocumentPut(URI uri, byte[] bytes) {
        String txt = new String(bytes);
        String[] words = txt.split(" ");
        DocumentImpl documentText = new DocumentImpl(uri, txt, null);
        if(txt.getBytes().length > this.maxDocBytes && this.maxDocBytes != -1){
            throw new IllegalArgumentException("tried to put a doc that has a byte length grater than the limit");
        }
        newMinHeap newMinHeap = new newMinHeap(documentText.getKey(), bTree);
        documentText.setLastUseTime(System.nanoTime());
        minHeap.insert(newMinHeap);
        minHeap.reHeapify(newMinHeap);
        uriSet.add(newMinHeap.uri);
        docBytes += txt.getBytes().length;
        docCount++;
        bTree.put(uri, documentText);
        for (String word : words) {
            Set<URI> set = documentTrie.get(word);
            if (!set.contains(documentText.getKey())) {
                documentTrie.put(word, documentText.getKey());
            }
        }
    }

    private void binaryDocumentPut(URI uri, byte[] bytes) {
        DocumentImpl documentBinary = new DocumentImpl(uri, bytes);
        if(bytes.length > this.maxDocBytes && this.maxDocBytes != -1){
            throw new IllegalArgumentException("tried to put a doc that has a byte length grater than the limit");
        }
        newMinHeap newMinHeap = new newMinHeap(documentBinary.getKey(), bTree);
        documentBinary.setLastUseTime(System.nanoTime());
        minHeap.insert(newMinHeap);
        minHeap.reHeapify(newMinHeap);
        uriSet.add(newMinHeap.uri);
        docBytes += bytes.length;
        docCount++;
        bTree.put(uri, documentBinary);
    }

    private void removeFromEverything() throws IOException {
        newMinHeap leastUsedURI = minHeap.peek();
        while (overTheLimit()) {
            newMinHeap leastUsedDoc = minHeap.remove();
            uriSet.remove(leastUsedDoc.uri);
            Document theNewDoc = bTree.get(leastUsedDoc.uri);
            if(theNewDoc  != null){
                if (theNewDoc.getDocumentTxt() != null) {
                    docBytes -= theNewDoc.getDocumentTxt().getBytes().length;
                    //because writing it out to disk should not delete from the trie, as per the piazza post
                } else {
                    docBytes -= theNewDoc.getDocumentBinaryData().length;
                }
            }

            docCount -= 1;
            bTree.moveToDisk(leastUsedDoc.uri);
            //bTree.put(leastUsedDoc, null);
            //make a tempStack, loop through the command stack until I find the document I'm looking for
            //take it out of the commandStack, put everything back (similar to what I did with undo methods
            //and removeFromMinHeap method)
            //commandFound = removeFromCommandStack(leastUsedURI, commandFound, tempStack);
        }
    }

    @Override
    public Document get(URI url) throws IOException{
        Document theDoc = bTree.get(url);
        if (theDoc != null) {
            docFormatCheckAndInsert(theDoc);
            long newTime = System.nanoTime();
            setNewTime(theDoc, newTime);
        }
        return theDoc;
    }

    @Override
    public boolean delete(URI url) {
        Document previousDocument = bTree.get(url);
        Set<String> words;
        MinHeap<Document> foundDoc = new MinHeapImpl<>();
        if (previousDocument != null) {
            words = previousDocument.getWords();
            MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
            removeFromMinHeap(previousDocument.getKey(), foundDoc, tempHeap, previousDocument);

            //could make a tempMinHeap, push elements onto it while looking for the current document to delete,
            //(same as I did with stack in the undo methods) peek at each one, and then delete that one when I find the
            //document

            if (words != null && !words.isEmpty()) {
                for (String word : previousDocument.getWords()) {
                    documentTrie.delete(word, previousDocument.getKey());
                }
            }
        }else{
            return false;
        }
        Undoable deleteCommand = new GenericCommand<>(url, (URI thisURI) -> {
            bTree.put(url, previousDocument);
            docFormatCheckAndInsert(previousDocument);
            if(!words.isEmpty()){
                for(String word : words){
                    documentTrie.put(word, previousDocument.getKey());
                }
            }
            docFormatCheckAndInsert(previousDocument);
        });
        commandStack.push(deleteCommand);
        bTree.put(url, null);
        return true;
    }
    @Override
    public void undo() throws IllegalStateException {
        if (commandStack.size() <= 0) {
            throw new IllegalStateException("command stack is empty");
        }
        Undoable command = commandStack.pop();
        if (command instanceof CommandSet) {
            ((CommandSet<URI>) command).undoAll();
            long newTime = System.nanoTime();
        }
        if (command instanceof GenericCommand<?>) {
            command.undo();
            URI theURI = (URI) ((GenericCommand<?>) command).getTarget();
            try {
                if (this.get(theURI) != null) {
                    long newTime = System.nanoTime();
                    setNewTime(this.get(theURI), newTime);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void undo(URI url) throws IllegalStateException {
        Stack<Undoable> tempStack = new StackImpl<>();
        boolean commandFound = false;
        while (commandStack.size() > 0) {
            Undoable currentCommand = commandStack.pop();
            if (currentCommand instanceof GenericCommand) {
                if ((((GenericCommand<URI>) currentCommand).getTarget().equals(url))) {
                    currentCommand.undo();
                    URI theURI = ((GenericCommand<URI>) currentCommand).getTarget();
                    long newTime = System.nanoTime();
                    try {
                        if (this.get(theURI) != null) {
                            setNewTime(this.get(theURI), newTime);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    commandFound = true;
                    break;
                }else{
                    tempStack.push(currentCommand);
                }
            } else if (currentCommand instanceof CommandSet) {
                if (((CommandSet<URI>) currentCommand).containsTarget(url)) {
                    CommandSet<URI> newCommand = (CommandSet<URI>) currentCommand;
                    newCommand.undo(url);
                    tempStack.push(currentCommand);
                    commandFound = true;
                    if (newCommand.isEmpty()) {
                        continue;
                    }
                }
            }
            if (commandFound) {
                break;
            }
        }
        //IF ITS A GENERIC COMMAND, I DONT WANT TO PUT IT BACK INTO THE COMMANDSTACK. IF IT'S A COMMANDSET, I NEED TO PUT THE REST BACK IN
        pushFromTempStack(tempStack, commandFound);
    }

    private void pushFromTempStack(Stack<Undoable> tempStack, boolean commandFound) {
        while (tempStack.size() > 0) {
            commandStack.push(tempStack.pop());
        }
        if (!commandFound) {
            throw new IllegalStateException("no command with that url found");
        }
    }
    @Override
    public List<Document> search(String keyword) throws IOException{
        Set<URI> searchSet = documentTrie.get(keyword);
        List<Document> searchList = new ArrayList<>();
        Comparator<Document> comparator = wordComparator(keyword);
        long newTime = System.nanoTime();
        for (URI uri : searchSet) {
            Document doc = get(uri);
            searchList.add(doc);
            setNewTime(doc, newTime);
        }
        searchList.sort(comparator);
        return searchList;
    }

    private Comparator<Document> wordComparator(String keyword) {
        Comparator<Document> comparator = (doc1, doc2) -> {
            if (doc1.wordCount(keyword) > doc2.wordCount(keyword)) {
                return -1;
            } else if (doc1.wordCount(keyword) < doc2.wordCount(keyword)) {
                return 1;
            } else {
                return 0;
            }
        };
        return comparator;
    }

    private Comparator<URI> prefixComparator(String prefix) {
        Comparator<URI> comparator = (doc1, doc2) -> {
            try {
                if (prefixCount(get(doc1), prefix) > prefixCount(get(doc2), prefix)) {
                    return -1;
                } else if (prefixCount(get(doc1), prefix) < prefixCount(get(doc2), prefix)) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        return comparator;
    }

    private int prefixCount(Document doc, String prefix) {
        int count = 0;
        Set<String> words = doc.getWords();
        for (String word : words) {
            if (word.startsWith(prefix)) {
                count += doc.wordCount(word);
            }
        }
        return count;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException{
        Comparator<URI> comparator = prefixComparator(keywordPrefix);
        List<URI> prefixList = documentTrie.getAllWithPrefixSorted(keywordPrefix, comparator);
        prefixList.sort(comparator);
        long newTime = System.nanoTime();
        for (URI doc : prefixList) {
            setNewTime(get(doc), newTime);
        }
        List<Document> docs = new ArrayList<>();
        for(URI uri : prefixList){
            docs.add(get(uri));
        }
        if (!docs.isEmpty()) {
            return docs;
        } else {
            return Collections.emptyList();
        }
        //sorts by prefixes as individual words, have to sort by prefixes as part of other words
        //maybe I fixed it? depends if the method being private in DocStore as opposed to DocImpl makes a difference
    }

    private void setNewTime(Document doc, long newTime) {
        doc.setLastUseTime(newTime);
        minHeap.reHeapify(new newMinHeap(doc.getKey(), bTree));
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deleteSet = new HashSet<>();
        Set<URI> documentsDeleted = documentTrie.deleteAll(keyword);
        Set<Document> docsDeleted = new HashSet<>();
        MinHeap<Document> foundDoc = new MinHeapImpl<>();
        MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
        Document document;
        for(URI uri : documentsDeleted){
            Document doc = bTree.get(uri);
            docsDeleted.add(doc);
            Set<String> docWords = doc.getWords();
            bTree.put(doc.getKey(), null);
            for (String word : docWords) {
                documentTrie.delete(word, doc.getKey());
            }
            removeFromMinHeap(doc.getKey(), foundDoc, tempHeap, doc);
        }
        for (URI doc : documentsDeleted) {
            deleteSet.add(doc);
        }
        this.undoDeleteAll(deleteSet, docsDeleted);
        return deleteSet;
    }
    private void removeFromMinHeap(URI doc, MinHeap<Document> foundDoc, MinHeap<newMinHeap> tempHeap, Document document) {
        while (minHeap.peek() != null) {
            newMinHeap heapDoc = minHeap.remove();
            uriSet.remove(heapDoc.uri);
            if (heapDoc.uri.equals(doc)) {
                foundDoc.insert(document);
                try {
                    this.docFormatCheckAndRemove(document);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            tempHeap.insert(new newMinHeap(heapDoc.uri, bTree));
        }
        while (tempHeap.peek() != null) {
            newMinHeap insertedDoc = tempHeap.remove();
            if (insertedDoc != null) {
                minHeap.insert(insertedDoc);
                minHeap.reHeapify(insertedDoc);
                uriSet.add(insertedDoc.uri);
            } else {
                break;
            }
        }
    }

    private void docFormatCheckAndRemove(Document heapDoc) throws IOException {
        if (heapDoc.getDocumentTxt() != null) {
            docBytes -= heapDoc.getDocumentTxt().getBytes().length;
        } else {
            docBytes -= heapDoc.getDocumentBinaryData().length;
        }
        docCount--;
        if (overTheLimit()) {
            removeFromEverything();
        }
    }

    private void undoDeleteAll(Set<URI> deleteSet, Set<Document> documentsDeleted) {
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : documentsDeleted) {
            GenericCommand<URI> deleteCommand = null;
                    deleteCommand = new GenericCommand<>(doc.getKey(), (URI thisURI) -> {
                        bTree.put(doc.getKey(), doc);
                        docFormatCheckAndInsert(doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc.getKey());
                        }
                    });
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }

    private void docFormatCheckAndInsert(Document doc) {
        if(uriSet.contains(doc.getKey())){
            return;
        }
        minHeap.insert(new newMinHeap(doc.getKey(), bTree));
        uriSet.add(doc.getKey());
        setNewTime(doc, System.nanoTime());
        if (doc.getDocumentTxt() != null) {
            docBytes += doc.getDocumentTxt().getBytes().length;
        } else {
            docBytes += doc.getDocumentBinaryData().length;
        }
        docCount++;
        if (overTheLimit()) {
            try {
                removeFromEverything();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> deleteSet = new HashSet<>();
        Set<URI> documentsDeleted = documentTrie.deleteAllWithPrefix(keywordPrefix);
        Set<Document> docsDeleted = new HashSet<>();
        MinHeap<Document> foundDoc = new MinHeapImpl<>();
        MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
        for(URI uri : documentsDeleted){
            Document doc = bTree.get(uri);
            if(doc != null){
                docsDeleted.add(doc);
                Set<String> docWords = doc.getWords();
                bTree.put(uri, null);
                for (String word : docWords) {
                    documentTrie.delete(word, uri);
                }
                removeFromMinHeap(uri, foundDoc, tempHeap, doc);
            }
        }
        for (URI doc : documentsDeleted) {
            deleteSet.add(doc);
        }
        this.undoDeleteAllWithPrefix(deleteSet, docsDeleted);
        return deleteSet;
    }
    private void undoDeleteAllWithPrefix(Set<URI> deletePrefixSet, Set<Document> documentsPrefixDeleted) {
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : documentsPrefixDeleted) {
            GenericCommand<URI> deleteCommand = null;
                    deleteCommand = new GenericCommand<>(doc.getKey(), (URI thisURI) -> {
                        bTree.put(doc.getKey(), doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc.getKey());
                        }
                        docFormatCheckAndInsert(doc);
                    });
                deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }

    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException{
        List<Document> documentList = new ArrayList<>();
        long newTime = System.nanoTime();
        for (Map.Entry<String, String> entry : keysValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Set<Map<URI, String>> documentsWithKey = metadataTrie.get(key);
            for (Map<URI, String> metadataMap : documentsWithKey) {
                if (metadataMap.containsValue(value)) {
                    URI uri = metadataMap.keySet().iterator().next();
                    Document doc = get(uri);
                    if (doc != null) {
                        documentList.add(doc);
                        setNewTime(doc, newTime);
                    }
                }
            }
        }
        return documentList;
    }

    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException{
        List<Document> keywordList = this.search(keyword);
        List<Document> metadataList = this.searchByMetadata(keysValues);
        List<Document> bothList = new ArrayList<>();
        boolean matches = false;
        long newTime = System.nanoTime();
        for (Document keyDoc : keywordList) {
            for (Document metadataDoc : metadataList) {
                if (keyDoc.equals(metadataDoc)) {
                    matches = true;
                    bothList.add(keyDoc);
                    setNewTime(keyDoc, newTime);
                }
            }
        }
        bothList.sort(wordComparator(keyword));
        return bothList;
    }

    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException{
        List<Document> prefixList = this.searchByPrefix(keywordPrefix);
        List<Document> metadaList = this.searchByMetadata(keysValues);
        List<URI> bothList = new ArrayList<>();
        long newTime = System.nanoTime();
        for (Document keyDoc : prefixList) {
            for (Document metadataDoc : metadaList) {
                if (keyDoc.equals(metadataDoc)) {
                    bothList.add(keyDoc.getKey());
                    setNewTime(keyDoc, newTime);
                }
            }
        }
        bothList.sort(prefixComparator(keywordPrefix));
        List<Document> docs = new ArrayList<>();
        for(URI uri : bothList){
            docs.add(get(uri));
        }
        return docs;
    }

    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException{
        Set<URI> deletedDocuments = new HashSet<>();
        List<Document> metadataDocuments = this.searchByMetadata(keysValues);
        MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
        MinHeap<Document> foundHeap = new MinHeapImpl<>();
        for (Document doc : metadataDocuments) {
            deletedDocuments.add(doc.getKey());
            Set<String> docWords = doc.getWords();
            for (String word : docWords) {
                documentTrie.delete(word, doc.getKey());
            }
            this.removeFromMinHeap(doc.getKey(), foundHeap, tempHeap, doc);
            bTree.put(doc.getKey(), null);
        }
        this.undoDeleteAllWithMetadata(deletedDocuments, metadataDocuments);
        return deletedDocuments;
    }

    private void undoDeleteAllWithMetadata(Set<URI> deletedDocuments, List<Document> metadataDocuments) {
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : metadataDocuments) {
            GenericCommand<URI> deleteCommand = null;
                    deleteCommand = new GenericCommand<>(doc.getKey(), (URI thisURI) -> {
                        bTree.put(doc.getKey(), doc);
                        docFormatCheckAndInsert(doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc.getKey());
                        }
                        docFormatCheckAndInsert(doc);
                    });
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }

    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues)throws IOException {
        List<Document> bothDocs = this.searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> deletedURIs = new HashSet<>();
        MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
        MinHeap<Document> foundHeap = new MinHeapImpl<>();
        for (Document doc : bothDocs) {
            deletedURIs.add(doc.getKey());
            Set<String> docWords = doc.getWords();
            for (String word : docWords) {
                documentTrie.delete(word, doc.getKey());
            }
            this.removeFromMinHeap(doc.getKey(), foundHeap, tempHeap, doc);
            bTree.put(doc.getKey(), null);

        }
        this.undoDeleteAllWithKeywordAndMetadata(deletedURIs, bothDocs);
        return deletedURIs;
    }

    private void undoDeleteAllWithKeywordAndMetadata(Set<URI> deletedURIs, List<Document> bothDocs) {
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : bothDocs) {
            GenericCommand<URI> deleteCommand = null;
                    deleteCommand = new GenericCommand<>(doc.getKey(), (URI thisURI) -> {
                        bTree.put(doc.getKey(), doc);
                        docFormatCheckAndInsert(doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc.getKey());
                        }
                        docFormatCheckAndInsert(doc);
                    });
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }

    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues)throws IOException {
        List<Document> bothDocs = this.searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> deletedURIs = new HashSet<>();
        MinHeap<newMinHeap> tempHeap = new MinHeapImpl<>();
        MinHeap<Document> foundHeap = new MinHeapImpl<>();
        for (Document doc : bothDocs) {
            deletedURIs.add(doc.getKey());
            Set<String> docWords = doc.getWords();
            for (String word : docWords) {
                documentTrie.delete(word, doc.getKey());
            }
            bTree.put(doc.getKey(), null);
            this.removeFromMinHeap(doc.getKey(), foundHeap, tempHeap, doc);
        }
        this.undoDeleteAllWithPrefixAndMetadata(deletedURIs, bothDocs);
        return deletedURIs;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit < 1){
            throw new IllegalArgumentException("limit was less than 1");
        }
        this.maxDocCount = limit;
        if (overTheLimit()) {
            try {
                removeFromEverything();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit < 1){
            throw new IllegalArgumentException("limit was less than 1");
        }
        this.maxDocBytes = limit;
        if (overTheLimit()) {
            try {
                removeFromEverything();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean overTheLimit() {
        return (docBytes > maxDocBytes && maxDocBytes != -1) || (docCount > maxDocCount && maxDocCount != -1);
    }

    private void undoDeleteAllWithPrefixAndMetadata(Set<URI> deletedURIs, List<Document> bothDocs) {
        CommandSet<URI> deleteCommandSet = new CommandSet<>();
        for (Document doc : bothDocs) {
            GenericCommand<URI> deleteCommand = null;
                    deleteCommand = new GenericCommand<>(doc.getKey(), (URI thisURI) -> {
                        bTree.put(doc.getKey(), doc);
                        docFormatCheckAndInsert(doc);
                        this.docFormatCheckAndInsert(doc);
                        Set<String> words = doc.getWords();
                        for (String word : words) {
                            documentTrie.put(word, doc.getKey());
                        }
                    });
            deleteCommandSet.addCommand(deleteCommand);
        }
        commandStack.push(deleteCommandSet);
    }
}