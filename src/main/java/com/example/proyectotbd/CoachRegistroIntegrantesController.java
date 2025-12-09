package com.example.proyectotbd;

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
import java.sql.Types;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class CoachRegistroIntegrantesController {

    @FXML private TextField txtNombre;
    @FXML private DatePicker dpNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private Label lblError;

    @FXML private Button btnAccion;
    @FXML private Button btnRegresar;
    @FXML private ListView<String> listaParticipantes;
    @FXML private Label lblContador;

    private ObservableList<String> participantes = FXCollections.observableArrayList();
    private final int MAX_PARTICIPANTES = 3;
    private int indiceEdicion = -1;
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");

    @FXML
    public void initialize() {
        listaParticipantes.setItems(participantes);

        // Obtenemos la categoría seleccionada al inicio
        int categoriaId = UserSession.getInstance().getTempCategoriaId();

        // 1. CONFIGURAR VALIDACIÓN CON RESTRICCIÓN DE EDAD DINÁMICA
        if (categoriaId != 0) {
            configurarValidacionFecha(categoriaId);
        } else {
            // Si el ID es 0, no se seleccionó categoría. Aplicar solo restricción futura.
            configurarValidacionFecha(0);
        }

        // 2. CONFIGURAR VALIDACIÓN EN TIEMPO REAL PARA EL NOMBRE
        configurarValidacionNombre();

        // 3. CARGAR DATOS SI ES MODO EDICIÓN
        if (UserSession.getInstance().isModoEdicion()) {
            int idEquipo = UserSession.getInstance().getEquipoIdTemp();
            System.out.println("Cargando alumnos para equipo ID: " + idEquipo);

            try {
                CoachDAO dao = new CoachDAO();
                ObservableList<String> existentes = dao.obtenerParticipantes(idEquipo);

                if (existentes != null && !existentes.isEmpty()) {
                    participantes.setAll(existentes);
                }
            } catch (SQLException e) {
                lblError.setText("Error cargando alumnos: " + e.getMessage());
                lblError.setVisible(true);
            }

            if (btnRegresar != null) {
                btnRegresar.setVisible(false);
            }
        }

        actualizarContador();
    }

    // --- NUEVO: VALIDACIÓN DE FECHA (CON RESTRICCIÓN DE RANGO POR CATEGORÍA) ---
    private void configurarValidacionFecha(int categoriaId) {
        final LocalDate HOY = LocalDate.now();
        LocalDate limiteMinimoFecha; // Fecha más antigua permitida
        LocalDate limiteMaximoFecha; // Fecha más reciente permitida


// --- Definición de Rangos por Categoría (CORREGIDO) ---
        switch (categoriaId) {
            case 1: // Primaria: 6 a 12 años
                // Máximo 12 años: Debe haber nacido DESPUÉS de (HOY - 12 años)
                limiteMinimoFecha = HOY.minusYears(12).minusDays(1);
                // Mínimo 6 años: Debe haber nacido ANTES de (HOY - 6 años)
                limiteMaximoFecha = HOY.minusYears(6).plusDays(1);
                break;

            case 2: // Secundaria: 12 a 15 años
                // Máximo 15 años: Debe haber nacido DESPUÉS de (HOY - 15 años)
                limiteMinimoFecha = HOY.minusYears(15).minusDays(1);
                // Mínimo 12 años: Debe haber nacido ANTES de (HOY - 12 años)
                limiteMaximoFecha = HOY.minusYears(12).plusDays(1);
                break;

            case 3: // Preparatoria: 15 a 18 años
                // Máximo 18 años: Debe haber nacido DESPUÉS de (HOY - 18 años)
                limiteMinimoFecha = HOY.minusYears(18).minusDays(1);
                // Mínimo 15 años: Debe haber nacido ANTES de (HOY - 15 años)
                limiteMaximoFecha = HOY.minusYears(15).plusDays(1);
                break;

            case 4: // Profesional: 18 años en adelante
                // Mínimo 18 años: Debe haber nacido ANTES de (HOY - 18 años)
                limiteMaximoFecha = HOY.minusYears(18).plusDays(1);
                // Sin límite de edad superior (Edad máxima arbitraria de 100 años)
                limiteMinimoFecha = HOY.minusYears(100);
                break;

            default: // Caso sin categoría (o error)
                limiteMinimoFecha = HOY.minusYears(100);
                limiteMaximoFecha = HOY; // Máximo hoy (no futuro)
        }

        final LocalDate minDate = limiteMinimoFecha;
        final LocalDate maxDate = limiteMaximoFecha;

        // A. Restringir el calendario visualmente
        dpNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Deshabilitar fechas futuras
                if (date.isAfter(HOY)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcdd2;");
                    return;
                }

                // Aplicar la restricción del rango de edad para la categoría
                if (date.isBefore(minDate) || date.isAfter(maxDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fce4ec;");
                } else {
                    setDisable(false);
                    setStyle("");
                }
            }
        });

        // B. Listener para avisar si la fecha es inválida (aunque el calendario esté filtrado, si la pegan)
        dpNacimiento.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                // Validación estricta en el momento del cambio
                if (newDate.isBefore(minDate) || newDate.isAfter(maxDate)) {
                    mostrarError("Fecha fuera del rango de edad permitido para la categoría seleccionada.");
                } else {
                    lblError.setVisible(false);
                }
            }
        });
    }

    // --- NUEVO: VALIDACIÓN DE NOMBRE (SOLO TEXTO + LÍMITE 50 CHARS) ---
    private void configurarValidacionNombre() {
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                txtNombre.setText(oldValue);
                return;
            }
            if (!PATRON_NOMBRE.matcher(newValue).matches()) {
                txtNombre.setText(oldValue);
                txtNombre.setStyle("-fx-border-color: red;");
            } else {
                txtNombre.setStyle("");
            }
        });
    }

    // --- 1. GESTIÓN LOCAL DE LA LISTA (MEMORIA) ---

    @FXML
    public void handleAgregarOActualizar(ActionEvent event) {
        // 1. Obtener datos crudos
        String rawNombre = txtNombre.getText();
        LocalDate nacimiento = dpNacimiento.getValue();
        String sexo = cbSexo.getValue();

        int categoriaId = UserSession.getInstance().getTempCategoriaId();
        final LocalDate HOY = LocalDate.now();

        // 2. Validaciones Básicas (Vacío)
        if (rawNombre == null || rawNombre.trim().isEmpty() || nacimiento == null || sexo == null) {
            mostrarError("Por favor llena todos los campos.");
            return;
        }

        // 3. Validación de Rango de Edad (Doble chequeo si la restricción visual falló)
        LocalDate limiteMinimo, limiteMaximo;
        switch (categoriaId) {
            case 1: limiteMinimo = HOY.minusYears(12).minusDays(1); limiteMaximo = HOY.minusYears(8).plusDays(1); break;
            case 2: limiteMinimo = HOY.minusYears(15).minusDays(1); limiteMaximo = HOY.minusYears(13).plusDays(1); break;
            default: limiteMinimo = HOY.minusYears(100); limiteMaximo = HOY; // Sin restricción fuerte
        }

        if (nacimiento.isBefore(limiteMinimo) || nacimiento.isAfter(limiteMaximo)) {
            mostrarError("La fecha de nacimiento no cumple con la edad requerida para la categoría '" + UserSession.getInstance().getTempCategoriaNombre() + "'.");
            return;
        }

        // 4. Normalización y Validación de Nombre Completo
        String nombre = capitalizarTexto(rawNombre);
        if (!nombre.contains(" ")) {
            mostrarError("Por favor ingresa nombre y apellido.");
            return;
        }

        // 5. Validación de Duplicados (Local)
        for (int i = 0; i < participantes.size(); i++) {
            if (i != indiceEdicion) {
                String[] datos = participantes.get(i).split(" \\| ");
                if (datos[0].equalsIgnoreCase(nombre)) {
                    mostrarError("El alumno '" + nombre + "' ya está en la lista.");
                    return;
                }
            }
        }

        // 6. Crear el registro formateado
        String registro = nombre + " | " + nacimiento.toString() + " | " + sexo;

        // 7. Lógica de Inserción / Edición
        if (indiceEdicion == -1) {
            // MODO AGREGAR
            if (participantes.size() >= MAX_PARTICIPANTES) {
                mostrarError("Límite alcanzado (" + MAX_PARTICIPANTES + ").");
                return;
            }
            participantes.add(registro);
        } else {
            // MODO EDITAR
            participantes.set(indiceEdicion, registro);
            handleLimpiar();
        }

        // 8. Actualizar interfaz
        actualizarContador();
        lblError.setVisible(false);

        if (indiceEdicion == -1) {
            txtNombre.clear();
            dpNacimiento.setValue(null);
            cbSexo.getSelectionModel().clearSelection();
            txtNombre.requestFocus();
        }
    }

    // Método auxiliar para mostrar errores
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setStyle("-fx-text-fill: #e74c3c;");
        lblError.setVisible(true);
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
        txtNombre.setStyle("");

        indiceEdicion = -1;
        btnAccion.setText("AGREGAR A LA LISTA");
        btnAccion.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
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
        if (participantes.size() != 3) {
            lblError.setText("Regla del Torneo: El equipo debe tener exactamente 3 integrantes.");
            lblError.setStyle("-fx-text-fill: #e74c3c;");
            lblError.setVisible(true);
            return;
        }

        UserSession session = UserSession.getInstance();
        int equipoId = session.getEquipoIdTemp();
        boolean esEdicion = session.isModoEdicion();

        if (!esEdicion) {
            if (session.getTempNombreEquipo() == null || session.getTempCategoriaNombre() == null) {
                lblError.setText("Error: Datos de sesión perdidos. Vuelve a iniciar.");
                lblError.setVisible(true);
                return;
            }
        } else {
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

                        if (stmtEq.execute()) {
                            try (ResultSet rs = stmtEq.getResultSet()) {
                                if (rs.next()) nuevoEquipoId = rs.getInt("nuevo_equipo_id");
                            }
                        }
                    }
                    if (nuevoEquipoId == 0) throw new SQLException("No se generó ID.");
                    equipoId = nuevoEquipoId;

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
        UserSession.getInstance().setTempNombreEquipo(null);
        UserSession.getInstance().setTempInstitucion(null);
        UserSession.getInstance().setModoEdicion(false);

        cambiarVista(event, "coach_menu.fxml");
    }

    private String capitalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return texto;

        String[] palabras = texto.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1).toLowerCase());
                }
                resultado.append(" ");
            }
        }
        return resultado.toString().trim();
    }
}