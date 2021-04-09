package com.example.agc_inventory.fragment;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.agc_inventory.adapter.BookInfoAdapter;
import com.example.agc_inventory.entity.*;
import com.example.agc_inventory.R;
import com.example.agc_inventory.activity.UHFMainActivity;
import com.example.agc_inventory.tools.*;
import com.rscja.deviceapi.RFIDWithUHF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UHFSearchTagFragment extends KeyDwonFragment {

    private String ConnURL = "";
    private Connection conn = null;
    private boolean _isOpened=false;
    public boolean isOpened()
    {
        return _isOpened;
    }

    List<BookInfo> mList;
    private AutoCompleteTextView mACTV;
    BookInfoAdapter bookinfoAdapter;
    ImageView ivPower;
    TextView tvRSSI;
    Button btnInventory;
    Button btnClear;

    private boolean loopFlag = false;
    Handler handler;
    private UHFMainActivity mContext;
    private Sound mSound;
    private Calculate mCalculate;
    TypedArray typedArray;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            if (isVisibleToUser) {
                String Tag = mContext.GetSelectedTag();
                if (!Tag.isEmpty()) {
                    mACTV.setText("標籤:\r\n" + Tag);
                    SetTagMask(Tag);
                }
            } else {
                stopInventory();
                btnClear.callOnClick();
            }
        }catch (Exception ex) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_uhfsearch_tag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (UHFMainActivity) getActivity();
        mSound = new Sound(mContext);
        mCalculate = new Calculate(-30,-80, 30);

        typedArray = getResources().obtainTypedArray(R.array.power);

        //tvTarget = (TextView) getView().findViewById(R.id.tvTarget);
        ivPower = (ImageView) getView().findViewById(R.id.ivPower);
        SetPowerImage(0);

        tvRSSI = (TextView) getView().findViewById(R.id.tvRSSI);

        btnInventory = (Button) getView().findViewById(R.id.btnInventory);
        btnInventory.setOnClickListener(new btnInventoryClickListener());
        btnInventory.setVisibility(View.INVISIBLE);
        btnClear = (Button) getView().findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new btnClearClickListener());

        //load book info
        LoginAccount Account = (LoginAccount) mContext.getApplication();
        ConnURL = "jdbc:jtds:sqlserver://" + Account.getDBHostIP() + ";instanceName=MSSQLSERVER2017;"
                + "databaseName=" + Account.getDBName() + ";charset=utf8;integratedSecurity=true;user=" + Account.getDBAccount() + ";password=" + Account.getDBPassword() + ";";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mList = new ArrayList<BookInfo>();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String result = httpConnectionPost("http://demo.gci.com.tw/stock/Api/Asset/GetAssetList", Collections.<String, String>emptyMap());
//                    ArrayList<Map<String, String>> List = jsonStringToArray(result);
//
//                    for (Map<String, String> map: List) {
//                        mList.add(new BookInfo(map.get("AssetsNo"),map.get("DeviceName"),map.get("TagNo")));
//                    }
//                }catch (Exception ex) { UIHelper.ToastMessage_alarm(mContext, "網路連線失敗!!"); }
//            }
//        }).start();

        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(ConnURL);

            if (conn.isClosed() == false){
                _isOpened = true;
                Log.i("colin", "Connect to SQL Server successfully");

                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery(" SELECT DISTINCT rp.BookNo ,rp.DataName, rp.ProdNo, ap.codecnt RoomName FROM rfid_prod rp JOIN ap_datcode ap ON rp.roomNO = ap.codeid ");

                //fruits.clear();
                int i = 0;
                while (rs.next()) {
                    if( rs.getString("BookNo") != null){
                        //download_list.add(str);
                        //fruits.add(i,rs.getString("SearchTag"));
                        mList.add(new BookInfo(rs.getString("BookNo"),rs.getString("DataName"),rs.getString("ProdNo"),rs.getString("RoomName")));
                        i++;
                    }
                }
            }

        } catch (SQLException se) {
            //ATLog.d(TAG, "Error:" + se);
            UIHelper.ToastMessage_alarm(mContext, "網路未連線!!");
            Log.i("vivian",  "Error:" + se);

        } catch (ClassNotFoundException e) {
            //ATLog.d(TAG, "Error:" + e);
            UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
            Log.i("vivian",  "Error:" + e);
        } catch (Exception e) {
            //ATLog.d(TAG, "Error:" + e);
            UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
            Log.i("vivian",  "Error:" + e);
        }




        /*
        try {
            String result = httpConnectionPost("http://demo.gci.com.tw/stock/Api/Asset/GetAssetList", Collections.<String, String>emptyMap());
            ArrayList<Map<String, String>> List = jsonStringToArray(result);

            for (Map<String, String> map: List) {
                mList.add(new BookInfo(map.get("AssetsNo"),map.get("DeviceName"),map.get("TagNo")));
            }
        }catch (Exception ex) { UIHelper.ToastMessage_alarm(mContext, "網路連線失敗!!"); }
        */
