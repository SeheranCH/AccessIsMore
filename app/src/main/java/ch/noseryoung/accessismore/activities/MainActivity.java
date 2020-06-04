package ch.noseryoung.accessismore.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.noseryoung.accessismore.R;
import ch.noseryoung.accessismore.domainModell.User;
import ch.noseryoung.accessismore.exception.InvalidDataException;
import ch.noseryoung.accessismore.persistence.AppDatabase;
import ch.noseryoung.accessismore.persistence.UserDAO;
import ch.noseryoung.accessismore.security.PasswordEncoder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private UserDAO mUserDAO;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mButtonToGetSignedIn = findViewById(R.id.signInButton);
        mButtonToGetSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextEmail = findViewById(R.id.emailFieldText);
                mEditTextPassword = findViewById(R.id.passwordFieldText);
                String email = mEditTextEmail.getText().toString();
                String password = mEditTextPassword.getText().toString();
                try {
                    password = PasswordEncoder.encrypt(password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkSignInData(email, password);
            }
        });
        Button mButtonToCreateAccount = findViewById(R.id.createNewAccountButton);
        mButtonToCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateAccountActivity();
            }
        });

        mUserDAO = AppDatabase.getAppDb(getApplicationContext()).getUserDAO();

       // ToastHandler toastHandler = new ToastHandler(getApplicationContext());
       // toastHandler.callToast("hello", 1);
    }


    private void checkSignInData(String email, String password) {
        User user = mUserDAO.checkSignInData(email, password);
        Log.d(TAG, "\n" + email + "\n" + password);
        if (user!=null) {
            Log.d(TAG, "You have been signed in successfully");
            Toast.makeText(this, getString(R.string.toast_signed_in_success), Toast.LENGTH_LONG).show();
            openWelcomeScreenActivity();
        } else {
            Log.e(TAG, "Sign in data are false");
            Toast.makeText(this, getString(R.string.toast_signed_in_failure), Toast.LENGTH_LONG).show();
            throw new InvalidDataException("Sign in data are false");
        }
    }

    private void openCreateAccountActivity() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        Log.d(TAG, "Open new activity 'CreateAccount'");
        startActivity(intent);
    };

    private void openWelcomeScreenActivity() {
        // TO DO input user data
        Intent intent = new Intent(this, WelcomeScreenActivity.class);
        Log.d(TAG, "Open new activity 'WelcomeScreen'");
        startActivity(intent);
    }
}
