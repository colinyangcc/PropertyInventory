package com.example.agc_inventory.fragment;


import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.agc_inventory.R;
import com.example.agc_inventory.activity.DatabaseHelper;
import com.example.agc_inventory.activity.UHFMainActivity;
import com.example.agc_inventory.tools.LoginAccount;
import com.example.agc_inventory.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.utility.StringUtility;

import static android.content.ContentValues.TAG;

public class UHFSetFragment extends KeyDwonFragment implements OnClickListener {
    private UHFMainActivity mContext;

    private Button btnSetFre;
    private Button btnGetFre;
    private Spinner spMode;
    @ViewInject(R.id.ll_freHop)
    private LinearLayout ll_freHop;

    @ViewInject(R.id.btnSetPower)
    private Button btnSetPower;
    @ViewInject(R.id.btnGetPower)
    private Button btnGetPower;
    @ViewInject(R.id.spPower)
    private Spinner spPower;
    @ViewInject(R.id.et_worktime)
    private EditText et_worktime;
    @ViewInject(R.id.et_waittime)
    private EditText et_waittime;
    @ViewInject(R.id.btnWorkWait)
    private Button btnWorkWait;
    @ViewInject(R.id.spFreHop)
    private Spinner spFreHop; //频点列表
    @ViewInject(R.id.btnSetFreHop)
    private Button btnSetFreHop; //设置频点设置
    @ViewInject(R.id.tv_normal_set)
    private TextView tv_normal_set; //普通设置(点击5次设置频点设置)
    @ViewInject(R.id.btnGetWait)
    private Button btnGetWait; //获取空占比
    @ViewInject(R.id.btnSetAgreement)
    private Button btnSetAgreement; //设置协议
    @ViewInject(R.id.SpinnerAgreement)
    private Spinner SpinnerAgreement; //协议列表
    @ViewInject(R.id.btnSetLinkParams)
    private Button btnSetLinkParams; //设置链路参数
    @ViewInject(R.id.btnGetLinkParams)
    private Button btnGetLinkParams; //获取链路参数
    @ViewInject(R.id.splinkParams)
    private Spinner splinkParams; //链路参数列表
    @ViewInject(R.id.btnSetQTParams)
    private Button btnSetQTParams; //设置QT参数
    @ViewInject(R.id.btnGetQTParams)
    private Button btnGetQTParams; //获取QT参数
    @ViewInject(R.id.cbQT)
    private CheckBox cbQt; //打开QT
    @ViewInject(R.id.cbTagFocus)
    private CheckBox cbTagFocus; //打开tagFocus
    @ViewInject(R.id.cbFastID)
    private CheckBox cbFastID; //打开FastID
    @ViewInject(R.id.cbEPC_TID)
    private CheckBox cbEPC_TID; //打开EPC+TID

    @ViewInject(R.id.rb_America)
    private RadioButton rb_America; //美国频点
    @ViewInject(R.id.rb_Others)
    private RadioButton rb_Others; //其他频点
    private ArrayAdapter adapter; //频点列表适配器

    @ViewInject(R.id.spsession)//session
    private  Spinner SpSession;

    @ViewInject(R.id.spinv)//
    private  Spinner SpInv;

    @ViewInject(R.id.btnGetSession)
    private  Button btnGetSession;

    @ViewInject(R.id.btnSetSession)
    private  Button btnSetSession;

    private DisplayMetrics metrics;
    private AlertDialog dialog;
    private long[] timeArr;

    private Handler mHandler = new Handler();
    private int arrPow; //输出功率

    DatabaseHelper myDb;
    private TextView txt_DBHostIPSet;
    private TextView txt_DBNameSet;
    private TextView txt_DBAccountSet;
    private TextView txt_DBPasswordSet;
    @ViewInject(R.id.btnSetDBInfo)
    private Button  btnSetDBInfo;

    private Spinner sp_RSSI_1_Set;
    private Spinner sp_RSSI_2_Set;

