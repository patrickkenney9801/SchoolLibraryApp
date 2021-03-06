package com.booksonthego.patrick.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateLibraryActivity extends AppCompatActivity {

    private CreateLibraryTask mTask;

    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;

    private EditText libraryNameEnter;
    private EditText librarianPasswordEnter;
    private EditText teacherPasswordEnter;
    private EditText generalPasswordEnter;
    private EditText generalCheckoutLimitEnter;
    private EditText lendDaysEnter;
    private EditText dailyFeeEnter;

    private Button takePhotoButton;
    private Button createLibraryButton;

    private View mProgressView;
    private View mCreateLibraryForm;

    private Bitmap mThumbnail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_library);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.create_library);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        libraryNameEnter = findViewById(R.id.create_library_name);
        librarianPasswordEnter = findViewById(R.id.create_librarian_password);
        teacherPasswordEnter = findViewById(R.id.create_teacher_password);
        generalPasswordEnter = findViewById(R.id.create_public_password);
        generalCheckoutLimitEnter = findViewById(R.id.create_general_checkout_limit);
        lendDaysEnter = findViewById(R.id.create_general_lend_time);
        dailyFeeEnter = findViewById(R.id.create_daily_fee);
        createLibraryButton = findViewById(R.id.create_library_button);
        createLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLibrary();
            }
        });
        takePhotoButton = findViewById(R.id.take_map_photo);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mProgressView = findViewById(R.id.create_library_progress);
        mCreateLibraryForm = findViewById(R.id.create_library_form);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // do not allow user to navigate the app if they do not belong to a library
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        String lastLibraryKey = savedData.getString(getString(R.string.last_library_key), null);
        if (lastLibraryKey == null || lastLibraryKey.length() != 36) {
            intent = new Intent(this, BrowseLibraryActivity.class);
            startActivity(intent);
            return true;
        }

        switch (item.getItemId()) {
            case R.id.show_map:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                return true;

            case R.id.change_library:
                intent = new Intent(this, BrowseLibraryActivity.class);
                startActivity(intent);
                return true;

            case R.id.advanced:
                intent = new Intent(this, AdvancedMenuActivity.class);
                startActivity(intent);
                return true;

            case R.id.report_bug:
                intent = new Intent(this, ReportBugActivity.class);
                startActivity(intent);
                return true;

            default:
                intent = new Intent(this, BrowseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("BROWSE_TYPE", "1");
                startActivity(intent);
                return true;

        }
    }

    /**
     * Takes a photo that will be used for the library map
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("CreateLibrary", "Error creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Creates a file to store the image before it is processed
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    /**
     * Gets picture after taken
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Get the dimensions of the View
            ScrollView mScrollView = findViewById(R.id.create_library_form);
            int targetW = mScrollView.getWidth();
            int targetH = mScrollView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            if(bmOptions.outHeight <= 0)
                return;

            // Determine how much to scale the image
            int scaleFactor = 25;//Math.max(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            mThumbnail = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        }
    }

    private void createLibrary() {
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        if (mThumbnail == null) {
            takePhotoButton.setError(getString(R.string.error_no_map_photo));
            return;
        }
        showProgress(true);
        mTask = new CreateLibraryTask(this, libraryNameEnter.getText().toString(), librarianPasswordEnter.getText().toString(),
                teacherPasswordEnter.getText().toString(), generalPasswordEnter.getText().toString(), generalCheckoutLimitEnter.getText().toString(),
                savedData.getString(getString(R.string.user_key), null), mThumbnail, lendDaysEnter.getText().toString(), dailyFeeEnter.getText().toString());
        mTask.execute();
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCreateLibraryForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateLibraryForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateLibraryForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCreateLibraryForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class CreateLibraryTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = CreateLibraryActivity.CreateLibraryTask.class.getSimpleName();

        private Activity mParent;

        private String name;
        private String libraryPassword;
        private String teacherPassword;
        private String generalPassword;
        private int generalCOLimit;
        private String userKey;

        private String libraryKey;

        private Bitmap mBitmap;
        private String thumbnail;
        private int lendDays;
        private float dailyFee;

        CreateLibraryTask(Activity parent, String name, String libPass, String teachPass, String genPass, String genCOLimit, String uKey, Bitmap bmap, String lDay, String dFee) {
            this.mParent = parent;
            this.name = name;
            this.libraryPassword = libPass;
            this.teacherPassword = teachPass;
            this.generalPassword = genPass;
            try {
                this.generalCOLimit = Integer.parseInt(genCOLimit);
            } catch (Exception e) {this.generalCOLimit = 5;}
            if (this.generalCOLimit < 1)
                this.generalCOLimit = 3;
            this.userKey = uKey;
            this.mBitmap = bmap;
            try {
                this.lendDays = Integer.parseInt(lDay);
            } catch (Exception e) {this.lendDays = 14;}
            if (this.lendDays < 7)
                this.lendDays = 7;
            try {
                this.dailyFee = Float.parseFloat(dFee);
            } catch (Exception e) {this.dailyFee = 0;}
            if (this.dailyFee < 0)
                this.dailyFee = 0;
            if (this.dailyFee > 3)
                this.dailyFee = 3;
        }

        protected Boolean doInBackground(Void... Params) {
            // variables that we will have to close in try loop
            HttpURLConnection urlConnection = null;
            BufferedWriter out = null;
            BufferedReader reader = null;

            // the unparsed JSON response from the server
            int responseCode = -1;


            // check for internet connection
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();

            if (info == null || !info.isConnected()) {
                // if there is no network, inform user through a toast
                mParent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mParent, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, "No connection available");
                    }
                });
            } else {
                try {
                    // make library map sendable
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    thumbnail = Base64.encodeToString(byteArray,Base64.NO_WRAP);

                    // create user JSON
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("server_password", getString(R.string.server_password));
                    userJSON.put("name", name);
                    userJSON.put("librarian_password", libraryPassword);
                    userJSON.put("teacher_password", teacherPassword);
                    userJSON.put("general_password", generalPassword);
                    userJSON.put("user_key", userKey);
                    userJSON.put("general_checkout_limit", generalCOLimit);
                    userJSON.put("library_map", thumbnail);
                    userJSON.put("lend_days", lendDays);
                    userJSON.put("late_fee", dailyFee);

                    String body = String.valueOf(userJSON);

                    // construct the URL to fetch a user
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("libraries")
                            .appendPath("create")
                            .build();
                    URL url = new URL(builder.toString());
                    // connect to the URL and open the reader
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.connect();

                    // send JSON to Cloud Server
                    out = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                    out.write(body);
                    out.flush();

                    // see if post was a success
                    responseCode = urlConnection.getResponseCode();
                    Log.d(LOG_TAG, "Response Code from Server: " + responseCode);

                    if (responseCode == 200) {
                        // read response to get user data from server
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String line = "";
                        String responseBody = "";
                        while ((line = reader.readLine()) != null) {
                            responseBody += line + '\n';
                        }

                        JSONObject userObj = new JSONObject(responseBody);

                        libraryKey = userObj.getString("library_key");

                        Log.d(LOG_TAG, userObj.toString());
                    } else if (responseCode == 321) {
                        libraryNameEnter.setError(getString(R.string.error_used_library_name));
                        return false;
                    } else {
                        Log.e(LOG_TAG, "response Code = " + responseCode);
                        return false;
                    }
                    return true;
                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, "The URL was incorrectly formed");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Unidentified error in network operations while creating account");
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (out != null) {
                        try {
                            out.close();
                            reader.close();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Couldn't close out or reader stream", e);
                        }

                    }
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mTask = null;

            if(success) {
                SharedPreferences savedData = mParent.getSharedPreferences(getString(R.string.saved_data_file_key),
                    Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedData.edit();
                editor.putString(getString(R.string.last_library_key), libraryKey);
                editor.putString(getString(R.string.user_role), "C");
                editor.putString(getString(R.string.checkout_limit), "1000");
                editor.putString(getString(R.string.checkout_books), "0");
                editor.putString(getString(R.string.last_library_name), name);
                editor.putString(getString(R.string.map), thumbnail);
                editor.apply();

                // launch browse activity so user can view new library
                Intent intent = new Intent(mParent, BrowseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("BROWSE_TYPE", "1");
                startActivity(intent);
            }
            showProgress(false);
        }
    }
}
