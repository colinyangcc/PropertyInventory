package com.example.agc_inventory.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agc_inventory.R;
import com.example.agc_inventory.activity.DatabaseHelper;
import com.example.agc_inventory.activity.UHFMainActivity;
import com.example.agc_inventory.tools.LoginAccount;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginFragment extends Activity implements View.OnClickListener{
    private UHFMainActivity mContext;
    TextView debug;
    EditText et_acc;
    Button btn_login;

    public static String operator_id,operator_pwd;
    public static String operator_name;
    //
    private Connection conn = null;
    private String ConnURL = "";

    private boolean _isOpened=false;
    public boolean isOpened()
    {
        return _isOpened;
    }

//    public static MySQLiteManager sqlitehelper;
//    public static SQLiteDatabase sqlite;

    //public static DataBase db;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        LoginAccount LA = (LoginAccount) getApplicationContext();

        myDb = new DatabaseHelper(this);
        Cursor res2 = myDb.getTableData("Tab_DBConfig");

        while(res2.moveToNext()) {
            if(res2.getString(0).indexOf("DB_HostIP") > -1)
            {
                LA.setDBHostIP(res2.getString(1));
            }
            else if(res2.getString(0).indexOf("DB_DBName") > -1)
            {
                LA.setDBName(res2.getString(1));
            }
            else if(res2.getString(0).indexOf("DB_Account") > -1)
            {
                LA.setDBAccount(res2.getString(1));
            }
            else if(res2.getString(0).indexOf("DB_Password") > -1)
            {
                LA.setDBPassword(res2.getString(1));
            }
        }

        //MSSQL Connection String
        ConnURL = "jdbc:jtds:sqlserver://" + LA.getDBHostIP() + ";instanceName=MSSQLSERVER2017;"
                + "databaseName=" + LA.getDBName() + ";charset=utf8;integratedSecurity=true;user=" + LA.getDBAccount() + ";password=" + LA.getDBPassword() + ";";

        operator_id = "";
        operator_name = "";
        operator_pwd = "";

        et_acc = findViewById(R.id.et_account);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);

