package ch.noseryoung.accessismore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ch.noseryoung.accessismore.domainModell.User;
import ch.noseryoung.accessismore.persistence.AppDatabase;
import ch.noseryoung.accessismore.persistence.UserDAO;

public class CreateAccountActivity extends AppCompatActivity {

    private UserDAO mUserDAO;

    private static final String TAG = "CreateAccountActivity";

    private EditText mEditTextFirstName;
    private EditText mEditTextLastName;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword1;
    private EditText mEditTextPassword2;


    private View.OnClickListener mSaveNewAccount = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditTextFirstName = findViewById(R.id.editFirstnameFieldText);
            mEditTextLastName = findViewById(R.id.editLastnameFieldText);
            mEditTextEmail = findViewById(R.id.editEmailFieldText);
            mEditTextPassword1 = findViewById(R.id.editPasswordFieldText);
            mEditTextPassword2 = findViewById(R.id.editConfirmPasswordFieldText);

            String firstName = mEditTextFirstName.getText().toString();
            String lastName = mEditTextLastName.getText().toString();
            String email = mEditTextEmail.getText().toString();
            String password1 = mEditTextPassword1.getText().toString();
            String password2 = mEditTextPassword2.getText().toString();

            // Validation

            Log.d(TAG, "\n" + firstName + "\n" + lastName + "\n" + email + "\n" + password1 + "\n" + password2);

            //Delete all existing users
            List<User> users = mUserDAO.getAllUsers();
            mUserDAO.deleteUsers(users);

            // Save new account
            User user = new User(firstName, lastName, email, password1);
            mUserDAO.insertUser(user);

            //Message for saving successfully
            Toast toast = Toast.makeText(getApplicationContext(), "Sie haben erfolgreich Ihren Account erstellt", Toast.LENGTH_LONG);
            toast.show();
        }
    };

    private View.OnClickListener mGoToSignInActivity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openMainActivity();
        }
    };

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Log.d(TAG, "open new activity 'MainActivity'");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Button saveNewAccount = findViewById(R.id.createNewAccountSecondButton);
        saveNewAccount.setOnClickListener(mSaveNewAccount);

        Button goToSignIn = findViewById(R.id.signInButton2);
        goToSignIn.setOnClickListener(mGoToSignInActivity);

        mUserDAO = AppDatabase.getAppDb(getApplicationContext()).getUserDAO();

    }
}
