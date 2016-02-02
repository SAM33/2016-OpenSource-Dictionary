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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam33 on 16/2/1.
 */


interface OnInstallDictionaryListener{
    public abstract void OnInstallDictionary(InstallDictionaryArgs args);
}

class InstallDictionaryArgs {
    private int _loaded;
    private int _total;
    public int getLoaded()
    {
        return _loaded;
    }
    public int getTotal()
    {
        return _total;
    }
    public double getPercentage()
    {
        return ((double)_loaded/(double)_total)*100;
    }
    public InstallDictionaryArgs(int loaded,int total)
    {
        _loaded = loaded;
        _total = total;
    }
}

class Database {

    OnInstallDictionaryListener _Listener;
    boolean Working = false;

    public void setOnInstallDictionaryListener(OnInstallDictionaryListener Listener)
    {
        _Listener = Listener;
    }

    MainActivity _MainActivityRef;
    SQLiteDatabase _Database;
    public Database(MainActivity MainActivityRef)
    {
        _MainActivityRef = MainActivityRef;
    }

    public String SearchKey(String TableName, String key)
    {
        if(Working)
            return "";
        if(_Database!=null) {
            // 使用編號為查詢條件
            String where = "_KEY" + "='" + key + "'";
            // 執行查詢
            Cursor sqlresult = _Database.query(TableName, null, where, null, null, null, null, null);
            if (sqlresult == null) {
                //Log.d("Database", "No Result");
                return "";
            }
            // 如果有查詢結果
            if (sqlresult.moveToFirst()) {
                // 讀取包裝一筆資料的物件
                try {
                    String _value = sqlresult.getString(2);
                    sqlresult.close();
                    return _value;
                } catch (Exception ex) {
                    return "";
                }
            }
            sqlresult.close();
            return "";
        }
        return "";
    }

    public void InsertKeyValue(String TableName,String Key,String Value){
        if(_Database!=null) {
            ContentValues values = new ContentValues();
            values.put("_KEY", Key);
            values.put("_VALUE", Value);
            _Database.insert(TableName, null, values);
        }
    }

    private void LoadTable(String TableName)
    {
        _Database = _MainActivityRef.openOrCreateDatabase("Sam33Translate", Context.MODE_PRIVATE, null);
        final String SQL = "CREATE TABLE IF NOT EXISTS " + TableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_KEY VARCHAR(50), " +
                "_VALUE VARCHAR(1000)" +
                ");";
        _Database.execSQL(SQL);
    }

    public void LoadDictionary(String DictionaryName)
    {
        LoadTable(DictionaryName);
    }

    //快速安裝的版本,有可能OutOfMemory
    /*
     public boolean InstallDictionary(String DictionaryName,DataInputStream idx,DataInputStream dict)
    {
        if(Working)
            return false;
        Working = true;
        byte context[] = null;
        try {
            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = dict.read(buffer)) != -1) {
                temp.write(buffer, 0, bytesRead);
            }
            context = temp.toByteArray();
            dict.close();
            temp.close();
        }catch (IOException ex)
        {
            Log.e("Database", "IOException at InstallDictionary : " + ex.toString());
            Working = false;
            return false;
        }
        DataInputStream objFsIdx = idx;
        try
        {
            int intNow;
            int intOffest = 0;
            int intLength = 0;
            String strX = "";
            List<DictData> DictDatas = new ArrayList<DictData>();
            while((intNow = objFsIdx.read()) != -1)
            {
                if (intNow == 00)
                {
                    intOffest = objFsIdx.readInt();
                    intLength = objFsIdx.readInt();
                    String key = strX;
                    DictDatas.add(new DictData(key,intOffest,intLength));
                    strX = "";
                }
                else
                {
                    strX += (char)intNow;
                }
            }
            objFsIdx.close();
            if(_Database!=null) {
                int update = (DictDatas.size() / 100);
                _Database.beginTransaction();
                for (int i = 0; i < DictDatas.size(); i++) {
                    DictData Data = DictDatas.get(i);
                    byte[] baBuffer = new byte[Data.Length];
                    for(int j=0 ; j<Data.Length ; j++)
                        baBuffer[j] = context[Data.Offset+j];
                    String value = new String(baBuffer, Charset.forName("UTF-8"));
                    ContentValues KeyValue = new ContentValues();
                    KeyValue.put("_KEY", Data.Key);
                    KeyValue.put("_VALUE", value);
                    _Database.insert(DictionaryName,null,KeyValue);
                    if (_Listener != null && i % update == 0) {
                        _Listener.OnInstallDictionary(new InstallDictionaryArgs(i, DictDatas.size()));
                    }
                }
                if (_Listener != null) {
                    _Listener.OnInstallDictionary(new InstallDictionaryArgs(DictDatas.size(), DictDatas.size()));
                }
                _Database.setTransactionSuccessful();
                _Database.endTransaction();
            }
        }
        catch (Exception ex)
        {
            Log.e("Database", "Exception at Install Dictionary : " + ex.toString());
            Working = false;
            return false;
        }
        Working = false;
        return true;
    }
     */

    public boolean InstallDictionary(String DictionaryName,DataInputStream idx,DataInputStream dict)
    {
        if(Working)
            return false;
        Working = true;
        byte context[] = null;
        try {
            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = dict.read(buffer)) != -1) {
                temp.write(buffer, 0, bytesRead);
            }
            context = temp.toByteArray();
            dict.close();
            temp.close();
        }catch (IOException ex)
        {
            Log.e("Database", "IOException at InstallDictionary : " + ex.toString());
            Working = false;
            return false;
        }
        DataInputStream objFsIdx = idx;
        final int filesize = 10106733 ;
        int update = (filesize / 100);
        int p = 0;
        int read = 0;
        //free memory
        System.gc();
        try
        {
            int intNow;
            int intOffest = 0;
            int intLength = 0;
            String strX = "";
            _Database.beginTransaction();
            while((intNow = objFsIdx.read()) != -1)
            {
                if (intNow == 00)
                {
                    intOffest = objFsIdx.readInt();
                    intLength = objFsIdx.readInt();
                    read+=8;
                    String key = strX;
                    byte[] baBuffer = new byte[intLength];
                    for(int j=0 ; j<intLength ; j++)
                        baBuffer[j] = context[intOffest+j];
                    String value = new String(baBuffer, Charset.forName("UTF-8"));
                    ContentValues KeyValue = new ContentValues();
                    KeyValue.put("_KEY", key);
                    KeyValue.put("_VALUE", value);
                    _Database.insert(DictionaryName,null,KeyValue);
                    if (_Listener != null && (read > update*p)) {
                        p++;
                        _Listener.OnInstallDictionary(new InstallDictionaryArgs(read,filesize));
                    }
                    strX = "";
                }
                else
                {
                    strX += (char)intNow;
                    read++;
                }
            }
            objFsIdx.close();
            _Database.setTransactionSuccessful();
            _Database.endTransaction();
            if (_Listener != null) {
                _Listener.OnInstallDictionary(new InstallDictionaryArgs(filesize, filesize));
            }
        }
        catch (Exception ex)
        {
            Log.e("Database", "Exception at Install Dictionary : " + ex.toString());
            Working = false;
            return false;
        }
        Working = false;
        return true;
    }

    class DictData
    {
        public String Key;
        public int Offset;
        public int Length;
        DictData(String StringKey,int IntOffset,int IntLength)
        {
            Key = StringKey;
            Offset = IntOffset;
            Length = IntLength;
        }
    }
}
