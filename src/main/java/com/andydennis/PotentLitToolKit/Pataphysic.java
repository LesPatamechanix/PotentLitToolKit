/**
 *  Main class that pulls everything together.
 *  Display: GUI via JavaFX framework
 *  Inputs: Document selected by user to be searched
 *          Ontology in YAML RDF format
 *          Search phrase selected by user.
**/

import java.util.*;
import java.io.*;
import java.io.File;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.web.HTMLEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Pataphysic extends Application {

    LinkedHashMap<List, List> loaded_ontology = new LinkedHashMap<List, List>();
    String input_document = "";
    String search_term = "";
    String category_selected = "";
    FindMatch search_for_term;
    TextDocumentLoader input_text;
    ReplaceMatch apply_replace;
    RDFLoader ontology;
    HTMLEditor modifiedDocument = new HTMLEditor();
    Label ontLoadLabel = new Label("No ontology loaded");
    Label pataDataLabel = new Label("No patadata found");
    Button searchBtn = new Button();
    Button textReplaceBtn = new Button();
    final ToggleGroup group = new ToggleGroup();
    Boolean has_patadata = false;

    @Override
    public void start(Stage primaryStage) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Patadata based find and replace");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        addOntologyLoader(grid, primaryStage);
        addSearch(grid);
        addDocumentLoader(grid, primaryStage);
        addSubsBtns(grid);
        addPataDataList(grid);
        addSaveBtn(grid, primaryStage);

        Scene scene = new Scene(grid, 900, 700);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(Pataphysic.class.getResource("style.css").toExternalForm());
        primaryStage.setTitle("Patadata based find and replace");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
    *  Add the Ontology file picker component
    *  to the JavaFX GUI
    **/
    public void addOntologyLoader(GridPane grid, Stage primaryStage){

        FileChooser ontologyFile = new FileChooser();
        Button btn = new Button();
        btn.setText("Load Ontology");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               try{
                  File file = ontologyFile.showOpenDialog(primaryStage);
                    if (file != null) {
                        loadOntology(file);
                    }
               } catch (Exception e) {
                 System.out.println("Error loading ontology file");
               }
            }
        });

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 1);
        grid.add(ontLoadLabel, 3, 1);
    }

    /**
    * Called by GUI takes the input ontology
    * and returns an object from the YAML
    **/
    public void loadOntology(File ont_file) throws Exception{
        try{
           ontology = new RDFLoader(ont_file);
           ontLoadLabel.setText(ont_file.getName());
           loaded_ontology = ontology.get_processed_ontology();
           check_enable_search();
        } catch (Exception e) {
           System.out.println("Error generating patadata");
        }
    }


    /**
    * Adds a label, text input field and button
    * to the JavaFX GUI.
    **/
    public void addSearch(GridPane grid) {

        Label searchLabel = new Label("Enter search term:");

        TextField searchField = new TextField();

        searchBtn.setText("Search");
        searchBtn.setDisable(true);
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               try{
                 search_term = searchField.getCharacters().toString();
                 searchForPhrase();
               } catch (Exception e) {
                 System.out.println("Error getting search term");
               }
            }
        });

        VBox vbSearchBtn = new VBox(10);
        vbSearchBtn.setAlignment(Pos.TOP_LEFT);
        vbSearchBtn.getChildren().add(searchLabel);
        vbSearchBtn.setAlignment(Pos.BOTTOM_RIGHT);
        vbSearchBtn.getChildren().add(searchField);
        vbSearchBtn.getChildren().add(searchBtn);
        grid.add(vbSearchBtn,1,3);
    }

    /**
    * Checks if we have a input text doucment and
    * ontology, then passes them with the search phrase
    * to the FindMatch class.
    **/
    public void searchForPhrase() {

      try {
        String modified_text = modifiedDocument.getHtmlText();
        search_for_term = new FindMatch(loaded_ontology, modified_text, search_term);
        Boolean patadata_match = search_for_term.findInOnt();
        Boolean document_match = search_for_term.findInText();
        has_patadata = false;
        if(document_match && patadata_match) {
            has_patadata = true;
            if (group.getSelectedToggle() != null) {
                textReplaceBtn.setDisable(false);
            }
            modifiedDocument.setHtmlText(search_for_term.getMatchedDoc());
            String patastring = search_for_term.displayPataData();
            patastring = patastring.replaceFirst("=","\r ");
            patastring = patastring.replaceAll("[{}]", "");
            patastring = patastring.replaceAll(",", "\r");
            patastring = patastring.replaceAll("=", " : ");
            pataDataLabel.setText(patastring);
        } else {
            pataDataLabel.setText("No match found");
        }

      } catch (Exception e) {
        System.out.println("Error searching for phrase");
      }
    }

    /**
    * Adds a document loading componenet to the screen
    * to allow a user to upload a document to
    * search
    **/
    public void addDocumentLoader(GridPane grid, Stage primaryStage){

        FileChooser textFileChooser = new FileChooser();
        Label docLabel = new Label("Loaded document:");
        grid.add(docLabel, 1, 5);
        TextArea documentField = new TextArea();
        documentField.setEditable(false);
        grid.add(documentField, 1, 6);

        Label modLabel = new Label("Your modified document:");
        grid.add(modLabel, 3, 5);
        grid.add(modifiedDocument, 3, 6);

        Button textLoadBtn = new Button();
        textLoadBtn.setText("Load text document");
        textLoadBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               try{
                  File textFile = textFileChooser.showOpenDialog(primaryStage);
                    if (textFile != null) {
                        loadTextDocument(textFile);
                        documentField.setText(input_document);
                        modifiedDocument.setHtmlText(input_document);
                    }
               } catch (Exception e) {
                 System.out.println("Error opening input text document");
               }
            }
        });

        HBox hbLoadTxtBtn = new HBox(10);
        hbLoadTxtBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbLoadTxtBtn.getChildren().add(textLoadBtn);
        grid.add(hbLoadTxtBtn, 1, 7);
    }

    /**
    * Loads a text document and stores it
    * the text document comes from
    * a file passed to the function
    **/
    public void loadTextDocument(File text_file) throws Exception{

        try{
           input_text = new TextDocumentLoader(text_file);
           input_document = input_text.get_processed_document();
           check_enable_search();
        } catch (Exception e) {
           System.out.println("Error loading text document");
        }
    }

    /**
    * Enable the search button and replace button.
    * If an ontology and text document are loaded
    * enable search.
    * If a radio button is selected, and we have
    * text, ontology and a matched search term
    * enable replace button.
    **/
    public void check_enable_search(){
        if(input_document !="" && loaded_ontology.size() > 0 ){
            searchBtn.setDisable(false);
            if (group.getSelectedToggle() != null && has_patadata) {
               textReplaceBtn.setDisable(false);
            }
        }
    }

    /**
    * Adds a togglegroup for storing
    * radio buttons which are used for
    * choosing which category of patadata
    * to use.
    **/
    public void addSubsBtns(GridPane grid) {


        RadioButton rb1 = new RadioButton("Synonym");
        rb1.setToggleGroup(group);

        RadioButton rb2 = new RadioButton("Antinomy");
        rb2.setToggleGroup(group);

        RadioButton rb3 = new RadioButton("Anomaly");
        rb3.setToggleGroup(group);

        RadioButton rb4 = new RadioButton("Syzygy");
        rb4.setToggleGroup(group);

        RadioButton rb5 = new RadioButton("Clinamen");
        rb5.setToggleGroup(group);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                Toggle old_toggle, Toggle new_toggle) {
                    if (group.getSelectedToggle() != null) {
                        RadioButton value = (RadioButton)group.getSelectedToggle(); // Cast object to radio button
                        category_selected = value.getText().toLowerCase();
                        if(input_document !="" && loaded_ontology.size() > 0 && has_patadata){
                            textReplaceBtn.setDisable(false);
                        }
                    }
                }
        });

        HBox hbRadioBtns = new HBox(10);
        hbRadioBtns.setAlignment(Pos.BOTTOM_RIGHT);
        hbRadioBtns.getChildren().add(rb1);
        hbRadioBtns.getChildren().add(rb2);
        hbRadioBtns.getChildren().add(rb3);
        hbRadioBtns.getChildren().add(rb4);
        hbRadioBtns.getChildren().add(rb5);
        grid.add(hbRadioBtns,3,7);

        textReplaceBtn.setText("Replace");
        textReplaceBtn.setDisable(true);
        textReplaceBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               try{

                  apply_replace = new ReplaceMatch(
                                search_for_term.getMatchedDoc(),
                                search_for_term.getPataData(),
                                category_selected,
                                search_for_term.getStemmedSearchPhrase(),
                                search_for_term.getUnstemmedSearchPhrase());
                   modifiedDocument.setHtmlText(apply_replace.replaceInText());
               } catch (Exception e) {
                 System.out.println("Error replacing text");
               }
            }
        });

        HBox hbRepTxtBtn = new HBox(10);
        hbRepTxtBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbRepTxtBtn.getChildren().add(textReplaceBtn);
        grid.add(hbRepTxtBtn, 3, 8);

    }

    /**
    * Adds a save button to the screen
    * this allows the modified text to
    * be saved as an HTML document
    **/
    public void addSaveBtn(GridPane grid, Stage primaryStage) {

        Button saveBtn = new Button();
        saveBtn.setText("Save as HTML");
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               try{
                     FileChooser fileChooser = new FileChooser();
                     FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
                     fileChooser.getExtensionFilters().add(extFilter);

                     File file = fileChooser.showSaveDialog(primaryStage);

                     if(file != null){
                       saveFile(modifiedDocument.getHtmlText(), file);
                     }
               } catch (Exception e) {
                 System.out.println("Error selecting/creating HTML file to save.");
               }
            }
        });

        HBox hbSaveBtn = new HBox(10);
        hbSaveBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbSaveBtn.getChildren().add(saveBtn);
        grid.add(hbSaveBtn, 3, 9);

    }

    /**
    * Methodology or saving the
    * modified document to a
    * a passed in HTML document
    **/
    private void saveFile(String content, File file){
        try{
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
           System.out.println("Error writing HTML to file");
        }
    }

    /**
    * Add a label to the screen
    * where matched pata data can
    * be displayed
    **/
    public void addPataDataList(GridPane grid) {
        grid.add(pataDataLabel, 3, 3);
    }
}
