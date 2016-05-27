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

import java.io.*;
import java.lang.*;
import java.lang.System;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.logging.Handler;
//https://plus.google.com/103583939320326217147/posts/BQ5iYJEaaEH driver for usb
//http://davidrs.com/wp/fix-android-device-not-showing-up-on-windows-8/
//http://stackoverflow.com/questions/8818290/how-to-connect-to-a-specific-wifi-network-in-android-programmatically
//http://stackoverflow.com/questions/24908280/automatically-and-programmatically-connecting-to-a-specific-wifi-access-point
//https://github.com/eryngii-mori/android-developer-preview/issues/2218
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
            p("onCreate at: "+et+", process id: "+android.os.Process.myPid());
            super.onCreate(savedInstanceState);
            l.info("android id: "+Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_main);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            networkStuff.setupToast();
            String filename="tablet.log";
            File logFile = new File(getFilesDir(), filename);
            logFile=new File(Environment.getDataDirectory(),filename);
            p("log file: "+logFile);
            try {
                OutputStream outputStream = new FileOutputStream(logFile);
                outputStream.write("foo\n".getBytes());
                outputStream.close();
                p("wrote to log file: ");
            } catch (Exception e) {
                l.severe("write to log file caught: "+e);
                //e.printStackTrace();
            }
            try {
                InputStream inputStream = new FileInputStream(logFile);
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                String string=bufferedReader.readLine();
                inputStream.close();
                p("read from log file: "+string);
            } catch (Exception e) {
                l.severe("read from log file caught: "+e);
                //e.printStackTrace();
            }
            if(LoggingHandler.areAnySockethandlersOn()) {
                l.severe("we already have sockethandlers!");
                LoggingHandler.printSocketHandlers();
            }
            Logger global=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            LoggingHandler.init();
            LoggingHandler.setLevel(Level.WARNING);
            try {
                p("log file get name: "+logFile.getPath());
                Handler handler=new FileHandler(logFile.getPath());
                handler.setLevel(Level.ALL);
                l.addHandler(handler);
                l.warning("added file handler: "+handler);
            } catch(Exception e) {
                l.warning("file handler caught: "+e);
            }
            //LoggingHandler.toggleSockethandlers(); // looks like i need to wait for this?
            // yes, whould wait until wifi is up
            //Settings.Global.putInt(getContentResolver(), Settings.Global.CAPTIVE_PORTAL_DETECTION_ENABLED, 0);
            Integer result=null;
            try {
                String captivePortalDetectionEnabled="captive_portal_detection_enabled";
                result=Settings.Global.getInt(getContentResolver(),captivePortalDetectionEnabled);
                p("captivePortalDetectionEnabled="+result);
            } catch(Exception e) {
                p("caught: "+e);
            }
            if(result==null||result!=0) {
                p("do an: adb shell settings put global captive_portal_detection_enabled 0 ");
                try {
                    Settings.Global.putInt(getContentResolver(),"captive_portal_detection_enabled",0);
                    p("set capture succeeded.");
                } catch(Exception e) {
                    p("caught: "+e);
                }
            }
            Map<String,Required> requireds=new TreeMap<>(new Group.Groups().groups.get("g0"));
            Group group=new Group("1",requireds,MessageReceiver.Model.mark1);
            p("starting runner at: "+et);
            p("requireds: "+requireds);
            new Thread(runner=new Runner(group,this),"tablet runner").start();
            p("exit onCreate at: "+et);
        } catch(Exception e) {
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
    public void onOptionsMenuClosed(Menu menu) {
        pl("in options menu closed");
        super.onOptionsMenuClosed(menu);
        pl("after super on options menu closed");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean rc=runner.gui.menuItem(item);
        if(!rc) {
            pl("calling super on otions item selected");
            rc=super.onOptionsItemSelected(item);
        }
        if(runner.gui.areWeQuitting) {
            pl("calling finish from on options item selected.");
            finish();
        }
        return rc;
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble(savedStateKey,et.etms());
        super.onSaveInstanceState(savedInstanceState);
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedState=savedInstanceState.getDouble(savedStateKey);
    }
    @Override
    public void onPause() {
       pl("paused at: "+et);
        super.onPause();  // Always call the superclass method first
    }
    @Override
    public void onResume() {
        pl("resumed at: "+et);
        super.onResume();  // Always call the superclass method first
    }
    @Override
    protected void onStart() {
        super.onStart();
        pl("start at: "+et);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        pl("restart at: "+et);
    }
    void stopTabletStuff() {
        if(LoggingHandler.areAnySockethandlersOn()) {
            pl("stopping socket handers.");
            LoggingHandler.stopSocketHandlers();
        }
        if(runner!=null) {
            runner.isShuttingDown=true;
            pl("interrupting runner thread: "+runner);
            runner.thread.interrupt();
        }
        if(runner.gui.tablet!=null) {
            pl("stopping server.");
            ((Group.TabletImpl2)runner.gui.tablet).stopServer();
        }
        else
            l.severe("tablet is null in on stop!");

    }
    @Override
    protected void onStop() {
        super.onStop();
        pl("stopped at: "+et);
        //stopTabletStuff(); // don't do this, android will resume this
    }
    @Override
    protected void onDestroy() {
        pl("destroyed at: "+et);
        closeOptionsMenu();
        stopTabletStuff();
        super.onDestroy();
        //System.runFinalizersOnExit(true);
        //System.exit(0);
    }
    @Override
    public void onClick(final View v) {
        runner.gui.onClick(v);
    }
    static final Et et=new Et();
    Runner runner;
    final NetworkStuff networkStuff=new NetworkStuff(this);
    final String savedStateKey="et";
    Double savedState;
}