package com.tayek.tablet;
import static com.tayek.tablet.io.IO.p;
import static org.junit.Assert.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Group.Info.History;
import com.tayek.tablet.io.LoggingHandler;
import com.tayek.utilities.Histogram;
public class Tablet32TestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        LoggingHandler.setLevel(Level.INFO);
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
        LoggingHandler.setLevel(Level.OFF);
    }
    @Test(timeout=20_000) public void testAll32WithOneMessage() throws InterruptedException {
        p("options: "+Group.defaultOptions);
        tablets=createForTest(32,offset);
        assertEquals(Group.defaultOptions,tablets.iterator().next().group.options);
        startListening();
        sendOneDummyMessageFromEachTabletAndWait(true);
        for(Tablet tablet:tablets)
            sendTime.add(tablet.sendTimeHistogram.mean());
        for(Tablet tablet:tablets) {
            checkHistory(tablet,tablets.size());
        }
        printStats();
        p("send time: "+sendTime);
    }
    @Test public void testJustOneWithOneMessage() throws InterruptedException {
        Map<Integer,Group.Info> map=new TreeMap<>();
        for(int i=1;i<=32;i++) // hack address so it can't connect
            map.put(i,new Group.Info("T"+i+" on PC",Main.defaultTestingHost,Main.defaultReceivePort+100+offset+i));
        tablets=new Group(1,map,Model.mark1,Group.defaultOptions).create();
        Tablet tablet=tablets.iterator().next();
        if(!tablet.startListening()) fail(tablet+" startListening() retuns false!");
        tablet.broadcast(Message.dummy(tablet.group.groupId,tablet.tabletId()),0);
        Thread.sleep(500);
        History history=tablet.group.info(tablet.tabletId()).history;
        assertEquals(one,history.serverHistory.received);
        assertEquals(tablet.group.options.replying?one:zero,history.serverHistory.replied);
        assertEquals(one,history.clientHistory.sent);
        assertEquals(tablet.group.options.replying?one:zero,history.clientHistory.replies);
        assertEquals(zero,history.serverHistory.failures);
        assertEquals(thirtyOne,history.clientHistory.failures);
        printStats();
        p("send time: "+sendTime);
    }
    Histogram sendTime=new Histogram(10,0,1000);
    static final Integer thirtyOne=new Integer(31);
}
