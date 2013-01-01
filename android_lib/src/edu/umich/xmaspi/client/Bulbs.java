package edu.umich.xmaspi.client;

import java.io.IOException;
import java.util.Arrays;

public class Bulbs {
    private int clamp(int x, int min, int max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        }
        return x;
    }
    
    private int gamma(int a) {
        double gamma_coeff = 2.0;
        return (int) (Math.pow(a/255.0, gamma_coeff) * 255.0);
    }
    
    public static class Color {
        public int red;
        public int green;
        public int blue;
        public int alpha;
        
        public Color(int r, int g, int b, int a) {
            set(r, g, b, a);
        }
        
        public void set(int r, int g, int b, int a) {
            red = r;
            green = g;
            blue = b;
            alpha = a;
        }
        
        public void set(Color c) {
            set(c.red, c.green, c.blue, c.alpha);
        }
        
        public boolean equals(Color other) {
            return (red == other.red &&
                    green == other.green &&
                    blue == other.blue &&
                    alpha == other.alpha);
        }
        
        public int[] toArray() {
            return new int[]{red, green, blue, alpha};
        }
    }
    
    public static final int COUNT = 100;
    public static final Color BLACK  = new Color(0,  0,  0,   0 );
    public static final Color WHITE  = new Color( 15, 15, 15, 255 );
    public static final Color RED    = new Color( 15,  0,  0, 255 );
    public static final Color GREEN  = new Color(  0, 15,  0, 255 );
    public static final Color BLUE   = new Color(  0,  0, 15, 255 );
    public static final Color CYAN   = new Color(  0, 15, 15, 255 );
    public static final Color PURPLE = new Color( 15,  0, 15, 255 );
    public static final Color YELLOW = new Color( 15, 15,  0, 255 );
    
    public static final Color[] COLORS = {
        BLACK, WHITE, RED, GREEN, BLUE, CYAN, PURPLE, YELLOW
    };
    
    private Color[] state = new Color[COUNT];
    private Color[] frame = new Color[COUNT];
    private Remote driver;
    
    public Bulbs(Remote driver) {
        for (int i = 0; i < COUNT; ++i) {
            this.state[i] = new Color(0, 0, 0, -1);
            this.frame[i] = new Color(0, 0, 0, 0);
        }
        this.driver = driver;
    }
    
    public void clear() {
        for (int i = 0; i < COUNT; ++i) {
            this.frame[i].set(0, 0, 0, 0);
        }
    }
    
    public void set(int bulb, int[] color) {
        assert(color.length == 4);
        
        if (bulb < 0 || bulb >= COUNT) return;
        int r = clamp(color[0], 0, 15);
        int g = clamp(color[1], 0, 15);
        int b = clamp(color[2], 0, 15);
        int a = clamp(color[3], 0, 255);
        this.frame[bulb].set(r, g, b, a);
    }
    
    public void add(int bulb, int[] color) {
        assert(color.length == 4);
        
        Color c = this.frame[bulb];
        int[] newColor = { c.red, c.green, c.blue, c.alpha };
        for (int i = 0; i < color.length; ++i) {
            newColor[i] += color[i];
        }
        set(bulb, newColor);
    }
    
    public void mix(int bulb, int[] color) {
        // XXX: not implemented in python API
        add(bulb, color);
    }
    
    public void render() throws IOException {
        render(false);
    }
    
    public void render(boolean force) throws IOException {
        boolean wrote_bulb = false;
        for (int bulb = 0; bulb < COUNT; ++bulb) {
            if (force || bulbUpdated(bulb)) {
                wrote_bulb = true;
                Color c = this.frame[bulb];
                this.driver.writeLed(bulb, gamma(c.alpha), c.red, c.green, c.blue);
                this.state[bulb].set(this.frame[bulb]);
            }
        }
        if (!wrote_bulb) {
            // prevent timeout
            this.driver.writeLed(COUNT, 0, 0, 0, 0);
        }
    }

    private boolean bulbUpdated(int bulb) {
        return !this.frame[bulb].equals(this.state[bulb]);
    }
}
