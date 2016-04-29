package com.tayek.tablet.gui.android.cb7;
import android.content.*;
import android.content.pm.*;
import android.provider.*;
import android.util.*;
import android.widget.*;

import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import static com.tayek.io.IO.*;
class AndroidRunner extends RunnerABC {
    final String key="com.tayek.tablet.sharedPreferencesKey";
    final SharedPreferences sharedPreferences; // add inet address and maybe service to preferences?
    final MainActivity mainActivity;
    final Thread thread;
    AndroidRunner(final Group group,final MainActivity mainActivity) {
        super(group);
        // group, model, colors, audio observer are all set up now.
        this.mainActivity=mainActivity;
        this.thread=Thread.currentThread();
        mainActivity.group=group;
        mainActivity.model=model;
        mainActivity.colors=colors;
        String key="com.tayek.tablet.sharedPreferencesKey";
        sharedPreferences=mainActivity.getSharedPreferences(key,Context.MODE_PRIVATE);
        //sharedPreferences.edit().clear().commit(); // only if we have to
        p("preferences: "+sharedPreferences.getAll());
    }
    @Override
    public void init() {
        mainActivity.setupAudioPlayer();
        Audio.audio.play(Audio.Sound.glass_ping_go445_1207030150);
    }
    @Override
    public void buildGui() {
        p("building gui.");
        mainActivity.colors=model.colors;
        final RelativeLayout relativeLayout=mainActivity.builGui();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setContentView(relativeLayout);
            }
        });
        gui=mainActivity;
        model.addObserver(mainActivity);
        p("building gui adapter.");
        guiAdapterABC=new GuiAdapter.GuiAdapterABC(model) {
            @Override
            public void processClick(int index) {
                int id=index+1;
                if(tablet!=null)
                    if(1<=id&&id<=model.buttons)
                        tablet.click(index+1);
                    else { // some other button
                        Histories histories=mainActivity.histories(index);
                        Toast.makeText(mainActivity,""+histories,Toast.LENGTH_LONG).show();
                    }
            }
            @Override
            public void setButtonText(final int id,final String string) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.buttons[id-1].setText(string);
                    }
                });
            }
            @Override
            public void setButtonState(final int id,final boolean state) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.buttons[id-1].setBackgroundColor(colors.aColor(id-1,state));
                    }
                });
            }
        };
        mainActivity.guiAdapterABC=guiAdapterABC;
        gui.setTablet(null);
        guiAdapterABC.setTablet(null);
        p("gui adapter: "+guiAdapterABC);
        p("gui built.");
    }
    InetAddress getInetAddress() {
        InetAddress inetAddress=null;
        inetAddress=addressWith(tabletNetworkPrefix);
        if(false&&inetAddress==null) // returns loopback!
            inetAddress=mainActivity.getIpAddressFromWifiManager();
        if(inetAddress!=null)
            // maybe just get this and not try to reach it
            // maybe save this in preferences?
            try {
                if(inetAddress.isReachable(1_000))
                    ;
            } catch(UnknownHostException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        return inetAddress;
    }
    @Override
    public void run() { // maybe put this in a forever loop?
        // kill this thread when the app quits!
        p("enter run() at: "+mainActivity.et+", tabletId: "+tabletId);
        inetAddress=getInetAddress();
        p("inetAddress: "+inetAddress);
        String storedTabletId=sharedPreferences.getString("tabletId","");
        p("stored tablet id: '"+storedTabletId+"' in preferences.");
        if(!storedTabletId.equals("")) {
            tabletId=storedTabletId;
            if(inetAddress==null) { // assume that they will all be the same!
                try {
                    inetAddress=InetAddress.getByName(group.required(tabletId).host);
                } catch(UnknownHostException e) {
                    p("oops");
                    l.severe(group.required(tabletId).host+" caused: "+e);
                }
            }
            p("found inetAddress from group: : "+inetAddress);
            p("found tablet id: "+tabletId+" in preferences.");
        } else {
            p("no tablet id found in preferences!");
            if(inetAddress!=null) {
                p("using: "+inetAddress);
                tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
                p("got tabletId from group: "+tabletId);
            } else
                p("1 no tablet id and no inetAddress!");
        }

        setTitle("runner::init");
        init();
        setTitle("runner::building gui");
        buildGui();
        while(true) {
            boolean isWifiOk=mainActivity.getIpAddressFromWifiManager()!=null;
            p("wifi is "+(isWifiOk?"ok":"not ok!"));
            mainActivity.setButtonColor(mainActivity.wifiStatus,Colors.aColor(isWifiOk?Colors.green:Colors.red));
            boolean isRouterOk=Exec.canWePing(tabletRouter,1_000);
            p("router is "+(isRouterOk?"ok":"not ok!"));
            mainActivity.setButtonColor(mainActivity.routerStatus,Colors.aColor(isRouterOk?Colors.green:Colors.red));
            // mainActivity.waitForWifi(); // do we need this?
            if(tablet==null) {
                if(isWifiOk&&isRouterOk)
                    if(tabletId!=null) {
                        createTabletAndStart(tabletId);
                        setTitle(tabletId);
                    }
                    else {
                        setTitle("don't know tabletId!");
                        if(inetAddress!=null) {
                            tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
                            p("got tabletId from group: "+tabletId);
                        } else {
                            inetAddress=getInetAddress();
                            p("no tablet id, but we have an inetAddress: "+inetAddress);
                            if(inetAddress!=null) {
                                tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
                                p("got tabletId from group: "+tabletId);
                            } else p("can not get inetAddress despite wifi being up!");
                        }
                    }
                else {
                    setTitle("can not start tablet, check wifi and router!");
                }
            } else {
                if(isWifiOk&&isRouterOk)
                    ;
                else {
                    stop();
                    setTitle("tablet was stopped, check wifi and router!");
                }
            } try {
                Thread.sleep(10_000);
            } catch(Exception e) {
                p("sleep was interrupted");
            }
        /*
        if(false) {
            if(!Exec.canWePing("127.0.0.1",1_000))
                l.severe("can not ping 127.0.0.1!");
            if(!Exec.canWePing("localhost",1_000))
                l.severe("can not ping localhost!");
            if(!Exec.canWePing(tabletRouter,2_000))
                l.severe("can not ping tabletRouter!");
            if(Exec.canWePing("google.com",5_000))
                l.severe("oops, we seem to be on the internet!");
        }
        if(false)
            new Thread(new Runnable() { // does not seem to find 192.168.0.x
                @Override
                public void run() {
                    Nics.main(new String[0]);
                }
            }).start();
        try {
            Thread.sleep(5_000);
        } catch(Exception e) {
        }
        Timer timer=new Timer();
        if(false)
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    printThreads();
                }
            },60_000,60_000);
        Timer timer2=new Timer();
            */
        }
    }
    public void setTitle(final String title) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setTitle(title);
            }
        });
    }
}
