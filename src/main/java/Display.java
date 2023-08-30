import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


public class Display extends Application {
    private XMLExtractor extractor = new XMLExtractor();
    private XMLToJava xmlToJava = new XMLToJava();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label informationLabel = new Label();
        // create two buttons
        Button importButton = new Button("Import XML");
        importButton.setOnAction(event -> {
            // create a file chooser dialog
            FileChooser fileChooser = new FileChooser();
            // set the initial directory
            String home = System.getProperty("user.home");
            fileChooser.setInitialDirectory(new File(home+"/Downloads"));
            // show the dialog and wait for the user to select a file
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            // if the user selected a file, print its path to the console
            if (selectedFile != null) {
                if (!selectedFile.getName().endsWith(".bpmn")) {
                    informationLabel.setText("Invalid file. Only .bpmn files are accepted.");
                } else {
                    try {
                        //extractor.convertIoTElements(selectedFile);
                        xmlToJava.convertXML(selectedFile);
                        informationLabel.setText("The file has been converted to standard BPMN 2.0.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Button exportButton = new Button("Export XML");

        // create a layout container to hold the buttons
        BorderPane root = new BorderPane();
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15,12,15,12));
        hbox.setSpacing(20);
        hbox.setStyle("-fx-background-color: #336699;");
        Paint paint = Color.RED;
        informationLabel.setTextFill(paint);
        informationLabel.setStyle("-fx-background-color: #616161;");
        hbox.getChildren().addAll(importButton,exportButton, informationLabel);

        root.setTop(hbox);

        // create a scene with the layout container as its root
        Scene scene = new Scene(root, 300, 200);

        // set the stage's title and scene
        primaryStage.setTitle("BPMNE4IoT To BPMN 2.0 Converter");
        primaryStage.setScene(scene);

        // show the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
