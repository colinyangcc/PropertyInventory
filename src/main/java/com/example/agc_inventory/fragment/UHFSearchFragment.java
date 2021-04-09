package com.example.agc_inventory.fragment;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.agc_inventory.R;
import com.example.agc_inventory.activity.UHFMainActivity;
import com.example.agc_inventory.tools.LoginAccount;
import com.example.agc_inventory.tools.StringUtils;
import com.example.agc_inventory.tools.UIHelper;
import com.rscja.deviceapi.RFIDWithUHF;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UHFSearchFragment extends KeyDwonFragment {

    private boolean loopFlag = false;
    private int inventoryFlag = 1;      //標記 單筆0 多筆1 盤點
    private int inventoryFlag2 = 1;     //標記 否0 是1 只盤點毛巾

    Handler handler;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    Button BtClear;
    TextView tv_count;
    TextView BigTowel_Count;
    TextView SmallTowel_Count;
    TextView Total_Count;
    RadioGroup RgInventory;
    CheckBox CBInventoryLoop;
    CheckBox CBInventorylimite;
    Button Btimport;
    Button BtInventory;
    ListView LvTags;
    private Button btnFilter;//過濾
    private LinearLayout llContinuous;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;
    PopupWindow popFilter;

    TextView tv_ProNoList;
    TextView tv_search_RSSI;
    TextView tv_search_Count;
    TextView tv_color1;
    TextView tv_color2;
    TextView tv_color3;

    private String ConnURL = "";
    private Connection conn = null;
    private boolean _isOpened=false;
    public boolean isOpened()
    {
        return _isOpened;
    }
    //private static String[] fruits = new String[] {"Apple", "Banana", "Cherry", "Date", "Grape", "Kiwi", "Mango", "Pear"};
    private static ArrayList<String> fruits = new ArrayList<String>();
    private AutoCompleteTextView actv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");


        return inflater
                .inflate(R.layout.uhf_search_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        mContext = (UHFMainActivity) getActivity();
        tagList = new ArrayList<HashMap<String, String>>();
        BtClear = (Button) getView().findViewById(R.id.BtClear);
        Btimport = (Button) getView().findViewById(R.id.BtImport);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        BigTowel_Count = (TextView) getView().findViewById(R.id.BigTowel_Count);
        SmallTowel_Count = (TextView) getView().findViewById(R.id.SmallTowel_Count);
        Total_Count = (TextView) getView().findViewById(R.id.Total_Count);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);

        tv_ProNoList= (TextView) getView().findViewById(R.id.tv_ProNoList);
        tv_search_RSSI = (TextView) getView().findViewById(R.id.tv_search_RSSI);
        tv_search_Count = (TextView) getView().findViewById(R.id.tv_search_Count);
        tv_color1 = (TextView) getView().findViewById(R.id.tv_color1);
        tv_color2 = (TextView) getView().findViewById(R.id.tv_color2);
        tv_color3 = (TextView) getView().findViewById(R.id.tv_color3);

        String Section1 = "&gt; " + mContext.RSSI_1;
        String Section2 = mContext.RSSI_1 + "&#8764;" + mContext.RSSI_2;
        String Section3 = "&lt; " + mContext.RSSI_2;
        tv_color1.setText(Html.fromHtml(Section1));
        tv_color2.setText(Html.fromHtml(Section2));
        tv_color3.setText(Html.fromHtml(Section3));

        CBInventoryLoop = (CheckBox) getView().findViewById(R.id.CBInventoryLoop);
        CBInventorylimite = (CheckBox) getView().findViewById(R.id.CBInventorylimite);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);


        LoginAccount Account = (LoginAccount) mContext.getApplication();

        ConnURL = "jdbc:jtds:sqlserver://" + Account.getDBHostIP() + ";instanceName=MSSQLSERVER2017;"
                + "databaseName=" + Account.getDBName() + ";charset=utf8;integratedSecurity=true;user=" + Account.getDBAccount() + ";password=" + Account.getDBPassword() + ";";

