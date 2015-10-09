package com.a363531807.curriculumdesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by 363531807 on 2015/9/26.
 */
public class CourseListAdapter extends BaseAdapter{
    List<Map> mMapList;
    Context mContext;
    public CourseListAdapter(Context context,List list){
        mMapList = list;
        mContext = context;
    }
    public void updateList(List list){
        mMapList =list;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mMapList.size();

    }

    @Override
    public Object getItem(int position) {
        return (mMapList.get(position)).get("course_name");
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.course_list_group_item,null);
             mHolder = new ViewHolder();
            mHolder.course_name = (TextView)convertView.findViewById(R.id.tv_course_name);
            mHolder.course_time= (TextView)convertView.findViewById(R.id.tv_course_time);
            mHolder.teacher = (TextView)convertView.findViewById(R.id.tv_course_teacher);
            mHolder.position= (TextView)convertView.findViewById(R.id.tv_course_position);
            mHolder.isSigh = (TextView)convertView.findViewById(R.id.tv_course_signtype);
            convertView.setTag(mHolder);
        }else mHolder = (ViewHolder)convertView.getTag();
        Map<String,String> _map = mMapList.get(position);
        mHolder.course_name.setText(_map.get("course_name"));
         mHolder.course_time.setText(_map.get("course_time"));
         mHolder.teacher.setText(_map.get("teacher"));
        mHolder.position.setText(_map.get("position"));
        if (_map.containsKey("isSign")) {
            String _signtye = "";
            switch (Integer.parseInt(_map.get("isSign"))) {
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
                    _signtye = "缺勤";
                    break;
            }
            mHolder.isSigh.setText(_signtye);
        }else{
            mHolder.isSigh.setText("未标记");
        }
        return convertView;
    }


    class ViewHolder{
        TextView course_name;
        TextView position;
        TextView teacher;
        TextView course_time;
        TextView isSigh;
    }
}
