package io.seoleir.io;

import ch.qos.logback.core.helpers.ThrowableToStringArray;
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
public class ExecutorSocketIO {

    private final SocketIOServer socketIOServer;

    private final ActiveClientConcurrentHashMap hashMap;

    public ExecutorSocketIO(SocketIOServer socketIOServer, ActiveClientConcurrentHashMap hashMap) {
        this.socketIOServer = socketIOServer;
        this.hashMap = hashMap;

        SocketIONamespace executorNameSpace = this.socketIOServer.addNamespace("/executor");
        executorNameSpace.addConnectListener(this::onConnect);
        executorNameSpace.addDisconnectListener(this::onDisconnect);
        executorNameSpace.addEventListener("executor-send-message", Message.class, this.onSendMessageExecutor());
    }


    public DataListener<Message> onSendMessageExecutor() {
        return (client, data, ackSender) -> {
            log.info("Message from executor: {} to user: {}. Body: {}", data.getSenderName(), data.getTargetUserName(), data.getMessage());

            SocketIONamespace userNameSpace = socketIOServer.getNamespace("/user");

            BroadcastOperations room = userNameSpace.getRoomOperations(ActiveClientConcurrentHashMap.roomUUID.toString());
            room.sendEvent("#order-1", client, data);

            ackSender.sendAckData("Message to user send successfully");
        };
    }

    private void onConnect(SocketIOClient client) {
        hashMap.addClient("executor-login", client);

        client.joinRoom(ActiveClientConcurrentHashMap.roomUUID.toString());

        log.info("EXECUTOR: [{}] CONNECTED TO ROOM: [{}] NAMESPACE: [{}]",
                client.getSessionId(),
                ActiveClientConcurrentHashMap.roomUUID.toString(),
                client.getNamespace().getName());
    }

    public void onDisconnect(SocketIOClient client) {
        hashMap.removeClient("executor-login");

        client.leaveRoom(ActiveClientConcurrentHashMap.roomUUID.toString());

        log.info("EXECUTOR [{}] DISCONNECTED FROM ROOM: [{}] NAMESPACE: [{}]",
                client.getSessionId(), client.getAllRooms(), client.getNamespace().getName());
    }
}