    @ViewInject(R.id.btn_RSSI_Set)
    private Button  btn_RSSI_Set;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater
                .inflate(R.layout.activity_uhfset, container, false);
        ViewUtils.inject(this, root);

        myDb = new DatabaseHelper(getActivity());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (UHFMainActivity) getActivity();

        btnSetFre = (Button) getView().findViewById(R.id.BtSetFre);
        btnGetFre = (Button) getView().findViewById(R.id.BtGetFre);

        spMode = (Spinner) getView().findViewById(R.id.SpinnerMode);
        spMode.setOnItemSelectedListener(new MyOnTouchListener());

        SpSession=(Spinner)getView().findViewById(R.id.spsession);
        SpInv=(Spinner)getView().findViewById(R.id.spinv);

        btnSetFre.setOnClickListener(new SetFreOnclickListener());
        btnGetFre.setOnClickListener(new GetFreOnclickListener());
        btnWorkWait.setOnClickListener(new SetPWMOnclickListener());
        btnGetWait.setOnClickListener(this);

        btnSetSession.setOnClickListener(this);
        btnGetSession.setOnClickListener(this);
        btnSetFreHop.setOnClickListener(this);
        tv_normal_set.setOnClickListener(this);
        btnSetAgreement.setOnClickListener(this);
        btnSetQTParams.setOnClickListener(this);
        btnGetQTParams.setOnClickListener(this);
        btnSetLinkParams.setOnClickListener(this);
        btnGetLinkParams.setOnClickListener(this);
        cbTagFocus.setOnCheckedChangeListener(new OnMyCheckedChangedListener());
        cbFastID.setOnCheckedChangeListener(new OnMyCheckedChangedListener());
        cbEPC_TID.setOnCheckedChangeListener(new OnMyCheckedChangedListener());

        SpSession.setSelection(1);
        SpInv.setSelection(0);
        String ver = mContext.mReader.getHardwareType();
        arrPow = R.array.arrayPower;
        if (ver != null && ver.contains("RLM")) {
            arrPow = R.array.arrayPower2;
        }
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, arrPow, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPower.setAdapter(adapter);

        txt_DBHostIPSet= (TextView) getView().findViewById(R.id.txt_DBHostIPSet);
        txt_DBNameSet= (TextView) getView().findViewById(R.id.txt_DBNameSet);
        txt_DBAccountSet= (TextView) getView().findViewById(R.id.txt_DBAccountSet);
        txt_DBPasswordSet= (TextView) getView().findViewById(R.id.txt_DBPasswordSet);
        btnSetDBInfo = (Button) getView().findViewById(R.id.btnSetDBInfo);
        sp_RSSI_1_Set= (Spinner) getView().findViewById(R.id.sp_RSSI_1_Set);
        sp_RSSI_2_Set= (Spinner) getView().findViewById(R.id.sp_RSSI_2_Set);
        btn_RSSI_Set = (Button) getView().findViewById(R.id.btn_RSSI_Set);

        Cursor res2 = myDb.getTableData("Tab_DBConfig");
        while(res2.moveToNext()) {
            if(res2.getString(0).indexOf("DB_HostIP") > -1)
            {
                txt_DBHostIPSet.setText(res2.getString(1));
            }
            else if(res2.getString(0).indexOf("DB_DBName") > -1)
            {
                txt_DBNameSet.setText(res2.getString(1));
            }
            else if(res2.getString(0).indexOf("DB_Account") > -1)
            {
                txt_DBAccountSet.setText(res2.getString(1));
            }
            else if(res2.getString(0).indexOf("DB_Password") > -1)
            {
                txt_DBPasswordSet.setText(res2.getString(1));
            }
        }

