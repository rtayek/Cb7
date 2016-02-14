package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.net.*;
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
    void alert(String string) {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle(string);
        alert.setMessage(string);
        alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int whichButton) {
                //Your action here
            }
        });
        l.info("showing alert.");
        alert.show();
    }
    void startSockethandler() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    l.info("start socket handler");
                    LoggingHandler.startSocketHandler(Main.defaultLogServerHost,LogServer.defaultService);
                    if(LoggingHandler.socketHandler!=null)
                        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
                    l.info("socket handler: "+LoggingHandler.socketHandler);
                } catch(Exception e) {
                    l.info("caught: "+e);
                }
            }
        }).start();
    }
    void setupAudioPlayer() {
        ((Audio.Bndroid)Audio.audio).setCallback(new IO.Callback<Audio.Sound>() {
            @Override
            public void call(Audio.Sound sound) {
                Integer id=id(sound);
                l.warning("sound: "+sound+", id: "+id);
                if(id!=null) {
                    mediaPlayer=MediaPlayer.create(MainActivity.this,id);
                    mediaPlayer.start();
                } else
                    l.warning("id for sound: "+sound+" is null!");
            }
            Integer id(Audio.Sound sound) {
                switch(sound) {
                    case electronic_chime_kevangc_495939803:
                        return R.raw.electronic_chime_kevangc_495939803;
                    case glass_ping_go445_1207030150:
                        return R.raw.glass_ping_go445_1207030150;
                    case store_door_chime_mike_koenig_570742973:
                        return R.raw.store_door_chime_mike_koenig_570742973;
                    default:
                        l.warning(""+" "+"default where!");
                        return null;
                }
            }
        });
    }
    void setupToast() {
        ((Toaster.Android_)Toaster.toaster).setCallback(new IO.Callback<String>() {
            @Override
            public void call(final String string) {
                if(false)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    l.info("toast was: "+string);
            }
        });
    }
    void startTablet(Set<InetAddress> addresses) {
        Group group=new Group(1,new Group.Groups().groups.get("g0"),Model.mark1,Group.defaultOptions);
        tablet=group.getTablet(addresses.iterator().next(),null);
        l.info("options: "+tablet.group.options);
        tablet.model.addObserver(this);
        tablet.model.addObserver(new AudioObserver(tablet.model));
        tablet.startListening();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        String ipAddress=getIpAddressFromWifiManager();
        l.info("get ip address from wifi manager says: "+ipAddress);
        if(ipAddress==null) {
            alert("wifi ip address is null!");
            l.info("sleeping.");
            try {
                Thread.sleep(10_000);
            } catch(InterruptedException e) {
            }
            l.info("finish.");
            finish();
            l.info("exiting after sleep.");
            System.exit(0);
        } else {
            alert("wifi ip address is: "+ipAddress);
        }
        if(false)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress localHost=InetAddress.getLocalHost();
                        l.info("localhost: "+localHost);
                        String host=localHost.getHostName();
                        l.info("host: "+host);
                        InetAddress inetAddress=InetAddress.getByName(host);
                        l.info("address: "+inetAddress);
                    } catch(UnknownHostException e) {
                        l.info("caught: "+e);
                    }
                }
            }).start();
        if(true) // need to run this on a thread
            startSockethandler();
        setContentView(R.layout.activity_main);
        l.info("android id: "+Secure.getString(getContentResolver(),Secure.ANDROID_ID));
        setupAudioPlayer();
        setupToast();
        l.info("playing sound.");
        Audio.audio.play(Audio.Sound.electronic_chime_kevangc_495939803);
        InetAddress inetAddress=null;
        Set<InetAddress> addresses=myInetAddress(Main.networkPrefix);
        l.info("addresses: "+addresses);
        if(addresses.size()==0)
            alert("can not get ip address!");
        startTablet(addresses);
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
                button.setText(tablet.getButtonText(Integer.valueOf(i+1)));
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
        if(Tablet.MenuItem.isItem(id))
            if(Tablet.MenuItem.item(id).equals(Tablet.MenuItem.Quit)) {
                alert("foo");
                finish();
                l.severe("after finish! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            } else {
                // check here for stuff just for real tablets
                Tablet.MenuItem.doItem(id,tablet);
                return true;
            } else l.severe(item+" is not atablet men item!");
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
    void retry() {
        WifiManager wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf;
        int ipAddress;List<WifiConfiguration> wifiConfigurations=wifiMan.getConfiguredNetworks();
        l.info("wifi configurations (all): "+wifiConfigurations.size());
        for(WifiConfiguration wifiConfiguration : wifiConfigurations) {
            l.info("wifi configuration status is disabled: "+(wifiConfiguration.status==WifiConfiguration.Status.DISABLED));
            l.info("wifi configuration toString(): "+wifiConfiguration.toString());
            if(wifiConfiguration.toString().contains(Main.networkPrefix)) {
                l.info("found our network: "+wifiConfiguration.toString());
                int networkId=wifiConfiguration.networkId;
                boolean ok=wifiMan.disconnect();
                l.info("disconnect() returns: "+ok);
                l.info("enabling our network: "+networkId);
                // https://code.google.com/p/android-developer-preview/issues/detail?id=2218
                ok=wifiMan.enableNetwork(networkId,true);
                l.info("enableNetwork() returns: "+ok);
                l.info("trying to recommect to wifi.");
                ok=wifiMan.reconnect();
                l.info("reconnect returns: "+ok);
                if(ok) {
                    wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
                    wifiInf=wifiMan.getConnectionInfo();
                    ipAddress=wifiInf.getIpAddress();
                    l.info("wifi ip adress: "+ipAddress);
                }
                break;
            }
        }
    }
    String getIpAddressFromWifiManager() {
        WifiManager wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        p("isWifiEnabled() returns: "+wifiMan.isWifiEnabled());
        WifiInfo wifiInf=wifiMan.getConnectionInfo();
        p("wifi info: "+wifiInf);
        int ipAddress=wifiInf.getIpAddress();
        p("wifi ip adress: "+ipAddress);
        if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
            ipAddress=Integer.reverseBytes(ipAddress);
        byte[] ipByteArray=BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString=null;
        try {
            ipAddressString=InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch(UnknownHostException ex) {
            l.warning("Unable to get host address.");
            if(true) retry();
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