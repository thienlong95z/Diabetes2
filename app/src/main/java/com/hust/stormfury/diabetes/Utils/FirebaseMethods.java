package com.hust.stormfury.diabetes.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hust.stormfury.diabetes.Home.HomeActivity;
import com.hust.stormfury.diabetes.Profile.AccountSettingsActivity;
import com.hust.stormfury.diabetes.R;
import com.hust.stormfury.diabetes.models.Photo;
import com.hust.stormfury.diabetes.models.User;
import com.hust.stormfury.diabetes.models.UserAccountSettings;
import com.hust.stormfury.diabetes.models.UserSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Akshat Pandey
 * @version 1.0
 * @date 23-10-2017
 */

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private double mPhotoUploadProgress = 0;

    private Context mContext;
    private String userID;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;
    }

    public void uploadNewPhoto(String photoType, final String caption,final int count, final String imgUrl,
                               Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: attempting to uplaod new photo.");

        FilePaths filePaths = new FilePaths();
        //case1) new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                    //add the new photo to 'photos' node and 'user_photos' node
                    addPhotoToDatabase(caption, firebaseUrl.toString());

                    //navigate to the main feed so the user can see their photo
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

        }
        //case new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");
            ((AccountSettingsActivity)mContext).setViewPager(
                    ((AccountSettingsActivity)mContext).pagerAdapter
                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
            );

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                    //insert into 'user_account_settings' node
                    setProfilePhoto(firebaseUrl.toString());



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }

    }

    /** upload photo to Firebase database **/
    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);
        photo.setPredicted_food("");
        photo.setCalories("");
        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

    }

    /**public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
        Log.d(TAG, "checkIfUsernameExists: checking if the username already exists");

        User user = new User();

        for (DataSnapshot ds : dataSnapshot.child(userID).getChildren()) {
            user.setUsername(ds.getValue(User.class).getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
                Log.d(TAG, "checkIfUsernameExists: Found a match.");
                return true;
            }
        }

        return false;
    }**/

    /**
     * Register a new email and password to Firebase Authentication.
     *
     * @param email
     * @param username
     * @param password
     */
    public void registerNewEmail(final String email, final String username, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            sendVerificationEmail();
                            Log.d(TAG, "onComplete: Authentication Successful.");
                            userID = mAuth.getCurrentUser().getUid();
                        }

                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "Couldn't send Verification Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){

        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");

        if(displayName != null){
            myRef.child("user_account_settings")
                    .child(userID)
                    .child("display_name")
                    .setValue(displayName);
        }


        if(website != null) {
            myRef.child("user_account_settings")
                    .child(userID)
                    .child("website")
                    .setValue(website);
        }

        if(description != null) {
            myRef.child("user_account_settings")
                    .child(userID)
                    .child("description")
                    .setValue(description);
        }

        if(phoneNumber != 0) {
            myRef.child("user_account_settings")
                    .child(userID)
                    .child("phone_number")
                    .setValue(phoneNumber);
        }
    }

    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: Updating username to " + username);

        myRef.child(mContext.getString(R.string.db_name_users)).child(userID).child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.db_user_account_settings)).child(userID).child(mContext.getString(R.string.field_username))
                .setValue(username);
    }


    /**
     * update the email in the 'user's' node
     * @param email
     */
    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: upadting email to: " + email);

        myRef.child("user").child(userID).child("email").setValue(email);

    }
    /**
     * Add information to the users node.
     * Add information to the users_account_setting node.
     *
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo) {

        User user = new User( userID,  1,  email,  StringManipulation.condenseUsername(username) );

        myRef.child(mContext.getString(R.string.db_name_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(description, username, 0, 0, 0, profile_photo, StringManipulation.condenseUsername(username), website, userID);

        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieves the user information from the firabase Database.
     *
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserSettings: retrieving user settings from the database.");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            if (ds.getKey().equals(mContext.getString(R.string.db_user_account_settings))) {
                Log.d(TAG, "getUserSettings: data snapshot " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID).getValue(UserAccountSettings.class).getDisplay_name()
                    );

                    settings.setDescription(
                            ds.child(userID).getValue(UserAccountSettings.class).getDescription()
                    );

                    settings.setFollowers(
                            ds.child(userID).getValue(UserAccountSettings.class).getFollowers()
                    );

                    settings.setFollowing(
                            ds.child(userID).getValue(UserAccountSettings.class).getFollowing()
                    );

                    settings.setPosts(
                            ds.child(userID).getValue(UserAccountSettings.class).getPosts()
                    );

                    settings.setProfile_photo(
                            ds.child(userID).getValue(UserAccountSettings.class).getProfile_photo()
                    );

                    settings.setUsername(
                            ds.child(userID).getValue(UserAccountSettings.class).getUsername()
                    );

                    settings.setWebsite(
                            ds.child(userID).getValue(UserAccountSettings.class).getWebsite()
                    );
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserSettings: Null pointer exception" + e.getMessage());
                }
            }

            if (ds.getKey().equals(mContext.getString(R.string.db_name_users))) {
                Log.d(TAG, "getUserSettings: data snapshot " + ds);

                user.setUser_id(
                        ds.child(userID).getValue(User.class).getUser_id()
                );

                user.setUsername(
                        ds.child(userID).getValue(User.class).getUsername()
                );

                user.setEmail(
                        ds.child(userID).getValue(User.class).getEmail()
                );

                user.setPhone_number(
                        ds.child(userID).getValue(User.class).getPhone_number()
                );
            }
        }

        return new UserSettings(user, settings);
    }
}
