package io.seoleir.io;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import io.seoleir.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SocketModule {

    private final SocketIOServer socketServer;

    public SocketModule(SocketIOServer socketIOServer) {
        this.socketServer = socketIOServer;

        this.socketServer.addConnectListener(client -> log.info("Perform operation on user connect in controller"));
        this.socketServer.addDisconnectListener(client -> log.info("Perform operation on user disconnect in controller"));
        this.socketServer.addEventListener("messageSendToUser", Message.class, this.onSendMessage());
    }

    public void startServer() {
        socketServer.start();
    }

    public DataListener<Message> onSendMessage() {
        return (client, data, ackSender) -> {
            log.info("{} user send message to user {} and message is {}", data.getSenderName(), data.getTargetUserName(), data.getMessage());
            socketServer.getBroadcastOperations().sendEvent(data.getTargetUserName(), client, data);

            ackSender.sendAckData("Message send to target user successfully");
        };
    }
}
