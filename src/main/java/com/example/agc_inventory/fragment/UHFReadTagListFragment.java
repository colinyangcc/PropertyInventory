package com.example.agc_inventory.fragment;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.example.agc_inventory.R;
import com.example.agc_inventory.activity.DatabaseHelper;
import com.example.agc_inventory.activity.UHFMainActivity;
import com.example.agc_inventory.tools.StringUtils;
import com.example.agc_inventory.tools.UIHelper;
import com.example.agc_inventory.tools.LoginAccount;
import com.rscja.deviceapi.RFIDWithUHF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UHFReadTagListFragment extends KeyDwonFragment {

    private boolean loopFlag = false;
    private int inventoryFlag = 1;      //標記: 0:單筆盤點 1:多筆盤點
    private int inventoryFlag2 = 1;     //標記: 0:false 1:true 只對清單盤點

    Handler handler;
    DatabaseHelper myDb;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    Spinner spi_inventoryList;
    Button BtClear;
    //TextView tv_count;
    TextView txt_InventoryCount;
    TextView txt_NotCheckCount;
    TextView txt_TotalCount;
    RadioGroup RgInventory;
    CheckBox CBInventoryLoop;
    CheckBox CBInventorylimite;
    Button BtnLoading;
    Button BtInvUpload;
    Button BtInventory;
    ListView LvTags;
    private Button btnFilter;//過濾
    private LinearLayout llContinuous;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;
    PopupWindow popFilter;

    private Connection conn = null;
    private String ConnURL = "";

    private boolean _isOpened=false;
    public boolean isOpened()
    {
        return _isOpened;
    }

    List<String> InvNoList = new ArrayList<String>();
    public static ArrayList<String> arr_ProdNo = new ArrayList<String>();
    public static ArrayList<String> arr_BookNo = new ArrayList<String>();
    public static ArrayList<String> arr_InvStatus = new ArrayList<String>();
    public static ArrayList<String> arr_CurrInvStatus = new ArrayList<String>();

    int InventoryCount = 0;
    int NotCheckCount = 0;
    int TotalCount = 0;

    String account = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");

        myDb = new DatabaseHelper(getActivity());

        return inflater
                .inflate(R.layout.uhf_readtaglist_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        tagList = new ArrayList<HashMap<String, String>>();
        BtnLoading = (Button) getView().findViewById(R.id.BtnLoading);
        BtClear = (Button) getView().findViewById(R.id.BtClear);
        BtInvUpload = (Button) getView().findViewById(R.id.BtInvUpload);
        //tv_count = (TextView) getView().findViewById(R.id.tv_count);
        txt_InventoryCount = (TextView) getView().findViewById(R.id.txt_InventoryCount);
        txt_NotCheckCount = (TextView) getView().findViewById(R.id.txt_NotCheckCount);
        txt_TotalCount = (TextView) getView().findViewById(R.id.txt_TotalCount);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);

        LoginAccount Account = (LoginAccount) mContext.getApplication();

        account = Account.getAccount();
        String tr = "";

        CBInventoryLoop = (CheckBox) getView().findViewById(R.id.CBInventoryLoop);
        CBInventorylimite = (CheckBox) getView().findViewById(R.id.CBInventorylimite);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

        // mssql connection str
        ConnURL = "jdbc:jtds:sqlserver://" + Account.getDBHostIP() + ";instanceName=MSSQLSERVER2017;"
                + "databaseName=" + Account.getDBName() + ";charset=utf8;integratedSecurity=true;user=" + Account.getDBAccount() + ";password=" + Account.getDBPassword() + ";";

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagProdNo", "tagBookNo", "tagCount"},
                new int[]{R.id.TvTagProdNo, R.id.TvTagBookNo, R.id.TvTagCount}){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView tv_Count = (TextView) view.findViewById(R.id.TvTagCount);
                TextView tv_BookNo = (TextView) view.findViewById(R.id.TvTagBookNo);
                //if(checkInvIsExist(textView.getText().toString() ) >  -1) {
                if( Integer.parseInt(tv_Count.getText().toString()) > 0 ) {
                    tv_BookNo.setTextColor(Color.BLACK);
                    tv_Count.setTextColor(Color.BLACK);
                }
                else{
                    tv_BookNo.setTextColor(Color.RED);
                    tv_Count.setTextColor(Color.RED);
                }

                return view;
            }
        };

        BtnLoading.setOnClickListener(new BtnLoadingClickListener());
        BtClear.setOnClickListener(new BtClearClickListener());
        BtInvUpload.setOnClickListener(new BtInvUploadClickListener());
        //RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());
        btnFilter = (Button) getView().findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(new OnClickListener() {
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


                    btSet.setOnClickListener(new OnClickListener() {
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
                    rbEPC.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbEPC.isChecked()) {
                                etPtr.setText("32");
                            }
                        }
                    });
                    rbTID.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbTID.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });
                    rbUser.setOnClickListener(new OnClickListener() {
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
        ClearData();
        //Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                addEPCToList(strs[0], strs[1]);
                mContext.playSound(1);
            }
        };

        spi_inventoryList = (Spinner) getView().findViewById(R.id.spi_inventoryList);
