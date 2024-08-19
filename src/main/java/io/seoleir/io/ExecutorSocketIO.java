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
public class ExecutorSocketIO {

    private final SocketIOServer socketIOServer;

    public ExecutorSocketIO(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;

        SocketIONamespace executorNameSpace = this.socketIOServer.addNamespace("/executor");
        executorNameSpace.addConnectListener(this::onConnect);
        executorNameSpace.addDisconnectListener(this::onDisconnect);
        executorNameSpace.addEventListener("executor-send-message", Message.class, this.onSendMessageExecutor());
    }


    public DataListener<Message> onSendMessageExecutor() {
        return (client, data, ackSender) -> {
            log.info("Message from user: {} to executor: {}. Body: {}", data.getSenderName(), data.getTargetUserName(), data.getMessage());

            SocketIONamespace executorNameSpace = socketIOServer.getNamespace("/user");

            BroadcastOperations broadcastOperations = executorNameSpace.getBroadcastOperations();

            broadcastOperations.sendEvent("#order-1", client, data);

            ackSender.sendAckData("Message to user send successfully");
        };
    }

    private void onConnect(SocketIOClient client) {
        log.info("EXECUTOR: [{}] CONNECTED TO NAMESPACE: [{}]", client.getSessionId(), client.getNamespace().getName());
    }

    public void onDisconnect(SocketIOClient client) {
        log.info("EXECUTOR [{}] DISCONNECTED FROM ROOM: [{}] NAMESPACE: [{}]",
                client.getSessionId(), client.getAllRooms(), client.getNamespace().getName());
    }
}
