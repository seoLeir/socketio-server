package io.seoleir.io;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import io.seoleir.data.Message;
import io.seoleir.holder.ActiveClientConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserSocketIO {

    private final SocketIOServer socketIOServer;

    private final ActiveClientConcurrentHashMap hashMap;

    public UserSocketIO(SocketIOServer socketIOServer, ActiveClientConcurrentHashMap hashMap) {
        this.socketIOServer = socketIOServer;
        this.hashMap = hashMap;

        SocketIONamespace executorNameSpace = this.socketIOServer.addNamespace("/user");
        executorNameSpace.addConnectListener(this::onConnect);
        executorNameSpace.addDisconnectListener(this::onDisconnect);
        executorNameSpace.addEventListener("user-send-message", Message.class, this.onSendMessageUser());
    }

    public DataListener<Message> onSendMessageUser() {
        return (client, data, ackSender) -> {
            log.info("Message from user: {} to executor: {}. Body: {}", data.getSenderName(), data.getTargetUserName(), data.getMessage());

            SocketIONamespace executorNameSpace = socketIOServer.getNamespace("/executor");

            BroadcastOperations room = executorNameSpace.getRoomOperations(ActiveClientConcurrentHashMap.roomUUID.toString());

            room.sendEvent("#order-1", client, data);

            ackSender.sendAckData("Message to user send successfully");
        };
    }

    private void onConnect(SocketIOClient client) {
        hashMap.addClient("user-login", client);

        client.joinRoom(ActiveClientConcurrentHashMap.roomUUID.toString());

        log.info("Client rooms: {}", client.getNamespace()
                .getBroadcastOperations()
                .getClients()
                .stream()
                .filter(client1 -> client1.getSessionId().equals(client1.getSessionId()))
                .findAny()
                .orElseThrow()
                .getAllRooms());

        log.info("USER: [{}] CONNECTED TO ROOM: [{}], NAMESPACE: [{}] ",
                client.getSessionId(),
                ActiveClientConcurrentHashMap.roomUUID.toString(),
                client.getNamespace().getName()
        );
    }

    public void onDisconnect(SocketIOClient client) {
        hashMap.removeClient("user-login");

        client.leaveRoom(ActiveClientConcurrentHashMap.roomUUID.toString());
        log.info("USER [{}] DISCONNECTED FROM ROOM: [{}] NAMESPACE: [{}]", client.getSessionId(), client.getAllRooms(), client.getNamespace().getName());
    }
}