//        fruits.add(0,"AAA");
//        fruits.add(1,"BBB");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(ConnURL);

            if (conn.isClosed() == false){
                _isOpened = true;
                Log.i("colin", "Connect to SQL Server successfully");

                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery(" SELECT DISTINCT BookNo + '【' + DataName + '】'  SearchTag FROM rfid_prod ");

                fruits.clear();
                int i = 0;
                while (rs.next()) {
                    if(!rs.wasNull() && rs.getString("SearchTag") != null){
                        //download_list.add(str);
                        fruits.add(i,rs.getString("SearchTag"));
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


        //Creating the instance of ArrayAdapter containing list of fruit names
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,fruits);

        //ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, fruits, android.R.layout.select_dialog_item);

        //Getting the instance of AutoCompleteTextView
        actv = (AutoCompleteTextView)  getView().findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter2);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.BLUE);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagProdNo", "tagBookNo", "tagCount"},
                new int[]{R.id.TvTagProdNo, R.id.TvTagBookNo, R.id.TvTagCount}){

//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                View view = super.getView(position, convertView, parent);
//
//                TextView textView = (TextView) view.findViewById(R.id.TvTagUii);
//                if(textView.getText().toString().indexOf(mContext.RSSI_1) == -1 && textView.getText().toString().indexOf(mContext.RSSI_2) == -1) {
//                    textView.setTextColor(Color.RED);
//                }
//                else{
//                    textView.setTextColor(Color.BLACK);
//                }
//
//                return view;
//            }
        };

        BtClear.setOnClickListener(new BtClearClickListener());
        Btimport.setOnClickListener(new BtImportClickListener());
        //RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());
        btnFilter = (Button) getView().findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popFilter == null) {
                    View viewPop = LayoutInflater.from(mContext).inflate(R.layout.popwindow_filter, null);

                    popFilter = new PopupWindow(viewPop, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);

                    popFilter.setTouchable(true);
                    popFilter.setOutsideTouchable(true);
                    popFilter.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    popFilter.setBackgroundDrawable(new BitmapDrawable());

                    final EditText etLen = (EditText) viewPop.findViewById(R.id.etLen);
                    final EditText etPtr = (EditText) viewPop.findViewById(R.id.etPtr);
                    final EditText etData = (EditText) viewPop.findViewById(R.id.etData);
                    final RadioButton rbEPC = (RadioButton) viewPop.findViewById(R.id.rbEPC);
                    final RadioButton rbTID = (RadioButton) viewPop.findViewById(R.id.rbTID);
                    final RadioButton rbUser = (RadioButton) viewPop.findViewById(R.id.rbUser);
                    final Button btSet = (Button) viewPop.findViewById(R.id.btSet);


                    btSet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String filterBank = "UII";
                            if (rbEPC.isChecked()) {
                                filterBank = "UII";
                            } else if (rbTID.isChecked()) {
                                filterBank = "TID";
                            } else if (rbUser.isChecked()) {
                                filterBank = "USER";
                            }
                            if (etLen.getText().toString() == null || etLen.getText().toString().isEmpty()) {
                                UIHelper.ToastMessage(mContext, "數據長度不為為空");
                                return;
                            }
                            if (etPtr.getText().toString() == null || etPtr.getText().toString().isEmpty()) {
                                UIHelper.ToastMessage(mContext, "起始地址不能為空");
                                return;
                            }
                            int ptr = StringUtils.toInt(etPtr.getText().toString(), 0);
                            int len = StringUtils.toInt(etLen.getText().toString(), 0);
                            String data = etData.getText().toString().trim();
                            if (len > 0) {
                                String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                                if (data == null || data.isEmpty() || !data.matches(rex)) {
                                    UIHelper.ToastMessage(mContext, "过滤的数据必须是十六进制数据");
//									mContext.playSound(2);
                                    return;
                                }

                                if (mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf(filterBank), ptr, len, data, false)) {
                                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_succ);
                                } else {
                                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail);
