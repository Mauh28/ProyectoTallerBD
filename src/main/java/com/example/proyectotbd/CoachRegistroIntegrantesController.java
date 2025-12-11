package com.example.proyectotbd;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import java.sql.Types;
import java.time.LocalDate;
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

    // PATRÓN: Solo permite letras (a-z, acentos), Ñ/ñ y espacios. SIN NÚMEROS.
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");

    // Temporizador para Debounce (espera 500ms antes de validar)
    private PauseTransition pause = new PauseTransition(Duration.millis(500));

    @FXML
    public void initialize() {
        if (cbSexo.getItems().isEmpty()) {
            cbSexo.setItems(FXCollections.observableArrayList("Femenino", "Masculino"));
        }

        dpNacimiento.setEditable(false);
        listaParticipantes.setItems(participantes);

        int categoriaId = UserSession.getInstance().getTempCategoriaId();

        configurarValidacionFecha(categoriaId);
        configurarValidacionNombre(); // <--- AQUÍ ESTÁ LA LÓGICA MEJORADA

        // Cargar datos si es modo edición
        if (UserSession.getInstance().isModoEdicion()) {
            int idEquipo = UserSession.getInstance().getEquipoIdTemp();
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
            if (btnRegresar != null) btnRegresar.setVisible(false);
        }
        actualizarContador();
    }

    // --- CONFIGURACIÓN DE VALIDACIÓN EN TIEMPO REAL (MEJORADA) ---
    private void configurarValidacionNombre() {
        // A. Qué hacer cuando el usuario deja de escribir por 0.5s
        pause.setOnFinished(event -> {
            String nombre = txtNombre.getText().trim();

            // Si está vacío, no hacemos nada (el listener ya limpió estilos)
            if (nombre.isEmpty()) return;

            // 1. VALIDACIÓN: Nombre y Apellido (Debe tener espacio)
            if (!nombre.contains(" ")) {
                Platform.runLater(() -> {
                    txtNombre.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;"); // Rojo
                    lblError.setText("⚠️ Ingresa Nombre y Apellido (debe haber un espacio).");
                    lblError.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    lblError.setVisible(true);
                });
                return; // Detenemos aquí, no consultamos BD
            }

            // 2. VALIDACIÓN: Duplicado en el EQUIPO ACTUAL (Lista Local)
            boolean duplicadoLocal = false;
            for (int i = 0; i < participantes.size(); i++) {
                // Si estamos editando, ignoramos el índice actual
                if (i == indiceEdicion) continue;

                // El formato en la lista es "Nombre | Fecha | Sexo"
                String nombreExistente = participantes.get(i).split(" \\| ")[0];
                if (nombreExistente.equalsIgnoreCase(nombre)) {
                    duplicadoLocal = true;
                    break;
                }
            }

            if (duplicadoLocal) {
                Platform.runLater(() -> {
                    txtNombre.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    lblError.setText("⚠️ Este alumno ya está en la lista de tu equipo.");
                    lblError.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    lblError.setVisible(true);
                });
                return; // Detenemos aquí
            }

            // 3. VALIDACIÓN: Duplicado en OTROS EQUIPOS (Consulta BD)
            // Solo si pasa las anteriores y cumple el regex
            if (nombre.length() >= 3 && PATRON_NOMBRE.matcher(nombre).matches()) {
                verificarDuplicadoEnBD(nombre);
            }
        });

        // B. Listener de escritura (Validaciones de Formato Básico)
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            // 1. Longitud
            if (newValue.length() > 50) {
                txtNombre.setText(oldValue);
                return;
            }

            // 2. Regex (Solo letras)
            if (!PATRON_NOMBRE.matcher(newValue).matches()) {
                txtNombre.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                return;
            } else {
                // Si el formato es válido, quitamos el error visual momentáneamente
                // para dejar escribir. El 'pause' hará la validación fuerte.
                txtNombre.setStyle("");
                lblError.setVisible(false);
            }

            // 3. Reiniciar temporizador
            pause.playFromStart();
        });
    }

    // --- Consulta a Base de Datos (Solo Lectura) ---
    private void verificarDuplicadoEnBD(String nombreParticipante) {
        int eventoId = UserSession.getInstance().getTempEventoId();
        int equipoId = UserSession.getInstance().getEquipoIdTemp();

        String sql = "{? = call FN_VerificarDuplicadoParticipante(?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, nombreParticipante);
            stmt.setInt(3, eventoId);
            stmt.setInt(4, equipoId);

            stmt.execute();

            int existe = stmt.getInt(1);

            Platform.runLater(() -> {
                if (existe > 0) {
                    txtNombre.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    lblError.setText("⚠️ El alumno ya está registrado en OTRO equipo del evento.");
                    lblError.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    lblError.setVisible(true);
                } else {
                    // ÉXITO: Borde verde suave
                    txtNombre.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px;");
                    lblError.setVisible(false);
                }
            });

        } catch (SQLException e) {
            // Silencio en consola, error visual si es crítico
        }
    }

    // --- LÓGICA DE FECHAS ---
    private static class RangoEdad {
        final LocalDate limiteAntiguo;
        final LocalDate limiteReciente;

        RangoEdad(LocalDate antiguo, LocalDate reciente) {
            this.limiteAntiguo = antiguo;
            this.limiteReciente = reciente;
        }
    }

    private RangoEdad obtenerLimitesEdad(int categoriaId) {
        final LocalDate HOY = LocalDate.now();
        switch (categoriaId) {
            case 1: return new RangoEdad(HOY.minusYears(12), HOY.minusYears(6));
            case 2: return new RangoEdad(HOY.minusYears(15), HOY.minusYears(12));
            case 3: return new RangoEdad(HOY.minusYears(18), HOY.minusYears(15));
            case 4: return new RangoEdad(HOY.minusYears(100), HOY.minusYears(18));
            default: return new RangoEdad(HOY.minusYears(100), HOY);
        }
    }

    private void configurarValidacionFecha(int categoriaId) {
        final LocalDate HOY = LocalDate.now();
        final RangoEdad rango = obtenerLimitesEdad(categoriaId);

        dpNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean fueraDeRango = date.isBefore(rango.limiteAntiguo) || date.isAfter(rango.limiteReciente);
                if (empty || date.isAfter(HOY) || fueraDeRango) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fce4ec;");
                } else {
                    setDisable(false);
                    setStyle("");
                }
            }
        });

        dpNacimiento.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                boolean fueraDeRango = newDate.isBefore(rango.limiteAntiguo) || newDate.isAfter(rango.limiteReciente);
                if (fueraDeRango) {
                    mostrarError("Fecha fuera del rango de edad permitido.");
                } else {
                    if (!txtNombre.getStyle().contains("#e74c3c")) lblError.setVisible(false);
                }
            }
        });
    }

    // --- BOTÓN AGREGAR ---
    @FXML
    public void handleAgregarOActualizar(ActionEvent event) {
        String rawNombre = txtNombre.getText().trim(); // Trim importante
        LocalDate nacimiento = dpNacimiento.getValue();
        String sexo = cbSexo.getValue();
        int categoriaId = UserSession.getInstance().getTempCategoriaId();

        // 1. Validaciones Básicas
        if (rawNombre.isEmpty() || nacimiento == null || sexo == null) {
            mostrarError("Por favor llena todos los campos.");
            return;
        }

        // 2. Validación Nombre y Apellido (Bloqueo final)
        if (!rawNombre.contains(" ")) {
            mostrarError("Ingresa Nombre y Apellido.");
            txtNombre.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }

        // 3. Si hay advertencia visual (rojo), no dejar pasar
        if (txtNombre.getStyle().contains("#e74c3c")) {
            mostrarError("Corrige el nombre (Duplicado o Formato incorrecto).");
            return;
        }

        // 4. Validación Edad
        final RangoEdad rango = obtenerLimitesEdad(categoriaId);
        if (nacimiento.isBefore(rango.limiteAntiguo) || nacimiento.isAfter(rango.limiteReciente)) {
            mostrarError("La fecha de nacimiento no cumple con la edad requerida.");
            return;
        }

        String nombre = capitalizarTexto(rawNombre);

        // 5. Validación Duplicados Locales (Redundancia de seguridad)
        for (int i = 0; i < participantes.size(); i++) {
            if (i != indiceEdicion) {
                String nombreExistente = participantes.get(i).split(" \\| ")[0];
                if (nombreExistente.equalsIgnoreCase(nombre)) {
                    mostrarError("El alumno '" + nombre + "' ya está en esta lista.");
                    return;
                }
            }
        }

        // 6. Agregar/Editar
        String registro = nombre + " | " + nacimiento.toString() + " | " + sexo;

        if (indiceEdicion == -1) {
            if (participantes.size() >= MAX_PARTICIPANTES) {
                mostrarError("Límite alcanzado (" + MAX_PARTICIPANTES + ").");
                return;
            }
            participantes.add(registro);
        } else {
            participantes.set(indiceEdicion, registro);
            handleLimpiar();
        }

        actualizarContador();
        lblError.setVisible(false);
        if (indiceEdicion == -1) handleLimpiar();
    }

    // --- MÉTODOS FINALES (Guardar en BD) ---
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

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!esEdicion) {
                    String sqlEquipo = "{call SP_NombreEquipoExiste(?, ?, ?, ?, ?)}";
                    int nuevoEquipoId = 0;
                    try (CallableStatement stmtEq = conn.prepareCall(sqlEquipo)) {
                        stmtEq.setInt(1, session.getUserId());
                        stmtEq.setString(2, session.getTempCategoriaNombre());
                        stmtEq.setString(3, session.getTempNombreEquipo());
                        stmtEq.setInt(4, session.getTempEventoId());
                        stmtEq.registerOutParameter(5, Types.INTEGER);
                        stmtEq.execute();
                        nuevoEquipoId = stmtEq.getInt(5);
                    }
                    if (nuevoEquipoId <= 0) throw new SQLException("No se generó ID.");
                    equipoId = nuevoEquipoId;

                    String sqlEvento = "{call SP_RegistrarEquipoEnEvento(?, ?)}";
                    try (CallableStatement stmtEv = conn.prepareCall(sqlEvento)) {
                        stmtEv.setInt(1, equipoId);
                        stmtEv.setInt(2, session.getTempEventoId());
                        stmtEv.execute();
                    }
                } else {
                    String sqlLimpiar = "{call SP_EliminarParticipantesPorEquipo(?)}";
                    try (CallableStatement stmtClean = conn.prepareCall(sqlLimpiar)) {
                        stmtClean.setInt(1, equipoId);
                        stmtClean.execute();
                    }
                }

                String sqlPart = "{call SP_RegistrarParticipante(?, ?, ?, ?, ?)}";
                try (CallableStatement stmtPart = conn.prepareCall(sqlPart)) {
                    for (String p : participantes) {
                        String[] datos = p.split(" \\| ");
                        stmtPart.setInt(1, equipoId);
                        stmtPart.setString(2, datos[0]);
                        stmtPart.setDate(3, java.sql.Date.valueOf(LocalDate.parse(datos[1])));
                        stmtPart.setString(4, datos[2]);
                        stmtPart.setInt(5, session.getTempEventoId());
                        stmtPart.execute();
                    }
                }
                conn.commit();
                session.setModoEdicion(false);
                session.setTempNombreEquipo(null);
                mostrarNotificacionExito("¡Datos guardados correctamente!");
                cambiarVista(event, esEdicion ? "coach_misEquipos.fxml" : "coach_menu.fxml");

            } catch (SQLException ex) {
                conn.rollback();
                String errorMsg = ex.getMessage().toLowerCase();
                if (errorMsg.contains("ya está registrado en otro equipo") || errorMsg.contains("error de duplicado")) {
                    lblError.setText("Error: Uno de los alumnos ya está inscrito en otro equipo.");
                } else {
                    lblError.setText("Error BD: " + ex.getMessage());
                }
                lblError.setVisible(true);
            }
        } catch (SQLException e) {
            lblError.setText("Error conexión: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

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
            } catch (Exception e) {}
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
            mostrarError("Selecciona un alumno.");
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "coach_registroEquipo.fxml");
    }

    @FXML
    public void handleIrAlMenu(ActionEvent event) {
        UserSession.getInstance().setTempNombreEquipo(null);
        UserSession.getInstance().setModoEdicion(false);
        cambiarVista(event, "coach_menu.fxml");
    }

    private void actualizarContador() {
        lblContador.setText(participantes.size() + " / " + MAX_PARTICIPANTES);
    }

    private String capitalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) resultado.append(palabra.substring(1).toLowerCase());
                resultado.append(" ");
            }
        }
        return resultado.toString().trim();
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
        } catch (Exception e) {}
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show();
        }
    }
}