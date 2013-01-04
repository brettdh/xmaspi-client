package edu.umich.xmaspi.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import android.util.Log;

public class Iterator {
    private Bulbs bulbs;
    
    public Iterator(Bulbs bulbs) {
        this.bulbs = bulbs;
    }
    
    public static abstract class PositionIterator implements java.util.Iterator<Double> {
        public void remove() { /* do nothing */}
    }
    
    // must return {r,g,b,a} with range {0-15,0-15,0-15,0-255}
    public static abstract class ColorIterator implements java.util.Iterator<int[]> {
        public void remove() { /* do nothing */}
    }

    private class IterPair {
        PositionIterator position;
        ColorIterator color;
        
        private IterPair(PositionIterator position, ColorIterator color) {
            this.position = position;
            this.color = color;
        }
    }
    
    private List<IterPair> iterators = new ArrayList<IterPair>();
    
    /**
     * Add a (position, color) iterator pair.
     * On each call to render, a new (position, color) pair 
     * will be rendered on the strand.
     * 
     * @param position position-generating iterator to add
     * @param color color-generating iterator to add
     */
    public void add(PositionIterator position, ColorIterator color) {
        iterators.add(new IterPair(position, color));
    }
    
    /** Returns true iff there are no iterators to render.
     */
    public boolean isEmpty() {
        return iterators.isEmpty();
    }
    
    private class BulbUpdate {
        int position;
        int[] color;
        private BulbUpdate(int position, int[] color) {
            this.position = position;
            this.color = color;
        }
    }
    
    /**
     * Generate the next set of color values using the 
     * previously add()ed iterators.
     * 
     * @throws IOException if connection to driver fails. 
     */
    public void render() throws IOException {
        List<BulbUpdate> frame = new ArrayList<BulbUpdate>();
        
        List<IterPair> survivors = new ArrayList<IterPair>();
        for (IterPair pair : iterators) {
            PositionIterator position = pair.position;
            ColorIterator color = pair.color;
            
            if (!position.hasNext() || !color.hasNext()) {
                continue;
            }
            survivors.add(pair);
            
            double nextPos = position.next();
            int[] nextColor = color.next();
            double fpart = nextPos - Math.floor(nextPos);
            int right_brightness = (int) (fpart * 255);
            int left_brightness = 255 - right_brightness;
            if (right_brightness > 0) {
                // Update two bulbs, adjusting the brightness value
                //  as if the color is in between them
                right_brightness = (int) (fpart * nextColor[3]);
                left_brightness = nextColor[3] - right_brightness;
                
                int rightPos = ((int) Math.floor(nextPos) + 1) % Bulbs.COUNT;
                int[] rightColor = Arrays.copyOf(nextColor, 4);
                rightColor[3] = right_brightness;
                nextColor[3] = left_brightness;
                
                bulbs.set(rightPos, rightColor);
            }
            bulbs.set((int) Math.floor(nextPos), nextColor);
        }
        iterators = survivors;
        bulbs.render();
    }
    
    
    // positional iterators
    
    /**
     * Returns a new iterator that returns the same position forever.
     * @param pos Bulb position to be returned
     */
    public static PositionIterator makeFixedIterator(final double pos) {
        return new PositionIterator() {
            public boolean hasNext() { return true; }
            public Double next() { return pos; }
        };
    }
    
    /**
     * Returns a new iterator that returns steps of size delta from
     * the starting position pos.
     * @param pos Starting position - first value returned
     * @param delta Size of step taken with each call to next()
     */
    public static PositionIterator makeInertialIterator(final double pos, final double delta) {
        return makeGravityIterator(pos, delta, 0.0);
    }
    
    private static class GravityIterator extends PositionIterator {
        double iterPos;
        double iterVelocity;
        double iterAccel;
        
        private GravityIterator(double pos, double acceleration, double velocity) {
            iterPos = pos;
            iterVelocity = velocity;
            iterAccel = acceleration;
        }
        
