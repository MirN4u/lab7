package client;

import utility.NetworkCommand;
import utility.NetworkResponse;
import utility.NetworkUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.time.Instant;

public class UDPClient implements AutoCloseable {
    private DatagramChannel channel;
    private final InetSocketAddress serverAddress;
    private volatile boolean serverAvailable = false;
    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final int RESPONSE_TIMEOUT_MS = 2000;

    public UDPClient(String host, int port) throws IOException {
        this.serverAddress = new InetSocketAddress(host, port);
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.serverAvailable = checkConnection();
    }

    public boolean checkConnection() {
        try {
            sendPing();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            Instant start = Instant.now();
            while (Duration.between(start, Instant.now()).toMillis() < CONNECTION_TIMEOUT_MS) {
                buffer.clear();
                SocketAddress sender = channel.receive(buffer);

                if (sender != null) {
                    buffer.flip();
                    byte[] responseData = new byte[buffer.remaining()];
                    buffer.get(responseData);

                    NetworkResponse response = (NetworkResponse) NetworkUtils.deserializeObject(responseData);
                    serverAvailable = response.getStatus() == 200 && "pong".equals(response.getMessage());
                    return serverAvailable;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка проверки соединения: " + e.getMessage());
        }
        serverAvailable = false;
        return false;
    }

    private void sendPing() throws IOException {
        byte[] pingData = NetworkUtils.serializeObject(new NetworkCommand("_ping", new Object[0]));
        channel.send(ByteBuffer.wrap(pingData), serverAddress);
    }

    public boolean isServerAvailable() {
        return serverAvailable;
    }

    public NetworkResponse sendCommand(NetworkCommand command) throws IOException, ClassNotFoundException {
        if (!serverAvailable) {
            throw new IOException("Сервер недоступен");
        }

        byte[] data = NetworkUtils.serializeObject(command);
        Instant start = Instant.now();
        channel.send(ByteBuffer.wrap(data), serverAddress);

        ByteBuffer responseBuffer = ByteBuffer.allocate(65507);
        while (Duration.between(start, Instant.now()).toMillis() < RESPONSE_TIMEOUT_MS) {
            responseBuffer.clear();
            SocketAddress sender = channel.receive(responseBuffer);

            if (sender != null) {
                responseBuffer.flip();
                byte[] responseData = new byte[responseBuffer.remaining()];
                responseBuffer.get(responseData);

                NetworkResponse response = (NetworkResponse) NetworkUtils.deserializeObject(responseData);
                serverAvailable = true; // Сервер ответил
                return response;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Прервано ожидание ответа");
            }
        }

        serverAvailable = false;
        throw new SocketTimeoutException("Сервер не ответил");
    }

    @Override
    public void close() throws IOException {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}