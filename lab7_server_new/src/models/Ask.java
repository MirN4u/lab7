package models;

import utility.Console;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

/**
 * Класс отвечающий за чтение объектов
 *
 * @author Miroslav
 * @version 1.0
 */

public class Ask {
    public static class AskBreak extends Exception {
    }

    public static Person askPerson(Console console, Long id) throws AskBreak {
        try {
            String name;
            while (true) {
                console.print("name(Поле не может быть null, Строка не может быть пустой): ");
                name = console.readln().trim();
                if (name.equals("exit")) throw new AskBreak();
                if (!name.isEmpty()) {
                    break;
                } else {
                    console.printError("Поле не может быть null, Строка не может быть пустой");
                }
            }
            var coordinates = askCoordinates(console);
            var height = askHeight(console);
            var birthday = askBirthday(console);
            var weight = askWeight(console);
            var eyeColor = askEyeColor(console);
            var location = askLocation(console);
            return new Person(id, name, coordinates, height, birthday, weight, eyeColor, location);
        } catch (NoSuchElementException | IllegalStateException | NullPointerException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Coordinates askCoordinates(Console console) throws AskBreak {
        try {
            Double x;
            while (true) {
                console.print("coordinates.x(Поле не может быть null): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        x = Double.parseDouble(line);
                        break;
                    } catch (NumberFormatException e) {
                        console.printError("Поле не может быть null");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            long y;
            while (true) {
                console.print("coordinates.y:(Максимальное значение поля: 34) ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        y = Long.parseLong(line);
                        if (y < 34) break;
                        else{
                            console.printError("Максимальное значение поля: 34");
                        }
                    } catch (NumberFormatException e) {
                        console.printError("Ошибка ввода, введите Long");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            return new Coordinates(x, y);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Long askHeight(Console console) throws AskBreak {
        try {
            Long height;
            while (true) {
                console.print("height(Поле не может быть null, Значение поля должно быть больше 0): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        height = Long.parseLong(line);
                        if (height > 0) break;
                        else{
                            console.printError("Значение поля должно быть больше 0");
                        }
                    } catch (NumberFormatException e) {
                        console.printError("Поле не может быть null, Значение поля должно быть больше 0");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            return height;
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Long askWeight(Console console) throws AskBreak {
        try {
            long weight;
            while (true) {
                console.print("weight(Значение поля должно быть больше 0): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        weight = Long.parseLong(line);
                        if (weight > 0) break;
                        else{
                            console.printError("Значение поля должно быть больше 0");
                        }
                    } catch (NumberFormatException e) {
                        console.printError("Ошибка ввода, введите long");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            return weight;
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static ZonedDateTime askBirthday(Console console) throws AskBreak {
        try {
            ZonedDateTime birthday;
            while (true) {
                console.print("birthday(Exemple: " +
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT) + " или enter): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (line.equals("")) {
                    birthday = null;
                    break;
                }
                try {
                    birthday = ZonedDateTime.parse(line, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    break;
                } catch (DateTimeException e) {
                    console.printError("Ошибка ввода, введите по образцу");
                }
                try {
                    birthday = ZonedDateTime.parse(line, DateTimeFormatter.ISO_DATE_TIME);
                    break;
                } catch (DateTimeException e) {
                }
            }
            return birthday;
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Color askEyeColor(Console console) throws AskBreak {
        try {
            Color c;
            while (true) {
                console.print("eyeColor (" + Color.names() + "): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        c = Color.valueOf(line.toUpperCase());
                        break;
                    } catch (NullPointerException | IllegalArgumentException e) {
                        console.printError("Ошибка ввода, введите по образцу ");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            return c;
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }

    public static Location askLocation(Console console) throws AskBreak {
        try {
            Double x;
            while (true) {
                console.print("location.x(Поле не может быть null): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        x = Double.parseDouble(line);
                        break;
                    } catch (NumberFormatException e) {
                        console.printError("Поле не может быть null");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            Integer y;
            while (true) {
                console.print("location.y(Поле не может быть null): ");
                var line = console.readln().trim();
                if (line.equals("exit")) throw new AskBreak();
                if (!line.equals("")) {
                    try {
                        y = Integer.parseInt(line);
                        break;
                    } catch (NumberFormatException e) {
                        console.printError("Поле не может быть null");
                    }
                } else {
                    console.printError("Введите данные");
                }
            }
            String name;
            while (true) {
                console.print("location.name(Строка не может быть пустой, Поле не может быть null): ");
                name = console.readln().trim();
                if (name.equals("exit")) throw new AskBreak();
                if (!name.isEmpty()) break;
                else {
                    console.printError("Введите данные");
                }
            }
            return new Location(x, y, name);
        } catch (NoSuchElementException | IllegalStateException e) {
            console.printError("Ошибка чтения");
            return null;
        }
    }
}