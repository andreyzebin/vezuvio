package io.github.zebin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void testFixWithPath() {
        Assertions.assertEquals(
                "/c/Users/amzabebenin", App.fixWinPath("C:/Users/amzabebenin"));

        Assertions.assertEquals(
                "/c/Users/amzabebenin", App.fixWinPath("/c/Users/amzabebenin"));

        Assertions.assertEquals(
                "Users/amzabebenin", App.fixWinPath("Users/amzabebenin"));
    }

}