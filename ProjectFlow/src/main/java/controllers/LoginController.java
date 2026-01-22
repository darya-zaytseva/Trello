package controllers;

import dao.UserDAO;
import dao.DatabaseConnection;
import javafx.stage.Modality;
import models.User;
import utils.HashUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        System.out.println("LoginController инициализирован");
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
        usernameField.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findByUsername(username);
            if (user == null) {
                showError("Пользователь не найден");
                return;
            }
            if (HashUtils.verifyPassword(password, user.getPasswordHash())) {
                DatabaseConnection.setCurrentUser(user);
                System.out.println("Пользователь авторизован: " + user.getUsername());
                openMainWindow(user);
            } else {
                showError("Неверный пароль");
            }
        } catch (Exception e) {
            showError("Ошибка при входе: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }
        if (username.length() < 3) {
            showError("Имя пользователя должно содержать минимум 3 символа");
            return;
        }
        if (password.length() < 6) {
            showError("Пароль должен содержать минимум 6 символов");
            return;
        }
        try {
            UserDAO userDAO = new UserDAO();
            if (userDAO.findByUsername(username) != null) {
                showError("Пользователь с таким именем уже существует");
                return;
            }
            String email = username + "@projectflow.local";
            String checkEmailSql = "SELECT * FROM users WHERE email = ?";
            try (var rs = DatabaseConnection.executeQuery(checkEmailSql, email)) {
                if (rs.next()) {
                    email = username + System.currentTimeMillis() + "@projectflow.local";
                }
            }
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPasswordHash(HashUtils.hashPassword(password));
            if (userDAO.create(newUser)) {
                DatabaseConnection.setCurrentUser(newUser);
                showSuccess("Регистрация успешна!\nТеперь вы можете войти в систему.");
                passwordField.clear();
                openMainWindow(newUser);
            } else {
                showError("Ошибка при создании пользователя");
            }
        } catch (Exception e) {
            showError("Ошибка при регистрации: " + e.getMessage());
        }
    }

    @FXML
    public void onFieldChanged() {
        errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #eb5a46; -fx-font-size: 14;");
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinSize(300, 200);
        alert.getDialogPane().setPrefSize(400, 250);
        Stage currentStage = (Stage) usernameField.getScene().getWindow();
        alert.initOwner(currentStage);
        alert.showAndWait();
    }

    private void openMainWindow(User user) {
        try {
            System.out.println("Открываем главное окно для пользователя: " + user.getUsername());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setCurrentUser(user);
            Stage mainStage = new Stage();
            mainStage.setTitle("ProjectFlow - " + user.getUsername());
            setupMainWindow(mainStage, root);
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
            mainStage.show();
            System.out.println("Главное окно открыто успешно");
        } catch (IOException e) {
            showError("Ошибка загрузки главного окна: " + e.getMessage());
        } catch (Exception e) {
            showError("Общая ошибка: " + e.getMessage());
        }
    }

    private void setupMainWindow(Stage mainStage, Parent root) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenBounds.getWidth() * 0.85;
        double windowHeight = screenBounds.getHeight() * 0.85;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        mainStage.setScene(scene);
        mainStage.setResizable(true);
        mainStage.setMinWidth(800);
        mainStage.setMinHeight(600);
        mainStage.setMaxWidth(screenBounds.getWidth() - 20);
        mainStage.setMaxHeight(screenBounds.getHeight() - 40);
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - windowWidth) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - windowHeight) / 2;
        mainStage.setX(centerX);
        mainStage.setY(centerY);
        mainStage.setOnCloseRequest(event -> {
            System.out.println("Закрытие главного окна...");
            DatabaseConnection.clearCurrentUser();
            DatabaseConnection.closeConnection();
        });
    }
}