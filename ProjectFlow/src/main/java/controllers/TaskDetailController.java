package controllers;

import dao.TaskDAO;
import dao.AttachmentDAO;
import javafx.scene.layout.HBox;
import models.Task;
import models.Attachment;
import utils.FileUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class TaskDetailController {

    @FXML private Label taskTitleLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label dueDateLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdLabel;
    @FXML private Button saveButton;
    @FXML private Button closeButton;
    @FXML private Button setDueDateButton;
    @FXML private Button toggleStatusButton;
    @FXML private Button archiveButton;
    @FXML private Button toggleStatusButtonTop;
    @FXML private Button setDueDateButtonSmall;
    @FXML private TabPane detailTabs;
    @FXML private VBox attachmentsContainer;

    private Task task;
    private TaskDAO taskDAO = new TaskDAO();
    private AttachmentDAO attachmentDAO = new AttachmentDAO();
    private boolean isModified = false;
    private MainController mainController;
    public void setMainController(MainController mainController) {this.mainController = mainController;}

    @FXML
    public void initialize() {
        setupButtonActions();
        setupAttachmentsTab();
    }

    private void setupAttachmentsTab() {
        Button uploadButton = new Button("📎 Добавить файл");
        uploadButton.setStyle("-fx-background-color: #0079bf; -fx-text-fill: white; -fx-font-weight: bold;");
        uploadButton.setOnAction(e -> handleUploadFile());
        attachmentsContainer.getChildren().add(uploadButton);
    }

    private void setupButtonActions() {
        if (saveButton != null) {
            saveButton.setOnAction(e -> handleSave());
        }
        if (closeButton != null) {
            closeButton.setOnAction(e -> handleClose());
        }
        if (setDueDateButton != null) {
            setDueDateButton.setOnAction(e -> handleSetDueDate());
        }
        if (toggleStatusButton != null) {
            toggleStatusButton.setOnAction(e -> handleToggleStatus());
        }
        if (archiveButton != null) {
            archiveButton.setOnAction(e -> handleArchiveTask());
        }
        if (setDueDateButtonSmall != null) {
            setDueDateButtonSmall.setOnAction(e -> handleSetDueDate());
        }
        if (toggleStatusButtonTop != null) {
            toggleStatusButtonTop.setOnAction(e -> handleToggleStatus());
        }
    }

    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для загрузки");
        File file = fileChooser.showOpenDialog(taskTitleLabel.getScene().getWindow());
        if (file != null) {
            try {
                String savedPath = FileUtils.saveFile(file, task.getId());
                Attachment attachment = new Attachment(task.getId(), file.getName(), savedPath, getFileType(file));
                if (attachmentDAO.create(attachment)) {
                    loadAttachments();
                    showSuccess("Файл загружен: " + file.getName());
                }
            } catch (IOException e) {
                showError("Ошибка загрузки файла: " + e.getMessage());
            }
        }
    }

    private String getFileType(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toUpperCase() : "FILE";
    }

    private void loadAttachments() {
        attachmentsContainer.getChildren().removeIf(node -> node instanceof HBox);
        List<Attachment> attachments = attachmentDAO.findByTaskId(task.getId());
        if (attachments.isEmpty()) {
            Label emptyLabel = new Label("Нет прикрепленных файлов");
            emptyLabel.setStyle("-fx-text-fill: #5e6c84; -fx-font-size: 13; -fx-padding: 10;");
            attachmentsContainer.getChildren().add(emptyLabel);
            return;
        }
        for (Attachment attachment : attachments) {
            HBox fileRow = new HBox(10);
            fileRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            fileRow.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
            Label fileLabel = new Label("📄 " + attachment.getFilename());
            fileLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #172b4d;");
            Button openButton = new Button("Открыть");
            openButton.setStyle("-fx-background-color: #0079bf; -fx-text-fill: white; -fx-font-size: 12;");
            openButton.setOnAction(e -> openFile(attachment));
            Button deleteButton = new Button("🗑️");
            deleteButton.setStyle("-fx-background-color: #eb5a46; -fx-text-fill: white; -fx-font-size: 12;");
            deleteButton.setOnAction(e -> deleteAttachment(attachment));
            fileRow.getChildren().addAll(fileLabel, openButton, deleteButton);
            attachmentsContainer.getChildren().add(fileRow);
        }
    }

    private void openFile(Attachment attachment) {
        try {
            File file = FileUtils.getFile(attachment.getFilePath());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showError("Файл не найден: " + attachment.getFilename());
            }
        } catch (IOException e) {
            showError("Не удалось открыть файл: " + e.getMessage());
        }
    }

    private void deleteAttachment(Attachment attachment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление файла");
        alert.setHeaderText("Удалить " + attachment.getFilename() + "?");
        alert.setContentText("Файл будет удален без возможности восстановления.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FileUtils.deleteFile(attachment.getFilePath());
                    if (attachmentDAO.delete(attachment.getId())) {
                        loadAttachments();
                        showSuccess("Файл удален");
                    }
                } catch (IOException e) {
                    showError("Ошибка удаления файла: " + e.getMessage());
                }
            }
        });
    }

    public void setTask(Task task) {
        this.task = task;
        updateUI();
        loadAttachments();
    }

    private void updateUI() {
        if (task == null) return;
        taskTitleLabel.setText(task.getTitle());
        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
        if (task.getDueDate() != null) {
            dueDateLabel.setText("📅 " + task.getDueDate().toString());
        } else {
            dueDateLabel.setText("📅 Без срока");
        }
        if (task.getPriority() != null) {
            priorityLabel.setText("Приоритет: " + task.getPriority().toString());
        } else {
            priorityLabel.setText("Приоритет: Не установлен");
        }
        if (task.isCompleted()) {
            statusLabel.setText("✅ Выполнено");
        } else {
            statusLabel.setText("⏳ В работе");
        }
        if (task.getCreatedAt() != null) {
            createdLabel.setText("Создано: " + task.getCreatedAt().toString());
        }
    }

    @FXML
    public void handleSave() {
        if (task != null && descriptionArea != null) {
            task.setDescription(descriptionArea.getText());
            if (taskDAO.update(task)) {
                showSuccess("Задача сохранена");
                isModified = false;
                if (mainController != null) {
                    mainController.refreshBoard();
                }
            }
        }
    }

    @FXML
    public void handleSetDueDate() {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Установить срок");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(task.getDueDate() != null ? task.getDueDate() : LocalDate.now().plusDays(7));
        VBox content = new VBox(10, new Label("Выберите дату:"), datePicker);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return datePicker.getValue();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(date -> {
            task.setDueDate(date);
            if (taskDAO.update(task)) {
                updateUI();
                showSuccess("Срок установлен");
            }
        });
    }

    @FXML
    public void handleToggleStatus() {
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            if (taskDAO.update(task)) {
                updateUI();
                showSuccess(task.isCompleted() ? "Задача выполнена" : "Задача возвращена в работу");
                if (mainController != null) {
                    mainController.refreshBoard();
                }
            }
        }
    }

    @FXML
    public void handleArchiveTask() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Архивация задачи");
        alert.setHeaderText("Архивировать задачу?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskDAO.archiveTask(task.getId())) {
                    showSuccess("Задача перемещена в архив");
                    if (mainController != null) {
                        mainController.refreshBoard();
                    }
                    handleClose();
                }
            }
        });
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) taskTitleLabel.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }
}