package ru.geekbrains.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import ru.geekbrains.client.Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class FXController {
    static private Stage currentStage;
    static private Parent currentRoot;
    private Scene mainScene;
    private FileChooser fileChooser = new FileChooser();
    private Client client = new Client();

    @FXML
    private TextField loginTextField;
    @FXML
    private TextField passwordTextField;

    @FXML
    public void setCurrentStage(Stage stage){

        currentStage = stage;
    }

    @FXML
    public void setMainScene(Scene scene){

        mainScene = scene;
    }

    @FXML
    private void login (ActionEvent event){
        loginTextField.setText("Alex");
        passwordTextField.setText("1234567");


        try{
            client.connect();

        }
        catch(InterruptedException e){
            e.printStackTrace();
        };

        client.authorisationClient(loginTextField.getText(), passwordTextField.getText());

        currentStage.setScene(mainScene);
        currentStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

    }

    @FXML
    private void exit (ActionEvent event){
        System.exit(0);
    }

    @FXML
    private void uploadFile (ActionEvent event) {

        Window currentWindow = currentStage.getOwner();
        fileChooser.setTitle("Выберите файл");

        File file = fileChooser.showOpenDialog(currentWindow);

        try{
            if (file != null) {
                System.out.println(file.getName());
                client.sendFileClient(Paths.get(file.getPath()));
            }
        }
        catch(IOException e){
            e.printStackTrace();
        };

    }


}
