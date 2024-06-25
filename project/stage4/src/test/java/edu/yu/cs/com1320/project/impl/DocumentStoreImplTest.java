package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreImplTest {
    private DocumentStoreImpl documentStore;
    private URI uri1;
    private URI uri2;
    private URI uri3;
    private Trie trie;


    @BeforeEach
    public void setUp() throws Exception {
        documentStore = new DocumentStoreImpl();
        uri1 = new URI("http://example.com/document1");
        uri2 = new URI("http://example.com/document2");
        uri3 = new URI("http://example.com/document3");
        trie = new TrieImpl();
    }

    @Test
    public void putTest() throws IOException {
        String content = "the text document";
        String newContent = "the new text document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        ByteArrayInputStream newInputStream = new ByteArrayInputStream(newContent.getBytes());
        documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        int doc = documentStore.get(uri1).hashCode();
        int newHashCode = documentStore.put(newInputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        assertEquals(newHashCode, doc);
    }
    @Test
    public void putWithNullInputStreamTest() throws IOException {
        String content = "the text document";
        assertEquals(0, documentStore.put(null, this.uri1, DocumentStore.DocumentFormat.TXT));
    }

    @Test
    public void putWithNullInputStreamWithExistingDocumentTest() throws IOException {
        String content = "the text document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        int hashCode = documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        assertEquals(hashCode, documentStore.put(null, this.uri2, DocumentStore.DocumentFormat.TXT));
    }

    @Test
    public void setMetaDataTest() throws IOException {
        String key = "the key";
        String value = "the value";
        String content = "the text document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        assertNull(documentStore.setMetadata(this.uri1, key, value));
    }

    @Test
    public void setMetadataWithExistingKeyTest() throws IOException {
        String key = "the key";
        String value = "the value";
        String value2 = "the second value";
        String content = "the text document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        assertNull(documentStore.setMetadata(this.uri1, key, value));
        assertEquals(value, documentStore.setMetadata(this.uri1, key, value2));

    }

    @Test
    public void setMetadataWithNullURITest() {
        String key = "the key";
        String value = "the value";
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.setMetadata(null, key, value);
        });
    }

    @Test
    public void setMetadataWithEmptyURITest() {
        String key = "the key";
        String value = "the value";
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.setMetadata(URI.create(""), key, value);
        });
    }

    @Test
    public void setMetadataWithEmptyKeyTest() {
        String value = "the value";
        assertThrows(IllegalArgumentException.class, () -> {
            documentStore.setMetadata(this.uri1, null, value);
        });
    }

    @Test
    public void getMetadataTest() throws IOException {
        String key = "the key";
        String value = "the value";
        String content = "the text document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        assertNull(documentStore.getMetadata(this.uri1, value));
        documentStore.setMetadata(this.uri1, key, value);
        assertEquals(value, documentStore.getMetadata(this.uri1, key));
    }

    @Test
    public void getTest() throws IOException {
        String key = "the key";
        String value = "the value";
        String content = "the text document";
        byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes);
        documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(inputStream1, this.uri2, DocumentStore.DocumentFormat.BINARY);
        Document document = documentStore.get(this.uri1);
        Document document1 = documentStore.get(this.uri2);
        assertNotNull(document);
        assertEquals(content, document.getDocumentTxt());
        assertArrayEquals(bytes, document1.getDocumentBinaryData());
    }


    @Test
    public void deleteTest() throws IOException {
        String key = "the key";
        String value = "the value";
        String content = "the text document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(documentStore.delete(this.uri1));
        assertFalse(documentStore.delete(this.uri2));
        assertNull(documentStore.get(this.uri1));
    }
    @Test
    public void undoPutTest() throws IOException {
        String content = "the content of the document";
        String content1 = "the new content";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(content1.getBytes());
        documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.BINARY);
        documentStore.put(inputStream1, uri2, DocumentStore.DocumentFormat.TXT);
        assertNotNull(documentStore.get(uri1));
        documentStore.undo(uri1);
        assertNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        documentStore.undo();
        assertNull(documentStore.get(uri2));
    }
    @Test
    public void undoDeleteTest() throws IOException {
        String content = "the content of the document";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(documentStore.delete(uri1));
        assertNull(documentStore.get(uri1));
        documentStore.undo();
        assertNotNull(documentStore.get(uri1));
    }
    @Test
    public void undoSetMetadataTest() throws IOException {
        String key = "the key";
        String value = "the value";
        String key2 = "the second key";
        String value2 = "the second value";
        String key3 = "the third key";
        String value3 = "the third value";
        String content = "the content of the document";
        byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes);
        documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(inputStream1, this.uri2, DocumentStore.DocumentFormat.BINARY);
        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key2, value2);
        documentStore.undo();
        documentStore.setMetadata(uri2, key3, value3);
        documentStore.undo(uri1);
        assertNull(documentStore.getMetadata(uri2, key2));
        assertEquals(value3, documentStore.getMetadata(uri2, key3));
        assertNull(documentStore.getMetadata(uri1, key2));
    }
    @Test
    public void searchTest() throws IOException {
        String text1 = "apple orange apple banana orange apple banana apple apple apple apple apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc = documentStore.get(uri1);
        assertEquals(8, doc.wordCount("apple"));

        String text2 = "apple orange orange orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        assertEquals(3, doc2.wordCount("orange"));

        String text3 = "apple apple apple orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        List<Document> searchResults = documentStore.search("orange");

        assertEquals(3, searchResults.size());
        assertTrue(searchResults.get(0).wordCount("orange") > searchResults.get(1).wordCount("orange"));
        assertTrue(searchResults.get(1).wordCount("orange") > searchResults.get(2).wordCount("orange"));
    }
    @Test
    public void searchByPrefixTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));


        String text3 = "apple apple apple apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        List<Document> searchResults = documentStore.searchByPrefix("app");
        assertEquals(3, searchResults.size());
        assertTrue(searchResults.get(0).wordCount("apple") >  searchResults.get(1).wordCount("apple"));
        assertTrue(searchResults.get(1).wordCount("apple") >  searchResults.get(2).wordCount("apple"));
    }
    @Test
    public void deleteAllTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));


        String text3 = "apple apple apple apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        Set<URI> deletedURIs = documentStore.deleteAll("apple");
        assertEquals(3, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        //assertTrue(documentStore.documentTrie.get("apple").isEmpty());
    }
    @Test
    public void deleteAllWithPrefixTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text2 = "orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));


        String text3 = "apple apple apple apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        Set<URI> deletedURIs = documentStore.deleteAllWithPrefix("app");
        assertEquals(2, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        //assertTrue(documentStore.documentTrie.get("app").isEmpty());
    }
    @Test
    public void searchByMetadataTest() throws IOException {
       String content = "text document";
       String key = "the key";
       String value = "the value";

       ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());

       documentStore.put(inputStream, uri1, DocumentStore.DocumentFormat.TXT);

        documentStore.setMetadata(uri1, key, value);
        //documentStore.setMetadata(uri2, "key2", "value2");
        Map<String, String> metadata = new HashMap<>();

        metadata.put(key, value);
        //metadata.put("key2", "value2");

        List<Document> metadataList = documentStore.searchByMetadata(metadata);
        assertEquals(1, metadataList.size());
        assertEquals(metadataList.get(0).getKey(), uri1);
    }
    @Test
    public void searchByKeywordAndMetadata() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text3 = "banana banana banana";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String content = "text document";
        String key = "the key";
        String value = "the value";

        documentStore.setMetadata(uri1, key, value);
        //documentStore.setMetadata(uri2, key, value);
        documentStore.setMetadata(uri3, key, value);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);
        List<Document> documentList = documentStore.searchByKeywordAndMetadata("apple", metadata);
        assertEquals(1, documentList.size());
    }
    @Test
    public void searchByPrefixAndMetadataTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);
        //trie.put(uri1.toString(), documentStore.get(uri1));

        String text3 = "banana banana banana";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);


        String content = "the text document";
        String key = "the key";
        String value = "the value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key, value);
        documentStore.setMetadata(uri3, key, value);

        Map<String, String> hashmap = new HashMap<>();
        hashmap.put(key, value);

        List<Document> documents = documentStore.searchByPrefixAndMetadata("app", hashmap);
        assertEquals(2, documents.size());
    }
    @Test
    public void deleteAllWithMetadataTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "banana banana banana";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String key = "the key";
        String value = "the value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key, value);
        //documentStore.setMetadata(uri3, key, value);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        //assertFalse(documentStore.documentTrie.get("apple").isEmpty());

        Set<URI> deletes = documentStore.deleteAllWithMetadata(metadata);
        assertEquals(2, deletes.size());
        assertTrue(deletes.contains(uri1));
        assertTrue(deletes.contains(uri2));
        assertFalse(deletes.contains(uri3));

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

