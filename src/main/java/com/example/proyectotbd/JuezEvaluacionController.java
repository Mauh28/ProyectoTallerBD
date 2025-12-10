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
import javafx.scene.layout.VBox; // Aseguramos VBox si necesitas deshabilitar contenedores
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class JuezEvaluacionController {

    @FXML private Label lblNombreEquipo;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnEnviar; // Añadimos el botón de envío para deshabilitarlo

    // --- Contenedor de CheckBoxes (Asumimos que están dentro del ScrollPane o VBox) ---
    // Si tienes un contenedor principal VBox/GridPane dentro del ScrollPane,
    // puedes inyectarlo para deshabilitar su contenido.
    // @FXML private VBox vboxContenidoPrincipal;

    @FXML private Label lblMensajeEstado; // Añadido para mostrar el mensaje de tiempo

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

        // 1. Verificar si la evaluación está permitida (Control de horario)
        verificarHoraDeEvaluacion();

        // 2. TODO: Si es modo EDICIÓN, aquí se debería implementar la lógica para cargar los estados.
    }

    /**
     * Verifica la hora actual contra la hora de inicio del evento y deshabilita la interfaz si es muy pronto.
     */
    private void verificarHoraDeEvaluacion() {
        // Asumimos que tempEventoId es el ID del evento que estamos evaluando.
        int eventoId = UserSession.getInstance().getTempEventoId();

        if (eventoId == 0) {
            mostrarAlertaError("Error de sesión: No se detectó el ID del evento.");
            deshabilitarControles(true);
            return;
        }

        try {
            LocalDateTime horaInicioEvento = juezDAO.obtenerFechaHoraInicioEvento(eventoId);
            LocalDateTime ahora = LocalDateTime.now();

            if (horaInicioEvento == null) {
                lblMensajeEstado.setText("Error: No se pudo verificar el horario del evento.");
                deshabilitarControles(true);
                return;
            }

            if (ahora.isBefore(horaInicioEvento)) {
                // Evaluación NO disponible
                long minutosRestantes = ChronoUnit.MINUTES.between(ahora, horaInicioEvento);
                long horas = minutosRestantes / 60;
                long minutos = minutosRestantes % 60;

                String mensaje = String.format("La evaluación inicia con el evento. Faltan %d horas y %d minutos.", horas, minutos);

                // Muestra el mensaje y deshabilita toda la interfaz de evaluación.
                if (lblMensajeEstado != null) {
                    lblMensajeEstado.setText(mensaje);
                    lblMensajeEstado.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                }
                deshabilitarControles(true);

            } else {
                // Evaluación disponible
                if (lblMensajeEstado != null) {
                    lblMensajeEstado.setText("Evaluación activa. El evento ha comenzado.");
                    lblMensajeEstado.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
                deshabilitarControles(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (lblMensajeEstado != null) {
                lblMensajeEstado.setText("Error de Base de Datos al verificar el horario.");
            }
            deshabilitarControles(true);
        }
    }

    /**
     * Deshabilita/Habilita todos los CheckBoxes y el botón de envío.
     */
    private void deshabilitarControles(boolean deshabilitar) {
        // Recoge todos los CheckBoxes
        List<CheckBox> allCheckboxes = Arrays.asList(
                chkBitRegistroFechas, chkBitJustificacion, chkBitDiagramas, chkBitOrtografia, chkBitPresentacion,
                chkDigVideo, chkDigModelado, chkDigAnalisis, chkDigEnsamble, chkDigModeloAcorde, chkDigSimulacion, chkDigRestricciones,
                chkProgSoftware, chkProgFunciones, chkProgComplejidad, chkProgJustificacion, chkProgEstructuras, chkProgDepuracion, chkProgModular,
                chkAutoDocumentacion, chkAutoVinculacion, chkAutoSensores,
                chkManVinculo, chkManHabilidad, chkManRespuesta, chkManDocCodigo,
                chkDemo15s, chkDemoNoIncon, chkDemoObjetivo, chkDemoExplicacion,
                chkConsEstetico, chkConsEstable, chkConsTransmision, chkConsSensores, chkConsCableado, chkConsNeumatico, chkConsAlcance, chkConsVex, chkConsProcesador,
                chkLibEstructuras, chkLibVelocidades, chkLibEngranes, chkLibGravedad, chkLibSistTrans, chkLibPotencia, chkLibTorque, chkLibVelocidad
        );

        // Deshabilitar todos los CheckBoxes
        for (CheckBox cb : allCheckboxes) {
            if (cb != null) {
                cb.setDisable(deshabilitar);
            }
        }

        // Deshabilitar el botón de envío
        if (btnEnviar != null) {
            btnEnviar.setDisable(deshabilitar);
        }

        // Deshabilitar el ScrollPane para evitar interacción (opcional, si es necesario)
        if (scrollPane != null) {
            scrollPane.setDisable(deshabilitar);
        }
    }


    @FXML
    public void handleEnviar(ActionEvent event) {
        // *** IMPORTANTE: Si deshabilitarControles(true) fue llamado, este método no debería ejecutarse ***

        UserSession session = UserSession.getInstance();

        int equipoId = session.getEquipoIdTemp();
        int eventoId = session.getTempEventoId();
        int juezId = session.getUserId();

        if (equipoId == 0 || eventoId == 0) {
            mostrarAlertaError("Error crítico: Datos de equipo/evento perdidos.");
            return;
        }

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. PRIMER PASO: CREAR LA EVALUACIÓN Y OBTENER LOS IDs
                JuezDAO.EvaluacionIds ids = juezDAO.iniciarEvaluacion(conn, equipoId, eventoId, juezId);

                // 2. SEGUNDO PASO: GUARDAR LOS DETALLES USANDO LOS IDs GENERADOS
                // --- DISEÑO ---
                juezDAO.registrarBitacora(conn, ids.idDiseno,
                        chkBitRegistroFechas.isSelected(), chkBitJustificacion.isSelected(), chkBitDiagramas.isSelected(), chkBitOrtografia.isSelected(), chkBitPresentacion.isSelected());

                juezDAO.registrarMedioDigital(conn, ids.idDiseno,
                        chkDigVideo.isSelected(), chkDigModelado.isSelected(), chkDigAnalisis.isSelected(), chkDigEnsamble.isSelected(), chkDigModeloAcorde.isSelected(), chkDigSimulacion.isSelected(), chkDigRestricciones.isSelected());

                // --- PROGRAMACIÓN ---
                juezDAO.registrarInspeccionProg(conn, ids.idProg,
                        chkProgSoftware.isSelected(), chkProgFunciones.isSelected(), chkProgComplejidad.isSelected(), chkProgJustificacion.isSelected(), chkProgEstructuras.isSelected(), chkProgDepuracion.isSelected(), chkProgModular.isSelected());

                juezDAO.registrarAutonomo(conn, ids.idProg,
                        chkAutoDocumentacion.isSelected(), chkAutoVinculacion.isSelected(), chkAutoSensores.isSelected());

                juezDAO.registrarManipulado(conn, ids.idProg,
                        chkManVinculo.isSelected(), chkManHabilidad.isSelected(), chkManRespuesta.isSelected(), chkManDocCodigo.isSelected());

                juezDAO.registrarDemostracion(conn, ids.idProg,
                        chkDemo15s.isSelected(), chkDemoNoIncon.isSelected(), chkDemoObjetivo.isSelected(), chkDemoExplicacion.isSelected());

                // --- CONSTRUCCIÓN ---
                juezDAO.registrarConstruccionInsp(conn, ids.idConst,
                        chkConsEstetico.isSelected(), chkConsEstable.isSelected(), chkConsTransmision.isSelected(), chkConsSensores.isSelected(), chkConsCableado.isSelected(), chkConsNeumatico.isSelected(), chkConsAlcance.isSelected(), chkConsVex.isSelected(), chkConsProcesador.isSelected());

                juezDAO.registrarLibreta(conn, ids.idConst,
                        chkLibEstructuras.isSelected(), chkLibVelocidades.isSelected(), chkLibEngranes.isSelected(), chkLibGravedad.isSelected(), chkLibSistTrans.isSelected(), chkLibPotencia.isSelected(), chkLibTorque.isSelected(), chkLibVelocidad.isSelected());

                // 3. CONFIRMAR TODO
                conn.commit();

                mostrarNotificacionExito("¡Evaluación enviada y registrada correctamente!");

                cambiarVista(event, "juez_equiposUnificado.fxml");

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                mostrarAlertaError("Error al guardar: " + ex.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error de conexión: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
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