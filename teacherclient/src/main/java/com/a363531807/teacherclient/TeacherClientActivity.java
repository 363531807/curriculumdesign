package com.a363531807.teacherclient;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.SimpleAdapter;
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

public class TeacherClientActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    //public static final String HOST="http://registersystem.sinaapp.com/registersystem/";
    public static final String HOST="http://10.10.164.205:8000/registersystem/";
    public static final String TAG ="stqbill";
    public static final String USER_TYPE  = "1"; //1代表教师；
    private String mAccount;
    private CourseExpanListAdapter mListAdapter;
    private List<Map> mGroupList;
    private List<List<Map>> mChildList;
    private ExpandableListView mCourselv;
    private MyHandler mMyHandler;
    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout  mSwipeRefreshLayout;
    private boolean mFilter=true;
    private int mYear;
    private int mMonth;
    private int mDate;
    private MenuItem mCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccount=getIntent().getStringExtra("account");
        mMyHandler = new MyHandler();
        Calendar calendar =Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH)+1;
        mDate = calendar.get(Calendar.DATE);
        initView();
        getResoursefromInternet();

    }
    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView _user_name = (TextView)findViewById(R.id.tv_user_name);
        _user_name.setText(getIntent().getStringExtra("name"));
        TextView  _user_sign = (TextView)findViewById(R.id.tv_user_sign);
        _user_sign.setText(getIntent().getStringExtra("sign"));
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_content_main);
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

        mCourselv = (ExpandableListView)findViewById(R.id.lv_course_view);
        registerForContextMenu(mCourselv);
    }
    public void getResoursefromInternet(){
        if(!mSwipeRefreshLayout.isRefreshing()){
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 123){
            getResoursefromInternet();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
       ExpandableListView.ExpandableListContextMenuInfo _info = (ExpandableListView.ExpandableListContextMenuInfo)
                menuInfo;
        Map<String,String> map = mGroupList.get((int)_info.id);
        //menu.setHeaderTitle("课程管理");
        Intent onintent = new Intent();
        if (map.containsKey("on_random")){
            onintent.putExtra("reset",true);
            menu.add(Menu.NONE, 1, Menu.NONE, "重新生成按时随机码")
                    .setIntent(onintent);
        }else {
            onintent.putExtra("reset",false);
            menu.add(Menu.NONE,1,Menu.NONE,"生成按时随机码")
                    .setIntent(onintent);

        }
        Intent laintent = new Intent();
        if (map.containsKey("la_random")){
            laintent.putExtra("reset",true);
            menu.add(Menu.NONE,2,Menu.NONE,"重新生成迟到随机码")
                .setIntent(laintent);
        }else {
            laintent.putExtra("reset",false);
            menu.add(Menu.NONE,2,Menu.NONE,"生成迟到随机码")
            .setIntent(laintent);
        }
        if(Integer.parseInt(map.get("leave_applying_num"))>0){
            menu.add(Menu.NONE, 3, Menu.NONE, "处理请假申请");
        }
        if (Integer.parseInt(map.get("unsign_total"))>0)
            menu.add(Menu.NONE, 4, Menu.NONE, "缺勤名单");
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final ExpandableListView.ExpandableListContextMenuInfo info=
                (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        Intent intent;
        final int position= (int) info.id;
        switch (item.getItemId()){
            case 1:
            case 2:
                intent=item.getIntent();
                final int signtype =item.getItemId();
                if (intent.getBooleanExtra("reset",false)){
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.reset_random_title)
                            .setMessage(R.string.reset_random_msg)
                            .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showProgressDialog(true,0);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setRamdom(signtype,position);
                                        }
                                    }).start();

                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel,null)
                            .show();
                }else {
                    showProgressDialog(true,0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setRamdom(signtype,position);
                        }
                    }).start();
                }
                return true;
            case 3://请假申请
                showProgressDialog(true,0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getASFApplication(position);
                    }
                }).start();
                return true;
            case 4://缺勤
                showProgressDialog(true,0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getUnsignStudent(position);
                    }
                }).start();
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
        //noinspection SimplifiableIfStatement
        int id = item.getItemId();

        if(id == R.id.action_sign_out ){
            Intent intent = new Intent(this,LoginActivity.class);
            intent.putExtra("auto_login",false);
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
        } else if (id == R.id.about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void showProgressDialog(boolean show,int type){
        if (show){
            switch (type){
                case 0:
                    mProgressDialog = ProgressDialog.show(this,null,"正在拼命从服务器加载数据中……");
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
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 0:
                    if(mSwipeRefreshLayout.isRefreshing()){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    showProgressDialog(false,0);
                    showMsg("更新失败");
                    break;
                case 1://获取列表
                    if(mSwipeRefreshLayout.isRefreshing()){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if (!mGroupList.isEmpty()){
                        if (mFilter==true){
                            GregorianCalendar calendar =new GregorianCalendar(mYear,mMonth-1,mDate);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE");
                            mCal.setTitle(simpleDateFormat.format(calendar.getTime()));
                        }else {
                            mCal.setTitle("全部");
                        }

                        if (mListAdapter==null){
                            mListAdapter=new CourseExpanListAdapter(TeacherClientActivity.this,mGroupList,mChildList);
                            mCourselv.setAdapter(mListAdapter);
                        }else {
                            mListAdapter.notifyDataSetChanged();
                        }
                    }else{
                        showMsg("没有课程哦");
                    }

                    break;
                case 2://随机码
                    showProgressDialog(false, 0);
                    getResoursefromInternet();
                    String random = msg.getData().getString("random");
                    TextView tv = new TextView(TeacherClientActivity.this);
                    tv.setText(random);
                    tv.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    tv.setTextSize(50);
                    tv.setPadding(0,0,0,50);
                    tv.setGravity(Gravity.CENTER);
                    new AlertDialog.Builder(TeacherClientActivity.this)
                            .setTitle(R.string.show_random_title)
                            .setView(tv)
                            .setMessage(R.string.random_warning)
                            .show();
                    break;
                case 3://缺勤名单
                    showProgressDialog(false,0);
                    if (msg.arg1==1) {
                        List<Map<String, String>> list = (List<Map<String, String>>) msg.getData().getSerializable("unsign_list");
                        SimpleAdapter adapter = new SimpleAdapter(TeacherClientActivity.this, list,
                                android.R.layout.simple_list_item_2,
                                new String[]{"student_name", "student_number"},
                                new int[]{android.R.id.text1, android.R.id.text2});
                        new AlertDialog.Builder(TeacherClientActivity.this)
                                .setAdapter(adapter, null)
                                .show();
                    }else{
                        showMsg("没有人缺勤哦！");
                    }
                    break;
                case 4://迟到申请
                    showProgressDialog(false,0);
                    if (msg.arg1==1) {
                        Intent intent =new Intent(TeacherClientActivity.this,AFLListActivity.class);
                        intent.putExtras(msg.getData());
                        startActivityForResult(intent, 123);
                    }else{
                        showMsg("没有人请假哦！");
                    }
                    break;
                case 5:
                    showProgressDialog(false, 0);
                    Intent intent =new Intent(TeacherClientActivity.this,MyRecordActivity.class);
                    intent.putExtras(msg.getData());
                    startActivity(intent);
                    break;
                case 6:
                    showProgressDialog(false, 0);
                    showMsg("获取数据失败，请重试！");
                    break;
            }
        }
    }

    public void getCourseList(boolean filter,int year,int month,int date){
        String _url = HOST+"getteachlist/";
        try {
            JSONObject _js = new JSONObject();
            _js.put("account", mAccount);
            _js.put("filter",filter);
            if (filter){
                _js.put("year",year);
                _js.put("month", month);
                _js.put("date", date);
            }
            String _result= HttpURLProtocol.postjson(_url, _js.toString().getBytes());
            if (_result.equals("error")){
                mMyHandler.sendEmptyMessage(0);
                return;
            }
            JSONArray jsarray = new JSONArray(_result);
            if(jsarray.optInt(0)==1){
                if (mChildList==null){
                    mChildList=new ArrayList<>();
                }else mChildList.clear();
                if (mGroupList==null){
                    mGroupList=new ArrayList<>();
                }else mGroupList.clear();
                int length = jsarray.length();
                Map<String,String> _map;
                Iterator _it;
                for (int i=1;i<length;i++){
                    _map = new HashMap<>();
                    _js =   jsarray.optJSONObject(i);
                    _it = _js.keys();
                    String _key;
                    while (_it.hasNext()){
                        _key = (String)_it.next();
                        _map.put(_key,_js.optString(_key));
                    }
                    if (!_map.isEmpty()){
                        JSONArray _childarray = new JSONArray(_map.get("sign_student"));
                        _map.remove("sign_student");
                        List<Map> childlist = new ArrayList<>();
                        int clength =_childarray.length();
                        for (int j=0;j<clength;j++){
                            JSONObject chilobj = _childarray.optJSONObject(j);
                            Iterator chilit = chilobj.keys();
                            Map<String,String> chmap=new HashMap<>();
                            String chilkey;
                            while (chilit.hasNext()){
                                chilkey=(String)chilit.next();
                                chmap.put(chilkey,chilobj.optString(chilkey));
                            }
                            if (!chmap.isEmpty()){
                                childlist.add(chmap);
                            }
                        }
                        mGroupList.add(_map);
                        mChildList.add(childlist);
                    }
                }
                mFilter =filter;
                mMyHandler.sendEmptyMessage(1);
                return ;
            }else {
                mMyHandler.sendEmptyMessage(0);
            }
        }  catch (Exception e) {
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(0);
        }
    }

    public void setRamdom(int type,int position){
        String url = HOST+"setrandom/";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("classing_id", "" + mGroupList.get(position).get("classing_id"));
            jsonObject.put("set_type", type);
            String result = HttpURLProtocol.postjson(url,jsonObject.toString().getBytes());
            if (result.equals("error")){
                mMyHandler.sendEmptyMessage(0);
                return;
            }
            jsonObject = new JSONObject(result);
            if (jsonObject.optBoolean("result")){
                Message msg = new Message();
                msg.what=2;
                Bundle bundle = new Bundle();
                bundle.putString("random", jsonObject.optString("random"));
                msg.setData(bundle);
                mMyHandler.sendMessage(msg);
                return;
            }else {
                mMyHandler.sendEmptyMessage(0);
            }
        }catch (Exception e){
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(0);
        }
    }
    public void getUnsignStudent(int position){
        String url = HOST+"getunsignlist/";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("classing_id",mGroupList.get(position).get("classing_id"));
            String result = HttpURLProtocol.postjson(url,jsonObject.toString().getBytes());

            if (result.equals("error")){
                mMyHandler.sendEmptyMessage(0);
                return;
            }
            jsonObject = new JSONObject(result);
            if (jsonObject.optBoolean("result")){
                JSONArray jsonArray = jsonObject.optJSONArray("body");
                int length = jsonArray.length();
                List<Map> list = new ArrayList<>();
                JSONObject js;
                Map<String,String> map;
                Iterator<String> it;
                String key;
                for (int i=0;i<length;i++){
                    js =jsonArray.optJSONObject(i);
                    map = new HashMap<>();
                    it = js.keys();
                    while (it.hasNext()){
                        key = it.next();
                        map.put(key,js.optString(key));
                    }
                    list.add(map);
                }
                Message msg = new Message();
                msg.what = 3;
                if (!list.isEmpty()) {
                    msg.arg1=1;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("unsign_list", (Serializable) list);
                    msg.setData(bundle);
                }else{
                    msg.arg1=0;
                }
               mMyHandler.sendMessage(msg);
            }else {
                mMyHandler.sendEmptyMessage(0);
            }
        }catch (Exception e){
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(0);
        }

    }
    public void getASFApplication(int position){
        String url = HOST+"getleavelist/";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("classing_id", mGroupList.get(position).get("classing_id"));
            String result = HttpURLProtocol.postjson(url, jsonObject.toString().getBytes());
            jsonObject = new JSONObject(result);
            if (jsonObject.optBoolean("result")){
                JSONArray jsonArray = jsonObject.optJSONArray("body");
                int length = jsonArray.length();
                List<Map> list = new ArrayList<>();
                JSONObject js;
                Map<String,String> map;
                Iterator<String> it;
                String key;
                for (int i=0;i<length;i++){
                    js =jsonArray.optJSONObject(i);
                     map = new HashMap<>();
                     it = js.keys();
                    while (it.hasNext()){
                        key = it.next();
                        map.put(key,js.optString(key));
                    }
                    list.add(map);
                }
                Message msg = new Message();
                msg.what = 4;
                if (!list.isEmpty()) {
                    msg.arg1=1;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("leave_list", (Serializable) list);
                    msg.setData(bundle);

                }else{
                    msg.arg1=0;
                }
                mMyHandler.sendMessage(msg);
            }else{
                mMyHandler.sendEmptyMessage(0);
            }
        }catch (Exception e){
             e.printStackTrace();
            mMyHandler.sendEmptyMessage(0);
        }
    }

    public void getMyRecord(){
        String url = HOST+"getteacherrecord/";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("account", mAccount);
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
                msg.what = 5;
                mMyHandler.sendMessage(msg);
                return;
            }
            mMyHandler.sendEmptyMessage(6);
        }catch (Exception e){
            e.printStackTrace();
            mMyHandler.sendEmptyMessage(6);
        }
    }
    void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }


}
