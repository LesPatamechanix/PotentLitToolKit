/**
 *  Class responsible for loading
 *  RDF ontology and making it accessible
 *  for searching
**/

import org.yaml.snakeyaml.Yaml;
import java.util.*;
import java.io.*;

class RDFLoader {

    private File ontology_path;
    private LinkedHashMap<List, List> processed_ontology;

    public RDFLoader(File ont) throws Exception {
      ontology_path = ont;
      try {
        loadYaml();
      } catch(Exception e){
        System.out.println("Error loading YAML");
      }
    }

    /**
    * Loads the YAML RDF document
    * containing the patadata
    **/
    private void loadYaml() throws Exception {
      Yaml yaml = new Yaml();
      try{
          InputStream document = new FileInputStream(ontology_path);
          processed_ontology = (LinkedHashMap<List, List>) yaml.load(document);
      } catch(Exception e){
          System.out.println("Error opening file");
      }
    }

    /**
    * Getter function for returning ontology
    **/
    public LinkedHashMap<List, List> get_processed_ontology() {
       return processed_ontology;
    }
}
