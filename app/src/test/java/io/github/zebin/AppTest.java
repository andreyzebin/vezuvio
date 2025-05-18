package io.github.zebin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void testFixWithPath() {
        Assertions.assertEquals(
                "/c/Users/amzabebenin", LineStreamIO.fixWinPath("C:/Users/amzabebenin"));

        Assertions.assertEquals(
                "/c/Users/amzabebenin", LineStreamIO.fixWinPath("/c/Users/amzabebenin"));

        Assertions.assertEquals(
                "Users/amzabebenin", LineStreamIO.fixWinPath("Users/amzabebenin"));
    }

}