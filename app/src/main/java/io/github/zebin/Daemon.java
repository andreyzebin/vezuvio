package io.github.zebin;

import io.github.zebin.javabash.sandbox.PosixPath;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class Daemon implements AutoCloseable {
    private final App app;
    private PosixPath socketPath;
    private ServerSocketChannel serverChannel;
    private Configurations cfg;

    public Daemon(App app) {
        this.app = app;
    }

    public void bind() {
        PosixPath socketPath = cfg.getVezuvioHome().climb("vezuvio.socket");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath.toPath());
        try {
            serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            serverChannel.bind(socketAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void accept() {
        try {
            SocketChannel serverAccept = serverChannel.accept();
            ByteBuffer buf = ByteBuffer.allocate(1024);
            StringBuilder sb = new StringBuilder();

            if (serverAccept != null) {
                while (true) {
                    buf.clear();
                    readSocketMessage(serverAccept, buf)
                            .ifPresent(msg -> {
                                sb.append(msg);
                                log.info("[Client message] {}", msg);
                            });
                    pollLines(sb).forEach(ll -> app.run(ll.split(" ")));
                    Thread.sleep(100);
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<String> pollLines(StringBuilder sb) {
        int lastLineEnd = sb.lastIndexOf("\n");
        if (lastLineEnd > -1) {
            Stream<String> lines = sb.substring(0, lastLineEnd + 1)
                    .lines();
            sb.delete(0, lastLineEnd + 1);
            return lines;
        }
        return Stream.empty();
    }

    @Override
    public void close() throws Exception {
        if (socketPath != null) {
            Files.deleteIfExists(socketPath.toPath());
        }
    }

    private Optional<String> readSocketMessage(SocketChannel channel, ByteBuffer buffer) throws IOException {
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0)
            return Optional.empty();

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);
        return Optional.of(message);
    }
}
