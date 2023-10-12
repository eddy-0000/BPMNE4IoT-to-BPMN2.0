import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


public class Display extends Application {
    private XMLToJava xmlToJava = new XMLToJava();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label informationLabel = new Label();
        // create two buttons
        Button importButton = new Button("Import XML");
        Button exportButton = new Button("Export XML");

        // Configure the FileChooser
        FileChooser fileChooser = new FileChooser();
        // set the initial directory
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home+"/Documents"));

        importButton.setOnAction(event -> {
            // show the dialog and wait for the user to select a file
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            // if the user selected a file, print its path to the console
            if (selectedFile != null) {
                if (!selectedFile.getName().endsWith(".bpmn")) {
                    informationLabel.setTextFill(Color.RED);
                    informationLabel.setText("Invalid file. Only .bpmn files are accepted.");
                } else {
                    try {
                        informationLabel.setTextFill(Color.BLACK);
                        informationLabel.setText("Converting model...");
                        xmlToJava.convertXML(selectedFile);
                        informationLabel.setTextFill(Color.GREEN);
                        informationLabel.setText("The model has been converted to standard BPMN 2.0.");
                    } catch (Exception e) {
                        informationLabel.setTextFill(Color.RED);
                        informationLabel.setText(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        exportButton.setOnAction(event -> {
            // Show the file dialog and get the selected file
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            if (selectedFile != null) {
                try {
                    xmlToJava.exportXML(selectedFile);
                    informationLabel.setTextFill(Color.GREEN);
                    informationLabel.setText("The file has been exported.");
                } catch (Exception e){
                    informationLabel.setTextFill(Color.RED);
                    informationLabel.setText(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        // create a layout container to hold the buttons
        VBox root = new VBox(20);
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15,12,15,12));
        hbox.setSpacing(20);
        hbox.setStyle("-fx-background-color: #336699;");
        hbox.getChildren().addAll(importButton,exportButton);

        HBox infoHbox = new HBox(20);
        infoHbox.getChildren().addAll(informationLabel);

        root.getChildren().addAll(hbox,infoHbox);

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
