package com.a363531807.teacherclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by 363531807 on 2015/9/26.
 */
public class CourseListAdapter extends BaseExpandableListAdapter{
    List<Map<String,String>> mMapList;
    List<List<Map<String,String>>> mChild;
    Context mContext;
    public CourseListAdapter(Context context, List<Map<String,String>> group,
                             List<List<Map<String,String>>> child){
        mMapList = group;
        mChild =child;
        mContext = context;
    }
    public void updateList(List<Map<String,String>> group,List<List<Map<String,String>>> child){
        mMapList = group;
        mChild =child;
        notifyDataSetChanged();
    }


    @Override
    public int getGroupCount() {
        return mMapList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChild.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewGroupHolder mHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.course_list_item,null);
            mHolder = new ViewGroupHolder();
            mHolder.course_name = (TextView)convertView.findViewById(R.id.tv_course_name);
            mHolder.course_time= (TextView)convertView.findViewById(R.id.tv_course_time);
            mHolder.position= (TextView)convertView.findViewById(R.id.tv_course_position);
            mHolder.on_random = (TextView)convertView.findViewById(R.id.tv_ontime_sign);
            mHolder.la_random = (TextView)convertView.findViewById(R.id.tv_late_sign);
            mHolder.sign_total= (TextView)convertView.findViewById(R.id.tv_total_sign);
            mHolder.unsign_total= (TextView)convertView.findViewById(R.id.tv_total_unsign);
            mHolder.ontime_num = (TextView)convertView.findViewById(R.id.tv_ontime_num);
            mHolder.leave_num = (TextView)convertView.findViewById(R.id.tv_leave_num);
            mHolder.late_num = (TextView)convertView.findViewById(R.id.tv_late_num);
            convertView.setTag(mHolder);
        }else mHolder = (ViewGroupHolder)convertView.getTag();
        Map<String,String> _map = mMapList.get(groupPosition);
        mHolder.course_name.setText(_map.get("course_name"));
        mHolder.course_time.setText(_map.get("course_time"));
        mHolder.position.setText(_map.get("position"));
        if(_map.containsKey("on_random")){
            mHolder.on_random.setText(_map.get("on_random"));
        }
        if(_map.containsKey("la_random")){
            mHolder.la_random.setText(_map.get("la_random"));
        }
        mHolder.sign_total.setText(_map.get("sign_total"));
        mHolder.unsign_total.setText(_map.get("unsign_total"));
        mHolder.ontime_num.setText(_map.get("ontime_num"));
        mHolder.leave_num.setText(_map.get("leave_num"));
        mHolder.late_num.setText(_map.get("late_num"));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                if(convertView==null){

                }
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    class ViewGroupHolder{
        TextView course_name;
        TextView position;
        TextView on_random;
        TextView course_time;
        TextView la_random;
        TextView sign_total;
        TextView unsign_total;
        TextView ontime_num;
        TextView leave_num;
        TextView late_num;
    }
}
