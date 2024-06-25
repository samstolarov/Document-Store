package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.*;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;

import jakarta.xml.bind.DatatypeConverter;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    JsonSerializer<Document> serializer;
    JsonDeserializer<Document> deserializer;
    File baseDir;
    public DocumentPersistenceManager(File baseDir){
        //baseDir is basically just the path (i.e. https://...) that we're writing to
        //if its null, we make a new file with the current directory ("user.dir")
        //if not, the directory that was passed in is our directory that we will use
        if(baseDir == null){
            this.baseDir = new File(System.getProperty("user.dir"));
        }else{
            this.baseDir = baseDir;
        }
        this.serializer = new theSerializer();
        this.deserializer = new theDeserializer();
        //this.baseDir = (baseDir == null) ? new File(System.getProperty("user.dir")) : baseDir;

    }
    private static class theSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document doc, Type type, JsonSerializationContext jsonSerializationContext) {
            Gson gson = new Gson();
            JsonObject json = new JsonObject();
            //json.add("uri", gson.toJsonTree(doc.getKey()));

            //uri serialization
            json.addProperty("uri", doc.getKey().toString());
            //serialize doc text, if it has
            if(doc.getDocumentTxt() != null){
                json.addProperty("text", doc.getDocumentTxt());

                Type typeWordMap = new TypeToken<HashMap<String, Integer>>(){}.getType();
                JsonElement wordMapElement = gson.toJsonTree(doc.getWordMap(), typeWordMap);
                json.add("wordmap", wordMapElement);
            }else{
                String byteEncoded = DatatypeConverter.printBase64Binary(doc.getDocumentBinaryData());
                //gson.toJsonTree(byteEncoded, new byte[]);
                json.addProperty("binary", byteEncoded);
            }
            if (!doc.getMetadata().isEmpty()) {
                Type typeMetadata = new TypeToken<HashMap<String, String>>(){}.getType();
                JsonElement metadataElement = gson.toJsonTree(doc.getMetadata(), typeMetadata);
                json.add("metadata", metadataElement);
            }
            return json;
        }
    }

    private Gson createGsonForSerialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Document.class, this.serializer)
                .setPrettyPrinting()
                .create();
        return gson;
    }
    private Gson createGsonforDeserialize(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Document.class, this.deserializer)
                .setPrettyPrinting()
                .create();
        return gson;
    }


    private static class theDeserializer implements JsonDeserializer<Document>{

        @Override
        public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            //first gotta create JsonElement into JsonObject
            //no clue what the difference between the 2 are but can only get the stuff
            //if its a JsonObject

            //get the URI out of json
            JsonObject json = jsonElement.getAsJsonObject();
            Gson gson  = new Gson();
            URI uri;
            HashMap<String, String> metadata = null;
            try {
                uri = new URI(json.get("uri").getAsString());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            //get the metadata
            if(json.has("metadata")){
                Type metadataType = new TypeToken<HashMap<String, String>>(){}.getType();
                metadata = gson.fromJson(json.get("metadata"), metadataType);

            }
            //get the text/wordMap
            if(json.has("text")){
                String docText = json.get("text").getAsString();
                Type typeWordMap = new TypeToken<HashMap<String, Integer>>(){}.getType();
                HashMap<String, Integer> wordMap = gson.fromJson(json.get("wordmap"), typeWordMap);
                Document doc =  new DocumentImpl(uri, docText, wordMap);
                if(metadata != null){
                    doc.setMetadata(metadata);
                }

                return doc;
            }else{
                byte[] bytes = DatatypeConverter.parseBase64Binary(json.get("binary").getAsString());
                Document doc = new DocumentImpl(uri, bytes);
                if(metadata != null){
                    doc.setMetadata(metadata);
                }
                return doc;
            }
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        String uriScheme = uri.getSchemeSpecificPart();
        File file = new File(baseDir, uriScheme + ".json");
        file.getParentFile().mkdirs();
        Gson gson = createGsonForSerialize();
        FileWriter theWriter = new FileWriter(file);
        String theStuff = gson.toJson(val, Document.class);
        theWriter.write(theStuff);
        theWriter.close();
        //basically the serialize and deserialize methods in the private classes implement the actual serialization
        //and the regular public serialize and deserialize methods pretty much just write it (or get it) to/from the
        //File baseDir that we're working with

        //use baseDir.getPath to serialize it into the File that was passed in. If the file was null,
        //we are using the working directory, so we have to somehow get the path of the working directory
        //and serialize it to there
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        Gson gson = createGsonforDeserialize(); //not sure if there was a purpose to making a separate one for serialize but whatever
        String uriScheme = uri.getSchemeSpecificPart();
        String theFile = new File(baseDir, uriScheme + ".json").getPath();// should get the file stored under this uri
        FileReader theReader = new FileReader(theFile);
        Document doc =  gson.fromJson(theReader, Document.class);
        theReader.close();
        return doc;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        String uriScheme = uri.getSchemeSpecificPart();
        String theFile = new File(baseDir, uriScheme + ".json").getPath();
        File file = new File(theFile);
            boolean fileDeleted = file.delete();
            if(fileDeleted){
                deleteEmptyParents(file.getParentFile());
               return true;
            }else{
                return false;
            }
    }
    private void deleteEmptyParents(File dir){
        while(dir != null && dir.isDirectory() && dir.list().length == 0){
            if(dir.delete()){
                deleteEmptyParents(dir.getParentFile());
            }
        }
    }
}
