package com.hust.stormfury.diabetes.Profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.hust.stormfury.diabetes.R;
import com.hust.stormfury.diabetes.Utils.ViewPostFragment;
import com.hust.stormfury.diabetes.models.Photo;


public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener{
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 2;

    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    private Context mContext = ProfileActivity.this;
    private static final int NUM_GRID_COLUMNS = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

/*        setupBottomNavigationView();
        setupToolbar();
        setupActivityWidgets();
        setProfileImage();

        tempGridSetup();*/
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview: " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);

        fragment.setArguments(args);

        FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();

    }

    private void init(){

        Log.d(TAG, "init: Inflating Fragment " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction trasaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        trasaction.replace(R.id.container,fragment);
        trasaction.addToBackStack(getString(R.string.profile_fragment));
        trasaction.commit();
    }


}
