from remote import RemoteDriver
from bulbs import Bulbs
import random
import time
import colorsys
import sys
import math

sleep_time = .3

colors = [(15, 0, 0), \
            (15, 3, 0), \
            (15, 15, 0), \
            (0,   15,   0), \
            (0,   0, 15), \
            (4,  14, 13), \
            (14, 0, 14) ]

class MergeSort(object):
    def __init__(self, bulbs, driver):
        self.driver = driver
        self.bulbs = bulbs
        self.lights = [(0,0,0,0)]*100
        self.mylist = []
        for i in range(50):
            (r, g, b) = colors[random.randint(0, len(colors)-1)]
            self.lights[i+25] = (r, g, b, 200)
            self.mylist.append((r,g,b,200))

    def freq(self, color):
        r, g, b, a = color
        return colorsys.rgb_to_hsv(r/15.0, g/15.0, b/15.0)[0]


    def display_list(self, bins, test_print=False):
        offPerInterval = int(50 / (bins + 1))
        #must be float for ceiling to work properly
        if 50 / bins != 1:
            onPerInterval = int(math.ceil(50.0 / bins))
        else:
            onPerInterval = 1
        listWritten = 0
        # first print off then on
        flag = False
        counter = 0
        for i in range(100):
            if not flag and (counter / offPerInterval) > 0:
                counter = 0
                flag = True
            elif (flag and counter / onPerInterval > 0):
                counter = 0
                flag = False

            # separate statement to avoid elif clause that can be skipped
            if listWritten >= 50:
                counter = 0
                flag = False

            if not flag:
                self.lights[i] = (0, 0, 0, 0)
                counter += 1
                
            elif flag:
                counter += 1
                self.lights[i] = self.mylist[listWritten]
                listWritten += 1
            
        if listWritten != 50:
            print "error"
            print listWritten
        self.driver.write_led(100, 0, 0, 0, 0)
        self.render()
        if test_print:
            print self.lights
        time.sleep(sleep_time)

    def display_lol(self, lol, test_print = False):
        offInterval = 50 / (len(lol) + 1)
        onInterval = int(math.ceil(50.0 / (len(lol))))
        if 50 / len(lol) == 1:
            onInterval = 1

        listWritten = 0
        # first print off then on
        i = 0
        roundNum = 0
        listWritten = 0
        print "length of lol: ", len(lol)
        while roundNum < len(lol) or i < 100:
            counter = 0
            while i < 100 and counter < offInterval:
                self.lights[i] = (0, 0, 0, 0)
                counter += 1
                i += 1
            if roundNum < len(lol):
                for element in lol[roundNum]:
                    print i, roundNum, len(lol)
                    self.lights[i] = element
                    i += 1
                    listWritten += 1
                roundNum += 1
            
        if listWritten != 50:
            print "error"
            print listWritten
        self.driver.write_led(100, 0, 0, 0, 0)
        self.render()
        if test_print:
            print self.lights
        
        time.sleep(sleep_time)

    
    def render(self):
        for i in range(100):
            self.bulbs.set(i, self.lights[i])
        print self.bulbs.frame
        self.bulbs.render()
        # Render seems to have an error in it... write it all myself right now
        for i in range(100):
            (r,g,b,a) = self.bulbs.frame[i]
            self.driver.write_led(i, a,r,g,b)
            

    # Recursive merge sort
    def sort(self, sub, leftMostPosition, bins):
        if len(sub) <= 1:
            # List is sorted
            return sub
        
        # Must continue sorting
        middle = len(sub) / 2
        left = []
        right = []
        for i in range(len(sub)):
            if i < middle:
                left.append(sub[i])
            else:
                right.append(sub[i])
        
        
        left = self.sort(left, leftMostPosition, bins * 2)
        #self.display_list(bins)
        #print bins
        right = self.sort(right, middle, bins * 2)
        
        mergedList = self.merge(left, right, leftMostPosition)
        #self.partialSortMyList(mergedList, leftMostPosition)
        self.display_list(bins)
        print bins
        self.check_list(mergedList)
        return mergedList


    def iter_sort(self):
        #working list of lists
        wlol = []
        for element in self.mylist:
            wlol.append([element])

        # Now i have a a list of 50 lists with 1 element each
        # simulate the original breakup
        for i in range(35):
            self.display_list(i+1)

        lollen = 1
        while lollen <= len(self.mylist):
            new_wlol = []
            i = 0
            while i < len(wlol)-1:
                new_wlol.append(self.merge(wlol[i], wlol[i+1], 0))
                i += 2
            #odd number of merges
            if i == len(wlol)-1:
                new_wlol.append(wlol[i])

            if new_wlol:
                wlol = new_wlol

            lollen *= 2
            self.display_lol(wlol)

        print "wlol", wlol
        print "lollen", lollen
        #self.check_list(wlol)
        return wlol

    def check_list(self, someList):
        if len(someList) < 2:
            return True
        for i in range(len(someList)-1):
            if self.freq(someList[i]) > self.freq(someList[i+1]):
                print someList
                for i in someList:
                    print i, self.freq(i)
                sys.exit(1)

    def partialSortMyList(self, mergedList, leftMostPosition):
        for i in range(len(mergedList)):
            self.mylist[i+leftMostPosition] = mergedList[i]
        
    def merge(self, left, right, leftMostPosition):
        totalLength = len(left) + len(right)
        lPtr = 0
        rPtr = 0
        mergedList = []
        for i in range(totalLength):
            if lPtr >= len(left):
                mergedList.append(right[rPtr])
                rPtr += 1
            elif rPtr >= len(right):
                mergedList.append(left[lPtr])
                lPtr += 1

            elif self.freq(left[lPtr]) <= self.freq(right[rPtr]):
                mergedList.append(left[lPtr])
                lPtr += 1
            else:
                mergedList.append(right[rPtr])
                rPtr += 1

        return mergedList

if __name__=="__main__":    
    d = RemoteDriver("MergeSort")
    print 'our turn!'

    # init
    b = Bulbs(d)
    merger = MergeSort(b, d)
    #for i in range(49):
    #    merger.display_list(i+1)
    #merger.mylist = merger.sort(merger.mylist, 0, 1)
    finalList = merger.iter_sort()
    #merger.check_list(finalList)
    merger.display_lol(finalList, True)
    #print merger.mylist

    d.busy_wait()
