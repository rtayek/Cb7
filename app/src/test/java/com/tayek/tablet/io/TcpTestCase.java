package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.*;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Info;
import com.tayek.tablet.Group.Info.History;
import com.tayek.tablet.Receiver.DummyReceiver;
import com.tayek.tablet.io.*;
public class TcpTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        service=Main.defaultReceivePort+offset;
    }
    boolean sendAndReceiveOneMessage() throws UnknownHostException,IOException,InterruptedException {
        DummyReceiver receiver=new DummyReceiver();
        SocketAddress socketAddress=new InetSocketAddress("localhost",service);
        boolean replying=true; // check this!!!!!!!!!
        History history=new History();
        Server server=new Server(null,socketAddress,receiver,replying,history.serverHistory);
        server.startServer();
        Client client=new Client(socketAddress,true,10);
        Message dummy=Message.dummy(1,1);
        client.send(dummy,1,history.clientHistory);
        while(history.serverHistory.received==0)
            Thread.yield();
        server.stopServer();
        // p(receiver.t);
        if(receiver.message==null) p("null");
        checkHistory(history,replying,1);
        boolean isOk=receiver.message!=null&&dummy.toString().equals(receiver.message.toString());
        return isOk;
    }
    @Test() public void testConnectAndClose() throws Exception {
        SocketAddress socketAddress=new InetSocketAddress("localhost",service);
        History history=new History();
        DummyReceiver receiver=new DummyReceiver();
        LoggingHandler.setLevel(Level.ALL);
        Server server=new Server(null,socketAddress,receiver,Group.defaultOptions.replying,history.serverHistory);
        server.startServer();
        Socket socket=Client.connect(socketAddress,100);
        if(socket!=null)
            p("connected to: "+socket);
        assertTrue(socket!=null);
        Thread.sleep(100);
        socket.close();
        Thread.sleep(100);
        //while(history.serverHistory.received==0)
            //Thread.yield();
        server.stopServer();
        // p(receiver.t);
        if(receiver.message==null) p("null");
        //checkHistory(history,Group.defaultOptions.replying,1);
        p("received: "+receiver.message);
        // assert??
        LoggingHandler.setLevel(Level.OFF);
    }
    @Test(timeout=100) public void testOnce() throws Exception {
        LoggingHandler.setLevel(Level.ALL);

        if(!sendAndReceiveOneMessage()) fail("failed!");
    }
    @Test(timeout=200) public void testTwice() throws Exception {
        if(!sendAndReceiveOneMessage()) fail("failed!");
        if(!sendAndReceiveOneMessage()) fail("failed!");
    }
    @Test(timeout=300) public void testThrice() throws Exception {
        if(!sendAndReceiveOneMessage()) fail("failed!");
        if(!sendAndReceiveOneMessage()) fail("failed!");
        if(!sendAndReceiveOneMessage()) fail("failed!");
    }
    @Test(timeout=1_500) public void testManyTimes() throws Exception {
        for(Integer i=1;i<=10;i++) {
            //p("i="+i);
            if(!(sendAndReceiveOneMessage())) {
                p("oops");
                fail("failed at: "+i);
            }
            Thread.sleep(10);
            if(i%1000==0) p(i.toString());
        }
    }
    int service;
    int threads;
}
