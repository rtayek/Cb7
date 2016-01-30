package com.tayek.tablet.gui.android.cb7;
import android.app.Application;
import android.support.test.runner.*;
import android.test.ApplicationTestCase;
import android.util.*;
import static com.tayek.tablet.io.IO.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.logging.*;
import org.junit.*;

import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.LogServer.Copier;

import org.junit.*;
import org.junit.runner.*;
@RunWith(AndroidJUnit4.class) public class LogServerApplicationTestCase extends ApplicationTestCase<Application> {
    public LogServerApplicationTestCase() {
        super(Application.class);
    }
    @BeforeClass public static void setUpBeforeClass() throws Exception {
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        super.setUp();
        // LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        LoggingHandler.startSocketHandler(Main.defaultLogServerHost,LogServer.defaultService);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
    }
    @After public void tearDown() throws Exception {
        LoggingHandler.stopSocketHandler();
        super.tearDown();
    }
    @Test public void test() { // just testing that we can log.
        IO.staticLogger.info("foo");
    }
}
