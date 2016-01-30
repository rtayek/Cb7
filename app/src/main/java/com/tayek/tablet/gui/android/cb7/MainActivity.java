package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.widget.*;

import com.tayek.tablet.io.Audio.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        if(true) //
        try {
            p("start socket handler");
            LoggingHandler.startSocketHandler(Main.defaultLogServerHost,LogServer.defaultService);
            LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
        } catch(Exception e) {
            p("caught: "+e);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        p("get ip address from wifi manager says: "+getIpAddressFromWifiManager(this));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress localHost=InetAddress.getLocalHost();
                    p("local: "+localHost);
                    String host=localHost.getHostName();
                    p("host: "+host);
                    InetAddress inetAddress=InetAddress.getByName(host);
                    p("address: "+inetAddress);
                } catch(UnknownHostException e) {
                    p("caught: "+e);
                }
            }
        }).start();
        setContentView(R.layout.activity_main);
        p("android id: "+Secure.getString(getContentResolver(),Secure.ANDROID_ID));
        ((Audio.Bndroid)Audio.audio).setCallback(new Callback<Audio.Sound>() {
            @Override
            public void call(Sound sound) {
                Integer id=id(sound);
                if(id!=null) {
                    mediaPlayer=MediaPlayer.create(MainActivity.this,id);
                    mediaPlayer.start();
                } else
                    l.warning("id for sound: "+sound+" is null!");
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
                        staticLogger.warning(""+" "+"default where!");
                        return null;
                }
            }
        });
        ((Toaster.Android_)Toaster.toaster).setCallback(new Callback<String>() {
            @Override
            public void call(final String string) {
                if(false)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
                    }
                });
                else p("toast was: "+string);
            }
        });
        Audio.audio.play(Sound.electronic_chime_kevangc_495939803);
        InetAddress inetAddress=null;
        Set<InetAddress> addresses = myInetAddress(Main.networkPrefix);
        p("addresses: "+addresses);
        if(addresses.size()==0) throw new RuntimeException("oops");
        Group group=new Group(1,new Group.Groups().groups.get("g0"),Model.mark1,Group.defaultOptions);
        tablet=group.getTablet(addresses.iterator().next(),null);
        p("options: "+tablet.group.options);
        tablet.model.addObserver(this);
        tablet.model.addObserver(new AudioObserver(tablet.model));
        tablet.startListening(tablet);
        buttons=new Button[tablet.colors.n];
        RelativeLayout relativeLayout=builGui();
        relativeLayout.setBackgroundColor(tablet.colors.background|0xff000000);
        guiAdapterABC=new GuiAdapterABC(tablet) {
            @Override
            public void setButtonText(final int id,final String string) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttons[id-1].setText(string);
                    }
                });
            }
            @Override
            public void setButtonState(final int id,final boolean state) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttons[id-1].setBackgroundColor(tablet.colors.aColor(id-1,state));
                    }
                });
            }
        };
        setContentView(relativeLayout);
    }
    RelativeLayout builGui() {
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics);
        final int size=225;
        final float fontsize=size*.30f;
        System.out.println(size+" "+(metrics.widthPixels*1./7));
        final int x0=size/4, y0=75;
        RelativeLayout relativeLayout=new RelativeLayout(this);
        RelativeLayout.LayoutParams params=null;
        final int rows=tablet.colors.rows;
        final int columns=tablet.colors.columns;
        //Objects.toString(new Integer(0)); // hack
        for(int i=0;i<rows*columns;i++) {
            Button button=new Button(this);
            button.setId(i); // id is index!
            button.setTextSize(fontsize);
            button.setGravity(Gravity.CENTER);
            params=new RelativeLayout.LayoutParams(size,size);
            params.leftMargin=(int)(x0+i%columns*1.2*size);
            params.topMargin=(int)(y0+i/columns*size*1.2);
            button.setLayoutParams(params);
            if(i/columns%2==0)
                button.setText(tablet.getButtonText(new Integer(i+1)/*wtf?*/));
            button.setBackgroundColor(tablet.colors.aColor(i,false));
            button.setOnClickListener(this);
            buttons[i]=button;
            relativeLayout.addView(button);
        }
        Button button=new Button(this);
        button.setId(rows*columns); // id is index!
        button.setTextSize(fontsize);
        button.setGravity(Gravity.CENTER);
        params=new RelativeLayout.LayoutParams(size,size);
        params.leftMargin=(int)(x0+(columns+1.2)*size);
        params.topMargin=y0;
        button.setLayoutParams(params);
        button.setText(tablet.getButtonText(tablet.model.resetButtonId));
        button.setBackgroundColor(tablet.colors.aColor(rows*columns,false));
        button.setOnClickListener(this);
        buttons[rows*columns]=button;
        relativeLayout.addView(button);
        return relativeLayout;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        l.info("on create options menu");
        super.onCreateOptionsMenu(menu);
        for(Tablet.MenuItem menuItem : Tablet.MenuItem.values()) {
            System.out.println("add menu item: "+menuItem);
            menu.add(Menu.NONE,menuItem.ordinal(),Menu.NONE,menuItem.name());
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        l.info("item: "+item);
        int id=item.getItemId();
        if(Tablet.MenuItem.isItem(id)) {
            Tablet.MenuItem.doItem(id,tablet);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        //Main.stop();
        // what should we stop here?
        if(tablet!=null)
            tablet.stopListening(tablet);
        else
            System.out.println("tablet is null in on destroy!");
        super.onDestroy();
    }
    @Override
    public void onClick(final View v) {
        p("click &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if(v instanceof Button) {
            Button button=(Button)v;
            int index=button.getId();
            guiAdapterABC.processClick(index);
        } else
            l.severe("not a button!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(o==tablet.model)
            guiAdapterABC.update(o,hint);
        else
            System.out.println("no gui for model: "+o);
    }
    static String getIpAddressFromWifiManager(Context context) { // unused
        WifiManager wifiMan=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf=wifiMan.getConnectionInfo();
        int ipAddress=wifiInf.getIpAddress();
        if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
            ipAddress=Integer.reverseBytes(ipAddress);
        byte[] ipByteArray=BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString=null;
        try {
            ipAddressString=InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch(UnknownHostException ex) {
            staticLogger.warning("Unable to get host address.");
            ipAddressString=null;
        }
        return ipAddressString;
    }
    GuiAdapterABC guiAdapterABC;
    Tablet tablet;
    MediaPlayer mediaPlayer;
    TextView bottom; // was used for messages, put it back
    Button[] buttons;
    final Logger l=Logger.getLogger(getClass().getName());
    final static Logger staticLogger=Logger.getLogger(MainActivity.class.getName());
}