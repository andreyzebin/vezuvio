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
        log.info("Preparing env...");
        tt = new FunnyTerminal(new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime())));
        fm = new FileManager(tt);
        fm.goUp();
        buildRoot = fm.getCurrent();
        git = TestGitUtils.mockRemote(buildRoot, fm);
    }

    @BeforeEach
    void prepare() {
        log.info("Resetting git server...");
        git.reset();
    }

    @Test
    public void testChangesAPI() {
        setupOrigin();

        runApp("leafs use foo/bar");
        runApp("branches use request-001");

        String loc = UUID.randomUUID().toString().substring(1, 5);
        String myPropName = "io.github.vu.myRandomProperty";

        runApp(String.format("properties " + myPropName + " set %s", loc));
        Assertions.assertTrue(runApp("changes list").contains(myPropName));
        runApp("changes merge");
        Assertions.assertEquals("", runApp("changes list"));
        runApp("branches use master");
        Assertions.assertEquals(loc, runApp("properties " + myPropName + " get"));
    }

    @Test
    public void testChangesAPI2() {
        setupOrigin();
        runApp("branches use request-001");

        runApp("leafs use foo/bar");
        String loc = UUID.randomUUID().toString().substring(1, 5);
        String myPropName = "io.github.vu.myRandomProperty";
        runApp(String.format("properties " + myPropName + " set %s", loc));

        runApp("leafs use foo");
        String loc1 = UUID.randomUUID().toString().substring(1, 5);
        String myPropName1 = "io.github.vu.myRandomProperty";
        runApp(String.format("properties " + myPropName1 + " set %s", loc1));

        Assertions.assertTrue(runApp("changes list").contains(myPropName));
        runApp("changes merge");
        Assertions.assertEquals("", runApp("changes list"));
        runApp("branches use master");

        runApp("leafs use foo/bar");
        Assertions.assertEquals(loc, runApp("properties " + myPropName + " get"));
    }

    @Test
    public void testChangesAPI3() {
        setupOrigin();

        runApp("branches use master");
        runApp("branches fork r2");
        Assertions.assertEquals("", runApp("changes explode"));
        String loc = UUID.randomUUID().toString().substring(1, 5);
        String myPropName = "io.github.vu.myRandomProperty";
        runApp("leafs use foo/application1");
        runApp(String.format("properties " + myPropName + " set %s", loc));

        runApp("changes use base master");
        Assertions.assertNotEquals("", runApp("changes explode"));

        runApp("branches use request-001");
        runApp("branches fork request-001.r2");
        runApp("changes rebase r2");
        Assertions.assertEquals("r2", runApp("changes which base"));

        runApp("changes explode");
        runApp("changes merge");
        Assertions.assertEquals("", runApp("changes explode"));

        runApp("changes explode master");
        runApp("changes list master");

        runApp("branches use r2");
        Assertions.assertEquals(loc, runApp("properties " + myPropName + " get"));
    }

    @Test
    public void testPropsAPI2() {
        setupOrigin();
        runApp("branches use request-001");

        runApp("leafs use foo/bar");
        String loc = UUID.randomUUID().toString().substring(1, 5);
        String myPropName = "io.github.vu.myRandomProperty";
        runApp(String.format("properties " + myPropName + " set %s", loc));

        runApp("leafs use foo");
        String loc1 = UUID.randomUUID().toString().substring(1, 5);
        String myPropName1 = "io.github.vu.myRandomProperty";
        runApp(String.format("properties " + myPropName1 + " set %s", loc1));

        runApp("leafs use foo/bar/wow");
        String loc2 = UUID.randomUUID().toString().substring(1, 5);
        String myPropName2 = "io.github.vu.myRandomProperty";
        runApp(String.format("properties " + myPropName2 + " set %s", loc2));

        runApp("properties explode");
        runApp("properties list");
    }


    @Test
    public void testPropertiesAPI() {
        setupOrigin();

        runApp("branches list");
        runApp("branches use master");
        Assertions.assertEquals("master", runApp("branches which"));

        runApp("leafs list");
        runApp("leafs use foo/bar");
        Assertions.assertEquals("foo/bar", runApp("leafs which"));

        String loc = UUID.randomUUID().toString().substring(1, 5);
        String myPropName = "io.github.vu.myRandomProperty";
        runApp(String.format("properties " + myPropName + " set %s", loc));

        Assertions.assertTrue(runApp("properties list").contains(myPropName));
        Assertions.assertEquals(loc, runApp("properties " + myPropName + " get"));

    }

    @Test
    public void testCursorAPI() {
        setupOrigin();

        runApp("branches list");
        runApp("branches use master");
        Assertions.assertEquals("master", runApp("branches which"));

        runApp("leafs list");
        runApp("leafs use foo/bar");
        Assertions.assertEquals("foo/bar", runApp("leafs which"));

        runApp("leafs drop");
        Assertions.assertEquals("null", runApp("leafs which"));
    }

    @Test
    public void testPropertiesAPI_delete() {
        setupOrigin();
        runApp("branches use master");
        runApp("leafs use foo/bar");

        runApp("properties prop1 set " + "def");
        runApp("properties prop2 set " + "def");
        runApp("properties prop1 delete");
        Assertions.assertEquals("def", runApp("properties prop2 get"));
        Assertions.assertEquals("null", runApp("properties prop1 get"));
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
        runApp("properties explode");

    }

    @Test
    void setupOrigin() {
        runApp("origins use ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git");
        runApp("credentials use ssh-agent:~/.ssh/zebin");

        Assertions.assertEquals("ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git",
                runApp("origins which"));
        Assertions.assertEquals("ssh-agent:~/.ssh/zebin",
                runApp("credentials which"));
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
                .paint("un", TerminalPalette.RED_BOLD)
                .paint("delete", TerminalPalette.RED_BOLD)
                .paint("merge", TerminalPalette.YELLOW_BOLD)
                .paint("list", TerminalPalette.YELLOW_BOLD)
                .paint("explode", TerminalPalette.YELLOW_BOLD)
                .paint("use", TerminalPalette.RED_BOLD)
                .paint("which", TerminalPalette.YELLOW_BOLD)
                .paint("commit", TerminalPalette.YELLOW_BOLD)
                .paint("get", TerminalPalette.YELLOW_BOLD)
                .paint("set", TerminalPalette.RED_BOLD)
                .paint("drop", TerminalPalette.RED_BOLD)

                .paint("versions", TerminalPalette.GREEN_BOLD)
                .paint("leafs", TerminalPalette.GREEN_BOLD)
                .paint("branches", TerminalPalette.GREEN_BOLD)
                .paint("changes", TerminalPalette.GREEN_BOLD)
                .paint("properties", TerminalPalette.GREEN_BOLD)
                .paint("origins", TerminalPalette.GREEN_BOLD)
                .paint("credentials", TerminalPalette.GREEN_BOLD)
                .toString();

        log.info(cmdRender);
        System.setProperty("io.github.vezuvio.logger.root.level", "ERROR");
        System.setProperty("VEZUVIO_resources_path", "../tmp");
        System.setProperty("VEZUVIO_repository_location", "/home/andrey/tmp/mock-repo");


        StringBuffer sb = new StringBuffer();
        App app = new App((line) -> {
            sb.append(line);
            sb.append(System.lineSeparator());
        }, (line) -> {
            sb.append(line);
            sb.append(System.lineSeparator());
        }, new Configurations(fm.getCurrent(), terminal, VirtualDirectoryTree.OS_LEVEL_CONF));
        app.run(s.split(" "));
        sb.toString().lines().forEach(cl -> log.info("> " + newCanvas(cl)
                //.fill(TerminalPalette.BLUE)
                .fill(TerminalPalette.BLUE)
                .toString()));
        return decode(sb.toString());
    }

    private static TextBrush newCanvas(String s) {
        TextBrush brush = new TextBrush(s);
        return brush;
    }

}