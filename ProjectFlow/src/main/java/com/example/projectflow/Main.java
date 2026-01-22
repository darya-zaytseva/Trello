package com.example.projectflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import dao.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!DatabaseConnection.testConnection()) {
                showDatabaseErrorDialog();
                return;
            }
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/projectflow/login.fxml"));
            if (loader.getLocation() == null) {
                throw new RuntimeException("FXML файл не найден: /com/example/projectflow/login.fxml");
            }
            System.out.println("Загружаем FXML из: " + loader.getLocation());
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 300);
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            primaryStage.setTitle("ProjectFlow - Вход в систему");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            double widthPercentage = 0.7;
            double heightPercentage = 0.8;
            primaryStage.setWidth(screenBounds.getWidth() * widthPercentage);
            primaryStage.setHeight(screenBounds.getHeight() * heightPercentage);
            primaryStage.centerOnScreen();
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(300);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка запуска приложения",
                    "Не удалось запустить приложение:\n" + e.getMessage());
        }
    }

    private void showDatabaseErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка подключения");
        alert.setHeaderText("Не удалось подключиться к базе данных");
        alert.setContentText("Проверьте:\n" +
                "1. Запущен ли MySQL сервер\n" +
                "2. Правильность параметров подключения в DatabaseConnection.java\n" +
                "3. Существует ли база данных 'projectflow_db'");
        alert.getDialogPane().setMinSize(400, 250);
        alert.showAndWait();
        System.exit(1);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinSize(300, 200);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
        System.out.println("Приложение завершено");
    }

    public static void main(String[] args) {
        launch(args);
    }
}