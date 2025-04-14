package io.github.zebin;

import io.github.zebin.javabash.sandbox.PosixPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class PosixWINTest {

    @Test
    void test() {
        String osNameLowercased = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osNameLowercased.startsWith("windows");
        if (isWindows) {
            Assertions.assertEquals(Path.of("C:", "Users"), PosixPath.ofPosix("/c/Users").toPath());
            Assertions.assertEquals(Path.of("Users"), PosixPath.ofPosix("Users").toPath());
        } else {
            Assertions.assertEquals(Path.of("/", "c", "Users"), PosixPath.ofPosix("/c/Users").toPath());
            Assertions.assertEquals(Path.of("Users"), PosixPath.ofPosix("Users").toPath());
        }
    }
}
