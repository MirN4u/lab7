package managers;

import models.Color;
import models.Coordinates;
import models.Location;
import models.Person;
import utility.Console;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;

public class DumpManager {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Console console;
    private final AuthManager authManager;

    public DumpManager(String dbUrl, String dbUser, String dbPassword, Console console) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.console = console;
        this.authManager = new AuthManager(dbUrl, dbUser, dbPassword, console);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement()) {
            // sequence для генерации ID
            String createSequenceSQL = "CREATE SEQUENCE IF NOT EXISTS person_id_seq START 1 INCREMENT 1";
            stmt.executeUpdate(createSequenceSQL);

            // Создаем таблицу
            String createTableSQL = "CREATE TABLE IF NOT EXISTS persons (" +
                    "id BIGINT PRIMARY KEY DEFAULT nextval('person_id_seq')," +
                    "name VARCHAR(100) NOT NULL," +
                    "coordinates_x DOUBLE PRECISION NOT NULL," +
                    "coordinates_y INTEGER NOT NULL," +
                    "creation_date TIMESTAMP WITH TIME ZONE NOT NULL," +
                    "height BIGINT NOT NULL," +
                    "birthday TIMESTAMP WITH TIME ZONE," +
                    "weight BIGINT NOT NULL," +
                    "eye_color VARCHAR(20) NOT NULL," +
                    "location_x DOUBLE PRECISION NOT NULL," +
                    "location_y INTEGER NOT NULL," +
                    "location_name VARCHAR(100) NOT NULL," +
                    "CHECK (id > 0)," +
                    "CHECK (height > 0)," +
                    "CHECK (weight > 0))";
            stmt.executeUpdate(createTableSQL);

            console.println("База данных инициализирована с использованием sequence для ID");
            this.authManager.initializeDatabase();// Инициализация таблицы пользователей

        } catch (SQLException e) {
            console.printError("Ошибка инициализации базы данных: " + e.getMessage());
        }
    }

    //Получает следующий ID
    public Long getNextIdFromSequence() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nextval('person_id_seq')")) {

            if (rs.next()) {
                Long nextId = rs.getLong(1);
                console.println("Сгенерирован новый ID: " + nextId); // Добавьте логирование
                return nextId;
            }
            return null;

        } catch (SQLException e) {
            console.printError("Ошибка получения следующего ID из sequence: " + e.getMessage());
            return null;
        }
    }

    //Сохраняет объект и возвращает сгенерированный ID
    public Long saveObjectAndGetId(Person person, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Проверяем валидность объекта перед сохранением
            if (person == null || !person.validate()) {
                console.printError("Невалидный объект для сохранения");
                return null;
            }

            String sql = "INSERT INTO persons (name, coordinates_x, coordinates_y, creation_date, " +
                    "height, birthday, weight, eye_color, location_x, location_y, " +
                    "location_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "RETURNING id";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, person.getName());
                pstmt.setDouble(2, person.getCoordinates().getX());
                pstmt.setLong(3, person.getCoordinates().getY());
                pstmt.setTimestamp(4, Timestamp.from(person.getCreationDate().toInstant()));
                pstmt.setLong(5, person.getHeight());

                if (person.getBirthday() != null) {
                    pstmt.setTimestamp(6, Timestamp.from(person.getBirthday().toInstant()));
                } else {
                    pstmt.setNull(6, Types.TIMESTAMP);
                }

                pstmt.setLong(7, person.getWeight());
                pstmt.setString(8, person.getEyeColor().toString());
                pstmt.setDouble(9, person.getLocation().getX());
                pstmt.setInt(10, person.getLocation().getY());
                pstmt.setString(11, person.getLocation().getName());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Long generatedId = rs.getLong(1);
                        person.setID(generatedId);
                        // Связываем объект с пользователем
                        if (authManager.linkObjectToUser(generatedId, username)) {
                            console.println("Объект успешно сохранен с ID: " + generatedId);
                            return generatedId;
                        } else {
                            console.printError("Не удалось связать объект с пользователем");
                            // Откатываем вставку, если не удалось связать с пользователем
                            connection.rollback();
                            return null;
                        }
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            console.printError("Ошибка сохранения объекта в БД: " + e.getMessage());
            return null;
        }
    }

    public void writeCollection(Collection<Person> collection) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Очищаем таблицу и сбрасываем sequence
            try (Statement clearStmt = connection.createStatement()) {
                clearStmt.executeUpdate("TRUNCATE TABLE persons RESTART IDENTITY CASCADE");
            }
            // Подготавливаем запрос для вставки
            String sql = "INSERT INTO persons (id, name, coordinates_x, coordinates_y, creation_date, " +
                    "height, birthday, weight, eye_color, location_x, location_y, " +
                    "location_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);

                for (Person person : collection) {
                    pstmt.setLong(1, person.getId());
                    pstmt.setString(2, person.getName());
                    pstmt.setDouble(3, person.getCoordinates().getX());
                    pstmt.setLong(4, person.getCoordinates().getY());
                    pstmt.setTimestamp(5, Timestamp.from(person.getCreationDate().toInstant()));
                    pstmt.setLong(6, person.getHeight());

                    if (person.getBirthday() != null) {
                        pstmt.setTimestamp(7, Timestamp.from(person.getBirthday().toInstant()));
                    } else {
                        pstmt.setNull(7, Types.TIMESTAMP);
                    }

                    pstmt.setLong(8, person.getWeight());
                    pstmt.setString(9, person.getEyeColor().toString());
                    pstmt.setDouble(10, person.getLocation().getX());
                    pstmt.setInt(11, person.getLocation().getY());
                    pstmt.setString(12, person.getLocation().getName());

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                connection.commit();
                console.println("Коллекция успешно сохранена в базу данных!");
            }
        } catch (SQLException e) {
            console.printError("Ошибка записи в базу данных: " + e.getMessage());
        }
    }
