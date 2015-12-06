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
    public class DrawableView extends View {
        public DrawableView(Context context,int i,boolean isRow1) {
            super(context);
            int r=(int)round(w*radius);
            d0=new ShapeDrawable(new RoundRectShape(new float[]{r,r,r,r,r,r,r,r},null,null));
            d0.getPaint().setColor(0xff000000);
            d0.setBounds(0,0,w,d);
            d1=new ShapeDrawable(new RoundRectShape(new float[]{r,r,r,r,r,r,r,r},null,null));
            d1.getPaint().setColor(on[i]);
            d1.setBounds(edge,edge,w-edge,d-edge);
            d2=new ShapeDrawable(new RoundRectShape(new float[]{r,r,r,r,r,r,r,r},null,null));
            int b=(int)round(w*border);
            d2.setBounds(b/2,b/2,w-b/2,d-b/2);
            d2.getPaint().setColor(isRow1?on[i]:off[i]);
        }
        protected void onDraw(Canvas canvas) {
            d0.draw(canvas);
            d1.draw(canvas);
            d2.draw(canvas);
        }
        ShapeDrawable d0, d1, d2;
    }
    LinearLayout row(boolean isRow1) {
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(w,d);
        int m=(int)round(w*margin);
        layoutParams.setMargins(m,m,m,m);
        for(int i=0;i<n;i++) {
            drawableView=new DrawableView(this,i,isRow1);
            layout.addView(drawableView,layoutParams);
        }
        return layout;
    }
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
    String getIpAddress() {
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
        IO io=new IO("192.168.0.");
        Group group=new Group(io,1,Group.groups.get("g0"));
        tablet=group.getTablet();
        tablet.model.addObserver(this);
        tablet.model.addObserver(new AudioObserver(tablet.model));
        tablet.group.io.startListening(tablet);
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
        Toaster toaster=new Toaster() {
            @Override
            public void toast(String string) {
                Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
            }
        };
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
            int id=index+1;
            if(index==colors.rows*colors.columns) {
                tablet.model.reset();
                Message message=Message.reset(tablet.group.groupId,tablet.tabletId(),id);
                tablet.send(message,0);
            } else {
                Boolean state=!tablet.model.state(id);
                tablet.model.setState(id,state);
                Message message=new Message(Message.Type.normal,tablet.group.groupId,tablet.tabletId(),id,tablet.model.toCharacters());
                tablet.send(message,0);
                Toaster.toaster.toast(buttonsToString());
            }
        } else
            logger.warning("not a button!");
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
    // new gui stuff
    final int n=5, edge=1;
    double margin=.10, radius=.05, border=.15;
    int w, d;
    final int[] on=new int[]{0xffff0000,0xffffff00,0xff00ff00,0xff0000ff,0xffff8000};
    final int[] off=new int[]{0xffff0000,0xffffff00,0xff00ff00,0xff0000ff,0xffff8000};
    DrawableView drawableView;
    final Logger logger=Logger.getLogger(getClass().getName());
}