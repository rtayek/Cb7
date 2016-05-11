package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.widget.*;

import com.tayek.*;
import com.tayek.io.*;

import static com.tayek.io.IO.*;

import com.tayek.io.Audio.Factory.FactoryImpl.AndroidAudio;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.utilities.*;

import static java.lang.Math.round;

import java.lang.*;
import java.lang.System;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
//https://plus.google.com/103583939320326217147/posts/BQ5iYJEaaEH driver for usb
//http://davidrs.com/wp/fix-android-device-not-showing-up-on-windows-8/
public class MainActivity extends Activity implements View.OnClickListener {
    void setButtonColor(final Button button,final int color) {
        if(button!=null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setBackgroundColor(color);
                }
            });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
        p("onCreate at: "+et);
        super.onCreate(savedInstanceState);
        l.info("android id: "+Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        networkStuff.setupToast();
        Logger global=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        if(true)
            LoggingHandler.toggleSockethandlers();
        Map<String,Required> requireds=new TreeMap<>(new Group.Groups().groups.get("g0"));
        Group group=new Group("1",requireds,MessageReceiver.Model.mark1);
        p("starting runner at: "+et);
        p("requireds: "+requireds);
        new Thread(runner=new Runner(group,this),"tablet runner").start();
        p("exit onCreate at: "+et);}
        catch(Exception e) {
            e.printStackTrace();
            l.severe("on create caught: "+e);
            //throw e;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        l.info("on create options menu");
        super.onCreateOptionsMenu(menu);
        for(Enums.MenuItem menuItem : Enums.MenuItem.values())
            if(menuItem!=Enums.MenuItem.Level)
                menu.add(Menu.NONE,menuItem.ordinal(),Menu.NONE,menuItem.name());
        menu.add(Menu.NONE,Enums.MenuItem.values().length,Menu.NONE,"Restart");
        SubMenu subMenu=menu.addSubMenu(Menu.NONE,99,Menu.NONE,"Level");
        for(Enums.LevelSubMenuItem levelSubMenuItem : Enums.LevelSubMenuItem.values())
            subMenu.add(Menu.NONE,Enums.MenuItem.values().length+levelSubMenuItem.ordinal()/*hack!*/,Menu.NONE,levelSubMenuItem.name());
        return true;
    }
    // http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
    /*
    public static void restart(Context context, int delay) {
        if (delay == 0) {
            delay = 1;
        }
        Log.e("", "restarting app");
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName() );
        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        System.exit(2);
    }
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean rc=runner.gui.menuItem(item);
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        p("in on destry() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if(runner!=null)
            runner.thread.interrupt();
        if(runner.gui.tablet!=null)
            ((Group.TabletImpl2)runner.gui.tablet).stopServer();
        else
            System.out.println("tablet is null in on destroy!");
        super.onDestroy();
        //System.runFinalizersOnExit(true);
        //System.exit(0);
    }    @Override
    public void onClick(final View v) {
        runner.gui.onClick(v);
    }
    static final Et et=new Et();
    Runner runner;
    final NetworkStuff networkStuff=new NetworkStuff(this);
}