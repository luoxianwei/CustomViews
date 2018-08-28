package com.yeuyt.customviews;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yeuyt.customviews.fragment.Fragment_1;
import com.yeuyt.customviews.fragment.Fragment_2;
import com.yeuyt.customviews.fragment.Fragment_3;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.view_pager);
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager() {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment("tagsViewGroup", new Fragment_1());
        adapter.addFragment("TextPathView", new Fragment_2());
        adapter.addFragment("SpiderWebChart", new Fragment_3());
        viewPager.setAdapter(adapter);
    }
}
