package edu.umich.xmaspi.client;

import java.io.IOException;

import android.test.InstrumentationTestCase;

public class RemoteTest extends InstrumentationTestCase {
    private Remote remote;
    
    public void setUp() {
        try {
            remote = new Remote("testing", "10.0.2.2");
        } catch (IOException e) {
            fail("Failed to connect");
        }
    }
    
    public void tearDown() {
        try {
            remote.done();
            remote = null;
        } catch (IOException e) {
            fail("Failed to shut down");
        }
    }
    
    public void testWriteLed() throws IOException, InterruptedException {
        for (int brightness = 0; brightness <= 255; brightness += 255) {
            for (int i = 0; i < 100; ++i) {
                remote.writeLed(i, brightness, 15, 15, 15);
                Thread.sleep(5);
            }
        }
        remote.writeLed(100, 0, 0, 0, 0);
        remote.done();
    }
    
    public void testBusyWait() throws IOException {
        for (int i = 0; i < 3; ++i) {
            for (int led = 0; led < 100; ++led) {
                if (led % 2 == (i % 2)) { // RGRGRG... or GRGRGR...
                    remote.writeLed(led, 255, 15, 0, 0);
                } else {
                    remote.writeLed(led, 255, 0, 15, 0);
                }
            }
            remote.busyWait(1.0);
        }
    }
}
