/**
 *  Takes as input:
 *  A user document
 *  A list of matched phrases
 *  Outputs a modified text document.
*/

import java.util.*;
import java.io.*;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import org.atteo.evo.inflector.English;

class ReplaceMatch {


    static final double CHUNK_SCORE = 1.0;

    private Map patadata = new HashMap();
    private String tagged_doc = "";
    private String search_phrase = "";
    private String unstemmed_search_phrase = "";
    private String modified_doc = "";
    private String replacement_category = "";
    private TokenizerFactory indoFact = IndoEuropeanTokenizerFactory.INSTANCE;
    private MapDictionary<String> dictionary = new MapDictionary<String>();

    public ReplaceMatch(String t_d, Map p_d, String r_c, String s_p, String us_p) {

      tagged_doc = t_d;
      patadata = p_d;
      replacement_category = r_c;
      search_phrase = s_p;
      unstemmed_search_phrase = us_p;
    }

   /**
   * Generate a list of terms to be replaced by the
   * chosen phrase from the patadata
   **/
   public String replaceInText() {

     dictionary.addEntry(new DictionaryEntry<String>(search_phrase,"SINGLE",CHUNK_SCORE));
     String check_plural = English.plural(search_phrase, 2);

     if(!search_phrase.equals(unstemmed_search_phrase)){
       dictionary.addEntry(new DictionaryEntry<String>(unstemmed_search_phrase,"SINGLE",CHUNK_SCORE));
     }

     if(!check_plural.equals(search_phrase) && !check_plural.equals(unstemmed_search_phrase)) {
       dictionary.addEntry(new DictionaryEntry<String>(check_plural,"PLURAL",CHUNK_SCORE));
     }

     ExactDictionaryChunker dictionaryChunkerTT
             = new ExactDictionaryChunker(dictionary,
                                         IndoEuropeanTokenizerFactory.INSTANCE,
                                         true,true);
     replaceText(dictionaryChunkerTT,tagged_doc);
     return modified_doc;
   }


   /**
   * Replace the matched english phrases with
   * chosen patadata value.
   * Handles by singular and plural version
   * of a noun.
   **/
   private void replaceText(ExactDictionaryChunker chunker, String text) {
        Chunking chunking = chunker.chunk(text.toLowerCase());
        StringBuffer textBuf = new StringBuffer(text);
        int offset = 0;
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String type = chunk.type();
            String replacement = getReplacement();

            if(type == "PLURAL") {
                replacement = English.plural(replacement, 2);
            }

            textBuf.replace(start+offset,end+offset,replacement);
            int length_dif = end - start;
            offset = replacement.length() - length_dif;
        }
        modified_doc = textBuf.toString();
   }

   /**
   * Return a Map containing replacement
   * values.
   **/
   private String getReplacement() {
     Map top_level = (Map)patadata.values().toArray()[0];
     return top_level.get(replacement_category).toString();
   }

}
