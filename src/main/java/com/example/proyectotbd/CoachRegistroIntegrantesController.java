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

    // PATRÓN: Solo permite letras (a-z, acentos), Ñ/ñ y espacios (\s). SIN NÚMEROS.
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");

    @FXML
    public void initialize() {
        if (cbSexo.getItems().isEmpty()) {
            cbSexo.setItems(FXCollections.observableArrayList("Femenino", "Masculino"));
        }
        dpNacimiento.setEditable(false);

        listaParticipantes.setItems(participantes);

        int categoriaId = UserSession.getInstance().getTempCategoriaId();

        // 1. CONFIGURAR VALIDACIÓN CON RESTRICCIÓN DE EDAD DINÁMICA
        configurarValidacionFecha(categoriaId);

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

    // --- LÓGICA CENTRAL DE FECHAS ---
    private static class RangoEdad {
        final LocalDate limiteAntiguo; // Fecha de nacimiento MÁS ANTIGUA (Participante más viejo)
        final LocalDate limiteReciente; // Fecha de nacimiento MÁS RECIENTE (Participante más joven)

        RangoEdad(LocalDate antiguo, LocalDate reciente) {
            this.limiteAntiguo = antiguo;
            this.limiteReciente = reciente;
        }
    }

    private RangoEdad obtenerLimitesEdad(int categoriaId) {
        final LocalDate HOY = LocalDate.now();
        LocalDate limiteAntiguo;
        LocalDate limiteReciente;

        switch (categoriaId) {
            case 1: // Primaria: 6 a 12 años
                limiteAntiguo = HOY.minusYears(12);
                limiteReciente = HOY.minusYears(6);
                break;
            case 2: // Secundaria: 12 a 15 años
                limiteAntiguo = HOY.minusYears(15);
                limiteReciente = HOY.minusYears(12);
                break;
            case 3: // Preparatoria: 15 a 18 años
                limiteAntiguo = HOY.minusYears(18);
                limiteReciente = HOY.minusYears(15);
                break;
            case 4: // Profesional: 18 años en adelante
                limiteAntiguo = HOY.minusYears(100);
                limiteReciente = HOY.minusYears(18);
                break;
            default:
                limiteAntiguo = HOY.minusYears(100);
                limiteReciente = HOY;
        }
        return new RangoEdad(limiteAntiguo, limiteReciente);
    }

    // --- VALIDACIÓN DE FECHA (CON RESTRICCIÓN DE RANGO POR CATEGORÍA) ---
    private void configurarValidacionFecha(int categoriaId) {
        final LocalDate HOY = LocalDate.now();
        final RangoEdad rango = obtenerLimitesEdad(categoriaId);

        // A. Restringir el calendario visualmente
        dpNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // La fecha es válida si está ENTRE (inclusive) el límite antiguo y el límite reciente
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

        // B. Listener para avisar si la fecha es inválida (si la pegan)
        dpNacimiento.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                boolean fueraDeRango = newDate.isBefore(rango.limiteAntiguo) || newDate.isAfter(rango.limiteReciente);

                if (fueraDeRango) {
                    mostrarError("Fecha fuera del rango de edad permitido para la categoría seleccionada.");
                } else {
                    lblError.setVisible(false);
                }
            }
        });
    }

    // --- VALIDACIÓN DE NOMBRE (SOLO TEXTO + LÍMITE 50 CHARS) ---
    private void configurarValidacionNombre() {
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                txtNombre.setText(oldValue);
                return;
            }
            // Usa el patrón que SOLO permite letras y espacios.
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

        // 3. Validación de Rango de Edad (Utiliza la misma lógica central)
        final RangoEdad rango = obtenerLimitesEdad(categoriaId);

        // Si la fecha está ANTES del límite más antiguo (es muy viejo) O DESPUÉS del límite más reciente (es muy joven)
        if (nacimiento.isBefore(rango.limiteAntiguo) || nacimiento.isAfter(rango.limiteReciente)) {
            mostrarError("La fecha de nacimiento no cumple con la edad requerida para la categoría '" + UserSession.getInstance().getTempCategoriaNombre() + "'.");
            return;
        }

        // 4. Normalización y Validación de Nombre Completo
        String nombre = capitalizarTexto(rawNombre);
        if (!nombre.contains(" ")) {
            mostrarError("Por favor ingresa nombre y apellido.");
            return;
        }

        // =================================================================
        // 5. VALIDACIÓN DE DUPLICADOS (COMPROBAR NOMBRE COMPLETO REPETIDO)
        // =================================================================
        for (int i = 0; i < participantes.size(); i++) {
            if (i != indiceEdicion) {
                // El registro es "Nombre Completo | Fecha | Sexo"
                String registroExistente = participantes.get(i);

                // Extrae solo el nombre completo (el primer elemento antes del primer separador)
                String nombreExistente = registroExistente.split(" \\| ")[0];

                // Compara el nombre nuevo (capitalizado) con el existente (ignorando mayúsculas/minúsculas)
                if (nombreExistente.equalsIgnoreCase(nombre)) {
                    mostrarError("El alumno '" + nombre + "' ya está registrado en este equipo.");
                    return;
                }
            }
        }
        // =================================================================

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
        // 1. Validar que la lista tenga exactamente 3 integrantes
        if (participantes.size() != 3) {
            lblError.setText("Regla del Torneo: El equipo debe tener exactamente 3 integrantes.");
            lblError.setStyle("-fx-text-fill: #e74c3c;");
            lblError.setVisible(true);
            return;
        }

        UserSession session = UserSession.getInstance();
        int equipoId = session.getEquipoIdTemp();
        boolean esEdicion = session.isModoEdicion();

        // 2. Validar datos de sesión
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

        // --- AQUÍ SE DECLARA 'conn'. Solo existe dentro de este bloque try ---
        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false); // Iniciar Transacción

            try {
                // --- BLOQUE A: CREACIÓN (Solo si NO es edición) ---
                if (!esEdicion) {
                    // LLAMADA AL SP CON 5 PARÁMETROS (Corrección importante)
                    String sqlEquipo = "{call SP_NombreEquipoExiste(?, ?, ?, ?, ?)}";
                    int nuevoEquipoId = 0;

                    try (CallableStatement stmtEq = conn.prepareCall(sqlEquipo)) {
                        stmtEq.setInt(1, session.getUserId());
                        stmtEq.setString(2, session.getTempCategoriaNombre());
                        stmtEq.setString(3, session.getTempNombreEquipo());
                        stmtEq.setInt(4, session.getTempEventoId()); // <--- FALTA ESTE EN TU CÓDIGO

                        stmtEq.registerOutParameter(5, Types.INTEGER); // Salida es el 5

                        stmtEq.execute();
                        nuevoEquipoId = stmtEq.getInt(5);
                    }

                    if (nuevoEquipoId <= 0) {
                        throw new SQLException("No se generó ID del equipo.");
                    }
                    equipoId = nuevoEquipoId;

                    // 2. Inscribir en Equipo_Evento
                    String sqlEvento = "{call SP_RegistrarEquipoEnEvento(?, ?)}";
                    try (CallableStatement stmtEv = conn.prepareCall(sqlEvento)) {
                        stmtEv.setInt(1, equipoId);
                        stmtEv.setInt(2, session.getTempEventoId());
                        stmtEv.execute();
                    }
                }

                // --- BLOQUE B: LIMPIEZA (Solo si ES edición) ---
                else {
                    String sqlLimpiar = "{call SP_EliminarParticipantesPorEquipo(?)}";
                    try (CallableStatement stmtClean = conn.prepareCall(sqlLimpiar)) {
                        stmtClean.setInt(1, equipoId);
                        stmtClean.execute();
                    }
                }

                // --- BLOQUE C: INSERCIÓN DE INTEGRANTES ---
                String sqlPart = "{call SP_RegistrarParticipante(?, ?, ?, ?, ?)}";
                try (CallableStatement stmtPart = conn.prepareCall(sqlPart)) {
                    for (String p : participantes) {
                        String[] datos = p.split(" \\| ");
                        stmtPart.setInt(1, equipoId);
                        stmtPart.setString(2, datos[0]);
                        stmtPart.setDate(3, java.sql.Date.valueOf(LocalDate.parse(datos[1])));
                        stmtPart.setString(4, datos[2]);
                        stmtPart.setInt(5, session.getTempEventoId()); // ID Evento para validación cruzada
                        stmtPart.execute();
                    }
                }

                conn.commit(); // CONFIRMAR CAMBIOS

                // Limpieza final
                session.setModoEdicion(false);
                session.setTempNombreEquipo(null);

                mostrarNotificacionExito("¡Datos guardados correctamente!");

                if (esEdicion) cambiarVista(event, "coach_misEquipos.fxml");
                else cambiarVista(event, "coach_menu.fxml");

            } catch (SQLException ex) {
                // AQUÍ USAMOS 'conn' PARA EL ROLLBACK
                conn.rollback();

                String errorMsg = ex.getMessage().toLowerCase();

                // Validación del mensaje de duplicado
                if (errorMsg.contains("ya está registrado en otro equipo") || errorMsg.contains("error de duplicado")) {
                    lblError.setText("Error: Uno de los alumnos ya está inscrito en otro equipo de este evento.");
                } else {
                    lblError.setText("Error BD: " + ex.getMessage());
                }
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            // AQUÍ 'conn' YA NO EXISTE (está cerrada). No intentes usarla aquí.
            lblError.setText("Error de conexión: " + e.getMessage());
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
        // Limpiamos los datos temporales al regresar al menú principal
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