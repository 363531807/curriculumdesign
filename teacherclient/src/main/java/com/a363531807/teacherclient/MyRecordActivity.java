package com.a363531807.teacherclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Map;

public class MyRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_record);
        Map<String,String> map = (Map<String, String>) getIntent().getExtras().getSerializable("result");
        TextView name =(TextView)findViewById(R.id.tv_my_name);
        TextView sign =(TextView)findViewById(R.id.tv_my_sign);
        TextView total =(TextView)findViewById(R.id.tv_course_total_num);
        TextView untotal =(TextView)findViewById(R.id.tv_course_untotal_num);
        RatingBar rate =(RatingBar)findViewById(R.id.rb_course_total_rate);
        name.setText(map != null ? map.get("name") : null);
        sign.setText(map != null ? map.get("sign") : null);
        total.setText(map != null ? map.get("total") : null);
        untotal.setText(map != null ? map.get("untotal") : null);
        rate.setRating(map != null ? Float.parseFloat(map.get("rate")) : 5.0f);


    }
}
