package io.github.zebin;

import io.github.zebin.javabash.sandbox.PosixPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class PosixWINTest {

    @Test
    void test() {
        Assertions.assertEquals(Path.of("/c/Users"), PosixPath.ofPosix("/c/Users").toPath());
    }
}
