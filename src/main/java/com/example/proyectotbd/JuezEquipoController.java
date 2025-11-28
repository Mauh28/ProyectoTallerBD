package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class JuezEquipoController {

    @FXML private VBox vboxListaEquipos;

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresar al paso anterior: Filtro de Categoría
        cambiarVista(event, "juez_categoria.fxml");
    }

    @FXML
    public void handleSeleccionarEquipo(ActionEvent event) {
        System.out.println("Equipo seleccionado para evaluar.");

        // AQUÍ ES DONDE IRÍA LA NAVEGACIÓN A LA VISTA DE RÚBRICA (EVALUACIÓN)
        // Por ahora solo imprimimos mensaje porque no hemos creado esa vista.
        // cambiarVista(event, "evaluacion_rubrica.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar: " + fxml);
        }
    }
}
