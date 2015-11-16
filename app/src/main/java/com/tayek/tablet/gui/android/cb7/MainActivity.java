package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.Settings.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.tayek.io.*;
import com.tayek.io.Audio.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.gui.common.*;

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
    void buildGui(final Tablet tablet,final Toaster toaster) {
        guiAdapterABC=new GuiAdapterABC(tablet.group.model) {
            @Override
            public void setButtonText(final int id,final String string) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Button)idToButton.get(id)).setText(string);
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
                                System.out.println("set "+id+" checked "+state);
                                int c=color(id,state);
                                buttons[id-1].setBackgroundColor(c);
                            }
                        });
                    }
                },0);
            }
        };
        DisplayMetrics m=getResources().getDisplayMetrics();
        System.out.println(m);
        double w=m.widthPixels*.9;
        double h=m.widthPixels*.9/tablet.group.model.buttons;
        System.out.println(w+" by +"+h);
        ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams((int)w,(int)h);
        System.out.println(lp);
        LinearLayout layout=new LinearLayout(MainActivity.this);
        // http://stackoverflow.com/a/11469528/51292
        // http://android-coding.blogspot.in/2011/05/resize-button-programmatically-using.html
        layout.setOrientation(LinearLayout.VERTICAL);  //Can also be done in xml by android:orientation="vertical"
        final TextView top=new TextView(MainActivity.this);
        top.setText("top");
        layout.addView(top);
        MainActivity.this.buttons=new Button[tablet.group.model.buttons];
        for(int i=1;i<=tablet.group.model.buttons;i++) {
            Button button=new Button(MainActivity.this);
            buttons[i-1]=button;
            button.setLayoutParams(new ViewGroup.LayoutParams(lp));
            button.setText("Button "+i);
            button.setId(i);
            button.setBackgroundColor(color(i,false));
            layout.addView(button);
            button.setOnClickListener(MainActivity.this);
            //gui.idToButton.put(i,button); // too early
        }
        bottom=new TextView(MainActivity.this);
        bottom.setText("bottom\n");
        layout.addView(bottom);
        MainActivity.this.layout=layout;
        System.out.println(idToButton);
        for(int i=1;i<=tablet.group.model.buttons;i++)
            idToButton.put(i,buttons[i-1]);
    }
    void initialize() {
        Main.log.init();
        Main.log.setLevel(Level.ALL);
        // move to common init?
        System.out.println(System.getProperty("os.name"));
        Properties properties=java.lang.System.getProperties();
        logger.info(properties.size()+" properties.");
        for(Map.Entry<Object,Object> entry : properties.entrySet())
            logger.info(entry.getKey()+"="+entry.getValue());
        System.out.println(Arrays.asList(Sound.values()));
        //
        android_id=Secure.getString(getContentResolver(),Secure.ANDROID_ID);
        logger.info("android id: '"+android_id+"'");
        if(!isNetworkAvailable())
            System.out.println("network is not available!");
        ((Audio.Android)Main.audio).setCallback(new Android.Callback<Sound>() {
            @Override
            public void call(Sound sound) {
                System.out.println("playing sound.");
                Integer id=id(sound);
                mediaPlayer=MediaPlayer.create(MainActivity.this,id);
                mediaPlayer.start();
            }
        });
        Integer tabletId=Group.androidIds.get(android_id);
        if(tabletId==null)
            tabletId=100;
        final Group group=new Group(1,Group.tabletsFive);
        String host=group.idToHost().get(tabletId);
        System.out.println("host="+host);
        tablet=new Tablet(group,tabletId);
        System.out.println("tablet: "+tablet);
        Toaster toaster=new Toaster() {
            @Override
            public void toast(String string) {
                Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
            }
        };
        buildGui(tablet,toaster);
        Main.toaster=new Toaster() {
            @Override
            public void toast(final String string) {
                strings.add(string);
                if(strings.size()>n)
                    strings.remove(0);
                bottom.post(new Runnable() {
                    public void run() {
                        String lines="";
                        for(String string : strings)
                            lines+=string+"\n";
                        bottom.setText(lines);
                    }
                });
            }
            int n=5;
            final List<String> strings=new LinkedList();
        };
        setContentView(layout);
    }
    public String buttonsToString() {
        String s="{";
        for(int i=0;i<tablet.group.model.buttons;i++)
            s+=buttons[i].isPressed()?'T':"F";
        s+='}';
        return s;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        tablet.startListening();
        tablet.group.model.addObserver(this);
        setContentView(layout);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.info("on create options menu");
        super.onCreateOptionsMenu(menu);
        for(TabletMenuItem menuItem : TabletMenuItem.values())
            menu.add(Menu.NONE,menuItem.ordinal(),Menu.NONE,menuItem.name());
        //getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.info("item: "+item);
        int id=item.getItemId();
        TabletMenuItem.doItem(id,tablet);
        if(id==R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
    }
    int color(int id,boolean state) {
        bg[0]=fg[0]=(float)((id-1)*360./tablet.group.model.buttons);
        return state?Color.HSVToColor(fg):Color.HSVToColor(bg);
    }
    @Override
    public void onClick(final View v) {
        if(v instanceof Button) {
            System.out.println("click on "+v);
            Button button=(Button)v;
            int id=button.getId();
            boolean state=tablet.group.model.state(id);
            tablet.group.model.setState(id,!state);
            com.tayek.tablet.model.Message message=new com.tayek.tablet.model.Message(tablet.group.groupId,tablet.tabletId,com.tayek.tablet.model.Message.Type.normal,id,tablet.group.model.state(id));
            tablet.broadcast(message);
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
    String android_id;
    Tablet tablet;
    Button[] buttons;
    MediaPlayer mediaPlayer;
    TextView bottom;
    LinearLayout layout;
    final Map<Integer,Object> idToButton=new LinkedHashMap<>();
    static float[] fg=new float[3];
    {
        fg[1]=1;
        fg[2]=1;
    }
    static float[] bg=new float[3];
    {
        bg[1]=.7f;
        bg[2]=.6f;
    }
    final Logger logger=Logger.getLogger(getClass().getName());
}
