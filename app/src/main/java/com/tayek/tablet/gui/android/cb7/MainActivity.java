package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
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

import java.lang.*;
import java.lang.System;
import java.util.*;
import java.util.logging.*;
//https://plus.google.com/103583939320326217147/posts/BQ5iYJEaaEH driver for usb
//http://davidrs.com/wp/fix-android-device-not-showing-up-on-windows-8/
public class MainActivity extends Activity implements Observer, View.OnClickListener {
    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo!=null&&activeNetworkInfo.isConnected();
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
                                int index=id-1;
                                buttons[index].setBackgroundColor(colors.aColor(index,state));
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics);
        setContentView(R.layout.activity_main);
        Main.log.init();
        Main.log.setLevel(Level.ALL);
        String android_id=Secure.getString(getContentResolver(),Secure.ANDROID_ID);
        ((Android)Main.audio).setCallback(new Android.Callback<Sound>() {
            @Override
            public void call(Sound sound) {
                System.out.println("playing sound.");
                Integer id=id(sound);
                mediaPlayer=MediaPlayer.create(MainActivity.this,id);
                mediaPlayer.start();
            }
        });
        Integer tabletId=Group.tabletIdFromAndroidId(android_id);
        logger.info("tablet id: "+tabletId);
        System.out.println("tablet id: "+tabletId);
        if(tabletId==null)
            tabletId=100;
        Map<Integer,Group.Info> map=Group.groups.get("g0");
        Group group=new Group(1,map);
        tablet=new Tablet(group,tabletId);
        Toaster toaster=new Toaster() {
            @Override
            public void toast(String string) {
                Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
            }
        };
        Main.toaster=new Toaster() {
            @Override
            public void toast(final String string) {
                System.out.println(string);
            }
        };
        int size=140;
        System.out.println(size+" "+(metrics.widthPixels*1./7));
        int x0=size/4, y0=75;
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
        Button reset=new Button(this);
        reset.setId(rows*columns);
        params=new RelativeLayout.LayoutParams(size,size);
        params.leftMargin=(int)(x0+(columns+1.2)*size);
        params.topMargin=y0;
        reset.setLayoutParams(params);
        reset.setText("R");
        buttons[rows*columns]=reset;
        reset.setBackgroundColor(colors.aColor(rows*columns,false));
        relativeLayout.addView(reset);
        guiAdapterABC=adapter(tablet);
        relativeLayout.setBackgroundColor(colors.background|0xff000000);
        setContentView(relativeLayout);
        tablet.startListening();
        tablet.group.model.addObserver(this);
        tablet.group.model.addObserver(new AudioObserver(tablet.group.model));

        Main.sound=true;
        int id=R.raw.electronic_chime_kevangc_495939803;
        mediaPlayer=MediaPlayer.create(MainActivity.this,id);
        mediaPlayer.start();
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
        Main.stop();
        if(tablet!=null)
            tablet.stopListening();

        else
            System.out.println("tablet is null in on destroy!");
        super.onDestroy();
    }
    @Override
    public void onClick(final View v) {
        if(v instanceof Button) {
            System.out.println("click on "+v);
            Button button=(Button)v;
            int index=button.getId();
            int id=index+1;
            boolean state=tablet.group.model.state(id);
            tablet.group.model.setState(id,!state);
            Message message=new Message(Message.Type.normal,tablet.group.groupId,tablet.tabletId(),id,tablet.group.model.toCharacters());
            tablet.send(message,0);
            System.out.println("on click: "+id+" "+tablet.group.model+" "+buttonsToString());
            Main.toaster.toast(buttonsToString());
        } else
            logger.warning("not a button!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(o==tablet.group.model)
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