/*
    public boolean saveObjectWithUser(Person person, String username) {
        // Используем новый метод
        Long generatedId = saveObjectAndGetId(person, username);
        return generatedId != null;
    }
*/
    public AuthManager getAuthManager() {
        return authManager;
    }

    public void readCollection(Collection<Person> collection) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM persons ORDER BY id")) {

            collection.clear();
            LinkedList<Person> loadedCollection = new LinkedList<>();

            while (rs.next()) {
                Person person = new Person(
                        rs.getLong("id"),
                        rs.getString("name"),
                        new Coordinates(rs.getDouble("coordinates_x"), rs.getInt("coordinates_y")),
                        rs.getTimestamp("creation_date").toInstant().atZone(ZonedDateTime.now().getZone()),
                        rs.getLong("height"),
                        rs.getTimestamp("birthday") == null ? null :
                                rs.getTimestamp("birthday").toInstant().atZone(ZonedDateTime.now().getZone()),
                        rs.getLong("weight"),
                        Color.valueOf(rs.getString("eye_color")),
                        new Location(
                                rs.getDouble("location_x"),
                                rs.getInt("location_y"),
                                rs.getString("location_name")
                        )
                );

                loadedCollection.add(person);
            }

            collection.addAll(loadedCollection);
            console.println("Коллекция успешно загружена из базы данных!");

        } catch (SQLException e) {
            console.printError("Ошибка чтения из базы данных: " + e.getMessage());
        }
    }
    public boolean updateObject(Person person) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "UPDATE persons SET name = ?, coordinates_x = ?, coordinates_y = ?, " +
                    "creation_date = ?, height = ?, birthday = ?, weight = ?, " +
                    "eye_color = ?, location_x = ?, location_y = ?, location_name = ? " +
                    "WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, person.getName());
                pstmt.setDouble(2, person.getCoordinates().getX());
                pstmt.setLong(3, person.getCoordinates().getY());
                pstmt.setTimestamp(4, Timestamp.from(person.getCreationDate().toInstant()));
                pstmt.setLong(5, person.getHeight());

                if (person.getBirthday() != null) {
                    pstmt.setTimestamp(6, Timestamp.from(person.getBirthday().toInstant()));
                } else {
                    pstmt.setNull(6, Types.TIMESTAMP);
                }

                pstmt.setLong(7, person.getWeight());
                pstmt.setString(8, person.getEyeColor().toString());
                pstmt.setDouble(9, person.getLocation().getX());
                pstmt.setInt(10, person.getLocation().getY());
                pstmt.setString(11, person.getLocation().getName());
                pstmt.setLong(12, person.getId());

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            console.printError("Ошибка обновления объекта: " + e.getMessage());
            return false;
        }
    }
    public void resetSequence() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement()) {
            String maxIdQuery = "SELECT COALESCE(MAX(id), 0) FROM persons";
            ResultSet rs = stmt.executeQuery(maxIdQuery);

            long maxId = 0;
            if (rs.next()) {
                maxId = rs.getLong(1);
            }
            String resetSequenceSQL = "SELECT setval('person_id_seq', " + (maxId + 1) + ", false)";
            stmt.executeQuery(resetSequenceSQL);

            console.println("Sequence сброшен. Следующий ID: " + (maxId + 1));

        } catch (SQLException e) {
            console.printError("Ошибка сброса sequence: " + e.getMessage());
        }
    }
    public boolean deleteObject(long objectId) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(
                     "DELETE FROM persons WHERE id = ?")) {

            pstmt.setLong(1, objectId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            console.printError("Ошибка удаления объекта из БД: " + e.getMessage());
            return false;
        }
    }
}
/*
INSERT INTO persons (
    name,
    coordinates_x,
    coordinates_y,
    creation_date,
    height,
    birthday,
    weight,
    eye_color,
    location_x,
    location_y,
    location_name
) VALUES (
    'Test User',          -- name
    123.456,              -- coordinates_x
    25,                   -- coordinates_y (<= 34)
    CURRENT_TIMESTAMP,    -- creation_date (текущая дата/время)
    180,                  -- height (> 0)
    NULL,                 -- birthday (может быть NULL)
    75,                   -- weight (> 0)
    'BLUE',               -- eye_color (RED, BLUE, YELLOW или ORANGE)
    50.789,               -- location_x
    30,                   -- location_y
    'Test Location'       -- location_name
);
 */