        Cursor res3 = myDb.getTableData("Tab_Config");
        while(res3.moveToNext()) {
            if(res3.getString(0).indexOf("RSSI_1") > -1)
            {
                selectValue(sp_RSSI_1_Set,res3.getString(1));
            }
            else if(res3.getString(0).indexOf("RSSI_2") > -1)
            {
                selectValue(sp_RSSI_2_Set,res3.getString(1));
            }
        }
    }

    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
		开启子线程获取参数，Handler更新UI,防止fragment打开卡顿
		 */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getFre();
                getPwm();
                getLinkParams();
                OnClick_GetPower(null);
            }
        });
    }

    /**
     * 工作模式下拉列表点击选中item监听
     */
    public class MyOnTouchListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (position == 3) {
                ll_freHop.setVisibility(View.VISIBLE);
                rb_America.setChecked(true); //默认美国频点
            } else if (position != 3) {
                ll_freHop.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class SetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            // byte[] bBaseFre = new byte[2];
            //
            // if (mContext.mReader.setFrequency(
            // (byte) spMode.getSelectedItemPosition(), (byte) 0,
            // bBaseFre, (byte) 0, (byte) 0, (byte) 0)) {
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_set_frequency_succ);
            // } else {
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_set_frequency_fail);
            // }

            if (mContext.mReader.setFrequencyMode((byte) spMode
                    .getSelectedItemPosition())) {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_frequency_succ);
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_frequency_fail);
//                mContext.playSound(2);
            }
        }
    }

    public void getFre() {
        int idx = mContext.mReader.getFrequencyMode();

        if (idx != -1) {
            int count = spMode.getCount();
            spMode.setSelection(idx > count - 1 ? count - 1 : idx);

            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_frequency_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_read_frequency_fail);
        }
    }

    public void getPwm() {
        int[] pwm = mContext.mReader.getPwm();

        if (pwm == null || pwm.length < 2) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_pwm_fail);
            return;
        }

        et_worktime.setText(pwm[0] + "");
        et_waittime.setText(pwm[1] + "");

        et_worktime.setSelection(et_worktime.getText().toString().length());
        et_waittime.setSelection(et_waittime.getText().toString().length());
    }

    /**
     * 获取链路参数
     */
    public void getLinkParams() {
        int idx = mContext.mReader.getRFLink();
        if (idx != -1) {
            splinkParams.setSelection(idx);

//			UIHelper.ToastMessage(mContext,
//					R.string.uhf_msg_get_para_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_get_para_fail);
        }
    }

    public class SetPWMOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mContext.mReader.setPwm(StringUtility.string2Int(et_worktime.getText().toString(), 0),
                    StringUtility.string2Int(et_waittime.getText().toString(), 0))) {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_pwm_succ);
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_pwm_fail);
//                mContext.playSound(2);
            }
        }
    }

    public class GetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            // String strFrequency = mContext.mReader.getFrequency();
            //
            // if (StringUtils.isNotEmpty(strFrequency)) {
            //
            // etFreRange.setText(strFrequency);
            //
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_frequency_succ);
            //
            // } else {
            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_frequency_fail);
            // }

            getFre();
        }
    }

    public class OnMyCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.cbTagFocus:
                    if (mContext.mReader.setTagFocus(isChecked)) {
                        if (isChecked) {
                            cbTagFocus.setText(R.string.tagFocus_off);
                        } else {
                            cbTagFocus.setText(R.string.tagFocus);
                        }
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }
                    break;
                case R.id.cbFastID:
                    if (mContext.mReader.setFastID(isChecked)) {
                        if (isChecked) {
                            cbFastID.setText(R.string.fastID_off);
                        } else {
                            cbFastID.setText(R.string.fastID);
                        }
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }
                    break;
                case R.id.cbEPC_TID:
                    if (mContext.mReader.setEPCTIDMode(isChecked)) {
                        if (isChecked) {
                            cbEPC_TID.setText(R.string.EPC_TID_off);
                        } else {
                            cbEPC_TID.setText(R.string.EPC_TID);
                        }
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }
                    break;
            }
        }
    }

    @OnClick(R.id.btnGetPower)
    public void OnClick_GetPower(View view) {
        int iPower = mContext.mReader.getPower();

        Log.i("UHFSetFragment", "OnClick_GetPower() iPower=" + iPower);

        if (iPower > -1) {
            int position = iPower - 5;
            int count = spPower.getCount();
            spPower.setSelection(position > count - 1 ? count - 1 : position);

            // UIHelper.ToastMessage(mContext,
            // R.string.uhf_msg_read_power_succ);

        } else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_power_fail);
        }

    }

    @OnClick(R.id.btnSetPower)
    public void OnClick_SetPower(View view) {
        int iPower = spPower.getSelectedItemPosition() + 5;

        Log.i("UHFSetFragment", "OnClick_SetPower() iPower=" + iPower);

        if (mContext.mReader.setPower(iPower)) {

            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_succ);
        } else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail);
