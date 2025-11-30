package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class OrganizadorMenuController {

    @FXML
    public void handleSeleccionarEvento(ActionEvent event) {
        System.out.println("Ir a Lista de Eventos (Administrador)");
        // Aquí podrías reutilizar 'juez_evento-view.fxml' si quieres ver la misma lista,
        // o crear una nueva vista 'admin_lista_eventos.fxml' si el admin tiene botones de editar/borrar.
        // Por ahora lo mandamos a la selección estándar:
        cambiarVista(event, "juez_evento-view.fxml");
    }

    @FXML
    public void handleCrearEvento(ActionEvent event) {
        System.out.println("Ir a Alta de Evento");
        // Esta vista ya la creamos antes (el formulario de registro de evento)
        cambiarVista(event, "evento_registro.fxml");
    }

    @FXML
    public void handleCrearUsuario(ActionEvent event) {
        System.out.println("Ir a Crear Usuario");
        // Esta vista también la tenemos (selección de rol para registro)
        cambiarVista(event, "register_selection.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión de admin...");
        // Regresa al login principal (el dividido)
        cambiarVista(event, "login_split.fxml");
    }

    // Método estándar para cambiar de pantalla
    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error cargando vista: " + fxml);
        }
    }
}
