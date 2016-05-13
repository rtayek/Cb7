package com.tayek.tablet.gui.android.cb7;
import android.content.*;
import android.content.pm.*;
import android.net.wifi.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.io.Prefs.Factory.FactoryImpl.AndroidPrefs;
import com.tayek.utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import static com.tayek.io.IO.*;
class Runner extends RunnerABC {
    final String key="com.tayek.tablet.sharedPreferencesKey";
    final SharedPreferences sharedPreferences; // add inet address and maybe service to preferences?
    final MainActivity mainActivity;
    Gui gui;
    Runner(final Group group,final MainActivity mainActivity) {
        super(group,tabletRouter,tabletRouterPrefix);
        // group, model, colors, audio observer are all set up now.
        this.mainActivity=mainActivity;
        gui=new Gui(mainActivity,group,model);
        String key="com.tayek.tablet.sharedPreferencesKey";
        sharedPreferences=mainActivity.getSharedPreferences(key,Context.MODE_PRIVATE);
        //sharedPreferences.edit().clear().commit(); // only if we have to
        p("preferences: "+sharedPreferences.getAll());
        ((AndroidPrefs)prefs).setDelegate(new AndroidPrefs() {
            @Override
            public String get(String key) {
                return sharedPreferences.getString(key,"");
            }
            @Override
            public void put(String key,String value) {
                sharedPreferences.edit().putString(key,value).commit();
            }
            @Override
            public Map<String,?> map() {
                return sharedPreferences.getAll();
            }
            @Override
            public String toString() {
                return sharedPreferences.getAll().toString();
            }
        });
        p("prefs: "+prefs);
    }
    @Override
    public void init(MessageReceiver.Model model) {
        mainActivity.networkStuff.setupAudioPlayer();
        Audio.audio.play(Audio.Sound.glass_ping_go445_1207030150);
    }
    @Override
    public void buildGui(MessageReceiver.Model model) {
        p("building gui.");
        final RelativeLayout relativeLayout=gui.builGui();
        gui.setStatusVisibility(gui.status[0].getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setContentView(relativeLayout);
            }
        });
        hasATablet=gui;
        model.addObserver(gui);
        p("building gui adapter.");
        guiAdapterABC=gui.buildGuiAdapter();
        gui.guiAdapterABC=guiAdapterABC;
        hasATablet.setTablet(null);
        guiAdapterABC.setTablet(null);
        p("gui adapter: "+guiAdapterABC);
        p("gui built.");
    }
    @Override
    protected void loop(int n) {
        super.loop(n);
        p("group: "+group);
        if(!isNetworkInterfaceUp) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InetAddress inetAddress=mainActivity.networkStuff.getIpAddressFromWifiManager();
                }
            },"get ipaddress from wifi mabager").start();
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gui.wifiStatus.setBackgroundColor(Colors.aColor(isNetworkInterfaceUp?Colors.green:Colors.red));
                gui.routerStatus.setBackgroundColor(Colors.aColor(isRouterOk?Colors.green:Colors.red));
                gui.singleStatus.setBackgroundColor(Colors.aColor(isNetworkInterfaceUp&&isRouterOk?Colors.green:Colors.red));
            }
        });
        mainActivity.networkStuff.checkWifi();
    }
}
