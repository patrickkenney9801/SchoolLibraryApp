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
import android.view.*;
import android.widget.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CreateAccountActivity extends AppCompatActivity {

    private CreateAccountTask mTask;

    private EditText firstNameEnter;
    private EditText lastNameEnter;
    private EditText emailAddressEnter;
    private EditText passwordEnter;
    private EditText retypePasswordEnter;
    private Button signUpButton;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String reTypePassword;

    private View mCreateAccountForm;
    private View mProgressView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firstNameEnter = findViewById(R.id.text_first_name);
        lastNameEnter = findViewById(R.id.text_last_name);
        emailAddressEnter = findViewById(R.id.text_email);
        passwordEnter = findViewById(R.id.text_password);
        retypePasswordEnter = findViewById(R.id.createRetypePassword);
        signUpButton = findViewById(R.id.buttonSignUp);

        mCreateAccountForm = findViewById(R.id.create_account_form);
        mProgressView = findViewById(R.id.create_account_progress);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateAccount();
            }
        });
    }

    private void attemptCreateAccount() {
        // if the AsyncTask has already been created, then don't restart it

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

        // for reporting errors
        boolean cancel = false;
        View focusView = null;

        // fetch values from EditTexts
        firstName = firstNameEnter.getText().toString();
        lastName = lastNameEnter.getText().toString();
        email = emailAddressEnter.getText().toString();
        password = passwordEnter.getText().toString();
        reTypePassword = retypePasswordEnter.getText().toString();

        // first, validate the email
        if(!email.contains("@") || !email.contains(".")) {
            cancel = true;
            emailAddressEnter.setError("Invalid email");
            focusView = emailAddressEnter;
        }
        else if(email.length() < 5) {
            cancel = true;
            emailAddressEnter.setError("Invalid email");
            focusView = emailAddressEnter;
        }
        else if(firstName.length() == 0) {
            cancel = true;
            firstNameEnter.setError("No first name");
            focusView = firstNameEnter;
        }
        else if(lastName.length() == 0) {
            cancel = true;
            lastNameEnter.setError("No last name");
            focusView = lastNameEnter;
        }
        // verify that the password is long enough
        else if(password.length() < 5) {
            cancel = true;
            passwordEnter.setError("Password must be at least 5 characters");
            focusView = passwordEnter;
        }

        // next, check that the passwords match
        else if(!password.equals(reTypePassword)) {
            cancel = true;

            passwordEnter.setError(getString(R.string.error_password_match));
            focusView = passwordEnter;
        }

        if(cancel) {
            // cancel and inform user of any errors
            focusView.requestFocus();
        } else {
            showProgress(true);
            mTask = new CreateAccountTask(this, firstName, lastName, email, password);
            mTask.execute();
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

            mCreateAccountForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateAccountForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateAccountForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mCreateAccountForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

        private final String LOG_TAG = CreateAccountTask.class.getSimpleName();

        private Activity mParent;

        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String userKey;
        private String lastLibraryKey = null;

        CreateAccountTask(Activity parent, String fn, String ln, String email, String pw) {
            this.mParent = parent;

            this.firstName = fn;
            this.lastName = ln;
            this.email = email;
            this.password = pw;
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
                    // create user JSON
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("server_password", getString(R.string.server_password));
                    userJSON.put("first_name", firstName);
                    userJSON.put("last_name", lastName);
                    userJSON.put("email", email);
                    userJSON.put("password", password);

                    String body = String.valueOf(userJSON);

                    // construct the URL to fetch a user
                    Uri.Builder  builder = new Uri.Builder();
                    builder.scheme("http")
                            .encodedAuthority(getString(R.string.KENNEY_SERVER_IP))
                            .appendPath("library")
                            .appendPath("api")
                            .appendPath("users")
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
                    Log.d(LOG_TAG, "Response Code from Server: "+responseCode);

                    if(responseCode == 200) {
                        // read response to get user data from server
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String line = "";
                        String responseBody = "";
                        while((line = reader.readLine()) != null) {
                            responseBody += line + '\n';
                        }

                        JSONObject userObj = new JSONObject(responseBody);
                        Log.d(LOG_TAG, userObj.toString());
                        userKey = userObj.getString("user_key");
                        return true;
                    } else if(responseCode == 301) {
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
            showProgress(false);

            if(success) {
                // store data for later use
                SharedPreferences savedData = mParent.getSharedPreferences(getString(R.string.saved_data_file_key),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedData.edit();
                editor.putString(getString(R.string.prompt_email), email);
                editor.putString(getString(R.string.prompt_password), password);
                editor.putString(getString(R.string.prompt_first_name), firstName);
                editor.putString(getString(R.string.prompt_last_name), lastName);
                editor.putString(getString(R.string.user_key), userKey);
                editor.putString(getString(R.string.last_library_key), lastLibraryKey);
                editor.apply();

                // launch browse library activity so user can find a library to join
                Intent intent = new Intent(mParent, BrowseLibraryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                // alert user that they had a duplicate email
                emailAddressEnter.setError(getString(R.string.error_email_duplicate));
                emailAddressEnter.requestFocus();
            }
        }
    }
}
