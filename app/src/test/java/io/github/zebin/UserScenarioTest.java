package io.github.zebin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class UserScenarioTest {


    @Test
    public void test() {

        assertEquals("HEAD", runApp("list branches"));
        assertEquals("""
                foo
                foo/bar
                foo/bar/lol""", runApp("list leafs"));

        assertEquals("", runApp("use branch master"));
        assertEquals("", runApp("use leaf foo/bar"));
        assertEquals("", runApp("unuse version"));

        // get current property value for executor
        assertEquals("88b6a30e384cda9c", runApp("get offset"));
        assertEquals("", runApp("use version 88b6a30e384cda9c"));
        assertEquals("foo/bar", runApp("get property io.github.gitOps.location"));

        // executor checks for newer versions and decides to execute work with respect to newer
        // property value io.github.gitOps.location
        // Assertions.assertEquals("88b6a30e384cda9c", runApp("list versions"));
        assertEquals("", runApp("use version 526d3bc58db"));
        assertEquals("56", runApp("get property io.github.gitOps.location"));

        // do work
        // done work. commit success offset
        assertEquals("", runApp("use lock"));
        assertEquals("", runApp("commit offset 526d3bc58db"));
        assertEquals("", runApp("unuse lock"));
        assertEquals("88b6a30e384cda9c", runApp("get offset"));

        assertEquals("", runApp("use version 526d3bc58db"));
        assertEquals("56", runApp("get property io.github.gitOps.location"));
        assertEquals("", runApp("unuse version"));

        // return back
        assertEquals("", runApp("use lock"));
        assertEquals("", runApp("commit offset 88b6a30e384cda9c"));
        assertEquals("", runApp("unuse lock"));
        assertEquals("88b6a30e384cda9c", runApp("get offset"));

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