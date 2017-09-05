package com.feedhenry.securenativeandroidtemplate;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.view.inputmethod.EditorInfo;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.text.TextUtils;
import android.os.AsyncTask;

import com.feedhenry.securenativeandroidtemplate.authenticate.AuthenticateProvider;
import com.feedhenry.securenativeandroidtemplate.authenticate.AuthenticateProviderImpl;
import com.feedhenry.securenativeandroidtemplate.authenticate.AuthenticateResult;

/**
 * A login screen that offers login via username/password.
 */
public class AuthenticationFragment extends Fragment {

    View view;

    private AuthenticateProvider mAuthProvider = new AuthenticateProviderImpl();
    // Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private AuthenticateSuccessCallback mAuthSuccessCallback;

    public AuthenticationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment - Set the view as the authenticate fragment
        view = inflater.inflate(R.layout.fragment_authentication, container, false);

        // Reference the username and password fields from the UI
        mUsernameView = (AutoCompleteTextView) view.findViewById(R.id.username);
        mPasswordView = (EditText) view.findViewById(R.id.password);

        // Add a listener for the keyboard input button so that the enter key will submit the form
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Reference the login button
        Button mSignInButton = (Button) view.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.login_progress);

        return view;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check if the password field is empty
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check if the username field is empty
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Allow override the default auth provider. Mainly used for tests.
     * @param authProvier
     */
    public void setAuthProvider(AuthenticateProvider authProvier) {
        mAuthProvider = authProvier;
    }


    /**
     * Set a callback that will be invoked when the authentication is completely successfully
     * @param callback
     */
    public void setAuthSuccessCallback(AuthenticateSuccessCallback callback) {
        mAuthSuccessCallback = callback;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, AuthenticateResult> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        /**
         * Placeholder for performing authentication in the background
         */
        @Override
        protected AuthenticateResult doInBackground(Void... params) {
            return mAuthProvider.authenticateWithUsernameAndPassword(mUsername, mPassword);
        }

        /**
         * Method to handle the UI after authentication has completed
         */
        @Override
        protected void onPostExecute(final AuthenticateResult result) {
            mAuthTask = null;
            showProgress(false);

            if (result != null) {
                if (mAuthSuccessCallback != null) {
                    mAuthSuccessCallback.authenticated(result);
                }
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_credentials));
                mPasswordView.requestFocus();
            }
        }
    }

    /**
     * To allow the caller define what actions need to be performed when the authentication is completed
     */
    public interface AuthenticateSuccessCallback {
        public void authenticated(AuthenticateResult result);
    }

}