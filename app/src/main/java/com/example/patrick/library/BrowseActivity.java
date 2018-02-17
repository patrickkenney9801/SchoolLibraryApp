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
import android.os.Debug;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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

public class BrowseActivity extends AppCompatActivity {

    private FetchBooksTask mTask;

    private ListView mListView;
    private ArrayAdapter mAdapter;
    private ArrayList<String> bookNames = new ArrayList<>();

    private View mBrowseForm;
    private View mProgressView;

    private final Object dataLock = new Object();

    private String lastLibraryKey;

    private SwipeRefreshLayout mSwipeRefresh;

    private int browseType;     // 1=reserve, 2=checkout, 3=return

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent startingIntent = getIntent();
        browseType = Integer.parseInt(startingIntent.getStringExtra("BROWSE_TYPE"));

        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        lastLibraryKey = savedData.getString(getString(R.string.last_library_key), null);

        if (browseType == 1)
            toolbar.setTitle(savedData.getString(getString(R.string.last_library_name), "Library App"));
        else if (browseType == 2)
            toolbar.setTitle(R.string.checkout_books);
        else
            toolbar.setTitle(R.string.prompt_return);
        setSupportActionBar(toolbar);
        if (browseType != 1)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBrowseForm = (View) findViewById(R.id.book_list);
        mProgressView = findViewById(R.id.browse_progress);

        updateBooks();

        mAdapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, bookNames);

        mListView = findViewById(R.id.book_list);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                openBookDetail(position);
            }
        });

        mSwipeRefresh = findViewById(R.id.browse_refresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateBooks();
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
                intent = new Intent(this, BrowseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("BROWSE_TYPE", "1");
                startActivity(intent);
                return super.onOptionsItemSelected(item);
        }
    }

    private void openBookDetail(int position) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("BOOK_ID", "" + position);
        intent.putExtra("BOOK_DETAIL_TYPE", "" + browseType);
        startActivity(intent);
    }

    /**
     * Updates all book names from the server after storing their data
     * @return
     */
    private void getBooks() {
        ArrayList<String> bNames = new ArrayList<>();
        for (int i = 0; i < Book.books.size(); i++)
            bNames.add(Book.books.get(i).name);
        bookNames.clear();
        for (int i = 0; i < bNames.size(); i++)
            bookNames.add(bNames.get(i));
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Gets books from the server for the library
     */
    private void updateBooks() {
        try {
            synchronized (dataLock) {
                showProgress(true);
                mTask = new BrowseActivity.FetchBooksTask(this, lastLibraryKey, browseType);
                mTask.execute();
            }
        } catch(Exception e) {Log.e("3651236128381", e.toString());}
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

            mBrowseForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mBrowseForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBrowseForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mBrowseForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class FetchBooksTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = BrowseActivity.FetchBooksTask.class.getSimpleName();

        private Activity mParent;
        private String mLibraryKey;
        private int bookType;

        FetchBooksTask(Activity parent, String lKey, int bType) {
            this.mParent = parent;
            this.mLibraryKey = lKey;
            this.bookType = bType;
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
                    serverJSON.put("library_key", mLibraryKey);

                    String body = String.valueOf(serverJSON);

                    // construct the URL to fetch a user
                    Uri.Builder  builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("libraries");
                    if (bookType == 1)
                        builder.appendPath("getbooks");
                    else if (bookType == 2)
                        builder.appendPath("getreservedbooks");
                    else
                        builder.appendPath("getcheckedoutbooks");
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

                        JSONArray bookJSON = new JSONArray(responseBody);

                        Book.books.clear();

                        for (int i = 0; i < bookJSON.length(); i++) {
                            JSONObject book = bookJSON.getJSONObject(i);

                            // add libraries to list
                            Book.books.add(new Book( book.getString("name"), book.getString("author_first_name"),
                                    book.getString("author_last_name"), book.getString("year_published"),
                                    book.getString("reserved").charAt(0), book.getString("date_reserved"),
                                    book.getString("checked_out").charAt(0), book.getString("date_checked_out"),
                                    book.getString("user_key"), book.getString("library_key"), book.getString("book_key")));
                        }

                        Log.d(LOG_TAG, bookJSON.toString());
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
            getBooks();
            mAdapter.notifyDataSetChanged();
            mTask = null;
            showProgress(false);
            mSwipeRefresh.setRefreshing(false);
        }
    }
}
