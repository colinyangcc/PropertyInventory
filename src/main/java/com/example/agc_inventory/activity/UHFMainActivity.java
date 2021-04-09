package com.example.agc_inventory.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.example.agc_inventory.R;
import com.example.agc_inventory.fragment.UHFReadTagFragment;
import com.example.agc_inventory.fragment.UHFReadTagListFragment;
import com.example.agc_inventory.fragment.UHFSearchTagFragment;
import com.example.agc_inventory.fragment.UHFSetFragment;
import com.example.agc_inventory.tools.UIHelper;
import com.rscja.utility.StringUtility;

import java.util.HashMap;
import java.util.Locale;

import android.content.SharedPreferences;

import android.os.BatteryManager;

/**
 * UHF使用demo
 *
 * 1、使用前请确认您的机器已安装此模块。 
 * 2、要正常使用模块需要在\libs\armeabi\目录放置libDeviceAPI.so文件，同时在\libs\目录下放置DeviceAPIver20160728.jar文件。 
 * 3、在操作设备前需要调用 init()打开设备，使用完后调用 free() 关闭设备
 *
 *
 * 更多函数的使用方法请查看API说明文档
 *
 * @author
 * 更新于 2016年8月9日
 */
public class UHFMainActivity extends BaseTabFragmentActivity {

    private final static String TAG = "MainActivity";

    private boolean flag = false;
    private String SelectedTag = "";

    public void SetSelectedTag(String tag) {
        flag = true;
        SelectedTag = tag;
    }

    public String GetSelectedTag() {
        if (flag) {
            flag = false;
            return SelectedTag;
        }
        else
            return "";
    }
//	public AppContext appContext;// ȫ��Context
//
    // public Reader mReader;
//	public RFIDWithUHF mReader;

    //	public void playSound(int id) {
//		appContext.playSound(id);
//	}
    DatabaseHelper myDb;
    public String DBHostIP;
    public String DBName;
    public String DBAccount;
    public String DBPassword;

    public String RSSI_1;
    public String RSSI_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setLanguage();

//		if (Build.VERSION.SDK_INT > 21) {
//
//
//			//读写内存权限
//			if (ContextCompat.checkSelfPermission(this,
//					Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//				// 请求权限
//				ActivityCompat
//						.requestPermissions(
//								this,
//								new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//								1);
//			}
//
//			int checkCallPhonePermission = ContextCompat.checkSelfPermission(
//					this, Manifest.permission.READ_EXTERNAL_STORAGE);
//			if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
//				ActivityCompat.requestPermissions(this, new String[]{
//						Manifest.permission.WRITE_EXTERNAL_STORAGE,
//						Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
//				return;
//			} else {
//				// 已申请权限直接跳转到下一个界面
//
//
//			}
//		}


//			appContext = (AppContext) getApplication();
        initSound();
        initUHF(); //��ʼ��
        initViewPageData();
        initViewPager();
        initTabs();

        myDb = new DatabaseHelper(this);

        //取得 資料庫 設定參數
        Cursor res2 = myDb.getTableData("Tab_DBConfig");
        while(res2.moveToNext()) {
            if(res2.getString(0).indexOf("DB_HostIP") > -1)
            {
                DBHostIP = res2.getString(1);
            }
            else if(res2.getString(0).indexOf("DB_DBName") > -1)
            {
                DBName = res2.getString(1);
            }
            else if(res2.getString(0).indexOf("DB_Account") > -1)
            {
                DBAccount = res2.getString(1);
            }
            else if(res2.getString(0).indexOf("DB_Password") > -1)
            {
                DBPassword = res2.getString(1);
            }
        }

        //取得 RSSI 設定參數
        res2 = myDb.getTableData("Tab_Config");
        while(res2.moveToNext()) {
            if(res2.getString(0).indexOf("RSSI_1") > -1)
            {
                RSSI_1 = res2.getString(1);
            }
            else if(res2.getString(0).indexOf("RSSI_2") > -1)
            {
                RSSI_2 = res2.getString(1);
            }
        }
    }

    //檢查是否低電量
    public void checkPower(){
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if(percentage <= 20)
        {
            UIHelper.alert(this, R.string.LowPoserNotice,
                    "電量剩餘" + percentage + "%,請盡速充電!! \n低電量(<20)將導致 RFID 模組無法正常運作" , R.drawable.notice);
        }
    }

    private void setLanguage(){
        //強制使用中文語言(for c72e 不會自動轉換)
       SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
       int language = preferences.getInt("Language",0);

       Resources resources = getResources();
       DisplayMetrics displayMetrices = resources.getDisplayMetrics();
       Configuration configuration = resources.getConfiguration();

       configuration.locale = Locale.CHINESE;
       resources.updateConfiguration(configuration, displayMetrices);

    }


    @Override
    protected void initViewPageData() {
//      增減顯示的頁籤數由此控制
//        lstFrg.add(new UHFReadTagFragment());
        lstFrg.add(new UHFReadTagListFragment());
//        lstFrg.add(new UHFSearchFragment());
        lstFrg.add(new UHFSearchTagFragment());
        lstFrg.add(new UHFSetFragment());
//         lstFrg.add(new UHFKillFragment());
//         lstFrg.add(new UHFLockFragment());
        lstFrg.add(new UHFSetFragment());

//        lstTitles.add(getString(R.string.title_random_inventory));
        lstTitles.add(getString(R.string.title_list_inventory));
        lstTitles.add(getString(R.string.title_search));
        lstTitles.add(getString(R.string.uhf_msg_tab_set));
//         lstTitles.add(getString(R.string.uhf_msg_tab_kill));
//         lstTitles.add(getString(R.string.uhf_msg_tab_lock));
        lstTitles.add(getString(R.string.uhf_msg_tab_set));

    }

    @Override
    protected void onDestroy() {

        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
    }

    /**
     * �豸�ϵ��첽��
     *
     * @author liuruifeng
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            return mReader.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                Toast.makeText(UHFMainActivity.this, "init fail",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(UHFMainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }



    /**
     * ��֤ʮ����������Ƿ���ȷ
     *
     * @param str
     * @return
     */
    public boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }

        // ���ȱ�����ż��
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }
    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;
    private void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        soundMap.put(3, soundPool.load(this, R.raw.first, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
    }
    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
