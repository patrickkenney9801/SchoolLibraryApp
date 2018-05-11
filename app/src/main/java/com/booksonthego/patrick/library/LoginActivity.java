package com.booksonthego.patrick.library;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private TextView mEmailView;
    private TextView mPasswordView;
    private Button mLogin;
    private Button mCreateAccount;

    private View mLoginFormView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // delete savedData
        SharedPreferences savedData = this.getSharedPreferences(getString(R.string.saved_data_file_key),
                Context.MODE_PRIVATE);

        mEmailView = findViewById(R.id.text_email);
        mEmailView.setText(savedData.getString(getString(R.string.prompt_email), ""));
        mPasswordView = findViewById(R.id.text_password);
        mPasswordView.setText(savedData.getString(getString(R.string.prompt_password), ""));
        mLogin = findViewById(R.id.buttonLogin);
        mCreateAccount = findViewById(R.id.buttonSignUp);

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createAccount();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        SharedPreferences.Editor editor = savedData.edit();
        editor.clear();
        editor.commit();
    }

    private void createAccount() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid password, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email
        if (email.length() == 0) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        // Check that a password was provided
        if(password.length() == 0) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            /*
            Book.books[0] = new Book("The Count of Monte Cristo", "Alexandre", "Dumas", "1884");
            Book.books[1] = new Book("The Three Musketeers", "Alexandre", "Dumas", "July 1844");
            Book.books[2] = new Book("Twenty Years After", "Alexandre", "Dumas", "1845");
            Book.books[3] = new Book("War and Peace", "Leo", "Tolstoy", "1867");
            Book.books[4] = new Book("Anna Karenina", "Leo", "Tolstoy", "1877");
            Book.books[5] = new Book("The Brothers Karamazov", "Fyodor", "Dostoyevsky", "1880");
            Book.books[6] = new Book("Animal Farm", "George", "Orwell", "August 17, 1945");
            Book.books[7] = new Book("Resurrection", "Leo", "Tolstoy", "1899");
            Book.books[8] = new Book("The Idiot","Fyodor", "Dostoyevsky", "1869");
            Book.books[9] = new Book("The Death of Ivan Ilyich", "Leo", "Tolstoy", "1886");
            */
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, email, password);
            mAuthTask.execute();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        private final String LOG_TAG = UserLoginTask.class.getSimpleName();

        private final int SUCCESS = 0;
        private final int NO_INTERNET = 1;
        private final int BAD_PASSWORD = 2;
        private final int BAD_EMAIL = 3;
        private final int OTHER_FAILURE = 4;

        private final Activity mParent;
        private final String mEmail;
        private final String mPassword;
        private String firstName;
        private String lastName;
        private String lastLibraryKey;
        private String lastLibraryName;
        private String userKey;
        private String userRole;
        private String checkoutLimit;
        private String userBookCount;
        private String libraryMap;

        UserLoginTask(Activity parent, String email, String password) {
            mParent = parent;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
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
                return NO_INTERNET;
            } else {
                try {
                    // create credentials JSON
                    JSONObject credentialsJSON = new JSONObject();
                    credentialsJSON.put("server_password", getString(R.string.server_password));
                    credentialsJSON.put("email", mEmail);
                    credentialsJSON.put("password", mPassword);

                    String body = String.valueOf(credentialsJSON);

                    // construct the URL to fetch a user
                    Uri.Builder  builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("users")
                            .appendPath("login")
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

                    // obtain status code
                    responseCode = urlConnection.getResponseCode();
                    Log.d(LOG_TAG, "response code="+responseCode);
                    if(responseCode == 200) {
                        // read response to get user data from server
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String line;
                        String responseBody = "";
                        while((line = reader.readLine()) != null) {
                            responseBody += line + '\n';
                        }

                        // parse response as JSON
                        JSONObject user = new JSONObject(responseBody);
                        firstName = user.getString("first_name");
                        lastName = user.getString("last_name");
                        userKey = user.getString("user_key");
                        lastLibraryKey = user.getString("last_library_key");
                        if (lastLibraryKey != null && lastLibraryKey.length() == 36) {
                            userRole = user.getString("role");
                            lastLibraryName = user.getString("last_library_name");
                            checkoutLimit = user.getString("checkout_limit");
                            userBookCount = user.getString("user_book_count");
                            libraryMap = user.getString("library_map");
                        }

                        return SUCCESS;
                    } else if (responseCode == 312) {
                        return BAD_PASSWORD;
                    } else if (responseCode == 311) {
                        return BAD_EMAIL;
                    } else {
                        return OTHER_FAILURE;
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error getting response from server", e);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error parsing JSON", e);
                } finally {
                    // release system resources
                    if(urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if(reader != null) {
                        try {
                            reader.close();
                        } catch(IOException e) {
                            Log.e(LOG_TAG, "Error closing input stream", e);
                        }
                    }
                }
            }

            // if anything goes wrong, don't let them log in
            return OTHER_FAILURE;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;

            if (success == SUCCESS) {
                // store data for later use
                SharedPreferences savedData = mParent.getSharedPreferences(getString(R.string.saved_data_file_key),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedData.edit();
                editor.putString(getString(R.string.prompt_email), mEmail);
                editor.putString(getString(R.string.prompt_password), mPassword);
                editor.putString(getString(R.string.prompt_first_name), firstName);
                editor.putString(getString(R.string.prompt_last_name), lastName);
                editor.putString(getString(R.string.user_key), userKey);
                if (userRole != null) {
                    editor.putString(getString(R.string.last_library_key), lastLibraryKey);
                    editor.putString(getString(R.string.last_library_name), lastLibraryName);
                    editor.putString(getString(R.string.user_role), userRole);
                    editor.putString(getString(R.string.checkout_limit), checkoutLimit);
                    editor.putString(getString(R.string.user_book_count), userBookCount);
                    editor.putString(getString(R.string.map), libraryMap);
                }
                editor.apply();

                // clear text boxes so they're empty when user logs out
                mEmailView.setText("");
                mPasswordView.setText("");

                // launch main activity so user can begin browsing if there is a last library on the server
                if (userRole != null) {
                    Intent intent = new Intent(mParent, BrowseActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("BROWSE_TYPE", "1");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mParent, BrowseLibraryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                finish();
            } else if (success == BAD_PASSWORD){
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else if (success == BAD_EMAIL) {
                mEmailView.setError(getString(R.string.error_incorrect_email));
                mEmailView.requestFocus();
            } else if (success == OTHER_FAILURE) {
                Toast.makeText(mParent, "Strange things did happen in the LoginTask", Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
