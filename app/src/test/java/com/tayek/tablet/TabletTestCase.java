package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Group.Info.History;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public class TabletTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test(/*timeout=500*/) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,offset);
        startListening();
        sendOneDummyMessageFromEachTabletAndWait(false);
        for(Tablet tablet:tablets)
            checkHistory(tablet,tablets.size());
        printStats();

    }
    @Test public void test2RealSimple() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,offset);
        startListening();
        for(Tablet tablet:tablets)
            tablet.broadcast(Message.dummy(tablet.group.groupId,tablet.tabletId()),0);
        Thread.sleep(200);
        Integer expected=2; // sending to self now
        Iterator<Tablet> i=tablets.iterator();
        Tablet t=i.next();
        History history=t.group.info(t.tabletId()).history;
        assertEquals(expected,history.serverHistory.received);
        t=i.next();
        history=t.group.info(t.tabletId()).history;
        assertEquals(expected,history.serverHistory.received);
        Tablet first=tablets.iterator().next();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++) {
            first.model.setState(buttoneId,true);
            Message message=Message.normal(first.group.groupId,first.tabletId(),buttoneId,first.model);
            first.broadcast(message,0);
        }
        Thread.sleep(600);
        for(Tablet tablet:tablets)
            ;//p("tablet: "+tablet.toString()+" received: "+tablet.group.io.server.received());
        if(true) return;
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertTrue(first.model.state(buttoneId));
        Thread.sleep(600);
        for(Tablet tablet:tablets)
            p(tablet+" "+tablet.model);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
                assertTrue(tablet.model.state(buttoneId));
        first.model.reset();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertFalse(first.model.state(buttoneId));
        Message message=Message.reset(first.group.groupId,first.tabletId(),first.model.buttons);
        first.broadcast(message,0);
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<first.model.buttons;buttoneId++)
                assertFalse(tablet.model.state(buttoneId));
        shutdown();
        printStats();
    }
    @Test(timeout=2_000) public void test2Real() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,offset);
        for(Tablet tablet:tablets)
            tablet.startListening();
        for(Tablet tablet:tablets)
            tablet.broadcast(Message.dummy(tablet.group.groupId,tablet.tabletId()),0);
        waitForEachTabletToReceiveOneMessageFromEachTablet(false);
        Tablet first=tablets.iterator().next();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++) {
            first.model.setState(buttoneId,true);
            Message message=Message.normal(first.group.groupId,first.tabletId(),buttoneId,first.model);

            first.broadcast(message,0);
        }
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertTrue(first.model.state(buttoneId));
        Thread.sleep(600);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<=tablet.model.buttons;buttoneId++)
                assertTrue(tablet.model.state(buttoneId));
        first.model.reset();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertFalse(first.model.state(buttoneId));
        Message message=Message.reset(first.group.groupId,first.tabletId(),first.model.buttons);
        first.broadcast(message,0);
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<tablet.model.buttons;buttoneId++)
                assertFalse(tablet.model.state(buttoneId));
        shutdown();
        printStats();
    }
}
