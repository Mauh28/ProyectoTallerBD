package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private void irAEvaluar(EquipoItem equipo) {
        System.out.println("Iniciando evaluación para: " + equipo.getNombre());

        try {
            int juezId = UserSession.getInstance().getUserId();
            int eventoId = UserSession.getInstance().getTempEventoId();

            // Llamar al SP_IniciarEvaluacionSegura a través del DAO
            // Este SP maneja si ya existe (error de unicidad) o si es nueva
            int evalId = juezDao.iniciarEvaluacion(equipo.getId(), eventoId, juezId);

            if (evalId > 0) {
                // Guardar ID de la evaluación para usarlo en la siguiente pantalla (Rúbrica)
                UserSession.getInstance().setEvaluacionIdTemp(evalId);

                // Pasar el nombre del equipo a la sesión temporal para mostrarlo en el título
                UserSession.getInstance().setTempNombreEquipo(equipo.getNombre());

                // Ir a la Rúbrica
                cambiarVistaBoton(vboxListaEquipos, "juez_evaluacion.fxml");
            }
        } catch (SQLException ex) {
            String msj = ex.getMessage();
            if (msj.contains("Error: Ya has iniciado una evaluación") || msj.contains("Duplicate entry")) {
                System.out.println("Evaluación existente. Entrando en modo edición...");
                // Aquí deberías tener lógica para recuperar el ID existente si no lo devolvió el SP,
                // pero por ahora asumimos que el flujo continúa.
                cambiarVistaBoton(vboxListaEquipos, "juez_evaluacion.fxml");
            } else {
                System.err.println("Error de negocio: " + msj);
                // Aquí podrías mostrar un Alert
            }
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