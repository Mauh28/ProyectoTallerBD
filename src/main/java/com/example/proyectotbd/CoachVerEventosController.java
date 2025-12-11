package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class CoachVerEventosController {

    @FXML private TableView<EventoItem> tablaEventos;
    @FXML private TableColumn<EventoItem, Integer> colId;
    @FXML private TableColumn<EventoItem, String> colNombre;
    @FXML private TableColumn<EventoItem, String> colLugar;
    @FXML private TableColumn<EventoItem, String> colFecha;
    @FXML private TableColumn<EventoItem, String> colHoraInicio;
    @FXML private TableColumn<EventoItem, String> colHoraFin;
    @FXML private TableColumn<EventoItem, String> colEstado; // Para mostrar si está vencido/disponible
    @FXML private Label lblMensaje;
    @FXML private Label lblCoachNombre;

    // CORRECCIÓN 1: Usar CoachDAO para acceder al filtro específico
    private CoachDAO coachDao = new CoachDAO();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarEventos();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugar"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHoraInicio.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        colHoraFin.setCellValueFactory(new PropertyValueFactory<>("horaFin"));

        // Renderización de estilo para deshabilitar eventos expirados
        colEstado.setCellFactory(column -> new TableCell<EventoItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    EventoItem evento = getTableRow().getItem();
                    LocalDate fechaEvento = LocalDate.parse(evento.getFecha());

                    if (fechaEvento.isAfter(LocalDate.now())) {
                        setText("DISPONIBLE");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setText("EXPIRADO / ACTIVO");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void cargarEventos() {
        // CORRECCIÓN 2: Obtener el ID del usuario y llamar al método filtrado del DAO
        int coachId = UserSession.getInstance().getUserId();
        try {
            // El DAO ahora trae SOLO eventos futuros y sin conflicto de Juez
            tablaEventos.setItems(coachDao.obtenerEventosSinConflicto(coachId));
        } catch (SQLException e) {
            lblMensaje.setText(e.getMessage());
            // e.printStackTrace();
        }
    }

    @FXML
    public void handleInscribirEquipo(ActionEvent event) {
        EventoItem seleccionado = tablaEventos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            lblMensaje.setText("Selecciona un evento para inscribir a tu equipo.");
            return;
        }

        LocalDate fechaEvento = LocalDate.parse(seleccionado.getFecha());
        LocalDate hoy = LocalDate.now();

        // Validación de fecha (Se mantiene por redundancia, aunque el DAO ya filtra el pasado)
        if (!fechaEvento.isAfter(hoy)) {
            lblMensaje.setText("Error: No puedes inscribirte. El evento ya ha comenzado o ha expirado.");
            return;
        }

        // Si es futuro: guardar ID del evento y navegar a registro
        UserSession session = UserSession.getInstance();
        session.setTempEventoId(seleccionado.getId());
        session.setTempNombreEvento(seleccionado.getNombre());

        // Navegar a la vista de registro modificada
        cambiarVista(event, "coach_registroEquipo.fxml");
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "coach_menu.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            // e.printStackTrace();
            lblMensaje.setText("Error al cargar la vista: " + fxml);
        }
    }
}