package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label; // Importación necesaria para Label
import javafx.stage.Stage;

import java.io.IOException;

public class OrganizadorMenuController {

    // --- NUEVO CAMPO FXML ---
    @FXML private Label lblBienvenida; // Label que mostrará "Hola, [Nombre Completo] (Admin)"
    // --------------------------

    @FXML
    public void initialize() {
        // Cargar el nombre y el rol del usuario logueado al inicio
        cargarNombreUsuario();
    }

    /**
     * Carga el nombre del usuario desde la sesión y lo muestra en la barra superior.
     */
    private void cargarNombreUsuario() {
        String nombre = UserSession.getInstance().getNombreCompleto();
        String rol = "Admin"; // Para esta vista siempre se asume el rol de Admin

        if (lblBienvenida != null && nombre != null) {
            // Establece el texto: "Hola, [Nombre Completo]"
            lblBienvenida.setText("Hola, " + nombre);
        } else if (lblBienvenida != null) {
            // Fallback
            lblBienvenida.setText("Hola, Administrador");
        }
    }


    @FXML
    public void handleAsignarJuez(ActionEvent event) {
        System.out.println("Ir a Asignar Juez");
        cambiarVista(event, "organizador_asignarJuez.fxml");
    }

    @FXML
    public void handleCrearEvento(ActionEvent event) {
        System.out.println("Ir a Alta de Evento");
        cambiarVista(event, "organizador_crearEvento.fxml");
    }

    @FXML
    public void handleCrearUsuario(ActionEvent event) {
        System.out.println("Ir a Crear Usuario");
        cambiarVista(event, "organizador_crearUsuario.fxml");
    }

    @FXML
    public void handleVerUsuarios(ActionEvent event) {
        System.out.println("Ir a Ver Directorio de Usuarios");
        cambiarVista(event, "organizador_verUsuarios.fxml");
    }

    /**
     * Maneja la navegación al Directorio de Eventos. Esta vista consolidada
     * ahora sirve como centro para el CRUD de eventos y la selección de reportes.
     */
    @FXML
    public void handleVerEventos(ActionEvent event) {
        System.out.println("Ir a Ver Eventos y Reportes (Consolidado)");
        cambiarVista(event, "organizador_verEventos.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión de admin...");
        // Limpiar la sesión antes de redirigir al login
        UserSession.getInstance().cleanUserSession();
        cambiarVista(event, "login.fxml");
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