//        ArrayAdapter<CharSequence> arrAdapSpn
//                = ArrayAdapter.createFromResource(mContext, //對應的 Context
//                R.array.spn_list, //選項資料內容
//                R.layout.spinner_item ); //自訂getView()介面格式(Spinner介面未展開時的View)
//
//        arrAdapSpn.setDropDownViewResource(R.layout.spinner_dropdown_item); //自訂getDropDownView()介面格式(Spinner介面展開時，View所使用的每個item格式)
//        spi_inventoryList.setAdapter(arrAdapSpn); //將宣告好的 Adapter 設定給 Spinner

        spi_inventoryList.setOnItemSelectedListener(spnOnItemSelected);

        // Loading spinner data from database
        loadInvListData();

        //檢查電量
        mContext.checkPower();
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadInvListData() {
        // database handler
        //Cursor res = myDb.getTableData("Tab_DBConfig");
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(ConnURL);

            if (conn.isClosed()==false)
            {
                //Toast.makeText(Inventory_download.this," Connecting to Database",Toast.LENGTH_SHORT).show();
                _isOpened=true;

                Log.i("vivian", "Connect to SQL Server successfully");
                //download_list.clear();
                /*
				            取得在VIE_RFID_INVENTORY_CAL中CAL_DATE欄位裡不重複的值。
                            */
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(" SELECT DISTINCT InvNo from inventory_list ORDER BY InvNo DESC ");
                InvNoList.clear();

                InvNoList.add("-- 請選擇 --");
                while (rs.next()) {
                    String str = rs.getString("InvNo");
                    if(!rs.wasNull()){
                        //download_list.add(str);
                        InvNoList.add(str);
                    }
                }

                    /*
                        連結已宣告的介面參數和listinventory_download.xml裡定義的介面參數。
                        由ListView來呈現來自SQL Sever的資訊
                    */
//                    listtable = (ListView) findViewById(R.id.downloadtable);
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_multiple_choice,download_list);
//                    listtable.setAdapter(arrayAdapter);
//                    listtable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                //myDb.CreateTable("tina");
            }
            else
            {
                _isOpened=false;
                //System.out.println("connect fail");
                //ATLog.d(TAG, "Fail to connect to SQL Server");
                UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
                Log.i("vivian", "Fail to connect to SQL Server");
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

        // Spinner Drop down elements
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, InvNoList);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        // attaching data adapter to spinner
        spi_inventoryList.setAdapter(dataAdapter);

    }

    private AdapterView.OnItemSelectedListener spnOnItemSelected
            = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
            if (v != null)
            {
                String aa = ((TextView) v).getText().toString();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
            // TODO Auto-generated method stub
        }
    };

    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();
        ClearData();
        // 停止识别
        stopInventory();
    }


    /**
     * 添加EPC到列表中
     *
     * @param epc
     */
    private void addEPCToList(String epc, String rssi) {

        try
        {
            if (!TextUtils.isEmpty(epc) && epc.length() >= 24) {

                //判斷讀取標籤在清單裡
                int Inv_index = checkInvIsExist(epc);

                //判斷讀取標籤在清單中仍未盤到
                int Inv2_index = checkInvIsExistAndNoCheck(epc);

                //int View_index = checkViewIsExist(epc);

                map = new HashMap<String, String>();

                map.put("tagProdNo", epc.substring(4,20));
                //map.put("tagBookNo", "");
                //map.put("tagCount", String.valueOf(1));

                //清單盤點
                if (CBInventorylimite.isChecked()) {
                    if (Inv_index == -1) //只盤點清單內標籤
                    {
                        return;
                    }
                    else if (Inv_index > -1)
                    {
                        arr_InvStatus.set( Inv_index, "1" );
                        arr_CurrInvStatus.set( Inv_index, "1" );

                        if(Inv2_index > -1)
                        {
                            txt_InventoryCount.setText(String.valueOf(Integer.parseInt(txt_InventoryCount.getText().toString()) + 1));
                            txt_NotCheckCount.setText(String.valueOf(Integer.parseInt(txt_NotCheckCount.getText().toString()) - 1));
                        }

                        int tagcount = Integer.parseInt(tagList.get(Inv_index).get("tagCount"), 10) + 1;
                        map.put("tagBookNo",arr_BookNo.get(Inv_index));
                        map.put("tagCount", String.valueOf(tagcount));
                        tagList.set(Inv_index, map);
                    }
                }
                //隨機盤點
                else if (!CBInventorylimite.isChecked()) {

                    if (Inv_index > -1)
                    {
                        arr_CurrInvStatus.set( Inv_index, "1" );
                    }
                    if(Inv2_index > -1)
                    {
                        arr_InvStatus.set( Inv2_index, "1" );
                        txt_InventoryCount.setText(String.valueOf(Integer.parseInt(txt_InventoryCount.getText().toString()) + 1));
                        txt_NotCheckCount.setText(String.valueOf(Integer.parseInt(txt_NotCheckCount.getText().toString()) - 1));
                    }

                    if (Inv_index == -1) {
                        tagList.add(map);
                        LvTags.setAdapter(adapter);
                        //tv_count.setText("" + adapter.getCount());
                    } else {
                        int tagcount = Integer.parseInt(
                                tagList.get(Inv_index).get("tagCount"), 10) + 1;
                        map.put("tagCount", String.valueOf(tagcount));
                        tagList.set(Inv_index, map);
                    }

                }

                adapter.notifyDataSetChanged();
            }
        }
        catch (Exception e)
        {
            Log.i("vivian",  "Error:" + e);
        }

    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            ClearData();

        }
    }

    public class BtnLoadingClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            try {

                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                conn = DriverManager.getConnection(ConnURL);

                if (conn.isClosed()==false)
                {
                    //Toast.makeText(Inventory_download.this," Connecting to Database",Toast.LENGTH_SHORT).show();
                    _isOpened=true;

                    Log.i("colin", "Connect to SQL Server successfully");
                    //download_list.clear();
                    /*
                        確認後載入盤點清單。
                    */
                    Statement stmt = conn.createStatement();

                    String sql = "SELECT ID.ProdNo, ap.codecnt + ' - ' + RP.BookNo BookNo, ID.InvStatus from inventory_detail ID LEFT JOIN rfid_prod RP ON ID.ProdNo = RP.ProdNo LEFT JOIN ap_datcode ap ON RP.RoomNo = ap.codeid WHERE InvNo = '" +  spi_inventoryList.getSelectedItem().toString() + "' AND RP.ProdStatus <> 'PR006' ORDER BY InvStatus DESC";
                    ResultSet rs = stmt.executeQuery(sql);

                    //tv_count.setText("0");
                    tagList.clear();
                    adapter.notifyDataSetChanged();

                    arr_ProdNo.clear();
                    arr_BookNo.clear();
                    arr_InvStatus.clear();
                    arr_CurrInvStatus.clear();

                    InventoryCount = 0;
                    NotCheckCount = 0;

                    txt_InventoryCount.setText("0");
                    txt_NotCheckCount.setText("0");
                    txt_TotalCount.setText("0");

                    while (rs.next()) {

                        //載入盤點資料時, 即顯示要盤點的清單
                        map = new HashMap<String, String>();
                        map.put("tagProdNo", rs.getString("ProdNo"));
                        map.put("tagBookNo", rs.getString("BookNo"));
                        map.put("tagCount", rs.getString("InvStatus"));

                        tagList.add(map);
                        LvTags.setAdapter(adapter);

                        String ProdNo = rs.getString("ProdNo");
                        String BookNo = rs.getString("BookNo");
                        String InvStatus = rs.getString("InvStatus");
                        if(!rs.wasNull()){
                            //if(myDb.insertData(InvNo,ProdNo,InvStatus)){
                            // }
                            arr_ProdNo.add(ProdNo);
                            arr_BookNo.add(BookNo);
                            arr_InvStatus.add(InvStatus);
                            arr_CurrInvStatus.add("0");
                            if(InvStatus.equals("1")){
                                InventoryCount++;
                            }
                        }
                    }
                    txt_InventoryCount.setText(String.valueOf(InventoryCount));
                    txt_NotCheckCount.setText(String.valueOf(arr_ProdNo.toArray().length - InventoryCount));
                    txt_TotalCount.setText(String.valueOf(arr_ProdNo.toArray().length));

                    /*
                        連結已宣告的介面參數和listinventory_download.xml裡定義的介面參數。
                        由ListView來呈現來自SQL Sever的資訊
                    */
//                    listtable = (ListView) findViewById(R.id.downloadtable);
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_multiple_choice,download_list);
//                    listtable.setAdapter(arrayAdapter);
//                    listtable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                    //myDb.CreateTable("tina");

                }
                else
                {
                    _isOpened=false;
                    //System.out.println("connect fail");
                    //ATLog.d(TAG, "Fail to connect to SQL Server");
                    UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
                    Log.i("vivian", "Fail to connect to SQL Server");
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
        }
    }

    public class BtInvUploadClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            if (BtInventory.getText().equals(
                    mContext.getString(R.string.btInventory))) {
                if(tagList.size()==0) {

                    UIHelper.ToastMessage(mContext,  R.string.msg_InvUpdateMes_nodata);
                    return;
                }

                if(arr_ProdNo.toArray().length == 0 ){
                    UIHelper.ToastMessage(mContext,  R.string.msg_InventoryCheck_fail);
                    return;
                }

                try {

                    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                    conn = DriverManager.getConnection(ConnURL);

                } catch (SQLException se) {
                    //ATLog.d(TAG, "Error:" + se);
                    UIHelper.ToastMessage_alarm(mContext, "網路未連線!!");
                    Log.i("vivian",  "Error:" + se);
                    return;

                } catch (ClassNotFoundException e) {
                    //ATLog.d(TAG, "Error:" + e);
                    UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
                    Log.i("vivian",  "Error:" + e);
                    return;
                } catch (Exception e) {
                    //ATLog.d(TAG, "Error:" + e);
                    UIHelper.ToastMessage(mContext, R.string.Network_disconnection);
                    Log.i("vivian",  "Error:" + e);
                    return;
                }

                //boolean re = FileImport.daochu("", tagList);
                try {
                    int re = InvUpload();

                    UIHelper.ToastMessage(mContext, "更新盤點筆數:" + String.valueOf(re) + "筆");
                    //UIHelper.ToastMessage(mContext, R.string.msg_InvUpdateMes_succ );

                    adapter.notifyDataSetChanged();
                    Log.i("MY", "tagList.size " + tagList.size());
                    //統計值歸零
                    ClearData();
                }
                catch (Exception e)
                {
                    UIHelper.ToastMessage(mContext, "Error:" + e);
                }
            }
            else
            {
                UIHelper.ToastMessage(mContext, R.string.msg_InvUpdateMes_fail2);
            }
        }
    }

    //己盤點資料上傳
    private int InvUpload(){
        int resule = 0;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(ConnURL);

            Statement stmt = conn.createStatement();

            for (int i = 0; i < arr_ProdNo.size(); i++) {
                if(arr_CurrInvStatus.get(i).equals("1")) {
                    int rs = stmt.executeUpdate(" UPDATE inventory_detail SET InvStatus = '1', UpdateUser = N'"+ account +"', UpdateDatetime = GETDATE() WHERE InvNo = '" + spi_inventoryList.getSelectedItem().toString() + "' AND ProdNo = '" + arr_ProdNo.get(i) + "'");
                    resule ++;
                }
            }

            //儲存盤點記錄
            int rs2  = stmt.executeUpdate("INSERT inventor_log VALUES('"+ spi_inventoryList.getSelectedItem().toString() +"',N'" + account + "','" + "清單盤點" + "','" + "上傳盤點筆數:" + String.valueOf(resule) + "筆" + "',GETDATE())");
        }

        catch (SQLException se) {
            //ATLog.d(TAG, "Error:" + se);
            Log.i("vivian",  "Error:" + se);

        } catch (ClassNotFoundException e) {
            //ATLog.d(TAG, "Error:" + e);
            Log.i("vivian",  "Error:" + e);
        } catch (Exception e) {
            //ATLog.d(TAG, "Error:" + e);
            Log.i("vivian",  "Error:" + e);
        }

        return resule;
    }

    //畫面回初始化
    private void ClearData() {
        //tv_count.setText("0");
        tagList.clear();

        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
        if(spi_inventoryList != null)
        {
            spi_inventoryList.setSelection(0);
        }

        arr_ProdNo.clear();
        arr_BookNo.clear();
        arr_InvStatus.clear();
        arr_CurrInvStatus.clear();

        txt_InventoryCount.setText("0");
        txt_NotCheckCount.setText("0");
        txt_TotalCount.setText("0");
    }

    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(
                mContext.getString(R.string.btInventory)))// 識別標籤
        {
            if(arr_ProdNo.toArray().length > 0 )
            {
                switch (inventoryFlag) {
                    case 0:// 單張讀取
                    {
                        String strUII = mContext.mReader.inventorySingleTag();
                        if (!TextUtils.isEmpty(strUII)) {
                            String strEPC = "EPC:" + mContext.mReader.convertUiiToEPC(strUII);
                            addEPCToList(strEPC, "N/A");
                            //tv_count.setText("" + adapter.getCount());
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
                        if (mContext.mReader.startInventoryTag(0,0)) {
                            BtInventory.setText(mContext
                                    .getString(R.string.title_stop_Inventory));
                            loopFlag = true;
                            setViewEnabled(false);
                            new TagThread().start();
                        } else {
                            mContext.mReader.stopInventory();
                            UIHelper.ToastMessage(mContext,
                                    R.string.uhf_msg_inventory_open_fail);
//					    mContext.playSound(2);
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
            else
            {
                UIHelper.ToastMessage(mContext,
                        R.string.msg_InventoryCheck_fail);
            }

        } else {// 停止讀取
            stopInventory();
        }
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
    public int checkViewIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagProdNo");
            if (strEPC.indexOf(tempStr) > -1)  {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    /**
     * 判斷EPC是否在盤點列表中仍未盤到
     *
     * @param strEPC 索引
     * @return
     */
    public int checkInvIsExistAndNoCheck(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        strEPC = strEPC.replace("EPC:","");
        for (int i = 0; i < arr_ProdNo.size(); i++) {
            if (strEPC.indexOf(arr_ProdNo.get(i).toString()) > -1 &&  arr_InvStatus.get(i).toString().equals("0") ) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    /**
     * 判斷EPC是否在盤點列表中
     *
     * @param strEPC 索引
     * @return
     */
    public int checkInvIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        strEPC = strEPC.replace("EPC:","");
        for (int i = 0; i < arr_ProdNo.size(); i++) {
            if (strEPC.indexOf(arr_ProdNo.get(i).toString()) > -1) {
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
