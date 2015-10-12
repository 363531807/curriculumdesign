package com.a363531807.teacherclient;

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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    public static final String HOST="http://registersystem.sinaapp.com/registersystem/";
    //public static final String HOST="http://10.10.164.200:8000/registersystem/";
    public static final String TAG ="stqbill";
    public static final String USER_TYPE  = "1"; //1代表教师；
    public static final int LOGIN_RESULT_CODE =11;
    private String mAccount;
    private CourseExpanListAdapter mListAdapter;
    private List<Map> mGroupList;
    private List<List<Map>> mChildList;
    private ExpandableListView mCourselv;
    private MyHandler mMyHandler;
    private SwipeRefreshLayout  mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccount=getIntent().getStringExtra("account");
        mMyHandler = new MyHandler();
        initView();
        mMyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getResoursefromInternet();
            }
        },500);

    }
    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView _user_name = (TextView)findViewById(R.id.tv_user_name);
        _user_name.setText("我的名字");
        TextView  _user_sign = (TextView)findViewById(R.id.tv_user_sign);
        _user_sign.setText("我的签名");
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_content_main);
        //设置刷新时动画的颜色，可以设置4个
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new CourseListRunable()).start();
            }
        });

        mCourselv = (ExpandableListView)findViewById(R.id.lv_course_view);
        registerForContextMenu(mCourselv);

    }
    public void getResoursefromInternet(){
        if(!mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(true);
        }
        new Thread(new CourseListRunable()).start();
    }

    public boolean getCourseList(){
        String _url = HOST+"getteachlist/";
        try {
            JSONObject _js = new JSONObject();
            _js.put("account", mAccount);
            String _result= HttpURLProtocol.postjson(_url, _js.toString().getBytes());
            if (_result.equals("error")){
                return false;
            }
            JSONArray jsarray = new JSONArray(_result);
            if(jsarray.optInt(0)==1){
                List<Map> grouplist = new ArrayList<>();
                List<List<Map>> _childmainlist = new ArrayList<>();
                int length = jsarray.length();
                for (int i=1;i<length;i++){
                    Map<String,String> _map = new HashMap<>();
                    _js =   jsarray.optJSONObject(i);
                    Iterator _it = _js.keys();
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
                        grouplist.add(_map);
                        _childmainlist.add(childlist);
                    }
                }
                mChildList=_childmainlist;
                mGroupList=grouplist;
                return true;
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
       ExpandableListView.ExpandableListContextMenuInfo _info = (ExpandableListView.ExpandableListContextMenuInfo)
                menuInfo;
        Map<String,String> map = mGroupList.get((int)_info.id);
        menu.setHeaderTitle("课程管理");
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
        menu.add(Menu.NONE,3,Menu.NONE,"处理请假申请");
        menu.add(Menu.NONE,4,Menu.NONE,"缺勤名单");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info=
                (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        Intent intent;
        switch (item.getItemId()){
            case 1:
                intent=item.getIntent();
                if (intent.getBooleanExtra("reset",false)){

                }
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;

        }
        return super.onContextItemSelected(item);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.course_list) {

        } else if (id == R.id.my_site) {

        } else if (id == R.id.myRecord) {

        } else if (id == R.id.about) {

        } else if (id == R.id.communciated) {

        } else if (id == R.id.share_some) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    class CourseListRunable implements Runnable {

        @Override
        public void run() {
            if (getCourseList()) {
//                if (mGroupList != null && !mGroupList.isEmpty() && mChildList != null && !mChildList.isEmpty()) {
//                    mMyHandler.sendEmptyMessage(1);
//                } else mMyHandler.sendEmptyMessage(0);
                mMyHandler.sendEmptyMessage(1);
            } else {
                mMyHandler.sendEmptyMessage(0);
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
                    showMsg("更新失败");
                    break;
                case 1:
                    if (mListAdapter==null){
                        mListAdapter=new CourseExpanListAdapter(MainActivity.this,mGroupList,mChildList);
                        mCourselv.setAdapter(mListAdapter);
                    }else {
                        mListAdapter.updateList(mGroupList,mChildList);
                    }
                    if(mSwipeRefreshLayout.isRefreshing()){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    break;

            }
        }
    }

    void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }


}
