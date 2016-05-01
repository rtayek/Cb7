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
public class MainActivity extends Activity implements Observer, View.OnClickListener, Tablet.HasATablet {
    @Override
    public Tablet tablet() {
        return tablet;
    }
    @Override public void setStatusText(final String text) {
        p("set status: "+text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(text);
            }
        });
    }
    @Override
    public void setTablet(Tablet tablet) {
        this.tablet=tablet;
    }
    void alert(String string,boolean cancelable) {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle(string);
        alert.setMessage(string);
        alert.setCancelable(cancelable);
        alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int whichButton) {
                //Your action here
            }
        });
        l.info("showing alert.");
        alert.show();
    }
    RelativeLayout builGui() {
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics);
        final int size=225;
        final float fontsize=size*.30f;
        System.out.println(size+" "+(metrics.widthPixels*1./7));
        final int x0=size/4, y0=75;
        p("x0="+x0+", y0="+y0);
        RelativeLayout relativeLayout=new RelativeLayout(this);
        RelativeLayout.LayoutParams params=null;
        final int rows=colors.rows;
        final int columns=colors.columns;
        buttons=new Button[colors.n]; // colors is intimatley tied to the mark 1 model!
        for(int i=0;i<rows*columns;i++) {
            Button button=new Button(this);
            button.setId(i); // id is index!
            button.setTextSize(fontsize);
            button.setGravity(Gravity.CENTER);
            params=new RelativeLayout.LayoutParams(size,size);
            params.leftMargin=(int)(x0+i%columns*1.2*size);
            params.topMargin=(int)(y0+i/columns*size*1.2);
            //p("button: "+i+", left margin="+params.leftMargin+", top margin="+params.topMargin);
            button.setLayoutParams(params);
            if(i/columns%2==0)
                button.setText(""+(char)('0'+(i+1)));
            button.setBackgroundColor(colors.aColor(i,false));
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
        p("reset: left margin="+params.leftMargin+", top margin="+params.topMargin);
        button.setLayoutParams(params);
        button.setText("R");
        button.setBackgroundColor(colors.aColor(rows*columns,false));
        button.setOnClickListener(this);
        buttons[rows*columns]=button;
        relativeLayout.addView(button);
        status=new Button[group.keys().size()];
        for(int i=0;i<group.keys().size();i++) {
            double x=x0+i*1.2*size/3;
            double y=y0+(3-.5)*size*1.2;
            button=getButton(size,""+Integer.valueOf(i+1),fontsize,rows,columns,i,(int)x,(int)y);
            status[i]=button;
            relativeLayout.addView(button);
        }
        int i=group.keys().size()+3;
        double xr=x0+i*1.2*size/3;
        double yr=y0+(3-.5)*size*1.2;
        //set layout params?
        wifiStatus=getButton(size,"w",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(wifiStatus);
        xr+=1.2*size/3;
        routerStatus=getButton(size,"r",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(routerStatus);
        if(false) {
            int testButtons=11;
            test=new Button[testButtons];
            for(i=0;i<testButtons;i++) {
                double x=x0+i*1.2*size/3;
                double y=y0+(3-.5)*size*1.2;
                y-=size*1.2/3;
                int r=(int)(i*.1*100);
                if(r>=100)
                    r=99;
                else if(r<0)
                    r=0;
                String text=""+r;
                while(text.length()<2)
                    text+='0';
                button=getButton((int)(size*1.),text,(float)(fontsize*.6),rows,columns,i,(int)x,(int)y);
                p("setting backgroud to: "+Colors.toString(Colors.smooth(i*.1)));
                button.setBackgroundColor(Colors.aColor(Colors.smooth(i*.1)));
                test[i]=button;
                relativeLayout.addView(button);
            }
        }
        relativeLayout.setBackgroundColor(colors.background|0xff000000);
        return relativeLayout;
    }
    void retry() {
        WifiManager wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf;
        int ipAddress;
        List<WifiConfiguration> wifiConfigurations=wifiMan.getConfiguredNetworks();
        if(wifiConfigurations==null) {
            return;
        }
        l.info("wifi configurations (all): "+wifiConfigurations.size());
        for(WifiConfiguration wifiConfiguration : wifiConfigurations) {
            l.info("wifi configuration status is disabled: "+(wifiConfiguration.status==WifiConfiguration.Status.DISABLED));
            //l.info("wifi configuration toString(): "+wifiConfiguration.toString());
            if(wifiConfiguration.toString().contains(tabletNetworkPrefix)) {
                //p("IP config: "+wifiConfiguration.getIpConfiguration());
                //l.info("found our network: "+wifiConfiguration.toString());
                int networkId=wifiConfiguration.networkId;
                l.info("found our network id: "+networkId);
                boolean ok=wifiMan.disconnect();
                l.info("enabling our network: "+networkId);
                // https://code.google.com/p/android-developer-preview/issues/detail?id=2218
                ok=wifiMan.enableNetwork(networkId,true);
                ok=wifiMan.reconnect();
                l.info("reconnect returns: "+ok);
                if(ok) {
                    wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
                    wifiInf=wifiMan.getConnectionInfo();
                    p("wifi inf: "+wifiInf);
                    ipAddress=wifiInf.getIpAddress();
                    l.info("wifi ip adress: "+ipAddress);
                }
                break;
            }
        }
    }
    InetAddress getIpAddressFromWifiManager() {
        WifiManager wifiMan=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        p("isWifiEnabled() returns: "+wifiMan.isWifiEnabled());
        WifiInfo wifiInf=wifiMan.getConnectionInfo();
        p("wifi info: "+wifiInf);
        int ipAddress=wifiInf.getIpAddress();
        String ipAddressString=null;
        if(ipAddress!=0) {
           // if(ipAddress.isL)
            p("wifi ip address string: "+ipAddress);
            if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
                ipAddress=Integer.reverseBytes(ipAddress);
            p("ipAddress string: "+Integer.toHexString(ipAddress));
            byte[] ipByteArray=BigInteger.valueOf(ipAddress).toByteArray();
            try {
                ipAddressString=InetAddress.getByAddress(ipByteArray).getHostAddress();
            } catch(UnknownHostException ex) {
                l.warning("Unable to get host address for: "+Integer.toHexString(ipAddress));
                if(true)
                    retry();
            }
        } else
            retry();
        InetAddress inetAddress=null;
        try {
            inetAddress=InetAddress.getByName(ipAddressString);
        } catch(UnknownHostException e) {

        }
        return inetAddress;
    }
    public class IpAddress implements Callable<InetAddress> {
        @Override
        public InetAddress call() throws Exception {
            System.out.println("calling: getIpAddressFromWifiManager()");
            InetAddress inetAddress=getIpAddressFromWifiManager();
            System.out.println("getIpAddressFromWifiManager() returns: "+inetAddress);
            return inetAddress;
        }
    }
    public class IpAddressCallable implements Runnable {
        @Override
        public void run() {
            int n=20;
            for(int i=1;i<=n;i++) {
                System.out.println("ipAddressCallable: i="+i);
                Future<InetAddress> future=executorService.submit(new IpAddress());
                while(!future.isDone())
                    try {
                        Thread.sleep(200);
                    } catch(InterruptedException e) {
                        System.out.println("1 caught: "+e);
                    }
                try {
                    InetAddress inetAddress=future.get();
                    if(inetAddress!=null) {
                        System.out.println("future.get() returns: "+inetAddress);
                        //Toast.makeText(MainActivity.this,"wifi ip address is: "+ipAddress,Toast.LENGTH_LONG).show(); // throws: Can't create handler inside thread that has not called Looper.prepare()
                        gotWifiUpOrFail=true;
                        break;
                    }
                } catch(InterruptedException e) {
                    System.out.println("2 caught: "+e);
                } catch(ExecutionException e) {
                    System.out.println("3 caught: "+e);
                }
                try {
                    Thread.sleep(1_000);
                } catch(InterruptedException e) {
                    System.out.println("4 caught: "+e);
                }
                System.out.println("---------------------------------");
            }
            gotWifiUpOrFail=true;
        }
    }
    void setupAudioPlayer() {
        ((AndroidAudio)Audio.audio).setCallback(new Callback<Audio.Sound>() {
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
                else
                    ; //p("toast was: "+string);
            }
        });
    }
    void waitForWifi() {
        p("waiting for wifi.");
        new Thread(new IpAddressCallable()).start();
        int n=10;
        for(int i=1;i<=n&&!gotWifiUpOrFail;i++)
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                System.out.println("5 caught: "+e);
            }
    }
    Histories histories(int index) {
        Histories histories=null;
        if(tablet!=null) {
            int tabletIndex=index-tablet.model().colors.rows*tablet.model().colors.columns;
            if(0<=tabletIndex&&tabletIndex<group.keys().size()) {
                String tabletId=IO.aTabletId(tabletIndex+1);
                histories=group.required(tabletId).histories();
            } else {
                l.severe(index+" is bad index!");
            }
        }
        return histories;
    }
    void setButtonColor(final Button button,final int color) {
        if(button!=null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setBackgroundColor(color);
                }
            });
    }
    private Button getButton(int size,String string,float fontsize,int rows,int columns,int i,int x,int y) {
        Button button;
        RelativeLayout.LayoutParams params;
        button=new Button(this);
        button.setId(rows*columns+i); // id is index!
        button.setTextSize(fontsize/4);
        button.setGravity(Gravity.CENTER);
        params=new RelativeLayout.LayoutParams(size/3,size/3);
        params.leftMargin=x;
        params.topMargin=y;
        //p("other: "+i+", left margin="+params.leftMargin+", top margin="+params.topMargin);
        button.setLayoutParams(params);
        button.setText(string);
        button.setBackgroundColor(colors.aColor(colors.whiteOn));
        button.setOnClickListener(this);
        return button;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        p("onCreate at: "+et);
        super.onCreate(savedInstanceState);
        l.info("android id: "+Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        setupToast();
        Logger global=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        if(false)
            LoggingHandler.toggleSockethandlers();
        Map<String,Required> requireds=new TreeMap<>(new Group.Groups().groups.get("g0"));
        group=new Group("1",requireds,MessageReceiver.Model.mark1);
        p("starting runner at: "+et);
        new Thread(runner=new Runner(group,this),"tablet runner").start();
        p("exit onCreate at: "+et);
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
        l.info("item: "+item);
        int id=item.getItemId();
        if(Enums.MenuItem.isItem(id))
            if(Enums.MenuItem.item(id).equals(Enums.MenuItem.Quit)) {
                alert("Quitting",false);
                finish();
                l.severe("after finish! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                try {
                    Thread.sleep(5_000);
                } catch(InterruptedException e) {
                    l.info("caught: "+e);
                }
                System.exit(0);
            } else {
                // check here for stuff just for real tablets
                Enums.MenuItem.doItem(id,tablet);
                return true;
            }
        else if(id==Enums.MenuItem.values().length) { // some hack for restarting tablet?
            Intent i=getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else if(Enums.LevelSubMenuItem.isItem(id-Enums.MenuItem.values().length)) {
            Enums.LevelSubMenuItem.doItem(id-Enums.MenuItem.values().length); // hack!
            return true;
        } else
            l.severe(item+" is not atablet men item!");
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        p("in on destry() &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if(runner!=null)
            runner.thread.interrupt();
        if(tablet!=null)
            ((Group.TabletImpl2)tablet).stopListening();
        else
            System.out.println("tablet is null in on destroy!");
        super.onDestroy();
        //System.runFinalizersOnExit(true);
        //System.exit(0);
    }
    @Override
    public void onClick(final View v) {
        p("click &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if(v instanceof Button) {
            Button button=(Button)v;
            int index=button.getId();
            if(guiAdapterABC!=null)
                guiAdapterABC.processClick(index);
            else
                l.severe("gui adapter is null!");
        } else
            l.severe("not a button!");
        if(lastClick!=Double.NaN)
            p((et.etms()-lastClick)+" between clicks.");
        lastClick=et.etms();
        if(tablet!=null) {
            Iterator<String> s=group.keys().iterator();
            p("tablets: "+group.keys());
            for(int i=0;i<group.keys().size();i++) {
                Histories histories=group.required(s.next()).histories();
                p("histories: "+histories);
                double recentFaulureRate=histories.senderHistory.history.recentFailureRate();
                int color=histories.senderHistory.history.attempts()==0?colors.aColor(Colors.yellow):colors.aColor(colors.smooth(recentFaulureRate));
                p("recent failure rate: "+recentFaulureRate+", color: "+Colors.toString(color));
                status[i].setBackgroundColor(color);
            }
        } else
            p("tablet is null!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(model.equals(o))
            guiAdapterABC.update(o,hint);
        else
            System.out.println("not our model: "+o);
    }
    {
        p("<init> "+et);
    }
    static final Et et=new Et();
    static {
        p("<static init> "+et+" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }
    Double lastClick=Double.NaN;
    ExecutorService executorService=Executors.newFixedThreadPool(10);
    boolean gotWifiUpOrFail=false;
    GuiAdapter.GuiAdapterABC guiAdapterABC;
    Group group;
    MessageReceiver.Model model;
    Colors colors;
    Tablet tablet;
    MediaPlayer mediaPlayer;
    TextView bottom; // was used for messages, put it back
    Button[] buttons, status, test;
    Button wifiStatus, routerStatus;
    Runner runner;
}