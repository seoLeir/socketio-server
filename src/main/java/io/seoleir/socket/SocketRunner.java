package io.seoleir.socket;

import io.seoleir.io.SocketModule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketRunner implements CommandLineRunner {

    private final SocketModule socketModule;

    @Override
    public void run(String... args) throws Exception {
        socketModule.startServer();
    }
}
