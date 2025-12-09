package com.example.proyectotbd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        stage.setTitle("Sistema de Gestion VexRobotics");
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();


    }
}
