package com.a363531807.teacherclient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by 363531807 on 2015/10/15.
 */
public class AFLListActivity extends ListActivity implements LeaveApplyAdapter.LeaveApplyInterface {
    private List<Map<String,String>> mList;
    public static final String TAG ="stqbill";
    private ProgressDialog mProgressDialog;
    private MyHandler mHandler;
    private LeaveApplyAdapter mLeaveApplyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_apply_content_layout);
        Bundle data = getIntent().getExtras();
        mList = (List<Map<String, String>>) data.getSerializable("leave_list");
//        SimpleAdapter adapter = new SimpleAdapter(this,mList,R.layout.leave_apply_child_layout,
//                new String[]{"student_name","leave_reason"},new int[]{R.id.tv_apply_name,R.id.tv_apply_reason});
        mHandler = new MyHandler();
        mLeaveApplyAdapter = new LeaveApplyAdapter(this,mList);
        setListAdapter(mLeaveApplyAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(123);
    }

    void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void agreeAfl(final int position) {
        mProgressDialog = ProgressDialog.show(AFLListActivity.this, null, "正在拼命从服务器加载数据中……");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = TeacherClientActivity.HOST+"agreeaskforleave/";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("afclass_id",mList.get(position).get("afclass_id"));
                    String result = HttpURLProtocol.postjson(url,jsonObject.toString().getBytes());
                    jsonObject = new JSONObject(result);
                    if (jsonObject.optBoolean("result")){
                        Message msg = new Message();
                        msg.what = 1;
                        msg.arg1 = position;
                        mHandler.sendMessage(msg);
                    }else {
                        mHandler.sendEmptyMessage(0);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(0);
                }

            }
        }).start();
    }

    @Override
    public void disagreeAfl(final int position) {
        final EditText editText = new EditText(this);
        editText.setTextColor(getResources().getColor(android.R.color.primary_text_light_nodisable));
        new AlertDialog.Builder(this).setTitle(R.string.dia_re_ask_leave_title)
                .setView(editText)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog = ProgressDialog.show(AFLListActivity.this, null, "正在拼命从服务器加载数据中……");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String url = TeacherClientActivity.HOST + "disagreeaskforleave/";
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("afclass_id", mList.get(position).get("afclass_id"));
                                            jsonObject.put("reject_reason",editText.getText().toString().trim());
                                            String result = HttpURLProtocol.postjson(url, jsonObject.toString().getBytes());
                                            jsonObject = new JSONObject(result);
                                            if (jsonObject.optBoolean("result")) {
                                                Message msg = new Message();
                                                msg.what = 1;
                                                msg.arg1 = position;
                                                mHandler.sendMessage(msg);
                                                } else {
                                                    mHandler.sendEmptyMessage(0);
                                                }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mHandler.sendEmptyMessage(0);
                                        }
                                    }
                                }).start();
                            }
                        }

                ).setNegativeButton(R.string.cancel, null)
                .show();
    }
    class MyHandler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                        mProgressDialog.cancel();
                    }
                    showMsg("操作失败");
                case 1://同意
                    if (mProgressDialog!=null&&mProgressDialog.isShowing()){
                        mProgressDialog.cancel();
                    }
                    mList.remove(msg.arg1);
                    if (mList.isEmpty()){
                        showMsg("所有名单已处理完！");
                        finish();
                    }else {
                        mLeaveApplyAdapter.notifyDataSetChanged();
                        showMsg("处理成功");
                    }

                    break;

            }
        }
    }
}
