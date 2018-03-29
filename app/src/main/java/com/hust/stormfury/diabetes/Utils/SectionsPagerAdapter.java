package com.hust.stormfury.diabetes.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Akshat Pandey
 * @version 1.0
 * @date 27-09-2017
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "SectionsPagerAdapter";

    private final List<Fragment> mFragentList = new ArrayList<>();

    
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragentList.size();
    }

    public void addFragment(Fragment fragment){
        mFragentList.add(fragment);
    }
}
