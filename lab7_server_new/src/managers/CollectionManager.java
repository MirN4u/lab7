package managers;

import models.Person;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import utility.*;

public class CollectionManager {
    private Long currentId = 1L;
    private Map<Long, Person> map = new TreeMap<>();
    private ZonedDateTime lastInitTime;
    private ZonedDateTime lastSaveTime;
    private final DumpManager dumpManager;
    private final AuthManager authManager;
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();

    public CollectionManager(DumpManager dumpManager) {
        this.lastInitTime = null;
        this.lastSaveTime = null;
        this.dumpManager = dumpManager;
        this.authManager = dumpManager.getAuthManager();
    }

    public TreeMap<Long, Person> getCollection() {
        dataLock.readLock().lock();
        try {
        return (TreeMap<Long, Person>) map;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public ZonedDateTime getLastInitTime() {
        return lastInitTime;
    }

    public ZonedDateTime getLastSaveTime() {
        return lastSaveTime;
    }

    public void saveCollection() {
        dumpManager.writeCollection(map.values());
        lastSaveTime = ZonedDateTime.now();
    }

    public Person byId(Long id) {
        dataLock.readLock().lock();
        try {
            return map.get(id);
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public boolean isСontain(Person e) {
        dataLock.readLock().lock();
        try {
            return e == null || byId(e.getId()) != null;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public boolean isIdUsed(long id) {
        dataLock.readLock().lock();
        try {
            return map.values().stream()
                    .anyMatch(person -> person.getId() == id);
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public void resetCurrentId() {
        this.currentId = 1L;
    }

    public boolean add(Person person, Long key, String username) {
        dataLock.writeLock().lock();
        try {
            if (map.containsKey(key)) {
                System.err.println("Ключ " + key + " уже существует в коллекции");
                return false;
            }
            Long generatedId = dumpManager.saveObjectAndGetId(person, username);
            if (generatedId == null) {
                System.err.println("Не удалось сохранить объект в БД");
                return false;
            }
            person.setID(generatedId);
            map.put(key, person);
            update();
            System.err.println("Объект успешно добавлен с ID: " + generatedId + " и ключом: " + key);
            return true;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public Long getFreeId() {
        //ID генерируется базой данных
        return dumpManager.getNextIdFromSequence();
    }

    public boolean add(Person person, Long key) {
        return add(person, key, null);
    }

    public boolean remove(Long key, String username) {
        dataLock.writeLock().lock();
        try {
            Person person = byId(key);
            if (person == null) return false;
            if (username != null) {
                String owner = authManager.getObjectOwner(person.getId());
                if (owner == null || !owner.equals(username)) {
                    return false; // Нет прав на удаление
                }
            }
            if (!dumpManager.deleteObject(person.getId())) {
                System.err.println("Не удалось удалить объект из БД");
                return false;
            }
            map.remove(key);
            update();
            return true;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public boolean remove(Long key) {
        return remove(key, null);
    }

    public Long getKeyByPersonId(Long personId) {
        for (Map.Entry<Long, Person> entry : map.entrySet()) {
            if (entry.getValue().getId().equals(personId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void update() {
    }

    public boolean loadCollection() {
        dataLock.writeLock().lock();
        try {
            try {
                LinkedList<Person> loadedPersons = new LinkedList<>();
                dumpManager.readCollection(loadedPersons);
                map.clear();
                int validCount = 0;
                int invalidCount = 0;

                for (Person person : loadedPersons) {
                    if (person != null && person.validate()) {
                        Long id = person.getId();
                        map.put(id, person);
                        validCount++;
                    } else {
                        invalidCount++;
                    }
                }

                // Сбрасываем sequence после загрузки
                dumpManager.resetSequence();

                lastInitTime = ZonedDateTime.now();

                if (invalidCount > 0) {
                    System.err.println("Пропущено " + invalidCount + " невалидных объектов");
                }
                return true;

            } catch (Exception e) {
                System.err.println("Ошибка при загрузке коллекции: " + e.getMessage());
                return false;
            }
        } finally {
            dataLock.writeLock().unlock();
        }
    }


    // Метод для проверки прав доступа к объекту
    public boolean hasAccess(Long key, String username) {
        if (username == null) return false;

        Person person = byId(key);
        if (person == null) return false;

        String owner = authManager.getObjectOwner(person.getId());
        return owner != null && owner.equals(username);
    }

    public Map<Long, Person> getUserObjects(String username) {
        dataLock.readLock().lock();
        try {
            if (username == null) return Collections.emptyMap();

            Map<Long, Person> userObjects = new TreeMap<>();
            for (Map.Entry<Long, Person> entry : map.entrySet()) {
                String owner = authManager.getObjectOwner(entry.getValue().getId());
                if (owner != null && owner.equals(username)) {
                    userObjects.put(entry.getKey(), entry.getValue());
                }
            }
            return userObjects;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    // Метод для обновления объекта с проверкой прав
    public boolean update(Long key, Person newPerson, String username) {
        dataLock.writeLock().lock();
        try {
        Person existingPerson = byId(key);
        if (existingPerson == null) return false;

        // Проверяем права доступа
        if (username != null) {
            String owner = authManager.getObjectOwner(existingPerson.getId());
            if (owner == null || !owner.equals(username)) {
                return false; // Нет прав на обновление
            }
        }
        // Сохраняем ID
        newPerson.setID(existingPerson.getId());
        map.put(key, newPerson);
        update();
        return true;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public boolean update(Long key, Person newPerson) {
        return update(key, newPerson, null);
    }

    @Override
    public String toString() {
        dataLock.readLock().lock();  //readLock для операций чтения
        try {
            if (map.isEmpty()) return "Коллекция пуста!";
            StringBuilder info = new StringBuilder();
            for (Long key : map.keySet()) {
                info.append("key: ").append(key).append("\n").append(map.get(key)).append("\n\n");
            }
            return info.toString().trim();
        } finally {
            dataLock.readLock().unlock();  //unlock того же типа
        }
    }

    public String toStringWithCreators() {
        dataLock.readLock().lock();
        try {
            if (map.isEmpty()) return "Коллекция пуста!";

            StringBuilder info = new StringBuilder();
            for (Long key : map.keySet()) {
                Person person = map.get(key);
                String creator = authManager.getObjectOwner(person.getId());

                info.append("key: ").append(key).append("\n")
                        .append(person).append("\n")
                        .append("Создатель: ").append(creator != null ? creator : "неизвестен").append("\n\n");
            }
            return info.toString().trim();
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public byte[] serializeCollection() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new ArrayList<>(map.values()));
            return baos.toByteArray();
        }
    }

    public void deserializeCollection(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            List<Person> list = (List<Person>) ois.readObject();
            map.clear();
            for (Person person : list) {
                if (person != null && person.validate()) {
                    Long newId = getFreeId();
                    person.setID(newId);
                    map.put(newId, person);
                }
            }
        }
    }

    public List<Person> getSortedCollection() {
        return map.values().stream()
                .sorted()
                .toList();
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public DumpManager getDumpManager() {
        return dumpManager;
    }
}