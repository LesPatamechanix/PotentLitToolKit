/**
 *  Class responsible for loading
 *  text document and making it accessible
 *  for searching
**/

import java.util.*;
import java.io.*;

class TextDocumentLoader {

    private File document_path;
    private String processed_document;

    public TextDocumentLoader(File doc) throws Exception {
      document_path = doc;
      try {
        loadDocument();
      } catch(Exception e){
        System.out.println("Error loading document");
      }
    }

    /**
    * Loads the text document
    * we wish to apply patadata to.
    **/
    private void loadDocument() throws Exception {
      try {
        String document = new Scanner(document_path).useDelimiter("\\A").next();
        processed_document = document;
      } catch(Exception e){
        System.out.println("Error opening document file");
      }
    }

    /**
    * Getter function for returning document
    **/
    public String get_processed_document() {
       return processed_document;
    }
}
