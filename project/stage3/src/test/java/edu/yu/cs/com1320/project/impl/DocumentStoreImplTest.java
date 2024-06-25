package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreImplTest {
    private DocumentStore documentStore;
    private URI uri1;
    private URI uri2;


    @BeforeEach
    public void setUp() throws Exception {
        documentStore = new DocumentStoreImpl();
        uri1 = new URI("http://example.com/document1");
        uri2 = new URI("http://example.com/document2");
        //documentStore = new DocumentStoreImpl();
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
}
