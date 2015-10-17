package com.a363531807.studentclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Map;

public class MyRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_record_layout);
        Map<String,String> map = (Map<String, String>) getIntent().getExtras().getSerializable("result");
        Log.i("stqbill", "" + map.size());
        TextView name =(TextView)findViewById(R.id.tv_my_name);
        TextView sign =(TextView)findViewById(R.id.tv_my_sign);
        TextView total =(TextView)findViewById(R.id.tv_course_total_num);
        TextView untotal =(TextView)findViewById(R.id.tv_course_untotal_num);
        TextView ontime =(TextView)findViewById(R.id.tv_course_total_ontime);
        TextView late =(TextView)findViewById(R.id.tv_course_total_late);
        TextView asf =(TextView)findViewById(R.id.tv_course_total_asf);
        TextView unsign =(TextView)findViewById(R.id.tv_course_total_unsign);
        name.setText(map != null ? map.get("name") : null);
        sign.setText(map != null ? map.get("sign") : null);
        total.setText(map != null ? map.get("total") : null);
        untotal.setText(map != null ? map.get("untotal") : null);
        ontime.setText(map != null ? map.get("ontime") : null);
        late.setText(map != null ? map.get("late") : null);
        asf.setText(map != null ? map.get("asf") : null);
        unsign.setText(map != null ? map.get("unsign") : null);

    }
}
