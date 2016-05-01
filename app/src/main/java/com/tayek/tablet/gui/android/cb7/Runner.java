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
    Runner(final Group group,final MainActivity mainActivity) {
        super(group,tabletNetworkPrefix);
        // group, model, colors, audio observer are all set up now.
        this.mainActivity=mainActivity;
        mainActivity.group=group;
        mainActivity.model=model;
        mainActivity.colors=colors;
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
        guiAdapterABC=new GuiAdapter.GuiAdapterABC(model) { // move this out of runner
            @Override
            public void setStatusText(String text) {
            }
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
    @Override
    protected void loop() {
        super.loop();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.wifiStatus.setBackgroundColor(Colors.aColor(isNetworkInterfaceUp?Colors.green:Colors.red));
                mainActivity.routerStatus.setBackgroundColor(Colors.aColor(isRouterOk?Colors.green:Colors.red));
            }
        });
    }
}
