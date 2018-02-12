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
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CreateBookActivity extends AppCompatActivity {

    private CreateBookTask mTask;

    private EditText bookNameEnter;
    private EditText authorFirstNameEnter;
    private EditText authorLastNameEnter;
    private EditText yearPublishedEnter;
    private EditText bookQuantityEnter;

    private Button createBook;

    private View mCreateBookForm;
    private View mProgressView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_book);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookNameEnter = findViewById(R.id.bookName);
        authorFirstNameEnter = findViewById(R.id.authorFirstName);
        authorLastNameEnter = findViewById(R.id.authorLastName);
        yearPublishedEnter = findViewById(R.id.yearPublished);
        bookQuantityEnter = findViewById(R.id.quantity);

        createBook = findViewById(R.id.createBookButton);
        createBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBook();
            }
        });
        mCreateBookForm = findViewById(R.id.create_book_form);
        mProgressView = findViewById(R.id.create_book_progress);
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
                return true;

        }
    }

    private void addBook() {
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        showProgress(true);
        mTask = new CreateBookTask(this, bookNameEnter.getText().toString(), authorFirstNameEnter.getText().toString(),
                                authorLastNameEnter.getText().toString(), yearPublishedEnter.getText().toString(), bookQuantityEnter.getText().toString(),
                                savedData.getString(getString(R.string.last_library_key), null));
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

            mCreateBookForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateBookForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateBookForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mCreateBookForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class CreateBookTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = CreateBookActivity.CreateBookTask.class.getSimpleName();

        private Activity mParent;

        private String name;
        private String authorFirstName;
        private String authorLastName;
        private String yearPublished;
        private int bookQuantity;
        private String libraryKey;

        CreateBookTask(Activity parent, String name, String afn, String aln, String yp, String qty, String libKey) {
            if (libKey == null)
                return;
            this.mParent = parent;
            this.name = name;
            this.authorFirstName = afn;
            this.authorLastName = aln;
            this.yearPublished = yp;
            if (qty.length() == 0)
                this.bookQuantity = 1;
            else
                this.bookQuantity = Integer.parseInt(qty);
            this.libraryKey = libKey;
            if (bookQuantity < 1)
                bookQuantity = 1;
            if (bookQuantity > 20)
                bookQuantity = 20;
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
                    for (int i = 0; i < bookQuantity; i++) {
                        // create user JSON
                        JSONObject userJSON = new JSONObject();
                        userJSON.put("server_password", getString(R.string.server_password));
                        userJSON.put("name", name);
                        userJSON.put("author_first_name", authorFirstName);
                        userJSON.put("author_last_name", authorLastName);
                        userJSON.put("year_published", yearPublished);
                        userJSON.put("library_key", libraryKey);

                        String body = String.valueOf(userJSON);

                        // construct the URL to fetch a user
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("http")
                                .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                                .appendPath("library")
                                .appendPath("api")
                                .appendPath("books")
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
                            Log.d(LOG_TAG, userObj.toString());
                        } else if (responseCode == 301) {
                            return false;
                        } else {
                            Log.e(LOG_TAG, "response Code = " + responseCode);
                        }
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
            showProgress(false);

            if(success) {
                // launch browse library activity so user can find a library to join
                Intent intent = new Intent(mParent, BrowseLibraryActivity.class);
                startActivity(intent);
            }
        }
    }
}
