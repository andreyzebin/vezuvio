package io.github.zebin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class UserScenarioTest {


    @Test
    public void test() {



        assertEquals("""
                cf57ed6a9de2a7064ef611a9cc28c9ec67dc45e5
                88b6a30e384cda9c8eb11aa0cf05f2be09949f2e
                c1d84a895c10ee5598e28af72658d6a4f1e51923""",
                lastElements(runApp("list versions").lines(), 3)
                .collect(Collectors.joining(System.lineSeparator()))
        );

        assertEquals("HEAD", runApp("list branches"));
        assertEquals("""
                foo
                foo/bar
                foo/bar/lol""", runApp("list leafs"));

        assertEquals("", runApp("use branch master"));
        assertEquals("", runApp("use leaf foo/bar"));

        // executor checks for newer versions and decides to execute work with respect to newer
        // property value io.github.gitOps.location
        // Assertions.assertEquals("88b6a", runApp("list versions"));
        // use current offset
        assertEquals("", runApp("use version 88b6a"));
        assertEquals("foo/bar", runApp("get property io.github.gitOps.location"));

        try {
            assertEquals("", runApp("use lock"));
            // do work
            // done work. commit success offset
            assertEquals("", runApp("commit offset 526d3"));
        } finally {
            assertEquals("", runApp("unuse lock"));
        }

        // assertEquals("88b6a", runApp("get offset"));

        assertEquals("", runApp("use version 526d3"));
        assertEquals("56", runApp("get property io.github.gitOps.location"));
        assertEquals("", runApp("unuse version"));

        // return back
        try {
            assertEquals("", runApp("use lock"));

            assertEquals("", runApp("commit offset 88b6a"));
        } finally {
            assertEquals("", runApp("unuse lock"));
        }

        assertEquals("88b6a", runApp("get offset"));
    }

    private static Stream<String> lastElements(Stream<String> l, int n) {
        LinkedList<String> ll = new LinkedList<String>();

        l.forEach(el -> {
            ll.addFirst(el);

            if (ll.size() > n) {
                ll.removeLast();
            }
        });


        return ll.stream();
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