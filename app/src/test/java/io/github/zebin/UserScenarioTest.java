package io.github.zebin;

import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.frontend.TerminalPalette;
import io.github.zebin.javabash.frontend.TextBrush;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.BashUtils;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
class UserScenarioTest {
    static TextTerminal tt;
    static FileManager fm;
    static PosixPath buildRoot;
    static TestGitUtils.MutableMock git;

    @BeforeAll
    static void prepareAll() {
        tt = new FunnyTerminal(new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime())));
        fm = new FileManager(tt);
        fm.goUp();
        buildRoot = fm.getCurrent();
        git = TestGitUtils.mockRemote(buildRoot, fm);
    }

    @BeforeEach
    void prepare() {
        git.reset();
    }


    @Test
    public void testPropertiesAPI() {
        setupOrigin();

        runApp("branches list");
        runApp("branches use master");
        Assertions.assertEquals("master", runApp("branches.current which"));

        runApp("leafs list");
        runApp("leafs use foo/bar");
        Assertions.assertEquals("foo/bar", runApp("leafs.current which"));

        runApp("properties list");
        runApp("properties io.github.gitOps.location get");
        String loc = "https://abc.def/ghi";
        runApp("properties io.github.gitOps.location set " + loc);
        Assertions.assertEquals(loc, runApp("properties io.github.gitOps.location get"));
    }

    @Test
    public void testChangesAPI() {
        setupOrigin();
        runApp("leafs use foo/bar");

        runApp("branches use master");
        runApp("properties io.github.gitOps.location get");

        runApp("branches use request-001");
        runApp("changes list");
        runApp("changes merge");
        Assertions.assertEquals("", runApp("changes list"));

        runApp("branches use master");
        runApp("properties io.github.gitOps.location get");
    }

    @Test
    public void testLockAPI() {
        setupOrigin();
        runApp("leafs use foo/bar");

        runApp("branches use master");
        runApp("properties io.github.gitOps.location get");

        String executorId = UUID.randomUUID().toString().substring(0, 5);
        runApp("leafs use foo/bar/.state");
        runApp(String.format("properties %s.lock.createdUnixTs set %d", App.IO_GITHUB_VEZUVIO, System.currentTimeMillis()));
        runApp(String.format("properties %s.lock.executorId set %s", App.IO_GITHUB_VEZUVIO, executorId));
        runApp(String.format("properties %s.request.branchName set %s", App.IO_GITHUB_VEZUVIO, "request-001"));
        runApp(String.format("properties %s.request.status set %s", App.IO_GITHUB_VEZUVIO, "STARTED"));
        runApp("properties list");

        runApp("branches use request-001");
        runApp("changes list");
        runApp("changes merge");
        Assertions.assertEquals("", runApp("changes list"));

        runApp("branches use master");
        runApp("properties io.github.gitOps.location get");

        runApp(String.format("properties io.github.vezuvio.lock.createdUnixTs delete", System.currentTimeMillis()));
        runApp(String.format("properties io.github.vezuvio.lock.executorId delete", executorId));
        runApp(String.format("properties io.github.vezuvio.request.status set %s", "FINISHED"));
        runApp("properties list");

    }

    @Test
    void setupOrigin() {
        runApp("state.origin.url use ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git");
        runApp("state.origin.auth use ssh-agent:~/.ssh/zebin");

        Assertions.assertEquals("ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git",
                runApp("state.origin.url which"));
        Assertions.assertEquals("ssh-agent:~/.ssh/zebin",
                runApp("state.origin.auth which"));
    }

    private static String decode(String s) {
        return s.lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String runApp(String s) {
        TextTerminal terminal = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
        );
        FileManager fm = new FileManager(terminal);
        fm.goUp();

        String cmdRender = newCanvas(String.format("vezuvio %s", s))
                //.fill(TerminalPalette.BLUE)
                .paint("vezuvio", TerminalPalette.MAGENTA_BACKGROUND)
                .paint("offset", TerminalPalette.GREEN_BOLD_BRIGHT)

                .paint("list", TerminalPalette.YELLOW_BOLD)
                .paint("un", TerminalPalette.RED_BOLD)
                .paint("use", TerminalPalette.YELLOW_BOLD)
                .paint("commit", TerminalPalette.YELLOW_BOLD)
                .paint("get", TerminalPalette.YELLOW_BOLD)
                .paint("set", TerminalPalette.YELLOW_BOLD)

                .paint("list", TerminalPalette.YELLOW_BOLD)
                .paint("use", TerminalPalette.YELLOW_BOLD)
                .paint("which", TerminalPalette.YELLOW_BOLD)
                .paint("commit", TerminalPalette.YELLOW_BOLD)
                .paint("get", TerminalPalette.YELLOW_BOLD)
                .paint("set", TerminalPalette.YELLOW_BOLD)

                .paint("offset", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("versions", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("version", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("leafs", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("leaf", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("lock", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("branches", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("branch", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("queue", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("properties", TerminalPalette.GREEN_BOLD_BRIGHT)
                .paint("property", TerminalPalette.GREEN_BOLD_BRIGHT)
                .toString();

        log.info("> " + cmdRender);
        System.setProperty("logger.root.level", "ERROR");
        System.setProperty("VEZUVIO_resources_path", "../tmp");
        System.setProperty("VEZUVIO_repository_location", "/home/andrey/tmp/mock-repo");


        StringBuffer sb = new StringBuffer();
        App app = new App((line) -> {
            sb.append(line);
            sb.append(System.lineSeparator());
        }, (line) -> {
            sb.append(line);
            sb.append(System.lineSeparator());
        }, new Configurations(fm.getCurrent(), terminal));
        app.run(s.split(" "));
        sb.toString().lines().forEach(cl -> log.info("> " + newCanvas(cl)
                //.fill(TerminalPalette.BLUE)
                .fill(TerminalPalette.YELLOW_BOLD)
                .toString()));
        return decode(sb.toString());
    }

    private static TextBrush newCanvas(String s) {
        TextBrush brush = new TextBrush(s);
        return brush;
    }

}