//									mContext.playSound(2);
                                }
                            } else {
                                //禁用過濾
                                String dataStr = "";
                                if (mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("UII"), 0, 0, dataStr, false)
                                        && mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("TID"), 0, 0, dataStr, false)
                                        && mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("USER"), 0, 0, dataStr, false)) {
                                    UIHelper.ToastMessage(mContext, R.string.msg_disable_succ);
                                } else {
                                    UIHelper.ToastMessage(mContext, R.string.msg_disable_fail);
                                }
                            }


                        }
                    });
                    CheckBox cb_filter = (CheckBox) viewPop.findViewById(R.id.cb_filter);
                    rbEPC.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbEPC.isChecked()) {
                                etPtr.setText("32");
                            }
                        }
                    });
                    rbTID.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbTID.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });
                    rbUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbUser.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });

                    cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) { //啟用過濾

                            } else { //禁用過濾

                            }
                            popFilter.dismiss();
                        }
                    });
                }
                if (popFilter.isShowing()) {
                    popFilter.dismiss();
                    popFilter = null;
                } else {
                    popFilter.showAsDropDown(view);
                }
            }
        });

        //CheckBox狀態改變觸發動作
        CBInventoryLoop.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                //判斷CheckBox是否有勾選，同mCheckBox.isChecked()
                if(isChecked)
                {
                    //CheckBox狀態 : 已勾選
                    inventoryFlag = 1;
                }
                else
                {
                    //CheckBox狀態 : 未勾選
                    inventoryFlag = 0;
                }
            }
        });



        LvTags.setAdapter(adapter);
        clearData();
        Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                addEPCToList(strs[0], strs[1]);
                //mContext.playSound(1);
            }
        };
    }


    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }

    /**
     * 添加EPC到列表中
     *
     * @param epc
     */
    private void addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            //int index = checkIsExist(epc);
            epc = epc.replace("EPC:","");

            String[] ProNoList = tv_ProNoList.getText().toString().split(",");

            for (int i = 0; i < ProNoList.length; i++)
            {
                if (ProNoList[i].length() > 0 && epc.indexOf(ProNoList[i]) > -1 )
                {
                    tv_search_RSSI.setText(rssi);
                    tv_search_Count.setText(String.valueOf(Integer.parseInt(tv_search_Count.getText().toString()) + 1));
                    if (Double.parseDouble(rssi)  > Double.parseDouble(mContext.RSSI_1))
                    {
                        mContext.playSound(1);
                        actv.setBackgroundColor(Color.RED);
                    }
                    else if ( Double.parseDouble(rssi)  < Integer.parseInt(mContext.RSSI_1) && Double.parseDouble(rssi) > Double.parseDouble(mContext.RSSI_2) )
                    {
                        mContext.playSound(2);
                        actv.setBackgroundColor(Color.YELLOW);
                    }
                    else if (Double.parseDouble(rssi) < Double.parseDouble(mContext.RSSI_2) )
                    {
                        mContext.playSound(3);
                        actv.setBackgroundColor(Color.GREEN);
                    }
                }
                else
                {
                    actv.setBackgroundColor(Color.WHITE);
                }
            }


            adapter.notifyDataSetChanged();
        }
    }

    public class BtClearClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
        }
    }

    public class BtImportClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (BtInventory.getText().equals(
                    mContext.getString(R.string.btInventory))) {
                if(tagList.size()==0) {

                    UIHelper.ToastMessage(mContext,  R.string.ExportMesErr);
                    return;
                }
                boolean re = FileImport.daochu("", tagList);
                if (re) {
                    UIHelper.ToastMessage(mContext, R.string.ExportMesSuc);

                    //統計值歸零
                    tv_count.setText("0");
                    //BigTowel_Count.setText("0");
                    //SmallTowel_Count.setText("0");
                    //Total_Count.setText("0");
                    tagList.clear();

                    Log.i("MY", "tagList.size " + tagList.size());

                    adapter.notifyDataSetChanged();
                }
            }
            else
            {
                UIHelper.ToastMessage(mContext, R.string.ExportMesErr2);
            }
        }
    }

    private void clearData() {
        actv.setText("");
        actv.requestFocus();
        actv.setBackgroundColor(Color.WHITE);
        //epcTohex = "";
        tv_search_RSSI.setText("--");
        tv_search_Count.setText("0");

        tv_count.setText("0");

        tagList.clear();

        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
        //BigTowel_Count.setText("0");
        //SmallTowel_Count.setText("0");
        //Total_Count.setText("0");

    }

    public class BtInventoryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(mContext.getString(R.string.btInventory)))// 識別標籤
        {
            switch (inventoryFlag) {
                case 0:// 單張讀取
                {
                    String strUII = mContext.mReader.inventorySingleTag();
                    if (!TextUtils.isEmpty(strUII)) {
                        String strEPC = mContext.mReader.convertUiiToEPC(strUII);
                        addEPCToList(strEPC, "N/A");
                        tv_count.setText("" + adapter.getCount());
                        mContext.playSound(1);
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
                        //mContext.playSound(2);
                    }
                }
                break;
                case 1:// 盤點讀取  .startInventoryTag((byte) 0, (byte) 0))
                {
                    //  mContext.mReader.setEPCTIDMode(true);
                    if (!actv.getText().toString().equals(""))
                    {
                        try {
                            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                            conn = DriverManager.getConnection(ConnURL);

                            //取得盤點 RFID 清單
                            Statement stmt = conn.createStatement();
                            String sql_str = "";
                            sql_str += " SELECT DISTINCT ProdNoList = ( ";
                            sql_str += "    SELECT CAST(ProdNo AS NVARCHAR) + ',' ";
                            sql_str += "    FROM rfid_prod WHERE BookNo = '"+ actv.getText().toString().substring(0,actv.getText().toString().indexOf("【")) + "' ";
                            sql_str += "    FOR XML PATH('')";
                            sql_str += " )";
                            sql_str += " FROM rfid_prod RP WHERE BookNo = '"+ actv.getText().toString().substring(0,actv.getText().toString().indexOf("【")) + "'";
                            ResultSet rs = stmt.executeQuery(sql_str);

                            while (rs.next()) {
                                if(!rs.wasNull() && rs.getString("ProdNoList") != null){
                                    tv_ProNoList.setText(rs.getString("ProdNoList"));
                                }
                            }
                        }
                        catch (SQLException se) {
                        //ATLog.d(TAG, "Error:" + se);
                            UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
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


                        //epcTohex = "";
                        actv.setBackgroundColor(Color.WHITE);
                        tv_search_RSSI.setText("--");
                        tv_search_Count.setText("0");

                        //epcTohex = getEpcCoding(actv.getText().toString());

                        if(tv_ProNoList.getText().equals(""))
                        {
                            UIHelper.ToastMessage(mContext, "此本圖冊尚未發卡，無法以 RFID 查找!!");
                        }
                        else if (mContext.mReader.startInventoryTag(0,0)) {
                            BtInventory.setText(mContext
                                    .getString(R.string.title_stop_Inventory));
                            loopFlag = true;
                            setViewEnabled(false);
                            new TagThread().start();
                        } else {
                            mContext.mReader.stopInventory();
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_open_fail);
//					        mContext.playSound(2);
                        }
                    }

                }
                break;
                default:
                    break;
            }
        } else {// 停止讀取
            stopInventory();
        }
    }

    private String getEpcCoding(String epc)
    {
        String rfid = "";

        for (int i = 0; i < epc.length(); i++)
        {
            //Char轉16進制
            rfid += Integer.toHexString((int) epc.substring(i,i+1).charAt(0));
        }

        return rfid;
    }

    private void setViewEnabled(boolean enabled) {
        CBInventoryLoop.setEnabled(enabled);
        CBInventorylimite.setEnabled(enabled);
        btnFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    /**
     * 停止讀取
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     * 判斷EPC是否在列表中
     *
     * @param strEPC 索引
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }


    class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            String[] res = null;
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

                    Log.i("data","EPC:"+res[1]+"|"+strResult);
                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + "EPC:" + mContext.mReader.convertUiiToEPC(res[1]) + "@" + res[2];

                    handler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }
}
