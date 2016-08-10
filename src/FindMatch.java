/**
 *  Class responsible for finding a match
 *  between a search term, ontology and
 *  text document.
 *
 *  Imports lingpipe NLTK
 *  Imports Evo for pluralizing nouns
 *  Takes as input ontology
 *  Takes as input search phrase
 *  Takes as input user document
 *  Returns values from onyology where match found
 *
 *  Tested against:
 *  https://www.gutenberg.org/files/730/730-h/730-h.htm
**/


import java.util.*;
import java.io.*;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;

/*word matching in text doc*/
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import org.atteo.evo.inflector.English;

class FindMatch {

    static final double CHUNK_SCORE = 1.0;

    private LinkedHashMap<List, List> input_ontology;
    private Map patadata = new HashMap();
    private String working_doc = "";
    private String search_phrase = "";
    private String unstemmed_search_phrase = "";
    private StanfordLemmatizer stan_lemmatizer;
    private TokenizerFactory indoFact = IndoEuropeanTokenizerFactory.INSTANCE;
    private MapDictionary<String> dictionary = new MapDictionary<String>();

    public FindMatch(LinkedHashMap<List, List> in_ont,
                     String text_doc,
                     String s_p) throws Exception {

      input_ontology = in_ont;
      working_doc = text_doc;
      unstemmed_search_phrase = s_p;
      stemSearchPhrase(s_p.toLowerCase());
    }


    /**
    * Takes an English phrase and returns a
    * string that has been stemmed with the
    * Porter stemmer
    **/
    private void stemSearchPhrase(String s_p) {
      stan_lemmatizer = new StanfordLemmatizer(unstemmed_search_phrase);
      search_phrase = stan_lemmatizer.lemmatize().get(0);
    }

    /**
    * Find a search term in the
    * ontology and then return
    * the patadata if a match is found
    **/
    public Boolean findInOnt() {

       List<Map> input_list = input_ontology.get("nouns");
       input_list.addAll(input_ontology.get("verbs"));

       Boolean foundMatch = false;
       for(Map o : input_list) {
           List keyList = new ArrayList (o.keySet());
           String checkKey = (String)keyList.get(0);
           if(checkKey.toLowerCase().equals(search_phrase) ||
              checkKey.toLowerCase().equals(unstemmed_search_phrase)){
            patadata = o;
            foundMatch = true;
            break;
           }
       }
       return foundMatch;
    }

   /**
   * Getter method for returning
   * the class instances patadata
   **/
   public Map getPataData() {
       return patadata;
   }

   /**
   * Returns the patadata as a
   * String rather than Map
   **/
   public String displayPataData() {
      return patadata.toString();
   }

   /**
   * Search document for a
   * substring that matches a search phrase.
   * If match found, extract word and see
   * if it is a stemmed or unstemmed version
   * of the search phrase.
   * If matched, tag with css class.
   * Returns modified version of the text
   **/
   public Boolean findInText() {


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
    return chunk(dictionaryChunkerTT,working_doc);
   }

   /**
   * Function for taking a chunk of text
   * and replacing the matched searched phrase
   * with a HTML tagged version of it
   **/
   private Boolean chunk(ExactDictionaryChunker chunker, String text) {
        Chunking chunking = chunker.chunk(text.toLowerCase());
        StringBuffer textBuf = new StringBuffer(text);
        Boolean foundMatch = false;
        int offset = 0;
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String type = chunk.type();
            double score = chunk.score();
            String phrase = text.substring(start,end);
            String matched = "<strong><i>"+phrase+"</i></strong>";
            textBuf.replace(start+offset,end+offset,matched);
            foundMatch = true;
            offset = offset + 24;
        }
        working_doc = textBuf.toString();
        return foundMatch;
    }

   /**
   * Returns the modified text document
   **/
   public String getMatchedDoc() {
       return working_doc;
   }

   /**
   * Return stemmed version of the search phrase
   **/
   public String getStemmedSearchPhrase() {
       return search_phrase;
   }

   /**
   * Get unstemmed version of the
   * search phrase
   **/
   public String getUnstemmedSearchPhrase() {
       return unstemmed_search_phrase;
   }

}
