package controllers;

import dao.LabelDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Label;

public class LabelsController {

    @FXML private TextField newLabelName;
    @FXML private javafx.scene.control.ColorPicker newLabelColor;
    @FXML private ListView<models.Label> labelsListView;
    @FXML private Button createLabelButton;
    @FXML private javafx.scene.control.Label statsLabel;

    private MainController mainController;
    private final LabelDAO labelDAO = new LabelDAO();
    private final ObservableList<models.Label> labels = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("LabelsController инициализирован");
        setupEventHandlers();
        loadLabels();
        setupListView();
        updateStats();
        try {
            newLabelColor.setValue(Color.web("#eb5a46"));
        } catch (Exception e) {
            newLabelColor.setValue(Color.GRAY);
        }
    }

    private void setupEventHandlers() {
        createLabelButton.setOnAction(event -> createLabel());
        labelsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newLabel) -> {
            if (newLabel != null) {
                newLabelName.setText(newLabel.getName());
                try {
                    String colorStr = newLabel.getColor();
                    if (colorStr != null && !colorStr.isEmpty()) {
                        if (!colorStr.startsWith("#")) {
                            colorStr = "#" + colorStr;
                        }
                        newLabelColor.setValue(Color.web(colorStr));
                    }
                } catch (Exception e) {
                    newLabelColor.setValue(Color.web("#5e6c84"));
                }
            }
        });
    }

    private void loadLabels() {
        labels.clear();
        labels.addAll(labelDAO.findAll());
        if (labels.isEmpty()) {
            addDefaultLabel("БАГ", "#eb5a46", "Ошибка в коде");
            addDefaultLabel("ФИЧА", "#61bd4f", "Новая функция");
            addDefaultLabel("ДИЗАЙН", "#00c2e0", "Дизайн UI/UX");
            addDefaultLabel("ТЕСТ", "#f2d600", "Тестирование");
            addDefaultLabel("СРОЧНО", "#ff9f1a", "Срочная задача");
            addDefaultLabel("ДОКУМЕНТАЦИЯ", "#c377e0", "Документация");
        }
    }

    private void addDefaultLabel(String name, String color, String description) {
        models.Label label = new models.Label(0, name, color, description);
        if (labelDAO.create(label)) {
            labels.add(label);
        }
    }

    private void setupListView() {
        labelsListView.setItems(labels);
        labelsListView.setCellFactory(param -> new ListCell<models.Label>() {
            @Override
            protected void updateItem(models.Label item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                HBox cellContent = new HBox(8);
                cellContent.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Rectangle colorRect = new Rectangle(16, 16);
                colorRect.setArcWidth(4);
                colorRect.setArcHeight(4);
                try {
                    String colorStr = item.getColor();
                    if (colorStr != null && !colorStr.isEmpty()) {
                        if (!colorStr.startsWith("#")) {
                            colorStr = "#" + colorStr;
                        }
                        Color color = Color.web(colorStr);
                        colorRect.setFill(color);
                    } else {
                        colorRect.setFill(Color.web("#5e6c84"));
                    }
                } catch (Exception e) {
                    colorRect.setFill(Color.web("#5e6c84"));
                }
                VBox textContent = new VBox(2);
                javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(item.getName());
                nameLabel.setStyle("-fx-font-weight: bold;");
                javafx.scene.control.Label descLabel = new javafx.scene.control.Label(item.getDescription() + " (используется: " + item.getUsageCount() + ")");
                descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #5e6c84;");
                textContent.getChildren().addAll(nameLabel, descLabel);
                cellContent.getChildren().addAll(colorRect, textContent);
                setGraphic(cellContent);
                setText(null);
            }
        });
    }

    @FXML
    public void createLabel() {
        String name = newLabelName.getText().trim().toUpperCase();
        Color color = newLabelColor.getValue();
        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите название метки", Alert.AlertType.ERROR);
            return;
        }
        for (models.Label existing : labels) {
            if (existing.getName().equals(name)) {
                showAlert("Ошибка", "Метка уже существует", Alert.AlertType.ERROR);
                return;
            }
        }
        String hexColor = toHex(color);
        models.Label label = new models.Label(0, name, hexColor, "Пользовательская метка");
        if (labelDAO.create(label)) {
            labels.add(label);
            newLabelName.clear();
            try {
                newLabelColor.setValue(Color.web("#eb5a46"));
            } catch (Exception e) {
                newLabelColor.setValue(Color.GRAY);
            }
            updateStats();
            showSuccess("Метка создана: " + name);
        } else {
            showError("Ошибка создания метки");
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private void updateStats() {
        int total = labels.size();
        int totalUsage = labels.stream().mapToInt(models.Label::getUsageCount).sum();
        if (statsLabel != null) {
            statsLabel.setText(String.format("📊 Всего меток: %d | Использований: %d", total, totalUsage));
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        showAlert("Успешно", message, Alert.AlertType.INFORMATION);
    }

    private void showError(String message) {
        showAlert("Ошибка", message, Alert.AlertType.ERROR);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) newLabelName.getScene().getWindow();
        stage.close();
    }
}