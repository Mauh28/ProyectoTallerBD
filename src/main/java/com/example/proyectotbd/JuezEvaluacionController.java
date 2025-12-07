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
    }

    @FXML
    public void handleEnviar(ActionEvent event) {
        int evalId = UserSession.getInstance().getEvaluacionIdTemp();
        int juezId = UserSession.getInstance().getUserId();

        if (evalId == 0) {
            System.out.println("Error crítico: No hay ID de evaluación en sesión.");
            return;
        }

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false); // INICIO TRANSACCIÓN

            try {
                // --- CAMBIO AQUÍ ---
                // Pasamos 'conn' como primer parámetro
                JuezDAO.IdsAreas ids = juezDAO.obtenerIdsAreas(conn, evalId, juezId);

                // 2. Guardar DISEÑO
                juezDAO.registrarBitacora(conn, ids.idDiseno,
                        chkBitRegistroFechas.isSelected(), chkBitJustificacion.isSelected(), chkBitDiagramas.isSelected(), chkBitOrtografia.isSelected(), chkBitPresentacion.isSelected());

                juezDAO.registrarMedioDigital(conn, ids.idDiseno,
                        chkDigVideo.isSelected(), chkDigModelado.isSelected(), chkDigAnalisis.isSelected(), chkDigEnsamble.isSelected(), chkDigModeloAcorde.isSelected(), chkDigSimulacion.isSelected(), chkDigRestricciones.isSelected());

                // 3. Guardar Área: PROGRAMACIÓN
                juezDAO.registrarInspeccionProg(conn, ids.idProg,
                        chkProgSoftware.isSelected(), chkProgFunciones.isSelected(), chkProgComplejidad.isSelected(), chkProgJustificacion.isSelected(), chkProgEstructuras.isSelected(), chkProgDepuracion.isSelected(), chkProgModular.isSelected());

                juezDAO.registrarAutonomo(conn, ids.idProg,
                        chkAutoDocumentacion.isSelected(), chkAutoVinculacion.isSelected(), chkAutoSensores.isSelected());

                juezDAO.registrarManipulado(conn, ids.idProg,
                        chkManVinculo.isSelected(), chkManHabilidad.isSelected(), chkManRespuesta.isSelected(), chkManDocCodigo.isSelected());

                juezDAO.registrarDemostracion(conn, ids.idProg,
                        chkDemo15s.isSelected(), chkDemoNoIncon.isSelected(), chkDemoObjetivo.isSelected(), chkDemoExplicacion.isSelected());

                // 4. Guardar Área: CONSTRUCCIÓN
                juezDAO.registrarConstruccionInsp(conn, ids.idConst,
                        chkConsEstetico.isSelected(), chkConsEstable.isSelected(), chkConsTransmision.isSelected(), chkConsSensores.isSelected(), chkConsCableado.isSelected(), chkConsNeumatico.isSelected(), chkConsAlcance.isSelected(), chkConsVex.isSelected(), chkConsProcesador.isSelected());

                juezDAO.registrarLibreta(conn, ids.idConst,
                        chkLibEstructuras.isSelected(), chkLibVelocidades.isSelected(), chkLibEngranes.isSelected(), chkLibGravedad.isSelected(), chkLibSistTrans.isSelected(), chkLibPotencia.isSelected(), chkLibTorque.isSelected(), chkLibVelocidad.isSelected());

                // TODO CORRECTO -> COMMIT
                conn.commit();

                mostrarNotificacionExito("¡Evaluación guardada correctamente!");

                // Volver a la lista de equipos
                cambiarVista(event, "juez_menu.fxml");

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                // Mostrar alerta de error (puedes implementar un showAlert)
                System.err.println("Error SQL al guardar: " + ex.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "juez_equiposUnificado.fxml");
    }

    // --- Helpers (PopUp y Loader) ---

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