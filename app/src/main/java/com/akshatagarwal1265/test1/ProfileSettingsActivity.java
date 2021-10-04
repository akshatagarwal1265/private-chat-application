package com.akshatagarwal1265.test1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileSettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorageRef;

    private CircleImageView mImage;
    private TextView mStatus;
    private TextView mName;

    private Button mChangeName;
    private Button mChangeStatus;
    private Button mChangeImage;

    private ProgressDialog mProgress;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        getSupportActionBar().setTitle("Profile Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mImageStorageRef = FirebaseStorage.getInstance().getReference();

        mStatus = (TextView)findViewById(R.id.profile_settings_status);
        mName = (TextView)findViewById(R.id.profile_settings_name);
        mImage = (CircleImageView)findViewById(R.id.profile_settings_image);

        mChangeImage = (Button)findViewById(R.id.profile_settings_change_image_btn);
        mChangeName = (Button)findViewById(R.id.profile_settings_change_name_btn);
        mChangeStatus = (Button)findViewById(R.id.profile_settings_change_status_btn);

        mUserDatabaseRef.keepSynced(true);

        mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot != null) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    //final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    mName.setText(name);
                    mStatus.setText(status);
                    if (!image.equals("default")) {

                        //TODO Why networkPolicy needed? Seems To Be Downloading Automatically From Cache If Exists.
                        Picasso.with(ProfileSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).
                                placeholder(R.drawable.default_avatar).into(mImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(ProfileSettingsActivity.this).load(image).
                                        placeholder(R.drawable.default_avatar).into(mImage);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changeStatusIntent = new Intent(ProfileSettingsActivity.this, ChangeStatusActivity.class);
                changeStatusIntent.putExtra("old_status", mStatus.getText().toString());
                startActivity(changeStatusIntent);

            }
        });

        mChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changeNameIntent = new Intent(ProfileSettingsActivity.this, ChangeNameActivity.class);
                changeNameIntent.putExtra("old_name", mName.getText().toString());
                startActivity(changeNameIntent);

            }
        });

        mChangeImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                ///*................................OUR WAY (Without Camera Option)

                //TODO Use Alert :- Choose From Gallery, Choose From Camera.
                //TODO Provide Square Crop For Both Options

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                //*/
                //OR//
                /*................................ARTHUR'S WAY (With Camera Option)

                //start picker to get image for cropping and then use the image in cropping activity
                //Default Aspect ratio is 1,1, so could have done just 'setFixAspectRatio(true)'
                //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setFixAspectRatio(true).start(SettingsActivity.this);
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setMinCropWindowSize(500,500)
                        .start(SettingsActivity.this);

                //*/
            }
        });
    }

    /*................................ARTHUR'S WAY

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Uploading Image");
                mProgress.setMessage("Please Wait As Upload & Processing Depends Upon Your Connection Speed");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                String current_uid = mCurrentUser.getUid();

                final File thumb_file = new File(resultUri.getPath());

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte_data = baos.toByteArray();

                //Since Image name is same as current_uid, for each user, the old image is automatically replaced
                //If random name would have been used, we would have had to manage the old image manually
                StorageReference filepath = mImageStorage.child("profile_images").child(current_uid + ".jpg");

                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_uid + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            @SuppressWarnings("VisibleForTests")
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte_data);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    @SuppressWarnings("VisibleForTests")
                                    final String thumb_url = task.getResult().getDownloadUrl().toString();

                                    if(task.isSuccessful())
                                    {

                                        Map updateMap = new HashMap();
                                        updateMap.put("image",download_url);
                                        updateMap.put("thumb_image",thumb_url);

                                        mUserDatabase.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Successfully Done", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Error Updating Image and Thumbnail", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        mProgress.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Error Uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            mProgress.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error Uploading Image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    //*/
    //OR//
    ///*................................OUR WAY

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri resultUri = data.getData();

            //TODO If Possible Square Crop Gallery Pic here
            // start cropping activity for pre-acquired image saved on the device
            //CropImage.activity(imageUri).setAspectRatio(1,1).setMinCropWindowSize(500,500).start(this);

            mProgress = new ProgressDialog(ProfileSettingsActivity.this);
            mProgress.setTitle("Uploading Image");
            mProgress.setMessage("Please Wait As Upload & Processing Depends Upon Your Connection Speed");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();

            String current_uid = mCurrentUser.getUid();

            final File thumb_file = new File(resultUri.getPath());

            Bitmap thumb_bitmap = null;
            try {
                thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] thumb_byte_data = baos.toByteArray();

            //Since Image name is same as current_uid, for each user, the old image is automatically replaced
            //If random name would have been used, we would have had to manage the old image manually
            StorageReference filepath = mImageStorageRef.child("profile_images").child(current_uid + ".jpg");

            final StorageReference thumb_filepath = mImageStorageRef.child("profile_images").child("thumbs").child(current_uid + ".jpg");

            filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        @SuppressWarnings("VisibleForTests")
                        final String download_url = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte_data);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if(task.isSuccessful())
                                {
                                    @SuppressWarnings("VisibleForTests")
                                    final String thumb_url = task.getResult().getDownloadUrl().toString();

                                    Map updateMap = new HashMap();
                                    updateMap.put("image",download_url);
                                    updateMap.put("thumb_image",thumb_url);

                                    mUserDatabaseRef.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                mProgress.dismiss();
                                                Toast.makeText(ProfileSettingsActivity.this, "Successfully Done", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                mProgress.dismiss();
                                                Toast.makeText(ProfileSettingsActivity.this, "Error Updating Image and Thumbnail", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    mProgress.dismiss();
                                    Toast.makeText(ProfileSettingsActivity.this, "Error Uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(ProfileSettingsActivity.this, "Error Uploading Image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //*/

}