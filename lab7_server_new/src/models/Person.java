package models;

import utility.Element;
import utility.Validatable;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Главный класс с описанием объекта Person и методами в нём
 *
 * @author Miroslav
 * @version 1.0
 */

public class Person extends Element implements Validatable, Serializable {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Long height; //Поле не может быть null, Значение поля должно быть больше 0
    private ZonedDateTime birthday; //Поле может быть null
    private long weight; //Значение поля должно быть больше 0
    private Color eyeColor; //Поле не может быть null
    private Location location; //Поле не может быть null

    public Person(Long id, String name, Coordinates coordinates, ZonedDateTime creationDate,
                  Long height, ZonedDateTime birthday, long weight, Color eyeColor, Location location) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.height = height;
        this.birthday = birthday;
        this.weight = weight;
        this.eyeColor = eyeColor;
        this.location = location;
    }

    public Person(Long id, String name, Coordinates coordinates, Long height, ZonedDateTime birthday,
                  long weight, Color eyeColor, Location location) {
        this(id, name, coordinates, ZonedDateTime.now(), height, birthday, weight, eyeColor, location);
    }

    @Override
    public boolean validate() {
        if (id == null || id <= 0) {
            System.err.println("Ошибка валидации: ID должно быть > 0 и не null");
            return false;
        }
        if (name == null || name.isEmpty()) {
            System.err.println("Ошибка валидации: name не может быть null или пустым");
            return false;
        }
        if (coordinates == null || !coordinates.validate()) {
            System.err.println("Ошибка валидации: coordinates невалидны");
            return false;
        }
        if (creationDate == null) {
            System.err.println("Ошибка валидации: creationDate не может быть null");
            return false;
        }
        if (height == null || height <= 0) {
            System.err.println("Ошибка валидации: height должно быть > 0 и не null");
            return false;
        }
        if (weight <= 0) {
            System.err.println("Ошибка валидации: weight должно быть > 0");
            return false;
        }
        if (eyeColor == null) {
            System.err.println("Ошибка валидации: eyeColor не может быть null");
            return false;
        }
        if (location == null || !location.validate()) {
            System.err.println("Ошибка валидации: location невалидна");
            return false;
        }
        if (birthday != null && birthday.isAfter(ZonedDateTime.now())) {
            System.err.println("Ошибка валидации: birthday не может быть в будущем");
            return false;
        }
        return true;
    }
    public Long getId() {
        return id;
    }

    public void setID(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Long getHeight() {
        return height;
    }

    public ZonedDateTime getBirthday() {
        return birthday;
    }

    public long getWeight() {
        return weight;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public Location getLocation() {
        return location;
    }


    public static Person fromArray(String[] a) {
        Long id;
        String name;
        Coordinates coordinates;
        ZonedDateTime creationDate;
        Long height;
        ZonedDateTime birthday;
        long weight;
        Color eyeColor;
        Location location;
        try {
            try {id = Long.parseLong(a[0]);} catch (NumberFormatException e) {id = null;}
            name = a[1];
            coordinates = new Coordinates(a[2]);
            try {creationDate = ZonedDateTime.parse(a[3], DateTimeFormatter.ISO_ZONED_DATE_TIME);} catch (DateTimeParseException e) {creationDate = null;}
            try {height = Long.parseLong(a[4]);} catch (NumberFormatException e) {height = null;}
            try {birthday = a[5].equals("null")? null: ZonedDateTime.parse(a[5], DateTimeFormatter.ISO_ZONED_DATE_TIME);} catch (NullPointerException| IllegalArgumentException | DateTimeParseException e) {birthday = null;}
            try {weight = Long.parseLong(a[6]);} catch (NumberFormatException e) {weight = 0;}
            try {eyeColor = a[7].equals("null") ? null : Color.valueOf(a[7]);} catch (NullPointerException | IllegalArgumentException e) {eyeColor = null;}
            location = new Location(a[8]);
            return new Person(id, name, coordinates, ZonedDateTime.now(), height, birthday, weight, eyeColor, location);
        } catch (ArrayIndexOutOfBoundsException e) {}
        return null;
    }

    public static String[] toArray(Person e) {
        var list = new ArrayList<String>();
        list.add(e.getId().toString());
        list.add(e.getName());
        list.add(e.getCoordinates().toString());
        list.add(e.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME));
        list.add(e.getHeight().toString());
        list.add(e.getBirthday() == null ? "null" : e.getBirthday().format(DateTimeFormatter.ISO_DATE_TIME));
        list.add(Long.toString(e.getWeight()));
        list.add(e.getEyeColor().toString());
        list.add(e.getLocation().toString());
        return list.toArray(new String[0]);
    }

    // Добавляем serialVersionUID для совместимости версий
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(Element element) {
        return (int) (this.id - element.getId());
    }

    @Override
    public String toString() {
        return "d{\"id\": " + id + ", " +
                "\"name\": \"" + name + "\", " +
                "\"coordinates\": \"" + coordinates + "\", " +
                "\"creationDate\" = \"" + creationDate.format(DateTimeFormatter.ISO_DATE_TIME) + "\", " +
                "\"height\": \"" + height + "\", " +
                "\"birthday\" = \"" + (birthday == null ? "null" : "\""+birthday+"\"") + ", " +
                "\"weight\": \"" + weight + "\", " +
                "\"eyeColor\": \"" + eyeColor + "\", " +
                "\"location\": \"" + location + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person that = (Person) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, height, birthday, weight, eyeColor, location);
    }
}
