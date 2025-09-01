package models;

import utility.Validatable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Класс объект для работы с Location и методами в нём
 *
 * @author Miroslav
 * @version 1.0
 */

public class Location implements Validatable, Serializable {
    private Double x; //Поле не может быть null
    private Integer y; //Поле не может быть null
    private String name; //Строка не может быть пустой, Поле не может быть null
    @Serial
    private static final long serialVersionUID = 1L;
    public Location(Double x, Integer y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getNum() {
        return x.intValue() + y;
    }

    public String getName() { return name; }

    public Location(String s) {
        try {
            try {this.x = s.split(" ; ")[0].equals("null") ? null : Double.parseDouble(s.split(" ; ")[0]); } catch (NumberFormatException e) {}
            try {this.y = s.split(" ; ")[1].equals("null") ? null : Integer.parseInt(s.split(" ; ")[1]);} catch (NumberFormatException e) {}
            this.name = s.split(" ; ")[2];
        } catch (ArrayIndexOutOfBoundsException e) {}
    }

    @Override
    public boolean validate() {
        if (x == null) return false;
        if (y == null) return false;
        if (name.isEmpty() || name == null) return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location that = (Location) obj;
        return x.equals(that.x) && y.equals(that.y) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return x.hashCode() + y.hashCode() + name.hashCode();
    }

    @Override
    public String toString() {
        return x.toString() + " ; " + y.toString() + " ; " + name;
    }
}