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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CoachRegistroIntegrantesController {

    @FXML private TextField txtNombre;
    @FXML private DatePicker dpNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private Label lblError;

    // Botón dinámico (cambia entre "AGREGAR" y "GUARDAR CAMBIOS")
    @FXML private Button btnAccion;

    @FXML private ListView<String> listaParticipantes;
    @FXML private Label lblContador;

    private ObservableList<String> participantes = FXCollections.observableArrayList();
    private final int MAX_PARTICIPANTES = 3;
    private int indiceEdicion = -1;

    @FXML
    public void initialize() {
        listaParticipantes.setItems(participantes);
        actualizarContador();
    }

    // --- 1. GESTIÓN DE LA LISTA VISUAL (MEMORIA) ---

    @FXML
    public void handleAgregarOActualizar(ActionEvent event) {
        String nombre = txtNombre.getText();
        LocalDate nacimiento = dpNacimiento.getValue();
        String sexo = cbSexo.getValue();

        if (nombre.isEmpty() || nacimiento == null || sexo == null) {
            lblError.setText("Por favor llena todos los campos.");
            lblError.setVisible(true);
            return;
        }

        String registro = nombre + " | " + nacimiento.toString() + " | " + sexo;

        if (indiceEdicion == -1) {
            // MODO AGREGAR
            if (participantes.size() >= MAX_PARTICIPANTES) {
                lblError.setText("Límite alcanzado (" + MAX_PARTICIPANTES + ").");
                lblError.setVisible(true);
                return;
            }
            participantes.add(registro);
        } else {
            // MODO EDITAR
            participantes.set(indiceEdicion, registro);
            handleLimpiar(); // Salir de edición
        }

        actualizarContador();
        lblError.setVisible(false);
        if (indiceEdicion == -1) handleLimpiar();
    }

    @FXML
    public void handleSeleccionarItem() {
        int index = listaParticipantes.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            String item = listaParticipantes.getSelectionModel().getSelectedItem();
            try {
                String[] datos = item.split(" \\| ");
                txtNombre.setText(datos[0]);
                dpNacimiento.setValue(LocalDate.parse(datos[1]));
                cbSexo.setValue(datos[2]);

                indiceEdicion = index;
                btnAccion.setText("GUARDAR CAMBIOS");
                btnAccion.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
                lblError.setVisible(false);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    public void handleLimpiar() {
        txtNombre.clear();
        dpNacimiento.setValue(null);
        cbSexo.getSelectionModel().clearSelection();
        indiceEdicion = -1;
        btnAccion.setText("AGREGAR A LA LISTA");
        btnAccion.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        listaParticipantes.getSelectionModel().clearSelection();
        lblError.setVisible(false);
    }

    @FXML
    public void handleEliminar() {
        int index = listaParticipantes.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            participantes.remove(index);
            handleLimpiar();
            actualizarContador();
        } else {
            lblError.setText("Selecciona un alumno para eliminar.");
            lblError.setVisible(true);
        }
    }

    private void actualizarContador() {
        lblContador.setText(participantes.size() + " / " + MAX_PARTICIPANTES);
    }

    // --- 2. TRANSACCIÓN FINAL A BASE DE DATOS ---

    @FXML
    public void handleFinalizar(ActionEvent event) {
        if (participantes.isEmpty()) {
            lblError.setText("Debes registrar al menos un integrante.");
            lblError.setVisible(true);
            return;
        }

        // Recuperar datos de la sesión (Pantalla anterior)
        UserSession session = UserSession.getInstance();
        int usuarioId = session.getUserId();
        String nombreCategoria = session.getTempCategoriaNombre();
        String nombreEquipo = session.getTempNombreEquipo();
        String institucion = session.getTempInstitucion();
        int eventoId = session.getTempEventoId();

        if (nombreEquipo == null) {
            lblError.setText("Error: Datos de sesión perdidos.");
            lblError.setVisible(true);
            return;
        }

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false); // INICIO DE TRANSACCIÓN

            try {
                // PASO A: Crear Equipo
                String sqlEquipo = "{call SP_NombreEquipoExiste(?, ?, ?, ?)}";
                int nuevoEquipoId = 0;

                try (CallableStatement stmtEq = conn.prepareCall(sqlEquipo)) {
                    stmtEq.setInt(1, usuarioId);
                    stmtEq.setString(2, nombreCategoria); // Enviamos texto, el SP busca el ID
                    stmtEq.setString(3, nombreEquipo);
                    stmtEq.setString(4, institucion);

                    boolean hasResults = stmtEq.execute();
                    if (hasResults) {
                        try (ResultSet rs = stmtEq.getResultSet()) {
                            if (rs.next()) nuevoEquipoId = rs.getInt("nuevo_equipo_id");
                        }
                    }
                }

                if (nuevoEquipoId == 0) throw new SQLException("No se generó ID para el equipo.");

                // PASO B: Inscribir en Evento
                String sqlEvento = "{call SP_RegistrarEquipoEnEvento(?, ?)}";
                try (CallableStatement stmtEv = conn.prepareCall(sqlEvento)) {
                    stmtEv.setInt(1, nuevoEquipoId);
                    stmtEv.setInt(2, eventoId);
                    stmtEv.execute();
                }

                // PASO C: Insertar Participantes
                String sqlPart = "{call SP_RegistrarParticipante(?, ?, ?, ?)}";
                try (CallableStatement stmtPart = conn.prepareCall(sqlPart)) {
                    for (String p : participantes) {
                        String[] datos = p.split(" \\| ");
                        stmtPart.setInt(1, nuevoEquipoId);
                        stmtPart.setString(2, datos[0]);
                        stmtPart.setDate(3, java.sql.Date.valueOf(LocalDate.parse(datos[1])));
                        stmtPart.setString(4, datos[2]);
                        stmtPart.execute();
                    }
                }

                // COMMIT
                conn.commit();
                session.setTempNombreEquipo(null); // Limpiar sesión

                mostrarNotificacionExito("¡Equipo y Alumnos registrados!");
                cambiarVista(event, "coach_menu.fxml");

            } catch (SQLException ex) {
                conn.rollback(); // ROLLBACK si algo falla
                ex.printStackTrace();
                lblError.setText("Error al guardar: " + ex.getMessage());
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Error de conexión: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    // --- 3. NAVEGACIÓN (EL MÉTODO QUE FALTABA) ---

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa a la pantalla de datos del equipo (los datos se cargarán desde UserSession)
        cambiarVista(event, "coach_registroEquipo.fxml");
    }

    // --- MÉTODOS AUXILIARES ---

    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            Label label = new Label("✅ " + mensaje);
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
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error cargando vista: " + fxml);
        }
    }
}