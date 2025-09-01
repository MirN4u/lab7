package managers;

import models.User;
import utility.Console;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class AuthManager {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Console console;

    public AuthManager(String dbUrl, String dbUser, String dbPassword, Console console) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.console = console;
    }

    public void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement()) {

            // таблица пользователей
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY," +
                    "password_hash VARCHAR(100) NOT NULL" +
                    ")";
            stmt.executeUpdate(sql);

            // таблица для связи пользователей с объектами
            sql = "CREATE TABLE IF NOT EXISTS user_objects (" +
                    "object_id BIGINT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE," +
                    "FOREIGN KEY (object_id) REFERENCES persons(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(sql);

            console.println("Таблицы аутентификации инициализированы");

        } catch (SQLException e) {
            console.printError("Ошибка инициализации базы данных для аутентификации: " + e.getMessage());
        }
    }

    // Хэширование пароля с помощью SHA-1
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хэширования пароля", e);
        }
    }

    // Регистрация пользователя
    public boolean register(String username, String password) {
        String hashedPassword = hashPassword(password);

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT INTO users (username, password_hash) VALUES (?, ?)")) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            console.printError("Ошибка регистрации пользователя: " + e.getMessage());
            return false;
        }
    }

    // Аутентификация пользователя
    public boolean authenticate(String username, String password) {
        String hashedPassword = hashPassword(password);

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT password_hash FROM users WHERE username = ?")) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return storedHash.equals(hashedPassword);
            }
            return false;

        } catch (SQLException e) {
            console.printError("Ошибка аутентификации: " + e.getMessage());
            return false;
        }
    }

    // Проверка существования пользователя
    public boolean userExists(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT username FROM users WHERE username = ?")) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            console.printError("Ошибка проверки пользователя: " + e.getMessage());
            return false;
        }
    }

    // Связь объекта с пользователем
    public boolean linkObjectToUser(long objectId, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT INTO user_objects (object_id, username) VALUES (?, ?)")) {

            pstmt.setLong(1, objectId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            console.printError("Ошибка связывания объекта с пользователем: " + e.getMessage());
            return false;
        }
    }

    // Получение владельца объекта
    public String getObjectOwner(long objectId) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT username FROM user_objects WHERE object_id = ?")) {

            pstmt.setLong(1, objectId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
            return null;

        } catch (SQLException e) {
            console.printError("Ошибка получения владельца объекта: " + e.getMessage());
            return null;
        }
    }

    // Удаление связи объекта с пользователем
    public boolean unlinkObjectFromUser(long objectId) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "DELETE FROM user_objects WHERE object_id = ?")) {

            pstmt.setLong(1, objectId);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            console.printError("Ошибка удаления связи объекта: " + e.getMessage());
            return false;
        }
    }
}