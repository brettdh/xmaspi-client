package edu.umich.xmaspi.client;

import java.io.IOException;

import android.test.InstrumentationTestCase;

public class BulbsTest extends InstrumentationTestCase {
    private Remote remote;
    private Bulbs bulbs;
    
    public void setUp() {
        try {
            remote = new Remote("testing", "10.0.2.2");
            bulbs = new Bulbs(remote);
            bulbs.clear();
        } catch (IOException e) {
            fail("Failed to connect");
        }
    }
    
    public void tearDown() {
        try {
            remote.done();
            bulbs = null;
            remote = null;
        } catch (IOException e) {
            fail("Failed to shut down");
        }
    }

    public void testBulbs() throws IOException, InterruptedException {
        int[] additive = new int[]{3, 3, 3, 255};
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 100; ++j) {
                bulbs.add(j, additive);
                bulbs.render();
                Thread.sleep(2);
            }
        }
    }
}
