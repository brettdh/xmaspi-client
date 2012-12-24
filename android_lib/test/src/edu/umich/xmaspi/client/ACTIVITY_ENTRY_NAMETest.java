package edu.umich.xmaspi.client;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class edu.umich.xmaspi.client.ACTIVITY_ENTRY_NAMETestTest \
 * edu.umich.xmaspi.client.tests/android.test.InstrumentationTestRunner
 */
public class ACTIVITY_ENTRY_NAMETestTest extends ActivityInstrumentationTestCase2<ACTIVITY_ENTRY_NAMETest> {

    public ACTIVITY_ENTRY_NAMETestTest() {
        super("edu.umich.xmaspi.client", ACTIVITY_ENTRY_NAMETest.class);
    }

}
