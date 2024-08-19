package io.seoleir.io;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import io.seoleir.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserSocketIO {

    private final SocketIOServer socketIOServer;

    public UserSocketIO(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;

        SocketIONamespace executorNameSpace = this.socketIOServer.addNamespace("/user");
        executorNameSpace.addConnectListener(this::onConnect);
        executorNameSpace.addDisconnectListener(this::onDisconnect);
        executorNameSpace.addEventListener("user-send-message", Message.class, this.onSendMessageUser());
    }

    public DataListener<Message> onSendMessageUser() {
        return (client, data, ackSender) -> {
            log.info("Message from user: {} to executor: {}. Body: {}", data.getSenderName(), data.getTargetUserName(), data.getMessage());

            SocketIONamespace executorNameSpace = socketIOServer.getNamespace("/executor");

            BroadcastOperations broadcastOperations = executorNameSpace.getBroadcastOperations();

            broadcastOperations.sendEvent("#order-1", client, data);

            ackSender.sendAckData("Message to user send successfully");
        };
    }

    private void onConnect(SocketIOClient client) {
        log.info("USER: [{}] CONNECTED TO NAMESPACE: [{}]", client.getSessionId(), client.getNamespace().getName());
    }

    public void onDisconnect(SocketIOClient client) {
        log.info("USER [{}] DISCONNECTED FROM ROOM: [{}] NAMESPACE: [{}]",
                client.getSessionId(), client.getAllRooms(), client.getNamespace().getName());
    }
}
