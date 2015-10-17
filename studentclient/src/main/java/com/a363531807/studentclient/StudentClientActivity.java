package com.a363531807.studentclient;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StudentClientActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
   // public static final String HOST = "http://registersystem.sinaapp.com/registersystem/";
    public static final String HOST="http://10.10.164.205:8000/registersystem/";
    public static final String TAG = "stqbill";
    public static final String USER_TYPE = "0"; //0代表学生；
    public static final int LOGIN_RESULT_CODE = 11;
    private String mIMEI;
    private String mIMSI;
    private String mAccount;
    private CourseListAdapter mListAdapter;
    private List<Map> mCourseList;
    private ListView mCourselv;
    private ProgressDialog mProgressDialog;
    private MyHandler mMyHandler;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mFilter=true;
    private int mYear;
    private int mMonth;
    private int mDate;
    private MenuItem mCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccount = getIntent().getStringExtra("account");
        mMyHandler = new MyHandler();
        TelephonyManager _te = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mIMEI = _te.getDeviceId();
        mIMSI = _te.getSubscriberId();
        if (mIMSI == null) {
            Toast.makeText(this, "请检查您的SIM卡是否插入", Toast.LENGTH_LONG).show();
            finish();
        }
        Calendar calendar =Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH)+1;
        mDate = calendar.get(Calendar.DATE);
        initView();
        getResoursefromInternet();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView _user_name = (TextView) findViewById(R.id.tv_user_name);
        _user_name.setText(getIntent().getStringExtra("name"));
        TextView _user_sign = (TextView) findViewById(R.id.tv_user_sign);
        _user_sign.setText(getIntent().getStringExtra("sign"));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_content_main);
        //设置刷新时动画的颜色，可以设置4个
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            getCourseList(mFilter,mYear,mMonth,mDate);
                        }
                    }
                }).start();
            }
        });

        mCourselv = (ListView) findViewById(R.id.lv_course_view);
        registerForContextMenu(mCourselv);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        Map map = mCourseList.get((int) info.id);
        if (map.containsKey("isSign")){
            if (map.get("isSign").equals("0")){
                menu.add(Menu.NONE,1,Menu.NONE,R.string.menu_signcourse);
                menu.add(Menu.NONE,2,Menu.NONE,R.string.ask_for_leave);
            }else if(map.get("isSign").equals("3")){
                menu.add(Menu.NONE,4,Menu.NONE,R.string.check_reason_ask_for_leave);
            }else if (map.get("isSign").equals("4")) {
                menu.add(Menu.NONE,1,Menu.NONE,R.string.menu_signcourse);
                menu.add(Menu.NONE,3,Menu.NONE,R.string.rewrite_ask_for_leave);
                menu.add(Menu.NONE,4,Menu.NONE,R.string.check_reason_ask_for_leave);
            }else if (map.get("isSign").equals("5")){
                menu.add(Menu.NONE,1,Menu.NONE,R.string.menu_signcourse);
                menu.add(Menu.NONE,5,Menu.NONE,R.string.reject_ask_for_leave);
                menu.add(Menu.NONE,2,Menu.NONE,R.string.reask_for_leave);
            }else {
                if (map.containsKey("myrate")){
                    menu.add(Menu.NONE,7,Menu.NONE,R.string.rate_result_for_this_class);
                }else {
                    menu.add(Menu.NONE, 6, Menu.NONE, R.string.rate_for_this_class);
                }
            }
        }else {
            menu.add(Menu.NONE,1,Menu.NONE,R.string.menu_signcourse);
            menu.add(Menu.NONE,2,Menu.NONE,R.string.ask_for_leave);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int position = (int)((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id;
        Map map;
        View view;
        final RatingBar ratingBar;
        switch (item.getItemId()){
            case 1:
                View _view = LayoutInflater.from(StudentClientActivity.this).inflate(R.layout.sign_dialog_layout, null);
                final NumberPicker[] numberPickers = new NumberPicker[4];
                numberPickers[0] = (NumberPicker) _view.findViewById(R.id.numberPicker1);
                numberPickers[1] = (NumberPicker) _view.findViewById(R.id.numberPicker2);
                numberPickers[2] = (NumberPicker) _view.findViewById(R.id.numberPicker3);
                numberPickers[3] = (NumberPicker) _view.findViewById(R.id.numberPicker4);
                for (NumberPicker picker : numberPickers) {
                    picker.setMaxValue(9);
                    picker.setMinValue(0);
                    picker.setValue(0);
                }
                final RadioGroup radioGroup = (RadioGroup) _view.findViewById(R.id.radiogroup_sign);
                new AlertDialog.Builder(StudentClientActivity.this)
                        .setView(_view)
                        .setTitle("请选择签到随机码")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showProgressDialog(true, 0);
                                StringBuilder _radom = new StringBuilder();
                                for (NumberPicker picker : numberPickers) {
                                    _radom.append(picker.getValue());
                                }
                                int signtype;
                                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton_ontime) {
                                    signtype = 1;
                                } else signtype = 2;
                                new Thread(new SigninRunnable(_radom.toString(), signtype, position)).start();

                            }
                        }).show();
                return true;
            case 2:
            case 3:
                final EditText editText = new EditText(this);
                editText.setTextColor(getResources().getColor(android.R.color.primary_text_light_nodisable));
                new AlertDialog.Builder(this).setTitle(R.string.dia_ask_leave_title)
                        .setMessage(R.string.dia_ask_leave_msg)
                        .setView(editText)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showProgressDialog(true,0);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        leaveApply(position,editText.getText().toString().trim());
                                    }
                                }).start();
                            }
                        }).setNegativeButton(R.string.cancel,null)
                        .show();
                return true;
            case 4://插看请假理由
                map = mCourseList.get(position);
                if (map.containsKey("afl_reason")) {
                    String reason = (String) map.get("afl_reason");
                    new AlertDialog.Builder(this).setTitle(R.string.reason_for_leave)
                            .setMessage(reason)
                            .setNeutralButton(R.string.cancel,null)
                            .show();
                }else {
                    showMsg("数据有点旧了，请刷新数据");
                }
                return true;
            case 5://插看拒绝理由
                map = mCourseList.get(position);
                if (map.containsKey("afl_reject")) {
                    String reason = (String) map.get("afl_reject");
                    new AlertDialog.Builder(this).setTitle(R.string.reject_for_leave)
                            .setMessage(reason)
                            .setNeutralButton(R.string.cancel, null)
                            .show();
                }else {
                    showMsg("数据有点旧了，请刷新数据");
                }
                return true;
            case 6://评分
                view = LayoutInflater.from(this).inflate(R.layout.rate_layout,null);
                ratingBar = (RatingBar)view.findViewById(R.id.rate_course);
                new AlertDialog.Builder(this)
                        .setView(view)
                        .setMessage(R.string.rate_hint)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    final float result =ratingBar.getRating();

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            rateforcourse(position,result);
                                        }
                                    }).start();

                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            case 7://查看评分
                map = mCourseList.get(position);
                if (map.containsKey("myrate")) {
                    view = LayoutInflater.from(this).inflate(R.layout.rate_layout,null);
                    ratingBar = (RatingBar)view.findViewById(R.id.rate_course);
                    ratingBar.setRating(Float.parseFloat((String) map.get("myrate")));
                    ratingBar.setIsIndicator(true);
                    new AlertDialog.Builder(this).setTitle(R.string.rate_result_for_this_class)
                            .setView(view)
                            .setNeutralButton(R.string.cancel, null)
                            .show();
                }else {
                    showMsg("数据有点旧了，请刷新数据");
                }
                return true;

        }
       return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mCal =menu.getItem(0);
        GregorianCalendar calendar =new GregorianCalendar(mYear,mMonth-1,mDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE");
        mCal.setTitle(simpleDateFormat.format(calendar.getTime()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_sign_out) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("auto_login", false);
            startActivity(intent);
            finish();
        }else if (id == R.id.action_calenda_menu){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view,int year,int monthOfYear,int dayOfMonth) {
                    mYear=year;
                    mMonth =monthOfYear+1;
                    mDate = dayOfMonth;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (this){
                                getCourseList(true,mYear,mMonth,mDate);
                            }
                        }
                    }).start();
                }
            }, mYear, mMonth-1,mDate).show();
        }else if (id ==R.id.action_calenda_all_menu){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        getCourseList(false,mYear,mMonth,mDate);
                    }
                }
            }).start();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.myRecord) {
            showProgressDialog(true,0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getMyRecord();
                }
            }).start();
        }else if (id == R.id.about) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    showProgressDialog(false,0);
                    showMsg("更新失败");
                    break;
                case 1:
                    if (mFilter==true){
                        GregorianCalendar calendar =new GregorianCalendar(mYear,mMonth-1,mDate);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE");
                        mCal.setTitle(simpleDateFormat.format(calendar.getTime()));
                    }else {
                        mCal.setTitle("全部");
                    }
                    if (mListAdapter == null) {
                        mListAdapter = new CourseListAdapter(StudentClientActivity.this, mCourseList);
                        mCourselv.setAdapter(mListAdapter);
                    } else {
                        mListAdapter.notifyDataSetChanged();
                    }
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case 3:
                    showProgressDialog(false,0);
                    switch (msg.arg1) {
                        case 0:
                            showMsg("只能在校园网中进行签到哦！");
                            break;
                        case 1:
                            showMsg("现在还不能签到哦！");
                            break;
                        case 2:
                            showMsg("您的签到密码有误哦！待会重试吧！");
                            break;
                        case 3:
                            showMsg("您的签到密码已超过有效期！");
                            break;
                        case 4:
                            showMsg("该设备已被其他用户使用！");
                            break;
                        case 5:
                            showMsg("该SIM卡已被其他用户使用！");
                            break;
                        case 6:
                            getResoursefromInternet();
                            showMsg("恭喜您签到成功！");

                            break;
                    }
                    break;
                case 4://申请请假失败
                    showProgressDialog(false,0);
                    showMsg("申请请假失败，请等会重试");
                    break;
                case 5:
                    showProgressDialog(false,0);
                    getResoursefromInternet();
                    showMsg("申请请假成功，请耐心等待老师处理");
                    break;
                case 6:
                    showProgressDialog(false,0);
                    showMsg("评分失败，请待会再试");
                    break;
                case 7:
                    showProgressDialog(false,0);
                    getResoursefromInternet();
                    showMsg("评分成功");
                    break;
                case 8:
                    showProgressDialog(false, 0);
                    Intent intent =new Intent(StudentClientActivity.this,MyRecordActivity.class);
                    intent.putExtras(msg.getData());
                    startActivity(intent);
                    break;
                case 9:
                    showProgressDialog(false, 0);
                    showMsg("获取数据失败，请重试！");
                    break;
            }
        }
    }
    public void getResoursefromInternet() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    getCourseList(mFilter,mYear,mMonth,mDate);
                }
            }
        }).start();
    }

    public void getCourseList(boolean filter,int year,int month,int date) {
        String _url = HOST + "getcourselist/";
        try {
            JSONObject _js = new JSONObject();
            _js.put("account", mAccount);
            _js.put("filter",filter);
            if (filter){
                _js.put("year",year);
                _js.put("month", month);
                _js.put("date", date);
            }
            String _result = HttpURLProtocol.postjson(_url, _js.toString().getBytes());
            if (_result.equals("error")) {
                mMyHandler.sendEmptyMessage(0);
            }
            JSONArray _jsarray = new JSONArray(_result);
            if (_jsarray.optString(0).equals("ok")) {
                if (mCourseList==null){
                    mCourseList = new ArrayList();
                }else mCourseList.clear();
                int length = _jsarray.length();
                for (int i = 1; i < length; i++) {
                    Map _map = new HashMap();
                    _js = _jsarray.optJSONObject(i);
                    Iterator _it = _js.keys();
                    String _key;
                    while (_it.hasNext()) {
                        _key = (String) _it.next();
                        _map.put(_key, _js.optString(_key));
                    }
                    if (!_map.isEmpty()) {
                        mCourseList.add(_map);
                    }
                }
                mFilter =filter;
                mMyHandler.sendEmptyMessage(1);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(0);
        }

    }

    void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public String signCourse(String radom, int signtype, int position) {
        String _url = HOST + "studentSign/";
        try {
            Map _item = (Map) mCourseList.get(position);
            JSONObject _js = new JSONObject();
            _js.put("sign_type", "" + signtype);
            _js.put("random_number", radom);
            _js.put("classing_id", _item.get("classing_id"));
            _js.put("account", mAccount);
            _js.put("imei", mIMEI);
            _js.put("imsi", mIMSI);
            if (_item.containsKey("afclass_id")) {
                _js.put("afclass_id", _item.get("afclass_id"));
            }
            return HttpURLProtocol.postjson(_url, _js.toString().getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    public void leaveApply(int position,String reason){
        String url = HOST+"leaveapply/";
        try{
            JSONObject jsonObject = new JSONObject();
            Map<String,String> map = mCourseList.get(position);
            if(map.containsKey("afclass_id")){
                jsonObject.put("afclass_id",map.get("afclass_id"));
            }else {
                jsonObject.put("classing_id",map.get("classing_id"));
                jsonObject.put("account",mAccount);
            }
            jsonObject.put("leave_reason",reason);
            String result = HttpURLProtocol.postjson(url, jsonObject.toString().getBytes());
            jsonObject = new JSONObject(result);
            if (jsonObject.optBoolean("result")){
                mMyHandler.sendEmptyMessage(5);
            }else mMyHandler.sendEmptyMessage(4);
        }catch (Exception e){
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(4);
        }
    }
    public void rateforcourse(int position,float rate){
        String url = HOST+"rateforcourse/";
        try{
            JSONObject jsonObject = new JSONObject();
            Map<String,String> map = mCourseList.get(position);
            if(map.containsKey("afclass_id")) {
                jsonObject.put("afclass_id", map.get("afclass_id"));
                jsonObject.put("myrate", rate);
                String result = HttpURLProtocol.postjson(url, jsonObject.toString().getBytes());
                jsonObject = new JSONObject(result);
                if (jsonObject.optBoolean("result")) {
                    mMyHandler.sendEmptyMessage(7);
                    return;
                }
            }
            mMyHandler.sendEmptyMessage(6);
        }catch (Exception e){
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(6);
        }
    }

    public void getMyRecord(){
        String url = HOST+"getstudentrecord/";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("account",mAccount);
            String result = HttpURLProtocol.postjson(url, jsonObject.toString().getBytes());
            jsonObject = new JSONObject(result);
            if (jsonObject.optBoolean("result")) {
                JSONObject object = jsonObject.optJSONObject("body");
                Iterator<String> iterator =object.keys();
                String key;
                Map<String,String> map =new HashMap<>();
                while (iterator.hasNext()){
                    key=iterator.next();
                    map.put(key,object.optString(key));
                }
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("result", (Serializable) map);
                msg.setData(data);
                msg.what = 8;
                mMyHandler.sendMessage(msg);
                return;
            }
            mMyHandler.sendEmptyMessage(9);
        }catch (Exception e){
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(9);
        }
    }

    public void showProgressDialog(boolean show,int type){
        if (show){
            switch (type){
                case 0:
                    mProgressDialog = ProgressDialog.show(this, null, "正在拼命从服务器加载数据中……");
                    break;
                case 1:
                    break;
            }
        }else{
            if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                mProgressDialog.cancel();
            }
        }
    }

    class SigninRunnable implements Runnable {
        String mRadom;
        int mSigntype;
        int mPosition;

        public SigninRunnable(String radom, int signtype, int position) {
            mRadom = radom;
            mSigntype = signtype;
            mPosition = position;
        }

        @Override
        public void run() {
            synchronized (this) {
                String result = signCourse(mRadom, mSigntype, mPosition);
                if (result.equals("error")) {
                    mMyHandler.sendEmptyMessage(0);
                }
                try {
                    JSONArray _ja = new JSONArray(result);
                    Message _ms = new Message();
                    _ms.what = 3;
                    _ms.arg1 = _ja.getInt(0);
                    mMyHandler.sendMessage(_ms);
                } catch (Exception e) {
                    e.printStackTrace();
                    mMyHandler.sendEmptyMessage(0);
                }
            }
        }
    }
}
