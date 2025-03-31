package io.github.zebin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class UserScenarioTest {


    @Test
    public void testExecutorAPI() {

        String from = "c1d84a895c10ee5598e28af72658d6a4f1e51923";
        String to = "a4920e25c1327a907e2a3add6dc23cc27d14eacf";


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
        assertEquals("", runApp("use version " + from));
        assertEquals("foo/bar", runApp("get property io.github.gitOps.location"));

        try {
            assertEquals("", runApp("use lock"));
            // do work
            // done work. commit success offset
            assertEquals("", runApp("commit offset " + to));
        } finally {
            assertEquals("", runApp("unuse lock"));
        }

        assertEquals("", runApp("use version " + to));
        assertEquals("56", runApp("get property io.github.gitOps.location"));
        assertEquals("", runApp("unuse version"));

        // return back
        try {
            assertEquals("", runApp("use lock"));

            assertEquals("", runApp("commit offset " + from));
        } finally {
            assertEquals("", runApp("unuse lock"));
        }

        assertEquals(from, runApp("get queue offset"));
        // runApp("get queue remaining").lines().forEach(log::info);
        assertEquals(to, runApp("get queue next"));
        assertEquals("""
                foo/bar/conf.properties
                foo/bar/lock.json
                foo/conf.properties""", runApp("get queue next-changes"));

    }

    private static String runApp(String s) {
        log.info(">vezuvio {}", s);
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
        return sb.toString().lines().collect(Collectors.joining(System.lineSeparator()));
    }

}