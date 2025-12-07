package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class JuezEquiposUnificadoController {

    @FXML private VBox vboxListaEquipos;
    @FXML private Label lblCategoriaActual;

    // Instancia exclusiva del DAO de Juez
    private JuezDAO juezDao = new JuezDAO();

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "juez_evento.fxml");
    }

    @FXML
    public void handleFiltroCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoria = btn.getText(); // "PRIMARIA", "SECUNDARIA", etc.

        lblCategoriaActual.setText("Viendo: " + categoria);

        // Cargar datos reales
        cargarEquipos(categoria);
    }

    private void cargarEquipos(String categoria) {
        vboxListaEquipos.getChildren().clear();

        // Recuperar el Evento seleccionado (Guardado previamente en sesión al entrar al módulo)
        // Nota: Asegúrate de que JuezEventoController haya hecho: UserSession.getInstance().setTempEventoId(id);
        int eventoId = UserSession.getInstance().getTempEventoId();

        // Validación de seguridad por si no hay evento en sesión
        if (eventoId == 0) {
            Label error = new Label("Error: No se ha seleccionado un evento.");
            error.setStyle("-fx-text-fill: red;");
            vboxListaEquipos.getChildren().add(error);
            return;
        }

        try {
            // Llamada al JuezDAO
            ObservableList<EquipoItem> equipos = juezDao.obtenerEquiposPorEventoYCategoria(eventoId, categoria);

            if (equipos.isEmpty()) {
                Label vacio = new Label("No hay equipos registrados en esta categoría.");
                vacio.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 20; -fx-font-size: 14;");
                vboxListaEquipos.getChildren().add(vacio);
                return;
            }

            // Generar tarjetas dinámicamente
            for (EquipoItem equipo : equipos) {
                vboxListaEquipos.getChildren().add(crearTarjetaEquipo(equipo));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label error = new Label("Error al cargar BD: " + e.getMessage());
            error.setStyle("-fx-text-fill: red;");
            vboxListaEquipos.getChildren().add(error);
        }
    }

    // Método para crear la tarjeta visual (HBox) de cada equipo
    private HBox crearTarjetaEquipo(EquipoItem equipo) {
        HBox tarjeta = new HBox();
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setSpacing(10);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tarjeta.setEffect(new DropShadow(5, Color.color(0,0,0,0.1)));

        // 1. Nombre del Equipo
        Label lblNombre = new Label(equipo.getNombre());
        lblNombre.setPrefWidth(400);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        // 2. Institución
        Label lblInst = new Label(equipo.getInstitucion());
        lblInst.setPrefWidth(300);
        lblInst.setStyle("-fx-text-fill: #7f8c8d;");

        // 3. Estado (Visualización condicional)
        boolean yaEvaluado = "EVALUADO".equalsIgnoreCase(equipo.getEstado());

        Label lblEstado = new Label(yaEvaluado ? "EVALUADO" : "PENDIENTE");
        lblEstado.setPrefWidth(200);
        lblEstado.setStyle(yaEvaluado
                ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                : "-fx-text-fill: #e67e22; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Botón de Acción
        Button btnAccion = new Button(yaEvaluado ? "EDITAR" : "EVALUAR");
        btnAccion.setStyle(yaEvaluado
                ? "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand;"
                : "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

        // Acción del botón: Ir a evaluar este equipo específico
        btnAccion.setOnAction(e -> irAEvaluar(equipo));

        tarjeta.getChildren().addAll(lblNombre, lblInst, lblEstado, spacer, btnAccion);
        return tarjeta;
    }

    // Método auxiliar para mostrar alertas de error
    private void mostrarAlertaError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error en la Evaluación");
        alert.setHeaderText("Fallo en la operación de inicio");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void irAEvaluar(EquipoItem equipo) {
        try {
            int juezId = UserSession.getInstance().getUserId();
            int eventoId = UserSession.getInstance().getTempEventoId();

            // 1. Llamada al método unificado: iniciarOObtenerEvaluacion
            // Este método devuelve un objeto EvaluacionIds con los 4 IDs.
            JuezDAO.EvaluacionIds ids = juezDao.iniciarEvaluacion(equipo.getId(), eventoId, juezId);

            // 2. Guardar todos los IDs y nombres en la Sesión (para evitar llamadas extra a la BD)
            UserSession session = UserSession.getInstance();

            session.setEvaluacionIdTemp(ids.evaluacionId);
            session.setTempNombreEquipo(equipo.getNombre());

            // Guardamos los IDs de las áreas para JuezEvaluacionController
            session.setIdDisenoTemp(ids.idDiseno);
            session.setIdProgramacionTemp(ids.idProg);
            session.setIdConstruccionTemp(ids.idConst);

            // 3. Ir a la Rúbrica. Si es edición, el controlador de evaluación se cargará con estos IDs.
            cambiarVistaBoton(vboxListaEquipos, "juez_evaluacion.fxml");

        } catch (SQLException ex) {
            // Capturamos errores de negocio reales (ej: límite de 3 jueces) o de BD
            System.err.println("Error de negocio o BD: " + ex.getMessage());

            // Mostrar alerta al usuario con el mensaje de error de negocio
            mostrarAlertaError(ex.getMessage());
        }
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

    // Método auxiliar para cambiar vista desde un evento de código (no ActionEvent directo)
    private void cambiarVistaBoton(Node nodo, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) nodo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}