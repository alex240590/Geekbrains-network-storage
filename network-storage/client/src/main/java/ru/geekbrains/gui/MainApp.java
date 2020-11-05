package ru.geekbrains.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;
import java.io.InputStream;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Scene loginScene = new Scene(root, 400, 400);

        FXController controller = loader.getController();
        controller.setCurrentStage(primaryStage);

        //main scene
        FXMLLoader loaderMain = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent rootMain = loaderMain.load();
        Scene mainScene = new Scene(rootMain, 400, 400);

        controller.setMainScene(mainScene);

        primaryStage.setTitle("Network-storage");

        InputStream iconStream = getClass().getResourceAsStream("/disk.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

}
