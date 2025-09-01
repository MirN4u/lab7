package models;

import utility.Validatable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Класс с описанием объекта Coordinates и методами в нём
 *
 * @author Miroslav
 * @version 1.0
 */

public class Coordinates implements Validatable, Serializable {
    private final Double x;  //Поле не может быть null
    private final long y;    //Максимальное значение поля: 34
    @Serial
    private static final long serialVersionUID = 1L;
    public Coordinates(Double x, long y) {
        if (x == null) {
            throw new IllegalArgumentException("Координата X не может быть null");
        }
        if (y > 34) {
            throw new IllegalArgumentException("Координата Y должна быть <= 34");
        }
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }
    public long getY() {
        return y;
    }

    public Coordinates(String s) {
        try {
            String[] parts = s.split(";");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Нужно 2 координаты: X;Y");
            }
            this.x = Double.parseDouble(parts[0]);
            this.y = Long.parseLong(parts[1]);
            // Проверяем валидность
            if (y > 34) {
                throw new IllegalArgumentException("Y должен быть <= 34");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный формат числа: " + s);
        }
    }

    @Override
    public boolean validate() {
        return x != null && y <= 34;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinates that = (Coordinates) obj;
        return y == that.y && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return x + ";" + y;
    }
    public Integer getNum() {
        return x.intValue() + Math.toIntExact(y);
    }
}

