package com.tayek.tablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Group.Info.History;
import com.tayek.tablet.io.LoggingHandler;
import static com.tayek.tablet.io.IO.*;
public class TwoTabletsTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        tablets=createForTest(2,offset);
        startListening();
    }
    @After public void tearDown() throws Exception {
        shutdown();
        super.tearDown();
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        sendOneDummyMessageFromEachTabletAndWait(false);
        if(false)
            for(Tablet tablet:tablets)
                p(tablet.toString2());
    }
    @Test() public void testDummy2Brokem() throws InterruptedException,UnknownHostException,ExecutionException {
        for(Tablet tablet:tablets)
            tablet.stopListening(tablet); // so send will fail
        sendOneDummyMessageFromEachTablet();
        Thread.sleep(500);
        shutdown();
        for(Tablet tablet:tablets) {
            History history=tablet.group.info(tablet.tabletId()).history;
            assertEquals(new Integer(2),history.clientHistory.failures);
        }
    }
    Level oldLevel;
}
