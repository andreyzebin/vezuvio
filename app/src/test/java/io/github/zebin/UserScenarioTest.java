package io.github.zebin;

import io.github.zebin.javabash.frontend.TerminalPalette;
import io.github.zebin.javabash.frontend.TextBrush;
import io.github.zebin.javabash.sandbox.PosixPath;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class UserScenarioTest {


    @Test
    public void testExecutorAPI() {
        assertEquals("""
                        cf57ed6a9de2a7064ef611a9cc28c9ec67dc45e5
                        88b6a30e384cda9c8eb11aa0cf05f2be09949f2e
                        c1d84a895c10ee5598e28af72658d6a4f1e51923""",
                App.lastElements(runApp("list versions").lines(), 3)
                        .collect(Collectors.joining(System.lineSeparator()))
        );
        assertEquals("""
                foo
                foo/bar
                foo/bar/lol""", runApp("list leafs"));

        assertEquals("", runApp("use branch master"));
        assertEquals("", runApp("use leaf foo/bar"));

        // executor checks for newer versions and decides to execute work with respect to newer
        // property value io.github.gitOps.location
        // use current offset
        // assertEquals("88b6a", runApp("get queue offset"));

        // get current full state
        String finallyHash = "c1d84a895c10ee5598e28af72658d6a4f1e51923";
        String to = "a4920e25c1327a907e2a3add6dc23cc27d14eacf";
        String from = finallyHash;

        // setup
        title1("1. Setup");
        commit(finallyHash);



        // use queue     [dispatcher]
        // queue next    hash          -> [hash]
        // # list affected projects
        //
        // --start
        // use   queue   [foo/bar]
        // queue next    hash          -> [hash]
        // queue acquire lock          -> [lockId]
        // queue next    changes       -> list<change>
        // # do work
        // queue commit  offset [hash]
        // queue release lock [lockId]
        // --loop
        List<PosixPath> projectsAffected = poll(finallyHash, from, to);

        title1("2. Projects affected: ");
        projectsAffected.forEach(p -> log.info(" - {}", p));
        title1("2.1. Changes of: foo/bar...");
        Map<String, String> state = getState("foo/bar", from);
        title1("2.2. Executor's current state for Project foo/bar: ");
        state.forEach((k, ch) -> log.info(" - {} = {}", k, ch));
        Map<String, Change<String>> changeSet = getChangeSet("foo/bar", from, to);
        title1("2.3. Executor's next updates for Project foo/bar: ");
        changeSet.forEach((k, ch) -> log.info(" - {} = {} -> {}", k, ch.before, ch.after));

        title1("3. Processing changes of: foo/bar...");
        title1("4. Committing changes of: foo/bar...");
        commit(to);
        title1("5. Asserting result state");
        assertState(to);
    }

    private static void title1(String s) {
        log.info(newCanvas(s).fill(TerminalPalette.BLUE).toString());
    }

    private static void assertState(String to) {
        assertEquals("", runApp("use version " + to));
        assertEquals("56", runApp("get property io.github.gitOps.location"));
        assertEquals("", runApp("unuse version"));
        log.info("Executor's current value is 56");
    }

    private static void commit(String to) {
        try {
            assertEquals("", runApp("use lock"));
            // do work
            // done work. commit success offset
            assertEquals("", runApp("commit offset " + to));
        } finally {
            assertEquals("", runApp("unuse lock"));
        }
    }

    private static List<PosixPath> poll(String finallyHash, String from, String to) {
        // return back
        assertEquals(from, runApp("get queue offset"));
        // runApp("get queue remaining").lines().forEach(log::info);
        assertEquals(to, runApp("get queue next"));
        assertEquals("""
                foo/bar/conf.properties
                foo/bar/lock.json
                foo/conf.properties""", runApp("get queue next-changes"));


        log.info("Executor's current offset for foo/bar is {}", from);

        String next = runApp("get queue next");
        log.info("Executor's next offset for foo/bar is {}", next);

        List<PosixPath> nextChanges = runApp("get queue next-changes").lines()
                .map(PosixPath::ofPosix)
                .filter(pp -> pp.getEnd().equals("conf.properties"))
                .toList();
        // parent dir;
        List<PosixPath> allProjects = runApp("list leafs")
                .lines()
                .map(PosixPath::ofPosix).toList();

        List<PosixPath> projectsAffected = allProjects.stream()
                .filter(prj -> nextChanges.stream()
                        .map(PosixPath::descend)
                        .anyMatch(prj::startsWith))
                .toList();

        log.info("Property files changed: ");
        nextChanges.forEach(p -> log.info(" - {}", p));
        return projectsAffected;
    }

    private static Map<String, String> getState(String project, String from) {
        assertEquals("", runApp(String.format("use leaf %s", project)));

        assertEquals("", runApp("use version " + from));
        List<String> listPropertiesBefore = runApp("list properties").lines().toList();
        Map<String, String> before = new HashMap<>();
        //
        listPropertiesBefore.forEach(cp -> before.put(cp, runApp(String.format("get property %s", cp))));
        return before;
    }

    private static Map<String, Change<String>> getChangeSet(String project, String from, String to) {
        return diffChanged(getState(project, from), getState(project, to));
    }

    public static <K, V> Map<K, Change<V>> diffChanged(Map<K, V> left, Map<K, V> right) {
        Map<K, Change<V>> difference = new HashMap<>();


        right.forEach((pp, v) -> difference.put(pp, Change.<V>builder()
                .before(left.get(pp))
                .after(right.get(pp))
                .build()));

        left.forEach((pp, v) -> difference.computeIfAbsent(pp, (ppp) -> Change.<V>builder()
                .before(left.get(pp))
                .after(null)
                .build()));

        return difference;
    }

    @Data
    @Builder
    public static class Change<T> {
        private final T before;
        private final T after;
    }

    private static String decode(String s) {
        return s.lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String runApp(String s) {
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

        //log.info("> " + cmdRender);
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
        });
        app.run(s.split(" "));
        return decode(sb.toString());
    }

    private static TextBrush newCanvas(String s) {
        TextBrush brush = new TextBrush(s);
        return brush;
    }

}