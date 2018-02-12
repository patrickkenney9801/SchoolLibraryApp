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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;

public class BrowseLibraryActivity extends AppCompatActivity {

    private FetchLibrariesTask mTask = null;

    private View mBrowseLibraryForm;
    private View mProgressView;

    private ArrayAdapter mAdapter;
    private ListView listView;
    private ArrayList<String> libraryNames = new ArrayList<>();

    private final Object dataLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_library);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBrowseLibraryForm = (View) findViewById(R.id.library_list);
        mProgressView = findViewById(R.id.browse_library_progress);
        
        updateLibraries();

        mAdapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, libraryNames);

        listView = findViewById(R.id.library_list);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                openLibraryLogin(position);
            }
        });
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

    private void openLibraryLogin(int position) {
        Intent intent = new Intent(this, LibraryLoginActivity.class);
        intent.putExtra("Library_ID", "" + position);
        startActivity(intent);
    }

    /**
     * Updates all library names from the server after storing their data
     * @return
     */
    private void getLibraries() {
        ArrayList<String> lNames = new ArrayList<>();
        for (int i = 0; i < Library.libraries.size(); i++)
            lNames.add(Library.libraries.get(i).name);
        libraryNames.clear();
        libraryNames.addAll(lNames);
    }

    private void updateLibraries() {
        try {
            synchronized (dataLock) {
                showProgress(true);
                mTask = new FetchLibrariesTask(this);
                mTask.execute();
            }
        } catch(Exception e) {}
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

            mBrowseLibraryForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mBrowseLibraryForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBrowseLibraryForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mBrowseLibraryForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class FetchLibrariesTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = BrowseLibraryActivity.FetchLibrariesTask.class.getSimpleName();

        private Activity mParent;

        FetchLibrariesTask(Activity parent) {
            this.mParent = parent;
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
                    // create server JSON
                    JSONObject serverJSON = new JSONObject();
                    serverJSON.put("server_password", getString(R.string.server_password));

                    String body = String.valueOf(serverJSON);

                    // construct the URL to fetch a user
                    Uri.Builder  builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("libraries")
                            .appendPath("getlibraries")
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
                    Log.d(LOG_TAG, "Response Code from Server: "+responseCode);

                    if(responseCode == 200) {
                        // read response to get user data from server
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String line = "";
                        String responseBody = "";
                        while((line = reader.readLine()) != null) {
                            responseBody += line + '\n';
                        }

                        JSONArray libraryJSON = new JSONArray(responseBody);

                        Library.libraries.clear();

                        for (int i = 0; i < libraryJSON.length(); i++) {
                            JSONObject library = libraryJSON.getJSONObject(i);

                            // add libraries to list
                            Library.libraries.add(new Library( library.getString("name"), library.getString("librarian_password"),
                                                               library.getString("teacher_password"), library.getString("general_password"),
                                                               Integer.parseInt(library.getString("general_checkout_limit")), library.getString("library_key")));
                        }

                        Log.d(LOG_TAG, libraryJSON.toString());
                        return true;
                    } else if(responseCode == 310) {
                        return false;
                    } else {
                        Log.e(LOG_TAG, "response Code = "+responseCode);
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
            getLibraries();
            mAdapter.notifyDataSetChanged();
            mTask = null;
            showProgress(false);
        }
    }
}
