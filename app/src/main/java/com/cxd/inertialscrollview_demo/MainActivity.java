package com.cxd.inertialscrollview_demo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cxd.inertialscrollview_demo.isiv.InertialScrollImageView;
import com.cxd.inertialscrollview_demo.isiv.ItemBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private InertialScrollImageView isiv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isiv = findViewById(R.id.isiv);
        isiv.getLayoutParams().height = ScreenUtil.getScreenWidth(this);

        List<ItemBean> beans = new ArrayList<>();
        ItemBean bean ;

        final int pel = 20 ;
        final float angle =  360 / pel;
        final int[] color1 = new int[]{Color.parseColor("#ff7575"),Color.parseColor("#FFCBB3")};
        final int[] color2 = new int[]{Color.parseColor("#8F4586"),Color.parseColor("#E2C2DE")};
        for (int i = 0; i < pel; i++) {
            bean = new ItemBean(null,null,i % 2 == 0 ? color1 :color2,angle);
            beans.add(bean);
        }

        isiv.setData(beans);
    }
}
