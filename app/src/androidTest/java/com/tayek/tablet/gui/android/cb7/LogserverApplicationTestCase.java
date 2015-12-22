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

import com.tayek.tablet.io.*;
import com.tayek.tablet.io.LogServer.Copier;

import org.junit.*;
import org.junit.runner.*;
@RunWith(AndroidJUnit4.class) public class LogServerApplicationTestCase extends ApplicationTestCase<Application> {
    public LogServerApplicationTestCase() {
        super(Application.class);
    }
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        executorService=Executors.newSingleThreadExecutor();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        writer=new StringWriter();
        Copier.Factory factory=new Copier.Factory(writer);
        logServer=new LogServer(++service,factory);
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
    }
    @After public void tearDown() throws Exception {
        executorService.shutdown();
        logServer.stop();
        printThreads();
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        for(Thread thread:threads)
            if(thread!=null&&thread.getName().contains("SocketHandlerCallable")) {
                p(thread.toString()+" --------");
                p("interrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
            }

    }
    void test() throws IOException,InterruptedException {
        p("------------------------------------------");
        LoggingHandler.startSocketHandler(service);
        printThreads();
        Logger logger=Logger.getLogger("foo");
        logger.setUseParentHandlers(false);
        Handler handler=new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(LoggingHandler.MyFormatter.instance);
        logger.addHandler(handler);
        if(LoggingHandler.socketHandler!=null) {
            logger.addHandler(LoggingHandler.socketHandler);
            LoggingHandler.socketHandler.setLevel(Level.ALL);
        } else p("socket handler is null!");
        p("hanlders; "+Arrays.asList(logger.getHandlers()));
        logger.setLevel(Level.ALL);
        logger.finest(expected);
        Thread.sleep(500); // this makes it work!
        logger.setLevel(Level.OFF);
        LoggingHandler.stopSocketHandler();
        writer.flush();
        //p("writer: "+writer.toString());
        if(!writer.toString().contains(expected)) p("will fail!");
        assertTrue(writer.toString().contains(expected));
        p("------------------------------------------");
    }
    @Test public void test0() throws IOException,InterruptedException {
        //test();
    }
    @Test public void test1() throws IOException,InterruptedException {
        //test();
    }
    LogServer logServer;
    Writer writer;
    Thread thread;
    private ExecutorService executorService;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService;
}
