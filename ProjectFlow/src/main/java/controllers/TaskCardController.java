package controllers;

import dao.TaskDAO;
import dao.AttachmentDAO;
import models.Task;
import models.Attachment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TaskCardController {

    @FXML private Rectangle priorityIndicator;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priorityLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label dueDateIcon;
    @FXML private FlowPane labelsContainer;
    @FXML private FlowPane membersContainer;
    @FXML private VBox taskCardContainer;
    @FXML private Button menuButton;

    private Task task;
    private ColumnController columnController;
    private TaskDAO taskDAO = new TaskDAO();
    private AttachmentDAO attachmentDAO = new AttachmentDAO();

    @FXML
    public void initialize() {
        taskCardContainer.setOnMouseClicked(event -> handleCardClick());
        taskCardContainer.setOnMouseEntered(event -> handleMouseEnter());
        taskCardContainer.setOnMouseExited(event -> handleMouseExit());
        menuButton.setOnMouseEntered(event -> handleMenuButtonHover());
        menuButton.setOnMouseExited(event -> handleMenuButtonExit());
        menuButton.setOnAction(event -> handleCardMenu());
    }

    public void setTask(Task task, ColumnController columnController) {
        this.task = task;
        this.columnController = columnController;
        updateUI();
    }

    private void updateUI() {
        if (task == null) return;
        titleLabel.setText(task.getTitle());
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            String shortDesc = task.getDescription();
            if (shortDesc.length() > 100) {
                shortDesc = shortDesc.substring(0, 97) + "...";
            }
            descriptionLabel.setText(shortDesc);
            descriptionLabel.setVisible(true);
            descriptionLabel.setManaged(true);
        } else {
            descriptionLabel.setVisible(false);
            descriptionLabel.setManaged(false);
        }
        if (task.getPriority() != null) {
            String priorityText = getPriorityText(task.getPriority());
            priorityLabel.setText(priorityText);
            String priorityColor = getPriorityColor(task.getPriority());
            String textColor = getPriorityTextColor(task.getPriority());
            priorityLabel.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: %s; " +
                            "-fx-font-size: 11; -fx-font-weight: bold; " +
                            "-fx-padding: 2 6; -fx-background-radius: 3;",
                    priorityColor, textColor
            ));
            priorityIndicator.setFill(Color.web(priorityColor));
        } else {
            priorityLabel.setText("НЕТ");
            priorityLabel.setStyle(
                    "-fx-background-color: #5e6c84; -fx-text-fill: white; " +
                            "-fx-font-size: 11; -fx-font-weight: bold; " +
                            "-fx-padding: 2 6; -fx-background-radius: 3;"
            );
            priorityIndicator.setFill(Color.web("#5e6c84"));
        }
        if (task.getDueDate() != null) {
            String dueDateText = formatDueDate(task.getDueDate());
            String dateColor = getDueDateColor(task.getDueDate());
            String icon = getDueDateIcon(task.getDueDate());
            dueDateLabel.setText(dueDateText);
            dueDateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + dateColor + ";");
            dueDateIcon.setText(icon);
            dueDateIcon.setStyle("-fx-font-size: 11; -fx-text-fill: " + dateColor + ";");
            dueDateLabel.setVisible(true);
            dueDateLabel.setManaged(true);
            dueDateIcon.setVisible(true);
            dueDateIcon.setManaged(true);
        } else {
            dueDateLabel.setVisible(false);
            dueDateLabel.setManaged(false);
            dueDateIcon.setVisible(false);
            dueDateIcon.setManaged(false);
        }
        updateLabels();
        updateMembers();
        updateCardStyle();
        updateAttachmentIndicator();
    }

    private void updateAttachmentIndicator() {
        List<Attachment> attachments = attachmentDAO.findByTaskId(task.getId());
        if (!attachments.isEmpty()) {
            Label attachLabel = new Label("📎 " + attachments.size());
            attachLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #5e6c84;");
            membersContainer.getChildren().add(0, attachLabel);
        }
    }

    public String getPriorityText(Task.Priority priority) {
        switch (priority) {
            case LOW: return "НИЗКИЙ";
            case MEDIUM: return "СРЕДНИЙ";
            case HIGH: return "ВЫСОКИЙ";
            case CRITICAL: return "КРИТИЧЕСКИЙ";
            default: return "НЕТ";
        }
    }

    public String getPriorityColor(Task.Priority priority) {
        switch (priority) {
            case LOW: return "#61bd4f";
            case MEDIUM: return "#f2d600";
            case HIGH: return "#ff9f1a";
            case CRITICAL: return "#eb5a46";
            default: return "#5e6c84";
        }
    }

    public String getPriorityTextColor(Task.Priority priority) {
        return priority == Task.Priority.MEDIUM ? "#172b4d" : "white";
    }

    public String formatDueDate(LocalDate date) {
        if (date == null) return "";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, date);
        if (daysUntil == 0) return "Сегодня";
        if (daysUntil == 1) return "Завтра";
        if (daysUntil == -1) return "Вчера";
        if (daysUntil < 0) return Math.abs(daysUntil) + "д назад";
        if (daysUntil <= 7) return "Через " + daysUntil + "д";
        return date.toString();
    }

    public String getDueDateColor(LocalDate date) {
        if (date == null) return "#5e6c84";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, date);
        if (daysUntil < 0) return "#eb5a46";
        if (daysUntil == 0) return "#f2d600";
        if (daysUntil <= 2) return "#ff9f1a";
        return "#61bd4f";
    }

    public String getDueDateIcon(LocalDate date) {
        if (date == null) return "📅";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, date);
        if (daysUntil < 0) return "⚠️";
        if (daysUntil == 0) return "⚡";
        if (daysUntil == 1) return "⏰";
        return "📅";
    }

    private void updateLabels() {
        labelsContainer.getChildren().clear();
        if (task.getPriority() == Task.Priority.CRITICAL) {
            createLabel("СРОЧНО", "#eb5a46");
        }
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            createLabel("ПРОСРОЧЕНО", "#eb5a46");
        }
        String title = task.getTitle().toLowerCase();
        if (title.contains("баг") || title.contains("bug")) {
            createLabel("БАГ", "#eb5a46");
        }
        if (title.contains("фича") || title.contains("feature")) {
            createLabel("ФИЧА", "#61bd4f");
        }
        if (title.contains("дизайн") || title.contains("design")) {
            createLabel("ДИЗАЙН", "#00c2e0");
        }
    }

    private void createLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-font-size: 10; -fx-font-weight: bold; " +
                        "-fx-padding: 1 4; -fx-background-radius: 3;",
                color
        ));
        labelsContainer.getChildren().add(label);
    }

    private void updateMembers() {
        membersContainer.getChildren().clear();
        String[] memberColors = {"#00c2e0", "#ff9f1a", "#61bd4f"};
        for (int i = 0; i < Math.min(2, memberColors.length); i++) {
            javafx.scene.shape.Circle member = new javafx.scene.shape.Circle(8);
            member.setFill(Color.web(memberColors[i]));
            membersContainer.getChildren().add(member);
        }
    }

    private void updateCardStyle() {
        if (task.isCompleted()) {
            taskCardContainer.setStyle(
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 3; " +
                            "-fx-border-color: #e9ecef; -fx-border-radius: 3; -fx-border-width: 1; " +
                            "-fx-padding: 8; -fx-opacity: 0.6; -fx-effect: null;"
            );
            titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6c757d; -fx-strikethrough: true;");
        } else {
            taskCardContainer.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 3; " +
                            "-fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-border-width: 1; " +
                            "-fx-padding: 8; -fx-effect: null;"
            );
            titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #172b4d; -fx-font-weight: normal;");
        }
    }

    public void handleCardClick() {
        if (task != null && columnController != null && columnController.getMainController() != null) {
            columnController.getMainController().showTaskDetailDialog(task);
        }
    }

    public void handleMouseEnter() {
        if (task != null && !task.isCompleted()) {
            taskCardContainer.setStyle(
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 3; " +
                            "-fx-border-color: #c1c7d0; -fx-border-radius: 3; -fx-border-width: 1; " +
                            "-fx-padding: 8; -fx-effect: null;"
            );
        }
    }

    public void handleMouseExit() {
        updateCardStyle();
    }

    public void handleMenuButtonHover() {
        menuButton.setStyle(
                "-fx-background-color: rgba(0,0,0,0.1); " +
                        "-fx-text-fill: #5e6c84; " +
                        "-fx-font-size: 14; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0;"
        );
    }

    public void handleMenuButtonExit() {
        menuButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: transparent; " +
                        "-fx-font-size: 14; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0;"
        );
    }

    public void handleCardMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem editItem = new MenuItem("✏️ Редактировать");
        editItem.setOnAction(e -> handleCardClick());
        MenuItem dueDateItem = new MenuItem("📅 Изменить срок");
        dueDateItem.setOnAction(e -> {
            if (columnController != null && columnController.getMainController() != null) {
                columnController.getMainController().showEditTaskDialog(task);
            }
        });
        MenuItem archiveItem = new MenuItem("🗑️ Архивировать");
        archiveItem.setOnAction(e -> {
            task.setArchived(true);
            if (taskDAO.update(task)) {
                if (columnController != null) {
                    columnController.refresh();
                }
            }
        });
        MenuItem deleteItem = new MenuItem("❌ Удалить");
        deleteItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Удаление задачи");
            alert.setContentText("Удалить задачу '" + task.getTitle() + "'?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (taskDAO.delete(task.getId())) {
                        if (columnController != null) {
                            columnController.refresh();
                        }
                    }
                }
            });
        });
        menu.getItems().addAll(editItem, dueDateItem, new SeparatorMenuItem(), archiveItem, deleteItem);
        menu.show(menuButton, menuButton.localToScreen(0, 0).getX() + menuButton.getWidth(), menuButton.localToScreen(0, 0).getY());
    }

    public Task getTask() {
        return task;
    }
}