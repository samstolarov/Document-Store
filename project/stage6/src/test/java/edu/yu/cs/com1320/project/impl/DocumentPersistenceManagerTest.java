package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;
import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentPersistenceManagerTest {

    private DocumentPersistenceManager dpm;
    private File baseDir;

    @BeforeEach
    public void setUp() {
        baseDir = new File(System.getProperty("user.dir"));
        baseDir.mkdirs();
        dpm = new DocumentPersistenceManager(baseDir);
    }
//    @AfterEach
//    public void tearDown(){
//        for(File file : baseDir.listFiles()){
//            file.delete();
//        }
//        baseDir.delete();
//    }


    @Test
    public void testSerializeAndDeserializeTextDocument() throws URISyntaxException, IOException {
        URI uri = new URI("http://edu.yu.cs/com1320/project/doc1");
        String text = "This is a text document";
        HashMap<String, Integer> wordMap = new HashMap<>();
        for(int i = 0; i < 10; i++){
            wordMap.put("hey" + i, i);
        }
        Document doc = new DocumentImpl(uri, text, wordMap);

        dpm.serialize(uri, doc);
        File file = new File(baseDir, uri.getSchemeSpecificPart() + ".json");
        assertTrue(file.exists());

        Document deserializedDoc = dpm.deserialize(uri);
        assertNotNull(deserializedDoc);
        assertEquals(doc.getKey(), deserializedDoc.getKey());
        assertEquals(doc.getMetadata(), deserializedDoc.getMetadata());
        assertEquals(doc.getDocumentTxt(), deserializedDoc.getDocumentTxt());
        assertEquals(doc.getWordMap(), deserializedDoc.getWordMap());
        assertEquals(doc, deserializedDoc);

//        dpm.delete(uri);
//        //Document theDoc = dpm.deserialize(uri);
//        //assertNull(theDoc);
//        assertFalse(file.exists());

    }

    @Test
    public void testSerializeAndDeserializeBinaryDocument() throws URISyntaxException, IOException {
        URI uri = new URI("http://edu.yu.cs/com1320/project/doc2");
        byte[] binaryData = "This is a test document".getBytes();
        Document doc = new DocumentImpl(uri, binaryData);

        dpm.serialize(uri, doc);

        File file = new File(baseDir, uri.getSchemeSpecificPart() + ".json");
        assertTrue(file.exists());

        Document deserializedDoc = dpm.deserialize(uri);
        assertNotNull(deserializedDoc);
        assertEquals(doc.getKey(), deserializedDoc.getKey());
        assertEquals(doc.getMetadata(), deserializedDoc.getMetadata());
        assertArrayEquals(doc.getDocumentBinaryData(), deserializedDoc.getDocumentBinaryData());
        assertEquals(doc, deserializedDoc);

//        dpm.delete(uri);
//        //Document theDoc = dpm.deserialize(uri);
//        //assertNull(theDoc);
//        assertFalse(file.exists());
    }

    @Test
    public void testDeleteDocument() throws URISyntaxException, IOException {
        URI uri = new URI("http://edu.yu.cs/com1320/project/doc3");
        String text = "This document will be deleted";
        HashMap<String, Integer> wordMap = new HashMap<>();
        for(int i = 0; i < 10; i++){
            wordMap.put("hey" + i, i);
        }
        Document doc = new DocumentImpl(uri, text, wordMap);

        dpm.serialize(uri, doc);

        boolean deleted = dpm.delete(uri);
        assertTrue(deleted);

        File file = new File(baseDir, uri.toString() + ".json");
        assertFalse(file.exists());
    }
}