package io.seoleir.holder;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.concurrent.ConcurrentHashMap;

public class ActiveClientConcurrentHashMap {

    private final ConcurrentHashMap<String, SocketIOClient> executorClients = new ConcurrentHashMap<>();

    public void addClient(String clientLogin, SocketIOClient client) {
        executorClients.compute(clientLogin, (k, v) -> client);
    }

    public void removeClient(String clientLogin) {
        executorClients.remove(clientLogin);
    }

    public SocketIOClient getClientByLogin(String login) {
        return executorClients.get(login);
    }
}
