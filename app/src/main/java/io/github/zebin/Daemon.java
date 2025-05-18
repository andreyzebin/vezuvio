package io.github.zebin;

import io.github.zebin.javabash.sandbox.PosixPath;
import jdk.net.ExtendedSocketOptions;
import jdk.net.UnixDomainPrincipal;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class Daemon implements AutoCloseable {
    private final LineStreamIO app;
    private PosixPath socketFile;
    private ServerSocketChannel serverSocket;
    private final Configurations cfg;

    static Map<SocketChannel, Integer> byteCounter = new HashMap<>();
    static Map<SocketChannel, StringBuilder> lineInputBuffer = new HashMap<>();

    public Daemon(LineStreamIO app, Configurations cfg) {
        this.app = app;
        this.cfg = cfg;
    }

    private void bind(Selector selector) {
        socketFile = cfg.getVezuvioHome().climb("daemon.socket");
        UnixDomainSocketAddress serverListenAddress = UnixDomainSocketAddress.of(socketFile.toPath());
        socketFile.toPath().toFile().deleteOnExit();
        try {
            serverSocket = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            serverSocket.configureBlocking(false);

            serverSocket.register(selector, SelectionKey.OP_ACCEPT, null);
            serverSocket.bind(serverListenAddress);

            log.info("Server: Listening on " + serverSocket.getLocalAddress());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while binding daemon's socket %s", socketFile), e);
        }
    }

    public void doServer() throws IOException {
        Selector selector = Selector.open();
        bind(selector);

        ByteBuffer readBuf = ByteBuffer.allocate(64 * 1024);
        int nextConnectionId = 1;
        while (true) {
            selector.select();
            var keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                try {
                    SelectableChannel c = key.channel();
                    if (c instanceof ServerSocketChannel server) {
                        var ch = server.accept();
                        var userid = "";
                        if (server.getLocalAddress() instanceof UnixDomainSocketAddress) {

                            // An illustration of additional capability of UNIX
                            // channels; it's not required behavior.

                            UnixDomainPrincipal pr = ch.getOption(ExtendedSocketOptions.SO_PEERCRED);
                            userid = "user: " + pr.user().toString() + " group: " +
                                    pr.group().toString();
                        }
                        ch.configureBlocking(false);
                        byteCounter.put(ch, 0);
                        lineInputBuffer.put(ch, new StringBuilder());
                        System.out.printf("Server: new connection\n\tfrom {%s}\n", ch.getRemoteAddress());
                        System.out.printf("\tConnection id: %s\n", nextConnectionId);
                        if (!userid.isEmpty()) {
                            System.out.printf("\tpeer credentials: %s\n", userid);
                        }
                        System.out.printf("\tConnection count: %d\n", byteCounter.size());
                        ch.register(selector, SelectionKey.OP_READ, nextConnectionId++);
                    } else {
                        var ch = (SocketChannel) c;
                        int id = (Integer) key.attachment();
                        int bytes = byteCounter.get(ch);
                        StringBuilder line = lineInputBuffer.get(ch);
                        readBuf.clear();
                        int n = ch.read(readBuf);
                        if (n < 0) {
                            String remote = ch.getRemoteAddress().toString();
                            System.out.printf("Server: closing connection\n\tfrom: {%s} Id: %d\n", remote, id);
                            System.out.printf("\tBytes received: %d\n", bytes);
                            byteCounter.remove(ch);
                            ch.close();
                        } else {
                            readBuf.flip();
                            bytes += n;
                            byteCounter.put(ch, bytes);
                            display(ch, readBuf, id);


                            byte[] bytesBuf = new byte[n];
                            readBuf.get(bytesBuf);
                            String message = new String(bytesBuf);
                            line.append(message);

                            pollLines(line).forEach(ll -> {
                                app.setStdOUT(f -> {
                                    try {
                                        log.info("Sending answer {}", f);
                                        ch.write(ByteBuffer.wrap(f.getBytes(StandardCharsets.UTF_8)));
                                        ch.write(ByteBuffer.wrap("\n".getBytes(StandardCharsets.UTF_8)));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                app.run(ll.split(" "));

                            });


                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            ;
            keys.clear();
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
        if (socketFile != null) {
            log.info("deleting {}...", socketFile);
            Files.deleteIfExists(socketFile.toPath());
        }
    }

    private static void display(SocketChannel ch, ByteBuffer readBuf, int id)
            throws IOException {
        System.out.printf("Server: received %d bytes from: {%s} Id: %d\n",
                readBuf.remaining(), ch.getRemoteAddress(), id);
    }
}