//            mContext.playSound(2);
        }

    }

    //儲存資料庫連線資訊
    @OnClick(R.id.btnSetDBInfo)
    public void OnClick_SetHostIP(View view) {
        String SaveMsg = "";
        LoginAccount LA = (LoginAccount) mContext.getApplicationContext();
        if(myDb.UpdateTab_DBConfig("DB_HostIP",txt_DBHostIPSet.getText().toString()))
        {
            mContext.DBHostIP = txt_DBHostIPSet.getText().toString();
            LA.setDBHostIP(txt_DBHostIPSet.getText().toString());
            SaveMsg = mContext.getString(R.string.msg_set_dbhostip_succ)+ "\n";
        }
        else {
            SaveMsg = mContext.getString(R.string.msg_set_dbhostip_fail) + "\n";
        }

        if(myDb.UpdateTab_DBConfig("DB_DBName",txt_DBNameSet.getText().toString()))
        {
            mContext.DBName = txt_DBNameSet.getText().toString();
            LA.setDBName(txt_DBNameSet.getText().toString());
            SaveMsg += mContext.getString(R.string.msg_set_dbname_succ)+ "\n";
        }
        else {
            SaveMsg += mContext.getString(R.string.msg_set_dbname_fail)+ "\n";
        }

        if(myDb.UpdateTab_DBConfig("DB_Account",txt_DBAccountSet.getText().toString()))
        {
            mContext.DBAccount = txt_DBAccountSet.getText().toString();
            LA.setDBAccount(txt_DBAccountSet.getText().toString());
            SaveMsg += mContext.getString(R.string.msg_set_dbaccount_succ)+ "\n";
        }
        else {
            SaveMsg += mContext.getString(R.string.msg_set_dbaccount_fail)+ "\n";
        }

        if(myDb.UpdateTab_DBConfig("DB_Password",txt_DBPasswordSet.getText().toString()))
        {
            mContext.DBPassword= txt_DBPasswordSet.getText().toString();
            LA.setDBPassword(txt_DBPasswordSet.getText().toString());
            SaveMsg += mContext.getString(R.string.msg_set_dbpassword_succ)+ "\n";
        }
        else {
            SaveMsg += mContext.getString(R.string.msg_set_dbpassword_fail)+ "\n";
        }

        UIHelper.ToastMessage(mContext,SaveMsg);
    }

    @OnClick(R.id.btn_RSSI_Set)
    public void OnClick_RSSI_1_Set(View view) {

        if(myDb.UpdateTabl("RSSI_1", sp_RSSI_1_Set.getSelectedItem().toString()))
        {
            mContext.RSSI_1 = sp_RSSI_1_Set.getSelectedItem().toString();
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_RSSI_succ);
        }
        else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_RSSI_fail);
        }

        if(myDb.UpdateTabl("RSSI_2", sp_RSSI_2_Set.getSelectedItem().toString()))
        {
            mContext.RSSI_2 = sp_RSSI_2_Set.getSelectedItem().toString();
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_RSSI_succ);
        }
        else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_RSSI_fail);
        }
    }

    /**
     * 设置频点
     *
     * @param value 频点数值
     * @return 是否设置成功
     */
    private boolean setFreHop(float value) {
        boolean result = mContext.mReader.setFreHop(value);
        if (result) {

            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_set_frehop_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_set_frehop_fail);
//            mContext.playSound(2);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnSetFreHop: //设置频点
//			showFrequencyDialog();
                View view = spFreHop.getSelectedView();
                if (view instanceof TextView) {
                    String freHop = ((TextView) view).getText().toString().trim();
                    setFreHop(Float.valueOf(freHop)); //设置频点
                }
                break;
            case R.id.btnGetWait: //获取空占比
                getPwm();
                break;
            case R.id.btnSetAgreement: //设置协议
                if (mContext.mReader.setProtocol(SpinnerAgreement.getSelectedItemPosition())) {
                    UIHelper.ToastMessage(mContext, R.string.setAgreement_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.setAgreement_fail);
//                    mContext.playSound(2);
                }
                break;
            case R.id.btnSetQTParams: //设置QT参数
                if (!cbQt.isChecked()) {
                    UIHelper.ToastMessage(mContext, R.string.please_on);
//                    mContext.playSound(2);
                    return;
                }
                if (mContext.mReader.setQTPara(cbQt.isChecked())) {
                    UIHelper.ToastMessage(mContext, R.string.setQTParams_succ);

                } else {
                    UIHelper.ToastMessage(mContext, R.string.setQTParams_fail);
//                    mContext.playSound(2);
                }
                break;
            case R.id.btnGetQTParams: //获取QT参数
                int[] QTParams = mContext.mReader.getQTPara();
                if (QTParams[0] == 1) {
                    cbQt.setChecked(QTParams[1] == 1);
                    UIHelper.ToastMessage(mContext,
                            R.string.getQTParams_succ);
                } else {
                    UIHelper.ToastMessage(mContext,
                            R.string.getQTParams_fail);
                }
                break;
            case R.id.btnSetLinkParams: //设置链路参数
                if (mContext.mReader.setRFLink(splinkParams.getSelectedItemPosition())) {

                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_succ);

                } else {
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_fail);
//                    mContext.playSound(2);
                }
                break;
            case R.id.btnGetLinkParams: //获取链路参数
                getLinkParams();
                break;

            case R.id.btnSetSession:

                // //设置SESSION只针对盘点EPC有效，对返回EPC+TID,EPC+TID+USER无效
                int seesionid =SpSession .getSelectedItemPosition();
                int inventoried = SpInv.getSelectedItemPosition();
                if(seesionid<0 || inventoried<0){
                    return;
                }
                char[] p=mContext.mReader.GetGen2();
                if(p!=null && p.length>=14) {
                    int target = p[0];
                    int action = p[1];
                    int t = p[2];
                    int q = p[3];
                    int startQ = p[4];
                    int minQ = p[5];
                    int maxQ = p[6];
                    int dr = p[7];
                    int coding = p[8];
                    int p1 = p[9];
                    int Sel = p[10];
                    int Session = p[11];
                    int g = p[12];
                    int linkFrequency = p[13];
                    StringBuilder sb=new StringBuilder();
                    sb.append("target=");sb.append(target);
                    sb.append(" ,action=");sb.append(action);
                    sb.append(" ,t=");sb.append(t);
                    sb.append(" ,q=");sb.append(q);
                    sb.append(" startQ=");sb.append(startQ);
                    sb.append(" minQ=");sb.append(minQ);
                    sb.append(" maxQ=");sb.append(maxQ);
                    sb.append(" dr=");sb.append(dr);
                    sb.append(" coding=");sb.append(coding);
                    sb.append(" p=");sb.append(p1);
                    sb.append(" Sel=");sb.append(Sel);
                    sb.append(" Session=");sb.append(Session);
                    sb.append(" g=");sb.append(g);
                    sb.append(" linkFrequency=");sb.append(linkFrequency);
                    sb.append("seesionid=");sb.append(seesionid);
                    sb.append(" inventoried=");sb.append(inventoried);
                    Log.i(TAG,sb.toString());
                    if(mContext.mReader.SetGen2(target, action, t, q, startQ, minQ, maxQ, dr, coding, p1, Sel, seesionid, inventoried, linkFrequency)){
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_succ);
                    }else{
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_set_fail);
                    }
                }else{
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_fail);
                }
                break;
            case R.id.btnGetSession:
                char[] pp=mContext.mReader.GetGen2();
                if(pp!=null && pp.length>=14) {
                    int target = pp[0];
                    int action = pp[1];
                    int t = pp[2];
                    int q = pp[3];
                    int startQ = pp[4];
                    int minQ = pp[5];
                    int maxQ = pp[6];
                    int dr = pp[7];
                    int coding = pp[8];
                    int p1 = pp[9];
                    int Sel = pp[10];
                    int Session = pp[11];
                    int g = pp[12];
                    int linkFrequency = pp[13];
                    StringBuilder sb=new StringBuilder();
                    sb.append("target=");sb.append(target);
                    sb.append(" ,action=");sb.append(action);
                    sb.append(" ,t=");sb.append(t);
                    sb.append(" ,q=");sb.append(q);
                    sb.append(" startQ=");sb.append(startQ);
                    sb.append(" minQ=");sb.append(minQ);
                    sb.append(" maxQ=");sb.append(maxQ);
                    sb.append(" dr=");sb.append(dr);
                    sb.append(" coding=");sb.append(coding);
                    sb.append(" p=");sb.append(p1);
                    sb.append(" Sel=");sb.append(Sel);
                    sb.append(" Session=");sb.append(Session);
                    sb.append(" g=");sb.append(g);
                    sb.append(" linkFrequency=");sb.append(linkFrequency);
                    Log.i(TAG,sb.toString());
                    SpSession.setSelection(Session);
                    SpInv.setSelection(g);
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_succ);
                }
                else
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_set_fail);
                break;
            default:
                break;
        }
    }

    /**
     * 显示频点设置
     */
    private void showFrequencyDialog() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//	        builder.setTitle(R.string.btSetFrequency);
            View view = getActivity().getLayoutInflater().inflate(R.layout.uhf_dialog_frequency, null);
            ListView listView = (ListView) view.findViewById(R.id.listView_frequency);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_dismissDialog);
            iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                }
            });

            String[] strArr = getResources().getStringArray(R.array.arrayFreHop);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.item_text1, strArr));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        float value = Float.valueOf(tv.getText().toString().trim());
                        setFreHop(value); //设置频点
                        dialog.dismiss();
                    }
                }

            });

            builder.setView(view);
            dialog = builder.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = getWindowWidth() - 100;
            params.height = getWindowHeight() - 200;
            dialog.getWindow().setAttributes(params);
        } else {
            dialog.show();
        }
    }

    /**
     * 判断是否为累计点击5次且时间少于1600毫秒（调用一次即点击一次）
     *
     * @return
     */
    private boolean isFiveClick() {
        if (timeArr == null) {
            timeArr = new long[5];
        }
        System.arraycopy(timeArr, 1, timeArr, 0, timeArr.length - 1);
        timeArr[timeArr.length - 1] = System.currentTimeMillis();
        return System.currentTimeMillis() - timeArr[0] < 1600;
    }


    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getWindowWidth() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public int getWindowHeight() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.heightPixels;
    }

    @OnClick(R.id.rb_America)
    public void onClick_rbAmerica(View view) {

        adapter = ArrayAdapter.createFromResource(mContext, R.array.arrayFreHop_us, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFreHop.setAdapter(adapter);
    }

    @OnClick(R.id.rb_Others)
    public void onClick_rbOthers(View view) {

        adapter = ArrayAdapter.createFromResource(mContext, R.array.arrayFreHop, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFreHop.setAdapter(adapter);

    }
}
