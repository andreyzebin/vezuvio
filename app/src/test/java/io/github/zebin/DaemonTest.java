package io.github.zebin;

import io.github.andreyzebin.gitSql.cache.FileManagerCacheProxy;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.AllFileManager;
import io.github.zebin.javabash.sandbox.BashUtils;
import io.github.zebin.javabash.sandbox.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
class DaemonTest {


    @Test
    public void test1() {
        StringBuilder sb = new StringBuilder("foo\nbar\nwow");
        Assertions.assertEquals("foo|bar", Daemon.pollLines(sb)
                .collect(Collectors.joining("|")));

        Assertions.assertEquals("wow", sb.toString());

    }

    @Disabled
    @Test
    public void testDaemon() {
        LineStreamIO app = new LineStreamIO() {

            @Override
            void run(String[] args) {
                log.info("\tReceived args: {}", String.join(" ", args));
                stdOUT.accept("Hello, my friend!");
            }
        };

        Configurations cfg = getConfigurations();

        try (Daemon daemon = new Daemon(app, cfg)) {
            daemon.doServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Configurations getConfigurations() {
        TextTerminal terminal = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
        );
        AllFileManager cfgFiles = new FileManager(terminal);
        cfgFiles = FileManagerCacheProxy.cachedProxy(cfgFiles, new AtomicReference<>("kk"));
        cfgFiles.goUp();
        return new Configurations(
                cfgFiles.getCurrent(),
                terminal,
                VirtualDirectoryTree.WORKDIR_LEVEL_CONF);
    }
}