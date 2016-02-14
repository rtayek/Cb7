package com.tayek.tablet;
import static com.tayek.tablet.io.IO.p;
import static org.junit.Assert.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Group.Info.History;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public abstract class AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        LoggingHandler.setLevel(Level.OFF);
    }
    @Before public void setUp() throws Exception {
        threads=Thread.activeCount();
        offset+=100;
        p("options: "+Group.defaultOptions);
    }
    @After public void tearDown() throws Exception { //Thread.sleep(100); // apparently not needed since we shutdown the executor service now.
        int threads=Thread.activeCount();
        if(threads>this.threads) {
            p((threads-this.threads)+" extra threads!");
            if(printThreads) {
                IO.printThreads();
                p((threads-this.threads)+" extra threads!");
            }
        }
    }
    public Set<Tablet> createForTest(int n,int offset) {
        Map<Integer,Group.Info> map=new TreeMap<>();
        for(int i=1;i<=n;i++)
            map.put(i,new Group.Info("T"+i+" on PC",Main.defaultTestingHost,Main.defaultReceivePort+100+offset+i));
        return new Group(1,map,Model.mark1,Group.defaultOptions).create();
    }
    protected void startListening() {
        Tablet first=tablets.iterator().next();
        for(Tablet tablet:tablets) {
            if(!tablet.startListening()) fail(tablet+" startListening() retuns false!");
            assertNotNull(tablet.server);
            assertEquals(first.model.serialNumber,tablet.model.serialNumber);
            assertEquals(first.group.serialNumber,tablet.group.serialNumber);
            Thread.yield();
        }
    }
    void sendOneDummyMessageFromEachTablet() {
        for(Tablet tablet:tablets) {
            //assertNotNull(tablet.group.io.server);
            tablet.broadcast(Message.dummy(tablet.group.groupId,tablet.tabletId()),0);
            Thread.yield();
        }
    }
    void waitForEachTabletToReceiveOneMessageFromEachTablet(boolean sleepAndPrint) throws InterruptedException {
        boolean once=false;
        boolean done=false;
        while(!done) {
            done=true;
            for(Tablet tablet:tablets) {
                History history=tablet.group.info(tablet.tabletId()).history;
                // put history into tablet for convenience?
                if(tablet.group.options.replying) {
                    if(history.clientHistory.replies<tablets.size()) {
                        done=false;
                        break;
                    }
                } else {
                    if(history.serverHistory.received<tablets.size()) {
                        done=false;
                        break;
                    }
                    // check for sent also
                    if(history.clientHistory.sent<tablets.size()) {
                        done=false;
                        break;
                    }
                }
                // fast fail: check history for any failures and return early.
                // this should avoid timeouts and make the tests run faster.
                if(history.failures()>0) {
                    if(!once) {
                        once=true;
                        p("failures: "+history);
                    }
                    // might cause some tests to fail if we return early
                    // maybe put a guard on this
                    // return;
                }
                if(!sleepAndPrint) Thread.yield();
            }
            p("-----------------");
            for(Tablet tablet:tablets) {
                History history=tablet.group.info(tablet.tabletId()).history;
                if(sleepAndPrint) {
                    p("history "+history);
                }
            }
            Thread.sleep(500);
        }
        if(false) for(Tablet tablet:tablets)
            p("history: "+tablet.group.info(tablet.tabletId()).history);
        for(Tablet tablet:tablets) {
            History history=tablet.group.info(tablet.tabletId()).history;
            if(history.serverHistory.received>tablets.size()) tablet.l.warning(tablet+" received too many messages: "+history.serverHistory.received);
        }
    }
    protected void shutdown() {
        for(Tablet tablet:tablets) {
            if(!tablet.executorService.isShutdown()) tablet.executorService.shutdown();
            // don't do this is we are testing and all using the same service.
            tablet.stopListening(tablet);
        }
    }
    protected void sendOneDummyMessageFromEachTabletAndWait(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromEachTablet();
        Et et=new Et();
        waitForEachTabletToReceiveOneMessageFromEachTablet(sleepAndPrint);
        p("wait for "+tablets.size()+" tablets, took: "+et);
        for(Tablet tablet:tablets) {
            History history=tablet.group.info(tablet.tabletId()).history;
            if(history.serverHistory.received!=tablets.size()) p(tablet+" received: "+history.serverHistory.received+" instead of "+tablets.size());
            checkHistory(tablet,tablets.size());
        }
        shutdown();
    }
    public void checkHistory(Tablet tablet,Integer n) {
        checkHistory(tablet.group.info(tablet.tabletId()).history,tablet.group.options.replying,n);
    }
    public void checkHistory(History history,boolean replying,Integer n) {
        //p("history: "+history);
        assertEquals(n,history.serverHistory.received);
        assertEquals(replying?n:zero,history.serverHistory.replied);
        assertEquals(n,history.clientHistory.sent);
        // fails when sending in parallel since it does not wait?
        // but how?, since sent is incremented after send is complete
        // looks like received is ok. but sent is a bit late perhaps?
        assertEquals(replying?n:zero,history.clientHistory.replies);
        assertEquals(zero,history.serverHistory.failures);
        assertEquals(zero,history.clientHistory.failures);
    }
    protected void printStats() {
        for(Tablet tablet:tablets) {
            p("send time: "+tablet.sendTimeHistogram);
        }
    }
    int threads;
    boolean printThreads;
    protected Set<Tablet> tablets;
    public static int offset=1_000; // too many places, fix!
    static final Integer zero=new Integer(0),one=new Integer(1);
}