        public boolean hasNext() { return true; }
        
        public Double next() {
            double prevPos = iterPos;
            iterPos = (iterPos + iterVelocity) % Bulbs.COUNT;
            iterVelocity += iterAccel;
            return prevPos;
        }
    }

    /**
     * Return a new iterator that returns positions starting at pos, 
     * moving with a given velocity and acceleration.  
     * @param pos Starting position
     * @param velocity Velocity of position movement
     * @param acceleration Rate of change of velocity
     */
    public static PositionIterator makeGravityIterator(final double pos, 
                                                       final double velocity, 
                                                       final double acceleration) {
        return new GravityIterator(pos, acceleration, velocity);
    }
    
    /**
     * Return a Gravity iterator that bounces at the ends instead of wrapping.
     * @param pos Starting position
     * @param velocity Velocity of position movement
     * @param acceleration Rate of change of velocity
     * @return
     */
    public static PositionIterator makeBouncyIterator(final double pos, 
                                                      final double velocity,
                                                      final double acceleration) {
        return new PositionIterator() {
            GravityIterator wrappedIter = new GravityIterator(pos, velocity, acceleration);
            
            public boolean hasNext() { return true; }
            public Double next() {
                double max = Bulbs.COUNT - 1;
                if (wrappedIter.iterPos > max) {
                    wrappedIter.iterPos = 2.0*max - wrappedIter.iterPos;
                    wrappedIter.iterVelocity = -wrappedIter.iterVelocity;
                }
                return wrappedIter.next();
            }
        };
    }
    
    
    // color iterators
    
    /**
     * Return new iterator that returns the given color forever.
     * @param color Color to maintain forever
     */
    public static ColorIterator makeSolidIterator(final int[] color) {
        return new ColorIterator() {
            public boolean hasNext() { return true; }
            public int[] next() {
                logColor("SolidIterator", color);
                return color;
            }
        };
    }
    
    public static ColorIterator makeFaderIterator(final int[] startColor,
                                                  final int[] endColor,
                                                  final int steps) {
        return new ColorIterator() {
            private int[] curColor = Arrays.copyOf(startColor, 4);
            private double[] floatColor = new double[4];
            private double[] delta = new double[4];
            private int step = 0;
            
            {
                for (int i = 0; i < 4; ++i) {
                    floatColor[i] = curColor[i]; 
                    delta[i] = ((endColor[i] - startColor[i]) * (1.0 / steps));
                }
            }
            
            public boolean hasNext() { return step <= steps; }
            public int[] next() {
                int[] prevColor = Arrays.copyOf(curColor, 4);
                for (int i = 0; i < 4; ++i) {
                    floatColor[i] += delta[i];
                    curColor[i] = (int) floatColor[i];
                }
                ++step;
                logColor("FaderIterator", prevColor);
                return prevColor;
            }
        };
    }
    
    private static class ColorIteratorChain extends ColorIterator {
        private List<ColorIterator> iterators = new ArrayList<ColorIterator>();
        public ColorIteratorChain(ColorIterator a, ColorIterator b) {
            iterators.add(a);
            iterators.add(b);
        }
        
        public boolean hasNext() {
            while (!iterators.isEmpty()) {
                if (iterators.get(0).hasNext()) {
                    return true;
                }
                iterators.remove(0);
            }
            
            return false;
        }

        public int[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            ColorIterator it = iterators.get(0);
            int[] val = it.next();
            if (!it.hasNext()) {
                iterators.remove(0);
            }
            return val;
        }
    }
    
    public static ColorIterator chain(ColorIterator a, ColorIterator b) {
        return new ColorIteratorChain(a, b);
    }

    private static void logColor(String tag, int[] prevColor) {
//        Log.d(tag, String.format("Faded color: %d %d %d %d", 
//                                        prevColor[0], prevColor[1],
//                                        prevColor[2], prevColor[3]));
    }
}
