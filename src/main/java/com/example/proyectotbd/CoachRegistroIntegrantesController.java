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
import java.time.Period;
import java.util.regex.Pattern;

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

    // EXPRESIÓN REGULAR PARA NOMBRES:
    // Permite letras (mayúsculas/minúsculas), acentos (áéí...), ñ y espacios.
    // Rechaza números y símbolos.
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");


    @FXML
    public void initialize() {
        listaParticipantes.setItems(participantes);
        // 1. CONFIGURAR VALIDACIÓN EN TIEMPO REAL PARA EL NOMBRE
        configurarValidacionNombre();

        // 2. CONFIGURAR VALIDACIÓN EN TIEMPO REAL PARA LA FECHA
        configurarValidacionFecha();

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

    // --- NUEVO: VALIDACIÓN DE NOMBRE (SOLO TEXTO + LÍMITE 50 CHARS) ---
    private void configurarValidacionNombre() {
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            // A. Evitar que escriba más de 50 caracteres (Límite de BD)
            if (newValue.length() > 50) {
                txtNombre.setText(oldValue); // Rechazar el cambio
                return;
            }

            // B. Validar que solo sean letras
            if (!PATRON_NOMBRE.matcher(newValue).matches()) {
                txtNombre.setText(oldValue); // Rechazar números o símbolos
                // Opcional: Mostrar borde rojo momentáneo
                txtNombre.setStyle("-fx-border-color: red;");
            } else {
                txtNombre.setStyle(""); // Restaurar estilo si es válido
            }
        });
    }

    // --- NUEVO: VALIDACIÓN DE FECHA (NO FUTURO + EDAD MÍNIMA) ---
    private void configurarValidacionFecha() {
        // A. Restringir el calendario visualmente para no elegir fechas futuras
        dpNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Deshabilitar fechas futuras
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });

        // B. Listener para calcular edad al momento
        dpNacimiento.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                if (newDate.isAfter(LocalDate.now())) {
                    mostrarError("La fecha de nacimiento no puede ser futura.");
                    dpNacimiento.setValue(null); // Borrar
                    return;
                }

                // Cálculo rápido de edad para feedback
                int edad = Period.between(newDate, LocalDate.now()).getYears();
                if (edad < 6) {
                    // Advertencia suave (Tu SP en BD tiene la lógica final por categoría,
                    // pero < 6 años es casi seguro un error para cualquier categoría robótica).
                    mostrarError("Advertencia: El participante parece tener " + edad + " años.");
                } else {
                    lblError.setVisible(false);
                }
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

        // 2. Validaciones Básicas (Vacío)
        if (rawNombre == null || rawNombre.trim().isEmpty() || nacimiento == null || sexo == null) {
            mostrarError("Por favor llena todos los campos.");
            return;
        }

        // 3. Normalización (Capitalizar Nombre: "juan perez" -> "Juan Perez")
        String nombre = capitalizarTexto(rawNombre);

        // 4. Validación de Nombre Completo (Mínimo un espacio)
        if (!nombre.contains(" ")) {
            mostrarError("Por favor ingresa nombre y apellido.");
            return;
        }

        // 5. Validación de Duplicados (Local)
        // Comparamos contra el nombre YA NORMALIZADO
        for (int i = 0; i < participantes.size(); i++) {
            if (i != indiceEdicion) { // Ignorar si me estoy editando a mí mismo
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
            handleLimpiar(); // Salir del modo edición y limpiar selección
        }

        // 8. Actualizar interfaz
        actualizarContador();
        lblError.setVisible(false);

        // Limpiar campos solo si estábamos agregando (para facilitar ingreso rápido)
        if (indiceEdicion == -1) {
            txtNombre.clear();
            dpNacimiento.setValue(null);
            cbSexo.getSelectionModel().clearSelection();
            txtNombre.requestFocus(); // Poner el foco listo para el siguiente
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
        txtNombre.setStyle(""); // Limpiar bordes rojos si quedaron

        indiceEdicion = -1;
        btnAccion.setText("AGREGAR A LA LISTA");
        btnAccion.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
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

    // --- EL MÉTODO AUXILIAR NECESARIO (Cópialo al final de la clase) ---
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