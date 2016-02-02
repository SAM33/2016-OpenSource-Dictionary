/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                             *
 *                         GNU General Public License                          *
 *                                                                             *
 *           本軟體使用GNU GPL授權，允許                                          *
 *           1. 以任何目的執行此程式的自由；                                       *
 *           2. 再發行複製件的自由；                                              *
 *           3. 改進此程式，並公開發布改進的自由                                    *
 *                                                                             *
 *           不過，相反地，所有GPL程式的演繹作品也要在GPL之下發布，促進自由軟體發展     *
 *                                                                             *
 *           本軟體作者：Sam33,國立中央大學資訊工程研究所                           *
 *                                                                             *
 *           本軟體在GNU GPL授權下使用星際譯王懶蟲字典的資料字典檔案                  *
 *                                                                             *
 *                                                                             *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.example.sam33.translater;

/**
 * Created by sam33 on 16/1/30.
 */
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

/**
 * Created by sam33 on 16/1/30.
 */


public class FloatWindow extends Service{

    public static final String CommID = "sam33.translater.FloatWindow";
    private WindowManager wm;
    private WindowManager.LayoutParams wmp;
    private LinearLayout ll;
    private TextView word;
    private Handler BackgroundHandler;
    private HandlerThread BackgroundThread;
    private Handler UIHandler;
    private Button depthup;
    private Button depthdown;
    public BroadcastReceiver Receiver;

    public FloatWindow()
    {
        super();
    }

    public void updatedepth(int value)
    {
        int depth = 255 - value;
        ll.setBackgroundColor(Color.argb(255 - depth, depth, depth, depth));
    }
    public void updatesize(int level)
    {
        int height = 33*(level+2);
        wmp.height = height;
        wm.updateViewLayout(ll, wmp);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FloatWindow", "onDestroy");
        unregisterReceiver(Receiver);
        //BackgroundThread.quitSafely();  //android 4.1.2 not suppot
        wm.removeView(ll);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("FloatWindow", "onCreate");
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        //llp.setLayoutDirection(LinearLayout.HORIZONTAL); //Not support by android 4.1.2
        ll.setLayoutParams(llp);
        wmp = new WindowManager.LayoutParams(500,99, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        wmp.x = 0;
        wmp.y = 0;
        wmp.gravity = Gravity.TOP;
        wm.addView(ll, wmp);
        word = new TextView(this);
        ll.addView(word);
        ll.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams updatewmp = wmp;
            int x, y;
            float tx, ty;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = updatewmp.x;
                        y = updatewmp.y;
                        tx = motionEvent.getRawX();
                        ty = motionEvent.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatewmp.x = (int) (x + motionEvent.getRawX() - tx);
                        updatewmp.y = (int) (y + motionEvent.getRawY() - ty);
                        wm.updateViewLayout(ll, updatewmp);

                        break;

                    default:
                        break;

                }
                return false;
            }
        });
        updatedepth(80);
        UIHandler = new Handler();
        BackgroundThread = new HandlerThread("BackgroundThread");
        BackgroundThread.start();
        BackgroundHandler = new Handler(BackgroundThread.getLooper());
        BackgroundHandler.post(new BackgroundTask_CheckClipboard());

        Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String searchresult = intent.getStringExtra("searchresult");
                //Log.d("Brocast","FloatWindow<<MainActivity : searchresult="+searchresult);
                if(searchresult!=null)
                {
                    UIHandler.post(new UITask_Updateword(searchresult));
                }
                String depth = intent.getStringExtra("depth");
                //Log.d("Brocast","FloatWindow<<MainActivity : depth="+depth);
                if(depth!=null)
                {
                    int value = Integer.parseInt(depth);
                    updatedepth(value);
                }
                String size = intent.getStringExtra("size");
                //Log.d("Brocast","FloatWindow<<MainActivity : size="+size);
                if(size!=null)
                {
                    int value = Integer.parseInt(size);
                    updatesize(value);
                }
            }
        };
        registerReceiver(Receiver, new IntentFilter(CommID));

    }


    private class BackgroundTask_CheckClipboard implements Runnable
    {
        ClipboardManager clipboard;

        public BackgroundTask_CheckClipboard()
        {
            clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        }
        @Override
        public void run() {
            String oldtext="";
            while(true)
            {
                String text = clipboard.getText()+"";
                if(!text.equalsIgnoreCase(oldtext)) {
                    Intent intent = new Intent(MainActivity.CommID);
                    intent.putExtra("search", text);
                    //Log.d("Brocast","FloatWindow>>MainActivity : search="+text);
                    sendBroadcast(intent);
                    oldtext = text;
                }
                try {
                    Thread.sleep(600);
                }catch(Exception ex)
                {

                }
            }
        }
    }

    private class UITask_Updateword implements Runnable
    {
        String Text;
        public UITask_Updateword(String text)
        {
            Text = text;
        }
        @Override
        public void run() {
            word.setText(Text);
        }
    }
}
