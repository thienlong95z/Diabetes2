package com.hust.stormfury.diabetes.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hust.stormfury.diabetes.R;
import com.hust.stormfury.diabetes.models.Photo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class CameraFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "CameraFragment";
    private ArrayList<Photo> mPhotos;
    private Integer days[];
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera,container,false);
        mPhotos = new ArrayList<>();
        int days[] = new int[6];
        getCal();
        System.out.print(days[0]);
        return view;
    }

    private void clearAll(){
        if(mPhotos != null){
            mPhotos.clear();
        }
        mPhotos = new ArrayList<>();
    }

    private void getCal(){
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
                    Log.d(TAG, "getMessages: messages: " + newPhoto.getCalories());
                    mPhotos.add(newPhoto);
                    newPhoto.getCalories();
                    calSum();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void calSum(){
        if (mPhotos != null){
            try{
                int iterations = mPhotos.size();
                for(int i = 0; i < iterations; i++){
                    String timestampDifference = getTimestampDifference(mPhotos.get(i));
                    if (timestampDifference.equals("0")){
                        days[0] += Integer.valueOf(mPhotos.get(i).getCalories());
                    } else if (timestampDifference.equals("1")){
                        days[1] += Integer.valueOf(mPhotos.get(i).getCalories());
                    } else if (timestampDifference.equals("2")){
                        days[2] += Integer.valueOf(mPhotos.get(i).getCalories());
                    } else if (timestampDifference.equals("3")){
                        days[3] += Integer.valueOf(mPhotos.get(i).getCalories());
                    } else if (timestampDifference.equals("4")){
                        days[4] += Integer.valueOf(mPhotos.get(i).getCalories());
                    } else if (timestampDifference.equals("5")){
                        days[5] += Integer.valueOf(mPhotos.get(i).getCalories());
                    } else if (timestampDifference.equals("6")){
                        days[6] += Integer.valueOf(mPhotos.get(i).getCalories());
                    }
                }
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
            }
        }
    }

    private String getTimestampDifference(Photo photo){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
}
