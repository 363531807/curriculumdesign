package com.a363531807.teacherclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by 363531807 on 2015/9/26.
 */
public class CourseExpanListAdapter extends BaseExpandableListAdapter{
    List<Map> mMapList;
    List<List<Map>> mChild;
    Context mContext;
    public CourseExpanListAdapter(Context context, List<Map> group,
                                  List<List<Map>> child){
        mMapList = group;
        mChild =child;
        mContext = context;
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
        return mMapList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChild.get(groupPosition).get(childPosition);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.course_list_group_item,null);
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
            mHolder.classes = (TextView)convertView.findViewById(R.id.tv_course_class);
            mHolder.leave_apply = (TextView)convertView.findViewById(R.id.tv_group_asfing);
            mHolder.mRatingBar = (RatingBar)convertView.findViewById(R.id.rb_group_rate);
            convertView.setTag(mHolder);
        }else mHolder = (ViewGroupHolder)convertView.getTag();
        Map<String,String> _map =  mMapList.get(groupPosition);
        mHolder.course_name.setText(_map.get("course_name"));
        mHolder.course_time.setText(_map.get("course_time"));
        mHolder.position.setText(_map.get("position"));
        mHolder.classes.setText(_map.get("classes_name"));
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
        mHolder.leave_apply.setText(_map.get("leave_applying_num"));
        mHolder.mRatingBar.setRating(Float.parseFloat(_map.get("rate_result")));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewChildHolder viewChildHolder;
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.course_list_child_item,null);
            viewChildHolder=new ViewChildHolder();
            viewChildHolder.signType = (TextView)convertView.findViewById(R.id.tv_student_sign_type);
            viewChildHolder.student_name = (TextView)convertView.findViewById(R.id.tv_student_name);
            viewChildHolder.sign_time=(TextView)convertView.findViewById(R.id.tv_student_sign_time);
            convertView.setTag(viewChildHolder);
        }else viewChildHolder = (ViewChildHolder)convertView.getTag();
        Map<String,String> map = mChild.get(groupPosition).get(childPosition);
        viewChildHolder.student_name.setText(map.get("student_name"));
        viewChildHolder.sign_time.setText(map.get("sign_time"));
        if (map.containsKey("isSign")) {
            String _signtye = "";
            switch (Integer.parseInt(map.get("isSign"))) {
                case 0:
                    _signtye = "未标记";
                    break;
                case 1:
                    _signtye = "已签到";
                    break;
                case 2:
                    _signtye = "迟到";
                    break;
                case 3:
                    _signtye = "请假";
                    break;
                case 4:
                    _signtye = "请假申请中";
                    break;
                case 5:
                    _signtye = "请假失败";
                    break;
            }
            viewChildHolder.signType.setText(_signtye);
        }else{
            viewChildHolder.signType.setText("未标记");
        }

        return convertView;
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
        TextView classes;
        RatingBar mRatingBar;
        TextView leave_apply;
    }
    class ViewChildHolder{
        TextView student_name;
        TextView signType;
        TextView sign_time;
    }
}
