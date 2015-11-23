package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.Settings.*;
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
    class Gui {
        Gui(final MainActivity activity) {
            relativeLayout=new RelativeLayout(activity);
            int size=130;
            final int rows=2;
            final int columns=5;
            int x0=size/4, y0=75;
            for(int i=0;i<columns;i++) {
                on[i]|=0xff000000;
                off[i]|=0xff000000;
            }
            for(Integer i:on)
                System.out.print(Integer.toString(i,16)+" ");
            System.out.println();
            for(Integer i:off)
                System.out.print(Integer.toString(i,16)+" ");
            System.out.println();
            System.out.println("cyan: "+Integer.toString(Color.CYAN,16)+" ");
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(50,50);
            for(int i=0;i<rows*columns;i++) {
                Button button=new Button(activity);
                button.setId(i);
                params=new RelativeLayout.LayoutParams(size,size);
                params.leftMargin=(int)(x0+i%columns*1.2*size);
                params.topMargin=(int)(y0+i/columns*size*1.2);
                button.setLayoutParams(params);
                button.setText(""+(i+1));
                if(i/columns%2==1)
                    button.setBackgroundColor(off[i%columns]);
                button.setOnClickListener(activity);
                buttons[i]=button;
                relativeLayout.addView(button);
            }
            Button reset=new Button(activity);
            reset.setId(rows*columns);
            params=new RelativeLayout.LayoutParams(size,size);
            params.leftMargin=(int)(x0+(columns+1.2)*size);
            params.topMargin=y0;
            reset.setLayoutParams(params);
            reset.setText("R");
            buttons[rows*columns]=reset;
            relativeLayout.addView(reset);
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
                                    if(index==n) { // reset
                                        System.out.println("reset");
                                    } else {
                                        if(index/columns%2==1)
                                            buttons[index].setBackgroundColor((!state)?on[index%columns]:off[index%columns]);
                                        else;
                                    }
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
            for(int i=0;i<n;i++)
                s+=buttons[i].isPressed()?'T':"F";
            s+='}';
            return s;
        }
        final RelativeLayout relativeLayout;
        final int n=11;
        final int rows=2;
        final int columns=5;
        Button[] buttons=new Button[n];
        Integer[] on=new Integer[columns];
        Integer[] off=new Integer[columns];
        {
            on[0]=0xff0000;
            on[1]=0xffff00;
            on[2]=0x00ff00;
            on[3]=0x0000ff;
            on[4]=0xffa500;
            off[0]=0x7e3517;
            off[1]=0xaf9b60;
            off[2]=0x254117;
            off[3]=0x0000A0;
            off[4]=0xf88017;
        }
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
        setContentView(R.layout.activity_main);
        Main.log.init();
        Main.log.setLevel(Level.ALL);
        android_id=Secure.getString(getContentResolver(),Secure.ANDROID_ID);
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
        gui=new Gui(this);
        guiAdapterABC=gui.adapter(tablet);
        setContentView(gui.relativeLayout);
        tablet.startListening();
        tablet.group.model.addObserver(this);
        Main.sound=false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.info("on create options menu");
        super.onCreateOptionsMenu(menu);
        for(Tablet.MenuItem menuItem : Tablet.MenuItem.values())
            menu.add(Menu.NONE,menuItem.ordinal(),Menu.NONE,menuItem.name());
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
            int id=button.getId();
            boolean state=tablet.group.model.state(id);
            tablet.group.model.setState(id,!state);
            Message message=new Message(Message.Type.normal,tablet.group.groupId,tablet.tabletId(),id,tablet.group.model.toCharacters());
            tablet.send(message,0);
            System.out.println("on click: "+id+" "+tablet.group.model+" "+gui.buttonsToString());
            Main.toaster.toast(gui.buttonsToString());
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
    Gui gui;
    GuiAdapterABC guiAdapterABC;
    String android_id;
    Tablet tablet;
    MediaPlayer mediaPlayer;
    TextView bottom;
    LinearLayout layout;
    final Logger logger=Logger.getLogger(getClass().getName());
}
