package com.hust.stormfury.diabetes.Home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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
import com.hust.stormfury.diabetes.Utils.FirebaseMethods;
import com.hust.stormfury.diabetes.Utils.MessageListAdapter;
import com.hust.stormfury.diabetes.models.Message;
import com.hust.stormfury.diabetes.models.UserAccountSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class MessagesFragment extends android.support.v4.app.Fragment implements OnUpdateListener, OnLoadListener{
    private static final String TAG = "MessagesFragment";

    @Override
    public void onUpdate() {
        Log.d(TAG, "ElasticListView: updating list view...");

        getMessage();
    }


    @Override
    public void onLoad() {
        Log.d(TAG, "ElasticListView: loading...");

        // Notify load is done
        mListview.notifyLoaded();
    }

    //vars
    private ArrayList<Message> mMessages;
    private ArrayList<Message> mPaginatedMessages;
    private ElasticListView mListview;
    private MessageListAdapter mAdapter;
    private int resultsCount = 0;
    private ArrayList<UserAccountSettings> mUserAccountSettings;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages,container,false);
        mListview = (ElasticListView) view.findViewById(R.id.listView);

        Log.d(TAG, "onCreateView: stared.");
        initListViewRefresh();
        displayMessages();
        return view;
    }

    private void initListViewRefresh(){
        mListview.setHorizontalFadingEdgeEnabled(true);
        mListview.setAdapter(mAdapter);
        mListview.enableLoadFooter(true).getLoadFooter().setLoadAction(LoadFooter.LoadAction.RELEASE_TO_LOAD);
        mListview.setOnUpdateListener(this)
                .setOnLoadListener(this);
//        mListView.requestUpdate();
    }
    private void clearAll(){
        if(mMessages != null){
            mMessages.clear();
            if(mMessages != null){
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
        }
        if(mUserAccountSettings != null){
            mUserAccountSettings.clear();
        }
        if(mPaginatedMessages != null){
            mPaginatedMessages.clear();
        }
        mMessages = new ArrayList<>();
        mPaginatedMessages = new ArrayList<>();
        mUserAccountSettings = new ArrayList<>();
    }
    private void getMessage(){
        clearAll();
        Log.d(TAG, "setupGridView: Getting messages.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("user_messages")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d("aaaa", FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()) {
                    Message message = new Message();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    message.setMessage(objectMap.get("message").toString());
                    message.setDate(objectMap.get("date").toString());
                    message.setMessage_id(objectMap.get("message_id").toString());
                    message.setUser_id(objectMap.get("user_id").toString());

                    Log.d(TAG, "getMessages: messages: " + message.getMessage_id());
                    Log.d(TAG, "getMessages: sadsa: " + message.getMessage());
                    mMessages.add(message);
                    displayMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private Date convertDate(String photoTimestamp ){
        Date timestamp = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));//google 'android list of timezones'
        try{
            timestamp = sdf.parse(photoTimestamp);
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
        }
        return timestamp;
    }

    private void displayMessages(){
        mPaginatedMessages = new ArrayList<>();
        if (mMessages != null){
            try {
                Collections.sort(mMessages, new Comparator<Message>(){
                    public int compare(Message m1, Message m2){
                        return m2.getDate().compareTo(m1.getDate());
                    }
                });

                //we want to load 10 at a time. So if there is more than 10, just load 10 to start
                int iterations = mMessages.size();
                if(iterations > 5){
                    iterations = 5;
                }

                resultsCount = 0;
                for(int i = 0; i < iterations; i++){
                    mPaginatedMessages.add(mMessages.get(i));
                    resultsCount++;
                    Log.d(TAG, "displayPhotos: adding a photo to paginated list: " + mMessages.get(i).getMessage_id());
                }
                mAdapter = new MessageListAdapter(getActivity(), R.layout.layout_message_listitem, mPaginatedMessages);
                mListview.setAdapter(mAdapter);

                // Notify update is done
                mListview.notifyUpdated();
            }
            catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
            }
        }
    }

    public void displayMoreMessages(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{

            if(mMessages.size() > resultsCount && mMessages.size() > 0){

                int iterations;
                if(mMessages.size() > (resultsCount + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mMessages.size() - resultsCount;
                }

                //add the new photos to the paginated list
                for(int i = resultsCount; i < resultsCount + iterations; i++){
                    mPaginatedMessages.add(mMessages.get(i));
                }

                resultsCount = resultsCount + iterations;
                mAdapter.notifyDataSetChanged();
            }
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
        }
    }

}