//        sqlitehelper = new MySQLiteManager(this);
//        sqlite = sqlitehelper.getWritableDatabase();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//        acc.setText("A001");
//        pwd.setText("1234");
    }

    @Override
    public void onClick(View v) {

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            //用jtds driver 來連接SQL Server
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(ConnURL);

            if (conn.isClosed()==false)
            {
                //Toast.makeText(Inventory_download.this," Connecting to Database",Toast.LENGTH_SHORT).show();
                _isOpened=true;

                Log.i("vivian", "Connect to SQL Server successfully");


                operator_id = et_acc.getText().toString();

                Statement stmt = conn.createStatement();
                //ResultSet rs = stmt.executeQuery(" SELECT AE.full_name,LG.ap_limit from ap_employee AE LEFT JOIN limit_group LG ON AE.limit_group_sn = LG.sn WHERE uid ='" + operator_id + "' AND pwd ='" + operator_pwd + "'");
                ResultSet rs2 = stmt.executeQuery(" SELECT AE.full_name, AE.pwd, ALG.authority from ap_employee AE LEFT JOIN ap_limit_group ALG ON AE.limit_group_sn = ALG.sn WHERE uid ='" + operator_id + "'");
                String full_name ="";

                while (rs2.next()) {
                    full_name = rs2.getString("full_name");
                    if(!rs2.wasNull()){

//                        et_acc.setFocusable(true);

//                        if(rs2.getString("pwd").equals(operator_pwd)  && Integer.parseInt(rs2.getString("authority").substring(rs2.getString("authority").length()-1)) > 0)
//                        {
                            LoginAccount Account = (LoginAccount) getApplication();
                            //Account.setAccount(et_acc.getText().toString());
                            Account.setAccount(full_name);
                            et_acc.setText("");

                            Intent intent = new Intent(this, com.example.agc_inventory.activity.UHFMainActivity.class);
                            startActivity(intent);
                            Toast.makeText(LoginFragment.this,R.string.msg_login_success,Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            et_acc.setText("");
//                            Toast.makeText(LoginFragment.this,R.string.msg_login_fail,Toast.LENGTH_SHORT).show();
//                        }
                    }
                    else
                    {
                        Toast.makeText(LoginFragment.this,R.string.msg_login_fail,Toast.LENGTH_SHORT).show();
                    }
                }
                if(full_name == "")
                {
                    et_acc.setText("");
//                    et_acc.setFocusable(true);
                    Toast.makeText(LoginFragment.this,R.string.msg_login_fail,Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(LoginFragment.this,R.string.Network_disconnection,Toast.LENGTH_SHORT).show();
                Toast toast = Toast.makeText(LoginFragment.this, R.string.Network_disconnection, Toast.LENGTH_SHORT);
                toast.getView().setBackgroundColor(Color.RED);
                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                tv.setTextColor(Color.WHITE);
                toast.show();
                Log.i("vivian", "Fail to connect to SQL Server");
            }

        } catch (SQLException se) {
            //ATLog.d(TAG, "Error:" + se);
            //Toast.makeText(LoginFragment.this,R.string.Network_disconnection,Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(LoginFragment.this, R.string.Network_disconnection, Toast.LENGTH_SHORT);
            toast.getView().setBackgroundColor(Color.RED);
            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
            tv.setTextColor(Color.WHITE);
            toast.show();
            Log.i("vivian",  "Error:" + se);

        } catch (ClassNotFoundException e) {
            //ATLog.d(TAG, "Error:" + e);
            //Toast.makeText(LoginFragment.this,R.string.Network_disconnection,Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(LoginFragment.this, R.string.Network_disconnection, Toast.LENGTH_SHORT);
            toast.getView().setBackgroundColor(Color.RED);
            toast.show();
            Log.i("vivian",  "Error:" + e);
        } catch (Exception e) {
            //ATLog.d(TAG, "Error:" + e);
            //Toast.makeText(LoginFragment.this,R.string.Network_disconnection,Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(LoginFragment.this, R.string.Network_disconnection, Toast.LENGTH_SHORT);
            toast.getView().setBackgroundColor(Color.RED);
            toast.show();
            Log.i("vivian",  "Error:" + e);
        }



//        Cursor cursor = sqlite.rawQuery("select * from Connect_Info",null);
//        String ip = "";
//        if(cursor.getCount() != 0){
//            cursor.moveToFirst();
//            try {
//                ip = cursor.getString(0);
//                String url = "jdbc:oracle:thin:@" + ip + ":" + cursor.getString(1) + ":" + cursor.getString(2) + "";
//                db = new DataBase(url,cursor.getString(3),cursor.getString(4));
//            } catch (java.sql.SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        if(isConnected(ip)){
//            try{
//                operator_id = acc.getText().toString();
//                operator_pwd = md5(pwd.getText().toString());
//                ResultSet result = db.select("SELECT * FROM EPC_PERSON WHERE PID = '" + operator_id + "' AND PPASSWORD = '" + operator_pwd + "'");
//                if(result.next()){
//                    Log.d("Name",result.getString("PNAME"));
//                    operator_name = result.getString("PNAME");
//                    startActivity(new Intent(LoginFragment.this,MenuActivity.class));
//                }
//                else
//                {
//                    Toast.makeText(LoginFragment.this,"帳號或密碼錯誤",Toast.LENGTH_SHORT).show();
//                }
//                db.close();
//            }
//            catch (Exception e){
//                Toast.makeText(LoginFragment.this,"無法登入資料庫",Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
//        }
//        else {
//            Toast.makeText(LoginFragment.this,"無法連線至資料庫",Toast.LENGTH_SHORT).show();
//        }

    }

    public static String md5(String content)
    {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){ hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    private boolean isConnected(String ipaddress){
        boolean result = false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info==null || !info.isConnected()) {
            Toast.makeText(LoginFragment.this,"網路未開啟",Toast.LENGTH_SHORT).show();
            result = false;
        }
        else {
            if (info.isAvailable()){
                try{
                    Process process = new ProcessBuilder().command("/system/bin/ping","-c 4",ipaddress)
                            .redirectErrorStream(true)
                            .start();
                    try
                    {
                        String strPing = "";
                        InputStream in = process.getInputStream();
                        OutputStream out = process.getOutputStream();
                        InputStreamReader reader = new InputStreamReader(in, "utf-8");
                        int i;
                        while ((i = in.read()) != -1) {
                            strPing = strPing + (char) i;
                        }
                        out.close();
                        in.close();
                        reader.close();
                        if(strPing.indexOf("ttl")>=0)
                            result = true;
                        else
                            result = false;
                    } catch(Exception e) {
                        result = false;
                    } finally {
                        process.destroy();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else {
                result = false;
            }
        }
        return result;
    }

}
