package it.unica.tcs.lib.model;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParticipantTest {

    Participant A = new Participant("Alice") {
        @Override
        public void run() {
        }
    };

    Participant B = new Participant("Bob") {
        @Override
        public void run() {
        }
    };

    @Before
    public void setup() {
    }

    @After
    public void clean() {
    }

    @Test
    public void testName() {
        assertEquals("Alice", A.getName());
        assertEquals("Bob", B.getName());
    }

    @Test
    public void testSendReceive() {
        String msgA = "Hello!";

        A.parallel(() -> {
            A.send(msgA, B);
        });

        String msgB = B.receive(A);
        assertEquals(msgA, msgB);
    }

    @Test
    public void testSelfSendReceive() {

        String msgA = "Hello!";

        A.parallel(() -> {
            A.send(msgA, A);
        });

        String msgB = A.receive(A);
        assertEquals(msgA, msgB);
    }
}
