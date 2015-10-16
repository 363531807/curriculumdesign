package com.a363531807.teacherclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by 363531807 on 2015/10/15.
 */
public class LeaveApplyAdapter extends BaseAdapter {
    private List<Map<String,String>> mList;
    private Context mContext;
    private LeaveApplyInterface mInterface;
    public LeaveApplyAdapter(Context context,List<Map<String,String>> list){
        mList=list;
        mContext=context;
        mInterface = (LeaveApplyInterface)context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.leave_apply_child_layout,null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)convertView.findViewById(R.id.tv_apply_name);
            viewHolder.reason = (TextView)convertView.findViewById(R.id.tv_apply_reason);
            viewHolder.agree = (Button)convertView.findViewById(R.id.bt_leave_agree);
            viewHolder.disagree = (Button)convertView.findViewById(R.id.bt_leave_disagree);
            convertView.setTag(viewHolder);
        }else viewHolder = (ViewHolder) convertView.getTag();
        Map<String,String> map =mList.get(position);
        viewHolder.name.setText(map.get("student_name"));
        viewHolder.reason.setText(map.get("leave_reason"));
        viewHolder.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.agreeAfl(position);
            }
        });
        viewHolder.disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.disagreeAfl(position);
            }
        });
        return convertView;
    }
    class ViewHolder{
        TextView name;
        TextView reason;
        Button agree;
        Button disagree;
    }
    interface LeaveApplyInterface{
        void agreeAfl(int position);
        void disagreeAfl(int position);
    }
}
