package io.seoleir.holder;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ActiveClientConcurrentHashMap {

    private final ConcurrentHashMap<String, SocketIOClient> executorClients = new ConcurrentHashMap<>();

    public static UUID roomUUID = UUID.randomUUID();

    public void addClient(String clientLogin, SocketIOClient client) {
        executorClients.compute(clientLogin, (k, v) -> client);
        log.info("CACHE: {}", executorClients);
    }

    public void removeClient(String clientLogin) {
        executorClients.remove(clientLogin);
        log.info("CACHE: {}", executorClients);
    }

    public SocketIOClient getClientByLogin(String login) {
        return executorClients.get(login);
    }
}