/*
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(ConnURL);

            if (conn.isClosed() == false){
                _isOpened = true;
                Log.i("colin", "Connect to SQL Server successfully");

                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery(" SELECT BookNo,DataName,ProdNo FROM rfid_prod ");

                mList = new ArrayList<BookInfo>();
                int i = 0;
                while (rs.next()) {
                    if(!rs.wasNull() && rs.getString("SearchTag") != null){
                        //download_list.add(str);
                        fruits.add(i,rs.getString("SearchTag"));
                        i++;
                    }
                }
                if (!rs.wasNull()) {
                    while (rs.next()) {
                        mList.add(new BookInfo(rs.getString("BookNo"),rs.getString("DataName"),rs.getString("ProdNo")));
                    }
                }
            }

        } catch (SQLException se) {
            //ATLog.d(TAG, "Error:" + se);
            UIHelper.ToastMessage_alarm(mContext, "網路未連線!!");
            Log.i("vivian",  "Error:" + se);

        } catch (ClassNotFoundException e) {
            //ATLog.d(TAG, "Error:" + e);
            UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
            Log.i("vivian",  "Error:" + e);
        } catch (Exception e) {
            //ATLog.d(TAG, "Error:" + e);
            UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
            Log.i("vivian",  "Error:" + e);
        }
*/
        //list adapter
        mACTV = (AutoCompleteTextView) getView().findViewById(R.id.actv_search);
        bookinfoAdapter = new BookInfoAdapter(mList,mContext);
        mACTV.setAdapter(bookinfoAdapter);
        mACTV.setThreshold(1);
        mACTV.setBackgroundColor(getResources().getColor(R.color.blue1));
        mACTV.setTextColor(Color.WHITE);
        mACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookInfo bi = (BookInfo)bookinfoAdapter.getItem(position);
                mACTV.setText("辨公室 : " + bi.getRoomName()+ "\r\n編號 : " + bi.getBookNo() + "\r\n名稱 : " + bi.getBookName() + "\r\n");
                if (bi.getHeader() != null) {
                    SetTagMask(bi.getHeader());
                    btnInventory.setVisibility(View.VISIBLE);
                }
                else {
                    UIHelper.ToastMessage(mContext, "此本圖冊尚未發卡，無法以 RFID 查找!!");
                    btnInventory.setVisibility(View.INVISIBLE);
                }
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                //addEPCToList(strs[0], strs[1]);
                //mContext.playSound(1);
                Log.i("TAG",result);
            }
        };

        //檢查電量
        mContext.checkPower();
    }

    private void SetTagMask(String tag) {
        if (tag.length() > 24)
            tag = tag.substring(0,25);
        mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("UII"),32,tag.length()*4,tag,false);
    }

    private void ClearMask() {
        mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("UII"),0,0,"",false);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopInventory();
        ClearMask();
    }

    public class btnInventoryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) { readTag(); }
    }

    public class btnClearClickListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            mACTV.setText("");
            ClearMask();
        }
    }

    /*public class mACTVItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BookInfo bi = mList.get(position);
            mACTV.setText(bi.getBookNo() + "\r\n" + bi.getBookName());
            if (bi.getHeader()!=null)
                SetTagMask(64,bi.getHeader());
        }
    }*/

    private void readTag() {
        if (btnInventory.isShown())
        {
            if (btnInventory.getText().equals(mContext.getString(R.string.btInventory))) {
                //  mContext.mReader.setEPCTIDMode(true);
                if (mContext.mReader.startInventoryTag(0,0)) {
                    btnInventory.setText(mContext.getString(R.string.title_stop_Inventory));
                    loopFlag = true;
                    //setViewEnabled(false);
                    //new TagThread().start();
                    new TagReceive().execute();
                } else {
                    mContext.mReader.stopInventory();
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
                }
            }
            else {
                stopInventory();
            }
        }

    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            //setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                btnInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    class TagReceive extends AsyncTask<Void,Integer,Void> {

        ArrayList<Integer> mArray = new ArrayList<>();
        int mArraySize = 5;

        @Override
        protected void onProgressUpdate(Integer... values) {
            mArray.add(values[0]);
            if (mArray.size() > mArraySize)
                mArray.remove(0);
            int avg = 0;
            for (int tmp: mArray) {
                avg += tmp;
            }
            avg = avg / mArray.size();
            avg = (avg > 100) ? 100 :avg;

            //seekBarRSSI.setProgress(avg);
            SetPowerImage(avg);
            tvRSSI.setText(avg + "");
            mSound.Play(avg);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String strTid;
            String strResult;
            String[] res = null;
            long time = System.currentTimeMillis();

            while (loopFlag) {

                res = mContext.mReader.readTagFromBuffer();
                if (res != null) {
                    strTid = res[0];
                    if (strTid.length() != 0 && !strTid.equals("0000000" +
                            "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }
                    //Log.i("data","EPC:"+res[1]+"|"+strResult);
                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + "EPC:" + mContext.mReader.convertUiiToEPC(res[1]) + "@" + res[2];
                    handler.sendMessage(msg);

                    int percent = mCalculate.GetResult(Float.valueOf(res[2]).intValue());
                    publishProgress(percent);
                    time = System.currentTimeMillis();
                }
                else {
                    if (System.currentTimeMillis() - time > 200)
                    {
                        publishProgress(0);
                        time = System.currentTimeMillis();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //seekBarRSSI.setProgress(0);
            SetPowerImage(0);
            tvRSSI.setText("");
            mSound.Stop();
        }
    }

    @Override
    public void myOnKeyDwon() { readTag(); }

    private void SetPowerImage(int power) {

        if (power <= 0)
            ivPower.setImageResource(typedArray.getResourceId(0,0));
        else if (power >= 100)
            ivPower.setImageResource(typedArray.getResourceId(10,0));
        else
            ivPower.setImageResource(typedArray.getResourceId((power / 10) + 1,0));

    }
}
