package com.hust.stormfury.diabetes.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.eschao.android.widget.elasticlistview.ElasticListView;
import com.eschao.android.widget.elasticlistview.LoadFooter;
import com.eschao.android.widget.elasticlistview.OnLoadListener;
import com.eschao.android.widget.elasticlistview.OnUpdateListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hust.stormfury.diabetes.R;
import com.hust.stormfury.diabetes.Utils.MainFeedListAdapter;
import com.hust.stormfury.diabetes.models.Message;
import com.hust.stormfury.diabetes.models.Photo;
import com.hust.stormfury.diabetes.models.UserAccountSettings;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeFragment extends android.support.v4.app.Fragment implements OnUpdateListener, OnLoadListener {
    private static final String TAG = "HomeFragment";

    @Override
    public void onUpdate() {
        Log.d(TAG, "ElasticListView: updating list view...");

        getPhotos();
    }


    @Override
    public void onLoad() {
        Log.d(TAG, "ElasticListView: loading...");

        // Notify load is done
        mListView.notifyLoaded();
    }
    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ElasticListView mListView;

    private MainFeedListAdapter adapter;
    private int resultsCount = 0;
    private ArrayList<UserAccountSettings> mUserAccountSettings;
    //    private ArrayList<UserStories> mAllUserStories = new ArrayList<>();
    private JSONArray mMasterStoriesArray;

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        mListView = (ElasticListView) view.findViewById(R.id.listView);
        mPhotos = new ArrayList<>();
        initListViewRefresh();
        displayPhotos();
        return view;
    }

    private void initListViewRefresh(){
        mListView.setHorizontalFadingEdgeEnabled(true);
        mListView.setAdapter(adapter);
        mListView.enableLoadFooter(true).getLoadFooter().setLoadAction(LoadFooter.LoadAction.RELEASE_TO_LOAD);
        mListView.setOnUpdateListener(this)
                .setOnLoadListener(this);
//        mListView.requestUpdate();
    }
    private void clearAll(){
        if(mPhotos != null){
            mPhotos.clear();
            if(mPhotos != null){
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        }
        if(mUserAccountSettings != null){
            mUserAccountSettings.clear();
        }
        if(mPaginatedPhotos != null){
            mPaginatedPhotos.clear();
        }
        mPhotos = new ArrayList<>();
        mPaginatedPhotos = new ArrayList<>();
        mUserAccountSettings = new ArrayList<>();
    }
    private void getPhotos(){
        clearAll();
        Log.d(TAG, "setupGridView: Getting messages.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("user_photos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d("aaaa", FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()) {
                    Photo newPhoto = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    newPhoto.setCaption(objectMap.get("caption").toString());
                    newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                    newPhoto.setPredicted_food(objectMap.get(getString(R.string.field_predicted_food)).toString());
                    newPhoto.setCalories(objectMap.get(getString(R.string.field_calories)).toString());
                    Log.d(TAG, "getMessages: messages: " + newPhoto.getPhoto_id());
                    mPhotos.add(newPhoto);
                    displayPhotos();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null){
            try{
                //sort for newest to oldest
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                //we want to load 10 at a time. So if there is more than 10, just load 10 to start
                int iterations = mPhotos.size();
                if(iterations > 10){
                    iterations = 10;
                }
//
                resultsCount = 0;
                for(int i = 0; i < iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                    resultsCount++;
                    Log.d(TAG, "displayPhotos: adding a photo to paginated list: " + mPhotos.get(i).getPhoto_id());
                }

                adapter = new MainFeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
                mListView.setAdapter(adapter);

                // Notify update is done
                mListView.notifyUpdated();

            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{

            if(mPhotos.size() > resultsCount && mPhotos.size() > 0){

                int iterations;
                if(mPhotos.size() > (resultsCount + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - resultsCount;
                }

                //add the new photos to the paginated list
                for(int i = resultsCount; i < resultsCount + iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                resultsCount = resultsCount + iterations;
                adapter.notifyDataSetChanged();
            }
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
        }
    }
}
