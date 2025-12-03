package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class CoachMenuController {

    // Referencia al contenedor principal para obtener el Stage
    @FXML private BorderPane rootPane;

    @FXML
    public void handleRegistrarEquipo(ActionEvent event) {
        System.out.println("Iniciando flujo de registro...");
        cambiarVista(event, "coach_registroEquipo.fxml");
    }

    @FXML
    public void handleVerReportes(ActionEvent event) {
        System.out.println("Abriendo reporte de resultados...");
        cambiarVista(event, "coach_reportes.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión...");

        // SOLUCIÓN: Usamos 'rootPane' en lugar de 'event' para obtener la ventana
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Obtenemos el Stage desde el rootPane que ya está cargado en pantalla
            Stage stage = (Stage) rootPane.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método genérico para botones normales (Registrar, Reportes)
    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Para botones normales, esto sí funciona bien
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar: " + fxml);
        }
    }
}