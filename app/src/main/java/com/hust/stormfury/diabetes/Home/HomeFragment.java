package com.hust.stormfury.diabetes.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hust.stormfury.diabetes.R;


/**
 * @author Akshat Pandey
 * @version 1.0
 * @date 27-09-2017
 */

public class HomeFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        return view;
    }
}
