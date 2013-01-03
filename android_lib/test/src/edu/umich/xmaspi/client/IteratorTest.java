package edu.umich.xmaspi.client;

import java.io.IOException;
import java.util.Random;

import edu.umich.xmaspi.client.Iterator.ColorIterator;
import edu.umich.xmaspi.client.Iterator.PositionIterator;

import android.test.InstrumentationTestCase;

public class IteratorTest extends InstrumentationTestCase {
    private Remote remote;
    private Bulbs bulbs;
    private Iterator iterator;
    
    public void setUp() {
        try {
            remote = new Remote("testing", "10.0.2.2");
            bulbs = new Bulbs(remote);
            bulbs.clear();
            iterator = new Iterator(bulbs);
        } catch (IOException e) {
            fail("Failed to connect");
        }
    }
    
    public void tearDown() {
        try {
            remote.done();
            bulbs = null;
            remote = null;
            iterator = null;
        } catch (IOException e) {
            fail("Failed to shut down");
        }
    }

    public void testRandomWalk() throws IOException, InterruptedException {
        Random rand = new Random(System.currentTimeMillis());
        
        double freq = 0.01;
        int steps = (int) (5 / freq);
        int pos = 50;
        for (int i = 0; i < steps; ++i) {
            ColorIterator redToWhite = 
                Iterator.makeFaderIterator(Bulbs.RED.toArray(),
                                           Bulbs.WHITE.toArray(), 10);
            ColorIterator whiteToBlack = 
                    Iterator.makeFaderIterator(Bulbs.WHITE.toArray(),
                                               Bulbs.BLACK.toArray(), 100);
            iterator.add(Iterator.makeFixedIterator(pos),
                         Iterator.chain(redToWhite, whiteToBlack));
            iterator.render();
            
            int step = 1 - rand.nextInt(2) * 2; // 1 or -1
            pos += step;
            if (pos < 0) {
                pos = Bulbs.COUNT - 1;
            }
            if (pos == Bulbs.COUNT) {
                pos = 0;
            }
            
            Thread.sleep((int) (freq * 1000));
        }
    }
    
    public void testAllBulbs() throws IOException, InterruptedException {
        double period = 0.01;
        double steps = 5 / period;
        
        for (int i = 0; i < Bulbs.COUNT; ++i) {
            PositionIterator pos = Iterator.makeFixedIterator(i);
            ColorIterator color = null;
            if (i % 2 == 0) {
                color = Iterator.makeFaderIterator(Bulbs.RED.toArray(), 
                                                   Bulbs.GREEN.toArray(), (int) steps);
            } else {
                color = Iterator.makeFaderIterator(Bulbs.GREEN.toArray(), 
                                                   Bulbs.RED.toArray(), (int) steps);
            }
            iterator.add(pos, color);
        }
        
        for (int i = 0; i < steps; ++i) {
            iterator.render();
            Thread.sleep((int) (period * 1000));
        }
    }
}
