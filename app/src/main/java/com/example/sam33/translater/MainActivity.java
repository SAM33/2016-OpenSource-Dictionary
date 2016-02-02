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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String CommID = "sam33.translater.MainActivity";
    private SeekBar depthbar;
    private SeekBar sizebar;
    private TextView searchresult;
    private EditText input;
    private Button search;
    private ProgressBar percentagebar;
    private Handler BackgroundHandler;
    private HandlerThread BackgroundThread;
    private Handler UIHandler;
    public BroadcastReceiver Receiver;
    private TextView installlog;
    private LinearLayout firstusealert;
    private LinearLayout controlboard;
    Database DictionaryDatabase;
    final String DefaultDictionary = "lazyworm_ec_big5";
    Intent FloatWindowIntent;

    //--------------- Install ---------------
    private void InstallDictionary()
    {
        //Use test dictionary
        String test = DictionaryDatabase.SearchKey(DefaultDictionary, "zoo");
        if(test.equals("")) {
            controlboard.setVisibility(View.GONE);
            DataInputStream dict, idx;
            //InputStream dict_gz = this.getResources().openRawResource(R.raw.lazyworm_ec_big5_dict);
            //GZIPInputStream dict_ungz = new GZIPInputStream(dict_gz);
            dict = new DataInputStream(this.getResources().openRawResource(R.raw.lazyworm_ec_big5_dict));
            idx = new DataInputStream(this.getResources().openRawResource(R.raw.lazyworm_ec_big5_idx));
            installlog.setText("狀態：載入字典索引...");
            BackgroundHandler.post(new BackgroundTask_InstallDictionary(DictionaryDatabase, DefaultDictionary, idx, dict));
        }else
        {
            firstusealert.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(FloatWindowIntent!=null)
            stopService(FloatWindowIntent);
        unregisterReceiver(Receiver);
        //BackgroundThread.quitSafely();  //android 4.1.2 not suppot
        Log.d("MainActivity", "onDestroy");
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d("MainActivity", "onStop");
    }
    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Log.d("MainActivity", "onRestart");
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("MainActivity", "onResume");
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d("MainActivity", "onPause");
    }

    protected void FullTextSearch(String q)
    {
        if(!q.equals("") && q.length()<60) {
            try{
                List<String> near = new EnglishBlurryFillter(new LowerFillter(new PunctuationMarkFillter(new TrimFillter(new Fillter(q))))).getCandidates();
                List<String> anses = new ArrayList<>();
                for(int i=0 ; i<near.size() ; i++)
                    anses.add(DictionaryDatabase.SearchKey(DefaultDictionary, near.get(i)));
                Intent resultintent = new Intent(FloatWindow.CommID);
                String ans = "";
                for(int i=0 ; i<anses.size() ; i++) {
                    if (!anses.get(i).equals("")) {
                        ans = anses.get(i);
                        break;
                    }
                }
                if(ans.equals(""))
                    ans = "查無資料";
                resultintent.putExtra("searchresult", ans);
                //Log.d("MainActivity", q + "\n" + ans);
                //Log.d("Brocast", "MainActivity>>FloatWindow : searchresult=" + ans);
                sendBroadcast(resultintent);
                searchresult.setText(q + "\n" + ans);
                input.setText(q);
            }
            catch (Exception e)
            {
                Log.e("MainActivity",e.toString());
            }
        }
    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);
        //--------------- DictionaryInit ---------------
        Log.d("MainActivity", "Init Dictionary");
        DictionaryDatabase = new Database(this);
        DictionaryDatabase.LoadDictionary(DefaultDictionary);

        //--------------- Normal Search ---------------
        Log.d("MainActivity", "Init Normal Search");
        searchresult = (TextView)findViewById(R.id.SearchResult);
        input = (EditText)findViewById(R.id.Input);
        search = (Button)findViewById(R.id.Search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String q = input.getText().toString();
                if (!q.equals("")) {
                    input.selectAll();
                    FullTextSearch(q);
                }
            }
        });

        //--------------- Sizebar and Depthbar ---------------
        Log.d("MainActivity", "Init Sizebar and Depthbar");
        depthbar = (SeekBar) findViewById(R.id.depthbar);
        depthbar.setProgress(80);
        sizebar = (SeekBar) findViewById(R.id.sizebar);
        sizebar.setProgress(1);
        Intent defaultdepthintent = new Intent(FloatWindow.CommID);
        defaultdepthintent.putExtra("depth", depthbar.getProgress() + "");
        sendBroadcast(defaultdepthintent);
        Intent defaultsizeintent = new Intent(FloatWindow.CommID);
        defaultsizeintent.putExtra("size", sizebar.getProgress() + "");
        sendBroadcast(defaultsizeintent);

        sizebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Intent intent = new Intent(FloatWindow.CommID);
                intent.putExtra("size", sizebar.getProgress() + "");
                //Log.d("Brocast", "MainActivity>>FloatWindow : size=" + sizebar.getProgress() + "");
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        depthbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Intent intent = new Intent(FloatWindow.CommID);
                intent.putExtra("depth", depthbar.getProgress() + "");
                //Log.d("Brocast", "MainActivity>>FloatWindow : depth=" + depthbar.getProgress() + "");
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //--------------- BroadcastReceiver ---------------
        Log.d("MainActivity", "Init BroadcastReceiver");
        Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String search = intent.getStringExtra("search");
                //Log.d("Brocast", "MainActivity<<FloatWindow : search=" + search);
                if(search!=null)
                {
                    FullTextSearch(search);
                }
            }
        };
        registerReceiver(Receiver, new IntentFilter(CommID));

        //--------------- Handler ---------------
        Log.d("MainActivity", "Init Handler");
        UIHandler = new Handler();
        BackgroundThread = new HandlerThread("BackgroundThread");
        BackgroundThread.start();
        BackgroundHandler = new Handler(BackgroundThread.getLooper());

        //--------------- Install ---------------
        Log.d("MainActivity", "Init Install");
        percentagebar = (ProgressBar)findViewById(R.id.InstallProgressBar);
        percentagebar.setMax(100);
        installlog = (TextView)findViewById(R.id.InstallLog);
        firstusealert = (LinearLayout)findViewById(R.id.FirstUseAlert);
        controlboard = (LinearLayout)findViewById(R.id.ControlBoard);
        InstallDictionary();

        //--------------- FloatWindow ---------------
        Log.d("MainActivity", "Init FloatWindow");
        FloatWindowIntent = new Intent(MainActivity.this, FloatWindow.class);
        Log.d("MainActivity", "startService");
        try {
            startService(FloatWindowIntent);
        }catch (Exception e)
        {
            Log.e("MainActivity",e.toString());
        }
    }


    class UITask_UpdateInstallDictionaryPercentage implements Runnable
    {
        double _Percentage;
        public UITask_UpdateInstallDictionaryPercentage(double Percentage)
        {
            _Percentage = Percentage;
        }
        @Override
        public void run() {
            installlog.setText("狀態：安裝字詞... " + (int) _Percentage + "%");
            percentagebar.setProgress((int)_Percentage);
            if(_Percentage==100) {
                installlog.setText("狀態：安裝完成");
                percentagebar.setProgress(100);
                firstusealert.setVisibility(View.GONE);
                controlboard.setVisibility(View.VISIBLE);
            }
        }
    }


    class BackgroundTask_InstallDictionary implements Runnable
    {
        Database _db;
        DataInputStream _idx;
        DataInputStream _dict;
        String _name;

        public BackgroundTask_InstallDictionary(Database db,String name,DataInputStream idx, DataInputStream dict)
        {
            _db = db;
            _name = name;
            _dict = dict;
            _idx = idx;
        }
        @Override
        public void run() {
            _db.setOnInstallDictionaryListener(new OnInstallDictionaryListener() {
                @Override
                public void OnInstallDictionary(InstallDictionaryArgs args) {
                    double p = args.getPercentage();
                    UIHandler.post(new UITask_UpdateInstallDictionaryPercentage(p));
                }
            });
            _db.InstallDictionary(_name,_idx,_dict);
        }
    }
}
