package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
public class DocumentImplTest {
    private URI uri;
    private String content;
    private byte[] binary;
    private DocumentImpl textDocument;
    private DocumentImpl binaryDocument;

    @BeforeEach
    public void setUp() throws Exception {
        uri = new URI("http://example.com/path");
        content = "Let's try this out";
        binary = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
        textDocument = new DocumentImpl(uri, content);
        binaryDocument = new DocumentImpl(uri, binary);
    }

    @Test
    public void DocumentImplNullConstructorTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DocumentImpl(null, content);
        });
    }

    @Test
    public void DocumentImplEmptyConstructorTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DocumentImpl(new URI(""), content);
        });
    }

    @Test
    public void DocumentImplEmptyContentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DocumentImpl(this.uri, "");
        });
    }

    @Test
    public void DocumentImplNullContentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DocumentImpl(this.uri, (String) null);
        });
    }

    @Test
    public void DocumentImplNullBinaryTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DocumentImpl(this.uri, (byte[]) null);
        });
    }
    @Test
    public void DocumentImplEmptyBinaryTest(){
        assertThrows(IllegalArgumentException.class, () -> {
           new DocumentImpl(this.uri, new byte[0]);
        });
    }
    @Test
    public void DocumentImplSetMetadataValueWithNullTest(){
        String key = "the key";
        String value = "the value";
        assertThrows(IllegalArgumentException.class, () ->{
            textDocument.setMetadataValue(null, value);
        });
    }
    @Test
    public void DocumentImplSetMetadataValueWithEmptyTest(){
        String key = "the key";
        String value = "the value";
        assertThrows(IllegalArgumentException.class, () ->{
            textDocument.setMetadataValue("", value);
        });
    }
    @Test
    public void DocumentImplSetMetadataValueTest(){
        String key = "the key";
        String value = "the value";
        assertNull(textDocument.setMetadataValue(key, value));
    }
    @Test
    public void DocumentImplSetMetadataValueWithExistingKeyTest(){
        String key = "the key";
        String value1 = "the value";
        String value2 = "the second value";
        assertNull(textDocument.setMetadataValue(key, value1));
        assertEquals(value1, textDocument.setMetadataValue(key, value2));
    }
    @Test
    public void DocumentImplGetMetadataValueTest(){
        String key = "the key";
        String value = "the value";
        textDocument.setMetadataValue(key, value);
        assertEquals("the value", textDocument.getMetadataValue(key));
    }
    @Test
    public void DocumentImplGetMetadataValueNullTest(){
        String key1 ="the key";
        String value = "the value";
        String key2 = "the second key";
        textDocument.setMetadataValue(key1, value);
        assertNull(textDocument.getMetadataValue(key2));
    }
    @Test
    public void DocumentImplGetClonedMetadataTest(){
        String key = "the key";
        String value = "the value";
        textDocument.setMetadataValue(key, value);
        HashTable<String, String> expectedMetaData = new HashTableImpl<>();
        expectedMetaData.put(key, value);
        //assertEquals(expectedMetaData, textDocument.getMetadata());
    }
    @Test
    public void DocumentImplGetDocumentTextTest(){
        assertEquals("Let's try this out", textDocument.getDocumentTxt());
    }
    @Test
    public void DocumentImplGetDocumentBinaryTest(){
        assertEquals(this.binary, binaryDocument.getDocumentBinaryData());
    }
    @Test
    public void DocumentImplGetKeyTest(){
        assertEquals(this.uri, textDocument.getKey());
        assertEquals(this.uri, binaryDocument.getKey());
    }
}