//        assertTrue(documentStore.documentTrie.get("apple").isEmpty());
//        assertTrue(documentStore.documentTrie.get("banana").isEmpty());
    }
    @Test
    public void deleteAllWithKeywordAndMetadataTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "banana banana banana";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String key = "the key";
        String value = "the value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key, value);
        documentStore.setMetadata(uri3, key, value);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        Set<URI> deletes = documentStore.deleteAllWithKeywordAndMetadata("apple", metadata);
        assertEquals(2, deletes.size());
        assertTrue(deletes.contains(uri1));
        assertTrue(deletes.contains(uri2));
        assertFalse(deletes.contains(uri3));

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

//        assertTrue(documentStore.documentTrie.get("apple").isEmpty());
//        assertTrue(documentStore.documentTrie.get("banana").isEmpty());
    }
    @Test
    public void deleteAllWithPrefixAndMetadataTest() throws IOException {
        String text1 = "apple orange apple banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "banana banana banana pear";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String key = "the key";
        String value = "the value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key, value);
        documentStore.setMetadata(uri3, key, value);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        Set<URI> deletes = documentStore.deleteAllWithPrefixAndMetadata("app", metadata);
        assertEquals(2, deletes.size());
        assertTrue(deletes.contains(uri1));
        assertTrue(deletes.contains(uri2));
        assertFalse(deletes.contains(uri3));

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

//        assertTrue(documentStore.documentTrie.get("orange").isEmpty());
//        assertTrue(documentStore.documentTrie.get("apple").isEmpty());
//        assertFalse(documentStore.documentTrie.get("banana").isEmpty());
//        assertFalse(documentStore.documentTrie.get("pear").isEmpty());
    }
    @Test
    public void undoDeleteAllTest() throws IOException {
        String text1 = "apple apple orange pear banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana pear banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "apple apple apple pear orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        Set<URI> deletedURIs = documentStore.deleteAll("apple");
        assertEquals(3, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        documentStore.undo(uri1);

        assertNotNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        documentStore.undo();

        assertNotNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

        //assertFalse(documentStore.documentTrie.get("pear").isEmpty());

        //THESE SHOULD NOT BE PASSING!!!!!
        assertNotEquals(documentStore.get(uri1), documentStore.get(uri2));
        assertNotEquals(documentStore.get(uri2), documentStore.get(uri3));
        //THESE SHOULD NOT BE PASSING!!!!!
    }
    @Test
    public void undoDeleteAllWithPrefixTest() throws IOException {
        String text1 = "apple apple orange pear banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "apple apple apple pear orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        Set<URI> deletedURIs = documentStore.deleteAllWithPrefix("app");
        assertEquals(3, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        documentStore.undo();

        assertNotNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

        //assertFalse(documentStore.documentTrie.get("pear").isEmpty());

        //THESE SHOULD NOT BE PASSING!!!!!
        assertNotEquals(documentStore.get(uri1), documentStore.get(uri2));
        assertNotEquals(documentStore.get(uri2), documentStore.get(uri3));
    }
    @Test
    public void undoDeleteAllWithMetadataTest() throws IOException {
        String text1 = "apple apple orange pear banana apple";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "apple apple apple pear orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String key = "the key";
        String value = "the value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key, value);
        documentStore.setMetadata(uri3, key, value);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        Set<URI> deletedURIs = documentStore.deleteAllWithMetadata(metadata);
        assertEquals(3, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        documentStore.undo(uri2);

        assertNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));

        //assertFalse(documentStore.documentTrie.get("pear").isEmpty());

        assertNotEquals(documentStore.get(uri1), documentStore.get(uri2));
        assertNotEquals(documentStore.get(uri2), documentStore.get(uri3));
    }
    @Test
    public void undoDeleteAllWithKeywordAndMetadataTest() throws IOException {
        String text1 = "apple orange pear banana";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "apple apple apple pear orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String key = "the key";
        String value = "the value";
        String key1 = "the second key";
        String value1 = "the second value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key1, value);
        documentStore.setMetadata(uri3, key, value1);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        Set<URI> deletedURIs = documentStore.deleteAllWithKeywordAndMetadata("apple", metadata);
        assertEquals(1, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

        documentStore.undo(uri1);

        assertNotNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

        //assertFalse(documentStore.documentTrie.get("pear").isEmpty());

        assertNotEquals(documentStore.get(uri1), documentStore.get(uri2));
        assertNotEquals(documentStore.get(uri2), documentStore.get(uri3));
    }
    @Test
    public void undoDeleteAllWithPrefixAndMetadataTest() throws IOException {
        String text1 = "aporange pear banana";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT);

        String text3 = "apple apple apple pear orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT);

        String key = "the key";
        String value = "the value";
        String key1 = "the second key";
        String value1 = "the second value";

        documentStore.setMetadata(uri1, key, value);
        documentStore.setMetadata(uri2, key1, value);
        documentStore.setMetadata(uri3, key, value1);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(key, value);

        Set<URI> deletedURIs = documentStore.deleteAllWithPrefixAndMetadata("ap", metadata);
        assertEquals(1, deletedURIs.size());

        assertNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

        documentStore.undo();

        assertNotNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));

        //assertFalse(documentStore.documentTrie.get("pear").isEmpty());

        assertNotEquals(documentStore.get(uri1), documentStore.get(uri2));
        assertNotEquals(documentStore.get(uri2), documentStore.get(uri3));
    }
    @Test
    public void undoCommandStackSizeTest() throws IOException {
        String text1 = "aporange pear banana";
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT); //20

        String text2 = "apple orange banana banana";
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(inputStream2, uri2, DocumentStore.DocumentFormat.TXT); //16

        String text3 = "apple banana apple apple pear orange apple apple";
        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(inputStream3, uri3, DocumentStore.DocumentFormat.TXT); //48

        documentStore.undo(uri2);
        assertEquals(2, documentStore.commandStack.size());
    }
}
