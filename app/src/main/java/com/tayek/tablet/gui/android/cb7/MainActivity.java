package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.media.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.widget.*;

import com.tayek.tablet.Message;
import com.tayek.tablet.io.Audio.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.Audio.Bndroid;

import static com.tayek.tablet.io.IO.*;
import static java.lang.Math.round;

import java.lang.*;
import java.lang.System;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;
//https://plus.google.com/103583939320326217147/posts/BQ5iYJEaaEH driver for usb
//http://davidrs.com/wp/fix-android-device-not-showing-up-on-windows-8/
public class MainActivity extends Activity implements Observer, View.OnClickListener {
    GuiAdapterABC adapter(Tablet tablet) {
        guiAdapterABC=new GuiAdapterABC(tablet) {
            @Override
            public void setButtonText(final int id,final String string) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttons[id-1].setText(string);
                            }
                        });
                    }
                },0);
            }
            @Override
            public void setButtonState(final int id,final boolean state) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttons[id-1].setBackgroundColor(colors.aColor(id-1,state));
                            }
                        });
                    }
                },0);
            }
        };
        return guiAdapterABC;
    }
    public String buttonsToString() {
        String s="{";
        for(int i=0;i<colors.n;i++)
            s+=buttons[i].isPressed()?'T':"F";
        s+='}';
        return s;
    }
    Integer id(Sound sound) {
        switch(sound) {
            case electronic_chime_kevangc_495939803:
                return R.raw.electronic_chime_kevangc_495939803;
            case glass_ping_go445_1207030150:
                return R.raw.glass_ping_go445_1207030150;
            case store_door_chime_mike_koenig_570742973:
                return R.raw.store_door_chime_mike_koenig_570742973;
            default:
                logger.warning(""+" "+"default where!");
                return null;
        }
    }
    void sound() {
        Audio.Instance.sound=true;
        int id=R.raw.electronic_chime_kevangc_495939803;
        mediaPlayer=MediaPlayer.create(MainActivity.this,id);
        mediaPlayer.start();
    }
    String getIpAddress() { // unused
        WifiManager wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf=wifiMan.getConnectionInfo();
        int ipAddress=wifiInf.getIpAddress();
        p("ipaddress: "+ipAddress+" "+Integer.toString(ipAddress,16));
        if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
            ipAddress=Integer.reverseBytes(ipAddress);
        byte[] ipByteArray=BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString=null;
        try {
            ipAddressString=InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch(UnknownHostException ex) {
            Log.e("WIFIIP","Unable to get host address.");
            ipAddressString=null;
        }
        return ipAddressString;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggingHandler.setLevel(Level.ALL);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics);
        setContentView(R.layout.activity_main);
        String android_id=Secure.getString(getContentResolver(),Secure.ANDROID_ID);
        ((Audio.Bndroid)Audio.audio).setCallback(new Callback<Audio.Sound>() {
            @Override
            public void call(Sound sound) {
                System.out.println("playing sound.");
                Integer id=id(sound);
                mediaPlayer=MediaPlayer.create(MainActivity.this,id);
                mediaPlayer.start();
            }
        });
        sound();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                ;
            }
        },"get local host");
        thread.start();
        InetAddress inetAddress=null;
        try {
            InetAddress.getLocalHost();
        } catch(UnknownHostException e) {
            Toast.makeText(MainActivity.this,"caught: "+e,Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        Group group=new Group(inetAddress,1,Group.groups.get("g0"));
        tablet=group.getTablet(inetAddress);
        Toast.makeText(MainActivity.this,"tablet: "+tablet,Toast.LENGTH_SHORT).show();
        tablet.model.addObserver(this);
        tablet.model.addObserver(new AudioObserver(tablet.model));
        tablet.group.io.startListening(tablet);
        System.out.println("set toast callback.");
        ((Toaster.Android_)Toaster.toaster).setCallback(new Callback<String>() {
            @Override
            public void call(final String string) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        System.out.println("build gui.");
        final int size=140;
        System.out.println(size+" "+(metrics.widthPixels*1./7));
        final int x0=size/4, y0=75;
        RelativeLayout relativeLayout=new RelativeLayout(this);
        RelativeLayout.LayoutParams params=null;
        final int rows=colors.rows;
        final int columns=colors.columns;
        for(int i=0;i<rows*columns;i++) {
            Button button=new Button(this);
            button.setId(i);
            params=new RelativeLayout.LayoutParams(size,size);
            params.leftMargin=(int)(x0+i%columns*1.2*size);
            params.topMargin=(int)(y0+i/columns*size*1.2);
            button.setLayoutParams(params);
            button.setText(""+(i+1));
            button.setBackgroundColor(colors.aColor(i,false));
            button.setOnClickListener(this);
            buttons[i]=button;
            relativeLayout.addView(button);
        }
        Button button=new Button(this);
        button.setId(rows*columns);
        params=new RelativeLayout.LayoutParams(size,size);
        params.leftMargin=(int)(x0+(columns+1.2)*size);
        params.topMargin=y0;
        button.setLayoutParams(params);
        button.setText("R");
        button.setBackgroundColor(colors.aColor(rows*columns,false));
        button.setOnClickListener(this);
        buttons[rows*columns]=button;
        relativeLayout.addView(button);
        guiAdapterABC=adapter(tablet);
        relativeLayout.setBackgroundColor(colors.background|0xff000000);
        setContentView(relativeLayout);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.info("on create options menu");
        super.onCreateOptionsMenu(menu);
        for(Tablet.MenuItem menuItem : Tablet.MenuItem.values()) {
            System.out.println("add menu item: "+menuItem);
            menu.add(Menu.NONE,menuItem.ordinal(),Menu.NONE,menuItem.name());
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.info("item: "+item);
        int id=item.getItemId();
        Tablet.MenuItem.doItem(id,tablet);
        if(id==R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        //Main.stop();
        if(tablet!=null)
            tablet.group.io.stopListening(tablet);
        else
            System.out.println("tablet is null in on destroy!");
        super.onDestroy();
    }
    @Override
    public void onClick(final View v) {
        if(v instanceof Button) {
            Button button=(Button)v;
            int index=button.getId();
            guiAdapterABC.processClick(index);
        } else
            logger.severe("not a button!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(o==tablet.model)
            guiAdapterABC.update(o,hint);
        else
            System.out.println("no gui for model: "+o);
    }
    GuiAdapterABC guiAdapterABC;
    Tablet tablet;
    MediaPlayer mediaPlayer;
    TextView bottom; // was used for messages, put it back
    final Colors colors=new Colors();
    Button[] buttons=new Button[colors.n];
    final Logger logger=Logger.getLogger(getClass().getName());
}