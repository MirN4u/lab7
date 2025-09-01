package server;

import managers.CommandManager;
import utility.NetworkCommand;
import utility.NetworkResponse;
import utility.NetworkUtils;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class UDPServer {
    private static final Logger logger = Logger.getLogger(UDPServer.class.getName());
    private final int port;
    private final CommandManager commandManager;
    private DatagramSocket socket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    // Пуллы для многопоточной обработки
    private ForkJoinPool readPool;
    private ExecutorService sendPool;
    // Для синхронизации доступа к коллекциям
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();

    public UDPServer(int port, CommandManager commandManager) {
        this.port = port;
        this.commandManager = commandManager;
        this.readPool = new ForkJoinPool();         // Инициализация пулов
        this.sendPool = Executors.newCachedThreadPool();
    }

    public void start() {
        if (running.get()) {
            logger.warning("Сервер уже запущен");
            return;
        }

        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);
            running.set(true);
            logger.info("Сервер запущен на порту " + port);

            // Цикл приема запросов
            while (running.get()) {
                try {
                    byte[] receiveBuffer = new byte[65507];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    try {
                        socket.receive(receivePacket);
                        // Передача задачи на обработку в ForkJoinPool
                        readPool.execute(() -> handleRequest(receivePacket));
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        logger.severe("Ошибка приема запроса: " + e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            logger.severe("Ошибка сокета: " + e.getMessage());
        } finally {
            closeResources();
            logger.info("Сервер остановлен");
        }
    }

    private void handleRequest(DatagramPacket receivePacket) {
        new Thread(() -> { // Обработка запроса в новом потоке
            try {
                NetworkCommand command = (NetworkCommand) NetworkUtils.deserializeObject(
                        receivePacket.getData());
                NetworkResponse response;
                dataLock.readLock().lock();
                try {
                    response = commandManager.executeNetworkCommand(command);
                } finally {
                    dataLock.readLock().unlock();
                }
                sendPool.execute(() -> {
                    try {
                        sendResponse(response, receivePacket.getAddress(), receivePacket.getPort());
                    } catch (IOException e) {
                        logger.severe("Ошибка отправки ответа: " + e.getMessage());
                    }
                });
            } catch (ClassNotFoundException | IOException e) {
                logger.severe("Ошибка обработки команды: " + e.getMessage());
            }
        }).start();
    }

    private void sendResponse(NetworkResponse response, InetAddress address, int port) throws IOException {
        byte[] responseData = NetworkUtils.serializeObject(response);
        DatagramPacket sendPacket = new DatagramPacket(responseData, responseData.length, address, port);
        socket.send(sendPacket);
    }

    public void stop() {
        if (!running.getAndSet(false)) {
            logger.warning("Сервер уже остановлен");
            return;
        }

        logger.info("Остановка сервера...");
        closeResources();
    }

    private void closeResources() {
        closeSocket();
        readPool.shutdown();
        sendPool.shutdown();

        try {
            if (!readPool.awaitTermination(5, TimeUnit.SECONDS)) {
                readPool.shutdownNow();
            }
            if (!sendPool.awaitTermination(5, TimeUnit.SECONDS)) {
                sendPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            readPool.shutdownNow();
            sendPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                logger.warning("Ошибка при закрытии сокета: " + e.getMessage());
            }
        }
    }


    public boolean isRunning() {
        return running.get();
    }
}