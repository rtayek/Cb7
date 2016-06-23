package com.tayek.tablet.gui.android.cb7;
import android.content.*;
import android.view.*;
import android.widget.*;

import com.tayek.io.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.io.Prefs.Factory.FactoryImpl.AndroidPrefs;
import com.tayek.utilities.*;

import java.util.*;

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
        pl("preferences: "+sharedPreferences.getAll());
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
        pl("prefs: "+prefs);
        //Exec.exec("settings put global captive_portal_detection_enabled 0 ");
        //loopSleep=10_000;
        //prefs.clear();
        // try clearing the prefs to see if it fixed the problem
        // that conrad found on sunday.
    }
    @Override
    public void init(MessageReceiver.Model model) {
        gui.setupAudio();
        Audio.audio.play(Audio.Sound.glass_ping_go445_1207030150);
    }
    @Override
    public void buildGui(MessageReceiver.Model model) {
        pl("building gui.");
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
        p("start runner loop "+mainActivity);
        if(heartbeatperiod!=0&&n%heartbeatperiod==0)
            pl("android id: "+mainActivity.androidId+", loop: "+n);

        super.loop(n);
        if(!isNetworkInterfaceUp) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.networkStuff.checkWifi();
                }
            },"checkwifi").start();
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isServerRunning=tablet!=null&&tablet.isServerRunning();
                gui.serverStatus.setBackgroundColor(Colors.aColor(isServerRunning?Colors.green:Colors.red));
                gui.wifiStatus.setBackgroundColor(Colors.aColor(isNetworkInterfaceUp?Colors.green:Colors.red));
                gui.routerStatus.setBackgroundColor(Colors.aColor(isRouterOk?Colors.green:Colors.red));
                gui.singleStatus.setBackgroundColor(Colors.aColor(isServerRunning&&isNetworkInterfaceUp&&isRouterOk?Colors.green:Colors.red));
            }
        });
        p("end runner loop "+mainActivity);
    }
}
