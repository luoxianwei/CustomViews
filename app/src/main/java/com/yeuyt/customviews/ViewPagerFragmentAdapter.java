package com.yeuyt.customviews;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yeuyt on 2018/4/22.
 */

public class ViewPagerFragmentAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragments;
    private List<CharSequence> titles;

    public ViewPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    public ViewPagerFragmentAdapter(FragmentManager fm, List<Fragment> fragments , List<CharSequence> titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    public void addFragment(CharSequence title, Fragment fragment) {
        titles.add(title);
        fragments.add(fragment);
    }
    public void addFragment(Fragment fragment) {
     addFragment("", fragment);
    }
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
