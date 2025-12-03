package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class CoachRegistroIntegrantesController {

    @FXML private TextField txtNombre;
    @FXML private DatePicker dpNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private Label lblError;
    @FXML private Button btnAccion; // Botón Agregar/Actualizar

    @FXML private ListView<String> listaParticipantes;
    @FXML private Label lblContador;

    private ObservableList<String> participantes = FXCollections.observableArrayList();
    private final int MAX_PARTICIPANTES = 3;

    // Variable para saber si estamos editando (-1 = No editando)
    private int indiceEdicion = -1;

    @FXML
    public void initialize() {
        listaParticipantes.setItems(participantes);
        actualizarContador();
    }

    // --- MANEJO DE SELECCIÓN EN LA LISTA ---
    @FXML
    public void handleSeleccionarItem() {
        int index = listaParticipantes.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            String item = listaParticipantes.getSelectionModel().getSelectedItem();

            // Convertir texto "Nombre | Fecha | Sexo" a los campos
            try {
                String[] datos = item.split(" \\| ");
                txtNombre.setText(datos[0]);
                dpNacimiento.setValue(LocalDate.parse(datos[1]));
                cbSexo.setValue(datos[2]);

                // Activar modo edición
                indiceEdicion = index;
                btnAccion.setText("GUARDAR CAMBIOS");
                btnAccion.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;"); // Naranja
                lblError.setVisible(false);

            } catch (Exception e) {
                // Error al parsear (raro)
            }
        }
    }

    @FXML
    public void handleLimpiar() {
        txtNombre.clear();
        dpNacimiento.setValue(null);
        cbSexo.getSelectionModel().clearSelection();

        // Salir de modo edición
        indiceEdicion = -1;
        btnAccion.setText("AGREGAR A LA LISTA");
        btnAccion.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;"); // Azul
        listaParticipantes.getSelectionModel().clearSelection();
        lblError.setVisible(false);
    }

    @FXML
    public void handleEliminar() {
        int index = listaParticipantes.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            participantes.remove(index);
            handleLimpiar(); // Resetear formulario
            actualizarContador();
        } else {
            lblError.setText("Selecciona un alumno de la lista para eliminar.");
            lblError.setVisible(true);
        }
    }

    @FXML
    public void handleAgregarOActualizar(ActionEvent event) {
        String nombre = txtNombre.getText();
        LocalDate nacimiento = dpNacimiento.getValue();
        String sexo = cbSexo.getValue();

        if (nombre.isEmpty() || nacimiento == null || sexo == null) {
            lblError.setText("Por favor llena todos los campos.");
            lblError.setStyle("-fx-text-fill: #e74c3c;");
            lblError.setVisible(true);
            return;
        }

        String registro = nombre + " | " + nacimiento.toString() + " | " + sexo;

        if (indiceEdicion == -1) {
            // MODO AGREGAR
            if (participantes.size() >= MAX_PARTICIPANTES) {
                lblError.setText("Límite alcanzado (" + MAX_PARTICIPANTES + "). Elimina o edita uno existente.");
                lblError.setVisible(true);
                return;
            }
            participantes.add(registro);
        } else {
            // MODO ACTUALIZAR
            participantes.set(indiceEdicion, registro);
            handleLimpiar(); // Volver a modo agregar
        }

        actualizarContador();
        lblError.setVisible(false);
        if (indiceEdicion == -1) handleLimpiar(); // Limpiar si fue agregar
    }

    private void actualizarContador() {
        lblContador.setText(participantes.size() + " / " + MAX_PARTICIPANTES);
    }

    @FXML
    public void handleFinalizar(ActionEvent event) {
        if (participantes.isEmpty()) {
            lblError.setText("Debes registrar al menos un integrante.");
            lblError.setStyle("-fx-text-fill: #e74c3c;");
            lblError.setVisible(true);
            return;
        }

        int equipoId = UserSession.getInstance().getEquipoIdTemp();
        if (equipoId == 0) {
            lblError.setText("Error crítico: No se encontró el ID del equipo.");
            lblError.setVisible(true);
            return;
        }

        System.out.println("Guardando en BD...");
        String sql = "{call SP_RegistrarParticipante(?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false); // Transacción

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                for (String p : participantes) {
                    String[] datos = p.split(" \\| ");
                    stmt.setInt(1, equipoId);
                    stmt.setString(2, datos[0]);
                    stmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(datos[1])));
                    stmt.setString(4, datos[2]);
                    stmt.execute();
                }

                conn.commit(); // Confirmar cambios

                mostrarNotificacionExito("¡Inscripción finalizada con éxito!");
                cambiarVista(event, "coach_menu.fxml");

            } catch (SQLException ex) {
                conn.rollback(); // Cancelar todo si falla uno
                ex.printStackTrace();

                // Mostrar error para que el usuario corrija
                lblError.setText("Error al guardar: " + ex.getMessage() + "\nRevisa la edad del alumno y vuelve a intentar.");
                lblError.setStyle("-fx-text-fill: #e74c3c;");
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Error de conexión: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    // --- POP UP DE ÉXITO ---
    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            Label label = new Label("🎓 " + mensaje);
            label.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 20px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            toastStage.setX(screenBounds.getMaxX() - 450);
            toastStage.setY(screenBounds.getMaxY() - 100);

            toastStage.show();
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}