package com.neueda.leap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Trivial passing test used to prove Maven + JDK + JUnit are wired together correctly. */
// a small edit to trigger a rebuild in the CI pipeline THRICE
class EnvironmentCheckTest {

    @Test
    void toolchainIsWorking() {
        assertEquals(4, 2 + 2);
    }
}
