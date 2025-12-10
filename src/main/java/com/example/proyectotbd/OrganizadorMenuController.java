package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Importación necesaria

public class OrganizadorMenuController {

    @FXML private Label lblBienvenida;

    @FXML
    public void initialize() {
        cargarNombreUsuario();
    }

    /**
     * Carga el nombre completo del usuario desde UserSession y lo muestra en la barra superior.
     */
    private void cargarNombreUsuario() {
        String nombre = UserSession.getInstance().getNombreCompleto();

        if (lblBienvenida != null) {
            if (nombre != null) {
                // Establece el texto: "Hola, [Nombre Completo]"
                lblBienvenida.setText("Hola, " + nombre);
            } else {
                // Fallback si la sesión no tiene nombre
                lblBienvenida.setText("Hola, Administrador");
            }
        }
    }


    @FXML
    public void handleAsignarJuez(ActionEvent event) {
        System.out.println("Ir a Asignar Juez");
        cambiarVista(event, "organizador_asignarJuez.fxml");
    }

    @FXML
    public void handleCrearEvento(ActionEvent event) {
        System.out.println("Ir a Crear Evento");
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

    @FXML
    public void handleVerEventos(ActionEvent event) {
        System.out.println("Ir a Ver Eventos y Reportes (Consolidado)");
        cambiarVista(event, "organizador_verEventos.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión de admin...");
        UserSession.getInstance().cleanUserSession();
        cambiarVista(event, "login.fxml");
    }

    // Método estándar para cambiar de pantalla (Ligeramente modificado para robustez)
    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            // Intenta obtener la URL del recurso de forma segura
            URL url = getClass().getResource(fxml);
            if (url == null) {
                // Si la URL es nula, el archivo FXML NO existe o la ruta es incorrecta.
                throw new IOException("El archivo FXML no fue encontrado: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Muestra un error más claro en la consola sobre la fuente del problema
            System.err.println("\n*** ERROR CRÍTICO DE NAVEGACIÓN ***");
            System.err.println("Fallo al cargar la vista FXML: " + fxml);
            System.err.println("Causa más probable: 1) Nombre de archivo incorrecto; 2) Error de sintaxis en el FXML de destino.");
            System.err.println("***********************************\n");
        }
    }
}