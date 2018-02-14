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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BookDetailActivity extends AppCompatActivity {

    private BookActionTask mTask;

    private Button action;
    private Book book;
    private int bookID;
    private int bookDetailType;     // 1=reserve, 2=checkout, 3=return
    private String userKey;

    private View mProgressView;
    private View mBookDetailForm;

    private final String RESERVE = "Reserve";
    private final String UNRESERVE = "Unreserve";
    private final String RESERVED = "Reserved";
    private final String CHECKOUT = "Checkout";
    private final String RETURN = "Return";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);

        Intent startingIntent = getIntent();
        bookID = Integer.parseInt(startingIntent.getStringExtra("BOOK_ID"));
        book = Book.books.get(bookID);
        bookDetailType = Integer.parseInt(startingIntent.getStringExtra("BOOK_DETAIL_TYPE"));
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);
        userKey = savedData.getString(getString(R.string.user_key), null);

        toolbar.setTitle(book.name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(R.id.book_name)).setText(book.name);
        ((TextView) findViewById(R.id.author_name)).setText(book.authorFirstName + " " + book.authorLastName);
        ((TextView) findViewById(R.id.date_published)).setText(book.datePublished);

        action = findViewById(R.id.book_detail_action);
        switch (bookDetailType) {
            case 1:     if (book.userKey == null || book.userKey.length() != 36)
                            action.setText(RESERVE);
                        else if (book.userKey.equals(userKey))
                            action.setText(UNRESERVE);
                        else {
                            action.setEnabled(false);
                            action.setText(RESERVED);
                        }
                        break;
            case 2:     action.setText(CHECKOUT);
                        ((TextView) findViewById(R.id.date_taken)).setText(book.dateReserved.substring(0, 10));
                        break;
            case 3:     action.setText(RETURN);
                        ((TextView) findViewById(R.id.date_taken)).setText(book.dateCheckedOut.substring(0, 10));
                        break;
        }
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (action.getText().toString().equals(RESERVE)) {
                    dealWithBook(1);
                }
                else if (action.getText().toString().equals(UNRESERVE))
                    dealWithBook(2);
                else if (action.getText().toString().equals(CHECKOUT))
                    dealWithBook(3);
                else if (action.getText().toString().equals(RETURN))
                    dealWithBook(4);
            }
        });

        mProgressView = findViewById(R.id.book_detail_progress);
        mBookDetailForm = findViewById(R.id.book_detail_form);
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

    // actionType:      1=reserve,2=unreserve,3=checkout,4=return
    private void dealWithBook(int actionType) {
        if (actionType == 1) {
            SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                    Context.MODE_PRIVATE);
            int currentBookCount = Integer.parseInt(savedData.getString(getString(R.string.user_book_count), null));
            int maxBookCount = Integer.parseInt(savedData.getString(getString(R.string.checkout_limit), null));

            if (currentBookCount >= maxBookCount)
                return;
        }

        showProgress(true);
        // librarians do this so the incorrect user key would be given
        if (actionType == 3 || actionType == 4)
            mTask = new BookActionTask(this, book.bookKey, book.userKey, book.libraryKey, actionType);
        else
        // user does this so correct user is the current user key
            mTask = new BookActionTask(this, book.bookKey, userKey, book.libraryKey, actionType);
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

            mBookDetailForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mBookDetailForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBookDetailForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mBookDetailForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class BookActionTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = BrowseActivity.FetchBooksTask.class.getSimpleName();

        private Activity mParent;
        private String mBookKey;
        private String mUserKey;
        private String mLibraryKey;
        private int actionType;

        BookActionTask(Activity parent, String bKey, String uKey, String lKey, int aType) {
            this.mParent = parent;
            this.mBookKey = bKey;
            this.mUserKey = uKey;
            this.mLibraryKey = lKey;
            this.actionType = aType;
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
                    serverJSON.put("user_key", mUserKey);
                    serverJSON.put("book_key", mBookKey);

                    String body = String.valueOf(serverJSON);

                    // construct the URL to fetch a user
                    Uri.Builder  builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("books");
                    if (actionType == 1)
                        builder.appendPath("reservebook");
                    else if (actionType == 2)
                        builder.appendPath("unreservebook");
                    else if (actionType == 3)
                        builder.appendPath("checkoutbook");
                    else
                        builder.appendPath("returnbook");
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
            mTask = null;

            // update book accessor's client side statistics
            SharedPreferences savedData = mParent.getSharedPreferences(getString(R.string.saved_data_file_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = savedData.edit();
            int currentBookCount = Integer.parseInt(savedData.getString(getString(R.string.user_book_count), null));
            if (actionType == 1) {
                editor.putString(getString(R.string.user_book_count), "" + (currentBookCount + 1));
            } else if (actionType == 2 || actionType == 4) {
                editor.putString(getString(R.string.user_book_count), "" + (currentBookCount - 1));
            }
            editor.apply();

            showProgress(false);
            Intent intent = new Intent(mParent, BrowseActivity.class);
            intent.putExtra("BROWSE_TYPE", "" + bookDetailType);
            startActivity(intent);
        }
    }
}
