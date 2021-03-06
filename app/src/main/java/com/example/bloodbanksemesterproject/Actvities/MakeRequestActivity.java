package com.example.bloodbanksemesterproject.Actvities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.example.bloodbanksemesterproject.R;
import com.example.bloodbanksemesterproject.Utils.Endpoints;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MakeRequestActivity extends AppCompatActivity {
 /*
    name: Clara McGrew
    project: Android Blood Bank Semester Project
    date: 4/21/2020 Presentation
    date: 4/23/2020 Final code upload
    relevant github libraries/code/repositories used in this application include
    VolleySingleton.java
    https://gist.github.com/RISHABH3821/6b1e58de77a4e4909b097c2ca51acf6e
    StringRequest.java
    https://gist.github.com/RISHABH3821/bc48fe91119c2efa14cfab1accc71376
    ExampleAdapter.java
    https://gist.github.com/RISHABH3821/f54ed1d96d95d827ac74bb86aee39521
    Android Networking library
    https://github.com/amitshekhariitbhu/Fast-Android-Networking

     */

    //These are the variables for this activity
    EditText message;
    TextView chooseImage;
    ImageView postImage;
    Button submitButton;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);
        AndroidNetworking.initialize(getApplicationContext());

        //Binds the variables to their ids.
        message = findViewById(R.id.message);
        chooseImage = findViewById(R.id.choose_text);
        postImage = findViewById(R.id.post_image);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(isValid())
            {
                //Write code to upload the post
                uploadRequest(message.getText().toString());
            }
            }
        });

        //This is the onclicklistener for choosing an image for uploading.
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Code to pick image
            permission();
            }
        });
    }

    //A lot of this code below is copied from
    private void pickImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    private void permission(){
        if(PermissionChecker.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
           //asking for permission
            requestPermissions(new String[] {READ_EXTERNAL_STORAGE}, 401);
        }
        else {
            //permission is already there
            pickImage();
        }
    }

    //This is used to request permission to pick an image.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 401) {
            if(grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                pickImage();
            }
            else {
                //permission isn't granted
                showMessage("Permission was not granted");
            }
        }
    }
//This is the method that will enable you to upload the message.
    private void uploadRequest(String message){
        //code that will upload the message
        String path = "";
        try{
        path = getPath(imageUri);
        } catch (URISyntaxException e) {
            showMessage("The uri is wrong");
        }
        String email = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("email", "clamcgrew98@gmail.com");
        AndroidNetworking.upload(Endpoints.upload_request)
                .addMultipartFile("file",new File(path))
                .addQueryParameter("message", message)
                .addQueryParameter("email", email)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                     long progress = (bytesUploaded/totalBytes)* 100;
                     chooseImage.setText(String.valueOf(progress + "%"));
                     chooseImage.setOnClickListener(null);
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                showMessage("Successful!");
                                MakeRequestActivity.this.finish();
                            }
                            else {
                                showMessage(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });


    }

//OnActivityResult  method called for uploading the image.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK) {
            if(data != null){
             imageUri = data.getData();
             Glide.with(getApplicationContext()).load(imageUri).into(postImage);
            }

        }
    }
//This is the isValid method to make sure that the user uploads a message and an image.
    private boolean isValid(){
        if(message.getText().toString().isEmpty()) {
            showMessage("Message is empty. Please enter a message in the message box");
            return false;
        } else if(imageUri == null) {
            showMessage("Pick image");
            return false;
        }
        return true;
    }

    //This is the method that is called above in various different ways.
    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //All of this code below
    // is copied from the code here: https://gist.github.com/RISHABH3821/5e03bb3863a7ff22f137a76a0e0fb421
    //It is used for a file path, and in this case it helps with getting images.
    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
