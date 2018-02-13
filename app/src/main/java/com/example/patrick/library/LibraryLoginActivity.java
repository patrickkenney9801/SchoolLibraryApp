package com.example.patrick.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.patrick.library.logic.Book;
import com.example.patrick.library.logic.Library;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LibraryLoginActivity extends AppCompatActivity {

    private LibraryLoginTask mTask;

    private Button libraryLogin;
    private Button generalSignUp;
    private Button teacherSignUp;
    private Button librarianSignUp;
    private TextView passwordEntry;

    private Library selectedLibrary;

    private View mLibraryLoginForm;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent startingIntent = getIntent();
        int libraryID = Integer.parseInt(startingIntent.getStringExtra("Library_ID"));
        selectedLibrary = Library.libraries.get(libraryID);

        passwordEntry = findViewById(R.id.library_password);

        libraryLogin = findViewById(R.id.library_login);
        libraryLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLibraryLogin(1, passwordEntry.getText().toString());
            }
        });
        generalSignUp = findViewById(R.id.library_general);
        generalSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLibraryLogin(2, passwordEntry.getText().toString());
            }
        });
        teacherSignUp = findViewById(R.id.library_teacher);
        teacherSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLibraryLogin(3, passwordEntry.getText().toString());
            }
        });
        librarianSignUp = findViewById(R.id.library_librarian);
        librarianSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLibraryLogin(4, passwordEntry.getText().toString());
            }
        });

        mLibraryLoginForm = findViewById(R.id.library_login_form);
        mProgressView = findViewById(R.id.library_login_progress);
    }

    private void attemptLibraryLogin(int loginMethod, String pass) {
        showProgress(true);
        mTask = new LibraryLoginActivity.LibraryLoginTask(this, loginMethod, pass, selectedLibrary.libraryKey);
        mTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // do not allow user to navidate the app if they do not belong to a library
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        String lastLibraryKey = savedData.getString(getString(R.string.last_library_key), null);
        if (lastLibraryKey == null || lastLibraryKey.length() != 36)
            return true;

        Intent intent;
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
                return super.onOptionsItemSelected(item);

        }
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

            mLibraryLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mLibraryLoginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLibraryLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLibraryLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class LibraryLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = LibraryLoginActivity.LibraryLoginTask.class.getSimpleName();

        private Activity mParent;
        private int loginMethod;
        private String mPassword;

        private String mRole;
        private String mCOLimit;
        private String mLibraryKey;
        private String mBookCount;

        LibraryLoginTask(Activity parent, int logMethod, String pass, String libKey) {
            this.mParent = parent;
            this.loginMethod = logMethod;
            this.mPassword = pass;
            this.mLibraryKey = libKey;
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

            if(info == null || !info.isConnected()) {
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
                    SharedPreferences savedData = mParent.getSharedPreferences(getString(R.string.saved_data_file_key),
                            Context.MODE_PRIVATE);
                    // create server JSON
                    JSONObject credentialsJSON = new JSONObject();
                    credentialsJSON.put("server_password", getString(R.string.server_password));
                    credentialsJSON.put("library_key", mLibraryKey);
                    credentialsJSON.put("user_key", savedData.getString(getString(R.string.user_key), ""));

                    // construct the URL to fetch a user
                    Uri.Builder  builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("libraries");
                    switch(loginMethod) {
                        case 1:     builder.appendPath("logintolibrary");
                                    break;
                        case 2:     builder.appendPath("signintolibrarygeneral");
                                    credentialsJSON.put("general_password", mPassword);
                                    break;
                        case 3:     builder.appendPath("signintolibraryteacher");
                                    credentialsJSON.put("teacher_password", mPassword);
                                    break;
                        case 4:     builder.appendPath("signintolibrarylibrarian");
                                    credentialsJSON.put("librarian_password", mPassword);
                                    break;
                        default:    return false;
                    }
                    builder.build();
                    
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

                    String body = String.valueOf(credentialsJSON);

                    // send JSON to Cloud Server
                    out = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                    out.write(body);
                    out.flush();

                    // see if post was a success
                    responseCode = urlConnection.getResponseCode();
                    Log.d(LOG_TAG, "Response Code from Server: "+responseCode);

                    if(responseCode == 200) {
                        // read response to get user data from server
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String line = "";
                        String responseBody = "";
                        while((line = reader.readLine()) != null) {
                            responseBody += line + '\n';
                        }

                        JSONObject permissionsJSON = new JSONObject(responseBody);

                        // get important info
                        mRole = permissionsJSON.getString("role");
                        mCOLimit = permissionsJSON.getString("checkout_limit");
                        mBookCount = permissionsJSON.getString("user_book_count");

                        Log.d(LOG_TAG, permissionsJSON.toString());
                        return true;
                    } else if(responseCode == 310) {
                        return false;
                    } else {
                        Log.e(LOG_TAG, "response Code = "+responseCode);
                        return false;
                    }
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
                        } catch(Exception e) {
                            Log.e(LOG_TAG, "Couldn't close out or reader stream", e);
                        }

                    }
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // update list view
            mTask = null;
            showProgress(false);

            if (success) {
                // add library and permissions to saved data
                SharedPreferences savedData = mParent.getSharedPreferences(getString(R.string.saved_data_file_key),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedData.edit();
                editor.putString(getString(R.string.user_role), mRole);
                editor.putString(getString(R.string.checkout_limit), mCOLimit);
                editor.putString(getString(R.string.user_book_count), mBookCount);
                editor.putString(getString(R.string.last_library_key), mLibraryKey);
                editor.apply();

                // take user to browse for the library
                Intent intent = new Intent(mParent, BrowseActivity.class);
                startActivity(intent);
            } else {
                passwordEntry.setError(getString(R.string.error_incorrect_password));
                passwordEntry.requestFocus();
            }
        }
    }
}
