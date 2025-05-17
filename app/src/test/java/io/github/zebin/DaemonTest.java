package io.github.zebin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

class DaemonTest {


    @Test
    public void test1() {
        StringBuilder sb =new StringBuilder("foo\nbar\nwow");
        Assertions.assertEquals("foo|bar",Daemon.pollLines(sb)
                .collect(Collectors.joining("|")));

        Assertions.assertEquals("wow", sb.toString());

    }

    @Test
    public void test() throws IOException, InterruptedException {
        Path socketPath = Path
                .of(System.getProperty("user.home"))
                .resolve("vezuvio.socket");

        try {
            UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

            ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            serverChannel.bind(socketAddress);

            send(socketAddress);

            SocketChannel serverAccept = serverChannel.accept();

            while (true) {
                readSocketMessage(serverAccept)
                        .ifPresent(msg -> System.out.printf("[Client message] %s", msg));
                Thread.sleep(100);
            }
        } finally {
            Files.deleteIfExists(socketPath);
        }

    }

    private static void send(UnixDomainSocketAddress socketAddress) throws IOException {
        SocketChannel channel = SocketChannel
                .open(StandardProtocolFamily.UNIX);
        channel.connect(socketAddress);
        String message = "Hello from Baeldung Unix domain socket article";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
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