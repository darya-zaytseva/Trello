package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.AutomationRule;
import java.util.ArrayList;
import java.util.List;

public class AutomationController {

    @FXML private ComboBox<String> triggerCombo;
    @FXML private ComboBox<String> actionCombo;
    @FXML private TextField parametersField;
    @FXML private Button createRuleButton;
    @FXML private TableView<AutomationRule> rulesTable;
    @FXML private Label statsLabel;
    @FXML private TextArea ruleDescriptionArea;
    private final ObservableList<AutomationRule> rules = FXCollections.observableArrayList();
    private final List<AutomationRule> executedRules = new ArrayList<>();
    private MainController mainController;

    @FXML
    public void initialize() {
        System.out.println("AutomationController инициализирован");
        setupEventHandlers();
        setupRulesTable();
        loadTriggersAndActions();
        loadExampleRules();
        updateStats();
    }

    private void setupEventHandlers() {
        createRuleButton.setOnAction(event -> createRule());
        triggerCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateRuleDescription());
        actionCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateRuleDescription());
        parametersField.textProperty().addListener((obs, oldVal, newVal) -> updateRuleDescription());
    }

    private void updateRuleDescription() {
        String trigger = triggerCombo.getValue();
        String action = actionCombo.getValue();
        String params = parametersField.getText();
        if (trigger == null || action == null) return;
        String description = "Когда: " + trigger + "\n" +
                "Тогда: " + action + "\n" +
                "Параметры: " + (params.isEmpty() ? "[не указаны]" : params);
        if (ruleDescriptionArea != null) {
            ruleDescriptionArea.setText(description);
        }
    }

    private void loadTriggersAndActions() {
        triggerCombo.setItems(FXCollections.observableArrayList("Задача создана", "Задача перемещена", "Срок истек", "Изменен приоритет", "Задача выполнена"));
        triggerCombo.setValue("Задача создана");
        actionCombo.setItems(FXCollections.observableArrayList("Добавить метку", "Изменить приоритет", "Переместить в колонку", "Назначить участника", "Установить срок"));
        actionCombo.setValue("Добавить метку");
    }

    private void setupRulesTable() {
        rulesTable.setItems(rules);
        TableColumn<AutomationRule, String> ruleCol = new TableColumn<>("Правило");
        ruleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        ruleCol.setPrefWidth(350);
        TableColumn<AutomationRule, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        TableColumn<AutomationRule, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(150);
        rulesTable.getColumns().setAll(ruleCol, statusCol, actionsCol);
    }

    private void loadExampleRules() {
        rules.add(new AutomationRule("Срок истек", "Изменить приоритет", "Высокий", "Активно"));
        rules.add(new AutomationRule("Задача создана", "Добавить метку", "Новые", "Неактивно"));
        rules.add(new AutomationRule("Задача выполнена", "Переместить в колонку", "Готово", "Активно"));
        rules.get(0).incrementExecution();
        rules.get(2).incrementExecution();
    }

    @FXML
    public void createRule() {
        String trigger = triggerCombo.getValue();
        String action = actionCombo.getValue();
        String parameters = parametersField.getText().trim();
        if (trigger == null || action == null) {
            showAlert("Ошибка", "Выберите триггер и действие", Alert.AlertType.ERROR);
            return;
        }
        if (parameters.isEmpty()) {
            showAlert("Ошибка", "Введите параметры", Alert.AlertType.ERROR);
            return;
        }
        for (AutomationRule rule : rules) {
            if (rule.getTrigger().equals(trigger) && rule.getAction().equals(action) &&
                    rule.getParameters().equalsIgnoreCase(parameters)) {
                showAlert("Ошибка", "Такое правило уже есть", Alert.AlertType.ERROR);
                return;
            }
        }
        AutomationRule rule = new AutomationRule(trigger, action, parameters, "Активно");
        rules.add(rule);
        parametersField.clear();
        updateStats();
        showSuccess("Правило создано и активировано");
        simulateRuleExecution(rule);
    }

    private void simulateRuleExecution(AutomationRule rule) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    rule.incrementExecution();
                    rulesTable.refresh();
                    updateStats();
                    if (mainController != null) {
                        mainController.showSuccess("Автоматизация выполнена: " + rule.getTrigger());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateStats() {
        int active = (int) rules.stream().filter(r -> r.getStatus().equals("Активно")).count();
        int totalExecutions = rules.stream().mapToInt(AutomationRule::getExecutionCount).sum();
        if (statsLabel != null) {
            statsLabel.setText(String.format("📊 %d правил (%d активно) | Выполнено: %d раз", rules.size(), active, totalExecutions));
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

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) triggerCombo.getScene().getWindow();
        stage.close();
    }
}