package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
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
import java.sql.Connection;
import java.sql.SQLException;

public class JuezEvaluacionController {

    @FXML private Label lblNombreEquipo;
    @FXML private ScrollPane scrollPane; // Añadido para mejor control del diseño si es necesario

    // --- DISEÑO ---
    @FXML private CheckBox chkBitRegistroFechas, chkBitJustificacion, chkBitDiagramas, chkBitOrtografia, chkBitPresentacion;
    @FXML private CheckBox chkDigVideo, chkDigModelado, chkDigAnalisis, chkDigEnsamble, chkDigModeloAcorde, chkDigSimulacion, chkDigRestricciones;

    // --- PROGRAMACIÓN ---
    @FXML private CheckBox chkProgSoftware, chkProgFunciones, chkProgComplejidad, chkProgJustificacion, chkProgEstructuras, chkProgDepuracion, chkProgModular;
    @FXML private CheckBox chkAutoDocumentacion, chkAutoVinculacion, chkAutoSensores;
    @FXML private CheckBox chkManVinculo, chkManHabilidad, chkManRespuesta, chkManDocCodigo;
    @FXML private CheckBox chkDemo15s, chkDemoNoIncon, chkDemoObjetivo, chkDemoExplicacion;

    // --- CONSTRUCCIÓN ---
    @FXML private CheckBox chkConsEstetico, chkConsEstable, chkConsTransmision, chkConsSensores, chkConsCableado, chkConsNeumatico, chkConsAlcance, chkConsVex, chkConsProcesador;
    @FXML private CheckBox chkLibEstructuras, chkLibVelocidades, chkLibEngranes, chkLibGravedad, chkLibSistTrans, chkLibPotencia, chkLibTorque, chkLibVelocidad;

    private JuezDAO juezDAO = new JuezDAO();

    @FXML
    public void initialize() {
        String nombre = UserSession.getInstance().getTempNombreEquipo();
        if (nombre != null) lblNombreEquipo.setText(nombre);

        // TODO: Si es modo EDICIÓN, aquí se debería implementar la lógica para
        // cargar los estados (true/false) de cada CheckBox desde la BD,
        // usando los IDs de área almacenados en UserSession.
    }

    @FXML
    public void handleEnviar(ActionEvent event) {
        UserSession session = UserSession.getInstance();
        int evalId = session.getEvaluacionIdTemp();

        // 1. Validaciones
        if (evalId <= 0) {
            mostrarAlertaError("Error de Sesión: ID de evaluación no válido o perdido.");
            return;
        }

        // OPTIMIZACIÓN: Recuperamos los IDs de área directamente de la sesión
        int idDiseno = session.getIdDisenoTemp();
        int idProg = session.getIdProgramacionTemp();
        int idConst = session.getIdConstruccionTemp();

        if (idDiseno <= 0 || idProg <= 0 || idConst <= 0) {
            mostrarAlertaError("Error de Sesión: IDs de áreas no válidos. Vuelve a seleccionar el equipo.");
            return;
        }

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false); // INICIO TRANSACCIÓN

            try {
                // ELIMINADO: La llamada a juezDAO.obtenerIdsAreas(conn, evalId, juezId)

                // 2. Guardar DISEÑO (Usando idDiseno de la Sesión)
                juezDAO.registrarBitacora(conn, idDiseno,
                        chkBitRegistroFechas.isSelected(), chkBitJustificacion.isSelected(), chkBitDiagramas.isSelected(), chkBitOrtografia.isSelected(), chkBitPresentacion.isSelected());

                juezDAO.registrarMedioDigital(conn, idDiseno,
                        chkDigVideo.isSelected(), chkDigModelado.isSelected(), chkDigAnalisis.isSelected(), chkDigEnsamble.isSelected(), chkDigModeloAcorde.isSelected(), chkDigSimulacion.isSelected(), chkDigRestricciones.isSelected());

                // 3. Guardar Área: PROGRAMACIÓN (Usando idProg de la Sesión)
                juezDAO.registrarInspeccionProg(conn, idProg,
                        chkProgSoftware.isSelected(), chkProgFunciones.isSelected(), chkProgComplejidad.isSelected(), chkProgJustificacion.isSelected(), chkProgEstructuras.isSelected(), chkProgDepuracion.isSelected(), chkProgModular.isSelected());

                juezDAO.registrarAutonomo(conn, idProg,
                        chkAutoDocumentacion.isSelected(), chkAutoVinculacion.isSelected(), chkAutoSensores.isSelected());

                juezDAO.registrarManipulado(conn, idProg,
                        chkManVinculo.isSelected(), chkManHabilidad.isSelected(), chkManRespuesta.isSelected(), chkManDocCodigo.isSelected());

                juezDAO.registrarDemostracion(conn, idProg,
                        chkDemo15s.isSelected(), chkDemoNoIncon.isSelected(), chkDemoObjetivo.isSelected(), chkDemoExplicacion.isSelected());

                // 4. Guardar Área: CONSTRUCCIÓN (Usando idConst de la Sesión)
                juezDAO.registrarConstruccionInsp(conn, idConst,
                        chkConsEstetico.isSelected(), chkConsEstable.isSelected(), chkConsTransmision.isSelected(), chkConsSensores.isSelected(), chkConsCableado.isSelected(), chkConsNeumatico.isSelected(), chkConsAlcance.isSelected(), chkConsVex.isSelected(), chkConsProcesador.isSelected());

                juezDAO.registrarLibreta(conn, idConst,
                        chkLibEstructuras.isSelected(), chkLibVelocidades.isSelected(), chkLibEngranes.isSelected(), chkLibGravedad.isSelected(), chkLibSistTrans.isSelected(), chkLibPotencia.isSelected(), chkLibTorque.isSelected(), chkLibVelocidad.isSelected());

                // TODO CORRECTO -> COMMIT
                conn.commit();

                mostrarNotificacionExito("¡Evaluación guardada correctamente!");

                // Volver a la lista de equipos
                cambiarVista(event, "juez_equiposUnificado.fxml"); // Navegación corregida

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                mostrarAlertaError(ex.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError(e.getMessage());
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa a la lista de equipos (no al menú principal, para mantener el contexto)
        cambiarVista(event, "juez_equiposUnificado.fxml");
    }

    // --- Helpers (PopUp, Loader y Alerta de Error) ---

    private void mostrarAlertaError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Evaluación");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

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
        } catch (IOException e) { e.printStackTrace(); }
    }
}