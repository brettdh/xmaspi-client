#!/usr/bin/python                                                               

def clamp(x, min, max):
    if x < min: return min
    if x > max: return max
    return x

class Bulbs:
    def __init__(self, driver):
        self.state = self.frame = [(0,0,0,0)] * 100
        self.driver = driver
    def set(self, i, (r,g,b,a)):
        r = clamp(r,0,15)
        g = clamp(g,0,15)
        b = clamp(b,0,15)
        a = clamp(a,0,255)
        self.frame[i] = (r,g,b,a)
    def add(self, i, (r,g,b,a)):
        (cr,cg,cb,ca) = self.frame[i]
        self.set(i,(cr+r,cg+g,cb+b,ca+a))
    def render(self):
        for i in range(100):
            if self.frame[i] != self.state[i]:
                (r,g,b,a) = self.frame[i]
                self.driver.write_led(i, a, r, g, b)
        self.state = self.frame
        self.frame = [(0,0,0,0)] * 100
