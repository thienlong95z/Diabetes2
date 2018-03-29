package com.hust.stormfury.diabetes.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Akshat Pandey
 * @version 1.0
 * @date 03-10-2017
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mfragmentList = new ArrayList<>();
    private final HashMap<Fragment,Integer> mFragments = new HashMap<>();
    private final HashMap<String,Integer> mFragmentNumber = new HashMap<>();
    private final HashMap<Integer,String> mFragmentName = new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mfragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mfragmentList.size();
    }

    public void addFragment(Fragment fragment, String fragmentName){
        mfragmentList.add(fragment);
        mFragments.put(fragment,mfragmentList.size()-1);
        mFragmentNumber.put(fragmentName,mfragmentList.size()-1);
        mFragmentName.put(mfragmentList.size()-1,fragmentName);
    }


    /**
     * returns the fragment with the name @param
     * @param fragmentName
     * @return
     */
    public Integer getFragmentNumber(String fragmentName){
        if(mFragmentNumber.containsKey(fragmentName)){

            return mFragmentNumber.get(fragmentName);
        }
        else{
            return null;
        }
    }

    /**
     * returns the fragment with the name @param
     * @param fragment
     * @return
     */
    public Integer getFragmentNumber(Fragment fragment){
        if(mFragmentNumber.containsKey(fragment)){

            return mFragmentNumber.get(fragment);
        }
        else{
            return null;
        }
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentNumber
     * @return
     */
    public String getFragmentName(Integer fragmentNumber){
        if(mFragmentName.containsKey(fragmentNumber)){

            return mFragmentName.get(fragmentNumber);
        }
        else{
            return null;
        }
    }
}
