package com.tayek.tablet.gui.android.cb7;
import static org.junit.Assert.*;
import java.net.InetAddress;
import java.util.concurrent.*;
import org.junit.*;

import com.tayek.tablet.gui.android.cb7.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.IO.*;
import static com.tayek.tablet.io.IO.*;
public class IOTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testGetByName() throws InterruptedException,ExecutionException {
        GetByNameCallable task=new GetByNameCallable("192.168.1.2");
        InetAddress inetAddress=runAndWait(task);
        p("inetAddress: "+inetAddress);
    }
    @Test public void testGetNetworkInterfacesWithHost() throws InterruptedException, ExecutionException {
        InetAddress inetAddress=IO.runAndWait(new GetNetworkInterfacesCallable("192.168.1.2"));
        p("inetAddress: "+inetAddress);
    }
    @Test public void testGetNetworkInterfacesWithPrefix() throws InterruptedException, ExecutionException {
        InetAddress inetAddress=IO.runAndWait(new GetNetworkInterfacesCallable("192.168.1."));
        p("inetAddress: "+inetAddress);
    }
    @Test public void testGetNetworkInterfacesWithLess() throws InterruptedException, ExecutionException {
        InetAddress inetAddress=IO.runAndWait(new GetNetworkInterfacesCallable("192."));
        p("inetAddress: "+inetAddress);
    }
    @Test public void testGetNetworkInterfacesWithDot() throws InterruptedException, ExecutionException {
        InetAddress inetAddress=IO.runAndWait(new GetNetworkInterfacesCallable("."));
        p("inetAddress: "+inetAddress);
    }
}
