package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
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
import javafx.animation.PauseTransition;

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

    // Botón que cambia de texto (AGREGAR / GUARDAR CAMBIOS)
    @FXML private Button btnAccion;
    @FXML private Button btnRegresar;

    @FXML private ListView<String> listaParticipantes;
    @FXML private Label lblContador;

    // Lista en memoria (aún no en BD)
    private ObservableList<String> participantes = FXCollections.observableArrayList();
    private final int MAX_PARTICIPANTES = 3;

    // Control para saber si estamos editando un item de la lista (-1 = No)
    private int indiceEdicion = -1;

    @FXML
    public void initialize() {
        listaParticipantes.setItems(participantes);

        // 1. CARGAR DATOS SI ES MODO EDICIÓN
        if (UserSession.getInstance().isModoEdicion()) {
            int idEquipo = UserSession.getInstance().getEquipoIdTemp();
            System.out.println("Cargando alumnos para equipo ID: " + idEquipo);

            try {
                // Llama a tu DAO (CoachDAO)
                CoachDAO dao = new CoachDAO();
                ObservableList<String> existentes = dao.obtenerParticipantes(idEquipo);

                if (existentes != null && !existentes.isEmpty()) {
                    participantes.setAll(existentes);
                }
            } catch (SQLException e) {
                lblError.setText("Error cargando alumnos: " + e.getMessage());
                lblError.setVisible(true);
            }

            // 2. OCULTAR BOTÓN DE REGRESAR
            // (Necesitas agregar @FXML private Button btnRegresar; en la clase)
            if (btnRegresar != null) {
                btnRegresar.setVisible(false); // O .setDisable(true);
            }
        }

        actualizarContador();
    }

    // --- 1. GESTIÓN LOCAL DE LA LISTA (MEMORIA) ---

    @FXML
    public void handleAgregarOActualizar(ActionEvent event) {
        String nombre = txtNombre.getText();
        LocalDate nacimiento = dpNacimiento.getValue();
        String sexo = cbSexo.getValue();

        // Validaciones locales
        if (nombre.isEmpty() || nacimiento == null || sexo == null) {
            lblError.setText("Por favor llena todos los campos.");
            lblError.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
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
            handleLimpiar(); // Salir del modo edición
        }

        actualizarContador();
        lblError.setVisible(false);

        // Limpiar campos si estábamos agregando
        if (indiceEdicion == -1) handleLimpiar();
    }

    @FXML
    public void handleSeleccionarItem() {
        int index = listaParticipantes.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            String item = listaParticipantes.getSelectionModel().getSelectedItem();
            try {
                // Recuperar datos del texto para ponerlos en los campos
                String[] datos = item.split(" \\| ");
                txtNombre.setText(datos[0]);
                dpNacimiento.setValue(LocalDate.parse(datos[1]));
                cbSexo.setValue(datos[2]);

                // Activar modo edición visual
                indiceEdicion = index;
                btnAccion.setText("GUARDAR CAMBIOS");
                btnAccion.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;"); // Naranja
                lblError.setVisible(false);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    public void handleLimpiar() {
        txtNombre.clear();
        dpNacimiento.setValue(null);
        cbSexo.getSelectionModel().clearSelection();

        // Resetear a modo agregar
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
            handleLimpiar();
            actualizarContador();
        } else {
            lblError.setText("Selecciona un alumno de la lista para eliminar.");
            lblError.setVisible(true);
        }
    }

    private void actualizarContador() {
        lblContador.setText(participantes.size() + " / " + MAX_PARTICIPANTES);
    }

    // --- 2. TRANSACCIÓN FINAL A LA BASE DE DATOS ---

    @FXML
    public void handleFinalizar(ActionEvent event) {
        // CAMBIA ESTO: antes decía isEmpty(), ahora debe validar que sean 3 exactos
        if (participantes.size() != 3) {
            lblError.setText("Regla del Torneo: El equipo debe tener exactamente 3 integrantes.");
            lblError.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
            lblError.setVisible(true);
            return; // Detenemos aquí, no molestamos a la Base de Datos
        }

        UserSession session = UserSession.getInstance();
        int equipoId = session.getEquipoIdTemp();
        boolean esEdicion = session.isModoEdicion(); // <--- IMPORTANTE

        // VALIDACIÓN DIFERENCIADA
        if (!esEdicion) {
            // Solo si es NUEVO validamos que existan los datos temporales
            if (session.getTempNombreEquipo() == null || session.getTempCategoriaNombre() == null) {
                lblError.setText("Error: Datos de sesión perdidos. Vuelve a iniciar.");
                lblError.setVisible(true);
                return;
            }
        } else {
            // Si es EDICIÓN, validamos solo el ID
            if (equipoId == 0) {
                lblError.setText("Error crítico: No se encontró el ID del equipo a editar.");
                lblError.setVisible(true);
                return;
            }
        }

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // --- BLOQUE A: CREACIÓN (Solo si NO es edición) ---
                if (!esEdicion) {
                    // 1. Crear Equipo
                    String sqlEquipo = "{call SP_NombreEquipoExiste(?, ?, ?)}";
                    int nuevoEquipoId = 0;
                    try (CallableStatement stmtEq = conn.prepareCall(sqlEquipo)) {
                        stmtEq.setInt(1, session.getUserId());
                        stmtEq.setString(2, session.getTempCategoriaNombre());
                        stmtEq.setString(3, session.getTempNombreEquipo());
                        // stmtEq.setString(4, session.getTempInstitucion());

                        if (stmtEq.execute()) {
                            try (ResultSet rs = stmtEq.getResultSet()) {
                                if (rs.next()) nuevoEquipoId = rs.getInt("nuevo_equipo_id");
                            }
                        }
                    }
                    if (nuevoEquipoId == 0) throw new SQLException("No se generó ID.");
                    equipoId = nuevoEquipoId; // Actualizamos ID para usarlo abajo

                    // 2. Inscribir
                    String sqlEvento = "{call SP_RegistrarEquipoEnEvento(?, ?)}";
                    try (CallableStatement stmtEv = conn.prepareCall(sqlEvento)) {
                        stmtEv.setInt(1, equipoId);
                        stmtEv.setInt(2, session.getTempEventoId());
                        stmtEv.execute();
                    }
                }

                // --- BLOQUE B: LIMPIEZA (Solo si ES edición) ---
                else {
                    // Borramos alumnos viejos para re-insertar la lista nueva
                    String sqlLimpiar = "{call SP_EliminarParticipantesPorEquipo(?)}";
                    try (CallableStatement stmtClean = conn.prepareCall(sqlLimpiar)) {
                        stmtClean.setInt(1, equipoId);
                        stmtClean.execute();
                    }
                }

                // --- BLOQUE C: INSERCIÓN (Común para ambos) ---
                String sqlPart = "{call SP_RegistrarParticipante(?, ?, ?, ?)}";
                try (CallableStatement stmtPart = conn.prepareCall(sqlPart)) {
                    for (String p : participantes) {
                        String[] datos = p.split(" \\| ");
                        stmtPart.setInt(1, equipoId);
                        stmtPart.setString(2, datos[0]);
                        stmtPart.setDate(3, java.sql.Date.valueOf(LocalDate.parse(datos[1])));
                        stmtPart.setString(4, datos[2]);
                        stmtPart.execute();
                    }
                }

                conn.commit();

                // Limpieza final
                session.setModoEdicion(false);
                session.setTempNombreEquipo(null);

                mostrarNotificacionExito("¡Datos guardados correctamente!");

                if (esEdicion) cambiarVista(event, "coach_misEquipos.fxml");
                else cambiarVista(event, "coach_menu.fxml");

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                lblError.setText("Error BD: " + ex.getMessage());
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Error conexión: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    // --- 3. NAVEGACIÓN ---

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa a la pantalla de datos del equipo.
        // Como los datos siguen en UserSession, se llenarán solos.
        cambiarVista(event, "coach_registroEquipo.fxml");
    }

    // --- MÉTODOS AUXILIARES ---

    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            Label label = new Label("✅ " + mensaje);
            label.setStyle(
                    "-fx-background-color: #27ae60;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-padding: 20px;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);"
            );

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

    @FXML
    public void handleIrAlMenu(ActionEvent event) {
        // Limpiamos la sesión para evitar datos basura en el próximo intento
        UserSession.getInstance().setTempNombreEquipo(null);
        UserSession.getInstance().setTempInstitucion(null);
        UserSession.getInstance().setModoEdicion(false);

        cambiarVista(event, "coach_menu.fxml");
    }

}