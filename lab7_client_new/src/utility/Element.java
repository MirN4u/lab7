package utility;

/**
 * Абстрактный класс от которого наследуется главная класс-модель
 *
 * @author Miroslav
 * @version 1.0
 */

public abstract class Element implements Comparable<Element>, Validatable {
    abstract public Long getId();
}

