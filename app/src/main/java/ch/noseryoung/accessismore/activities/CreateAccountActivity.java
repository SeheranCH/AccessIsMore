package ch.noseryoung.accessismore.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.noseryoung.accessismore.R;
import ch.noseryoung.accessismore.domainModell.User;
import ch.noseryoung.accessismore.exception.UserAlreadyExistingException;
import ch.noseryoung.accessismore.messages.ToastHandler;
import ch.noseryoung.accessismore.persistence.AppDatabase;
import ch.noseryoung.accessismore.persistence.UserDAO;
import ch.noseryoung.accessismore.security.PasswordEncoder;
import ch.noseryoung.accessismore.validation.InputValidation;

public class CreateAccountActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private UserDAO mUserDAO;

    private static final String TAG = "CreateAccountActivity";

    private InputValidation inputValidation = new InputValidation();

    private PasswordEncoder passwordEncoder = new PasswordEncoder();

    private String currentImagePath;

    private String errorIsNotEmpty;
    private String errorHasMaxLength50;
    private String errorHasMaxLength100;
    private String errorOnlyLetters;
    private String errorEmail;
    private String errorMatchPasswords;

    private Button getAPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        errorIsNotEmpty = getString(R.string.error_is_not_empty);
        errorHasMaxLength50 = getString(R.string.error_hasMaxLength50);
        errorHasMaxLength100 = getString(R.string.error_hasMaxLength100);
        errorOnlyLetters = getString(R.string.error_onlyLetters);
        errorEmail = getString(R.string.error_email);
        errorMatchPasswords = getString(R.string.error_match_passwords);

        Button saveNewAccount = findViewById(R.id.createNewAccountSecondButton);
        // OnClickHandler for button 'Neues Konto erstellen'
        saveNewAccount.setOnClickListener(mSaveNewAccount);

        Button goToSignIn = findViewById(R.id.signInButton2);
        // OnClickHandler for button 'Anmelden'
        goToSignIn.setOnClickListener(mGoToSignInActivity);

        getAPicture = findViewById(R.id.roundedButton);
        // OnClickHandler for button 'Ein Foto aufnehmen'
        getAPicture.setOnClickListener(mGetAPicture);

        // DB connection
        mUserDAO = AppDatabase.getAppDb(getApplicationContext()).getUserDAO();

    }

    private View.OnClickListener mSaveNewAccount = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ToastHandler toastHandler = new ToastHandler(getApplicationContext());

            EditText mEditTextFirstName = findViewById(R.id.editFirstnameFieldText);
            EditText mEditTextLastName = findViewById(R.id.editLastnameFieldText);
            EditText mEditTextEmail = findViewById(R.id.editEmailFieldText);
            EditText mEditTextPassword1 = findViewById(R.id.editPasswordFieldText);
            EditText mEditTextPassword2 = findViewById(R.id.editConfirmPasswordFieldText);

            String firstName = mEditTextFirstName.getText().toString().trim();
            String lastName = mEditTextLastName.getText().toString().trim();
            String email = mEditTextEmail.getText().toString().trim();
            String password1 = mEditTextPassword1.getText().toString();
            String password2 = mEditTextPassword2.getText().toString();

            Resources res = getResources();
            String[] allTypes = res.getStringArray(R.array.all_types);
            String[] allValues = new String[]{firstName, lastName, email, password1, password2};
            List<Boolean> maySubmitting = new ArrayList<>();

            // Validation
            for (int i = 0; i < allTypes.length; i++) {
                if (inputValidation.isNotEmpty(allValues[i])) {
                    // All names
                    if (allTypes[i].endsWith(getString(R.string.suffix_name))) {
                        if (inputValidation.onlyLetters(allValues[i])) {
                            if (inputValidation.hasMaxLength50(allValues[i])) {
                                maySubmitting.add(true);
                            } else {
                                maySubmitting.add(false);
                                sendErrorMessage(allTypes[i], errorHasMaxLength50);
                            }
                        } else {
                            maySubmitting.add(false);
                            sendErrorMessage(allTypes[i], errorOnlyLetters);
                        }
                    }
                    // Email
                    if (allTypes[i].equals(getString(R.string.email))) {
                        if (inputValidation.isValidEmail(allValues[i])) {
                            if (inputValidation.hasMaxLength100(allValues[i])) {
                                maySubmitting.add(true);
                            } else {
                                maySubmitting.add(false);
                                sendErrorMessage(allTypes[i], errorHasMaxLength100);
                            }
                        } else {
                            maySubmitting.add(false);
                            sendErrorMessage(allTypes[i], errorEmail);
                        }
                    }
                    // Password
                    if (allTypes[i].equals(getString(R.string.password))) {
                        if (inputValidation.matchPasswords(allValues[i], allValues[i + 1])) {
                            maySubmitting.add(true);
                        } else {
                            maySubmitting.add(false);
                            sendErrorMessage(allTypes[i], errorMatchPasswords);
                        }
                    }
                } else {
                    maySubmitting.add(false);
                    sendErrorMessage(allTypes[i], errorIsNotEmpty);
                }
            }

            // Check if validation is ok
            if (!maySubmitting.contains(false)) {

                //Check if user is already existing
                User userToCheck = mUserDAO.getSingleUser(email);
                if (userToCheck == null) {
                    User user = new User(firstName, lastName, email, password1);
                    // Set picture if available
                    if (currentImagePath != null) {
                        user.setPathPicture(currentImagePath);
                    }
                    // Encrypt password and set it
                    user.setPassword(passwordEncoder.encryptPassword(user.getPassword()));
                    Log.d(TAG, "Generated encrypted password: " + user.getPassword());

                    // Save new account
                    mUserDAO.insertUser(user);

                    // Message for saving successfully
                    toastHandler.callToast(getString(R.string.toast_signed_up_success), 1);

                    // Go back to MainActivity
                    openMainActivity();

                } else {
                    try {
                        toastHandler.callToast(getString(R.string.toast_user_already_existing), 0);
                        Log.e(TAG, "User is already existing");
                        throw new UserAlreadyExistingException("User is already existing");
                    } catch (UserAlreadyExistingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private View.OnClickListener mGetAPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            caputreImage();
        }
    };

    private View.OnClickListener mGoToSignInActivity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openMainActivity();
        }
    };

    public void sendErrorMessage(String field, String errorType) {
        ToastHandler toastHandler = new ToastHandler(getApplicationContext());

        if (errorType.equals(errorIsNotEmpty)) {
            toastHandler.callToast(getString(R.string.toast_error_is_not_empty_1) + field + getString(R.string.toast_error_is_not_empty_2), 0);
        } else if (errorType.equals(errorOnlyLetters)) {
            toastHandler.callToast(getString(R.string.toast_error_only_letters) + field, 0);
        } else if (errorType.equals(errorEmail)) {
            toastHandler.callToast(getString(R.string.toast_error_email), 0);
        } else if (errorType.equals(errorHasMaxLength50)) {
            toastHandler.callToast(field + getString(R.string.toast_error_has_max_length_50), 0);
        } else if (errorType.equals(errorHasMaxLength100)) {
            toastHandler.callToast(field + getString(R.string.toast_error_has_max_length_100), 0);
        } else if (errorType.equals(errorMatchPasswords)) {
            toastHandler.callToast(getString(R.string.toast_error_match_passwords), 0);
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        Log.d(TAG, "Open new activity 'MainActivity'");
        startActivity(intent);
    }

    private void caputreImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null) {
                // Add picture into intent
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String dateStamp = new SimpleDateFormat("dd.MM.yy_HH:mm").format(new Date());
        String imageFileName = "PersonalImage_" + dateStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentImagePath = image.getAbsolutePath();
        Log.d(TAG, "Show path of image: " + currentImagePath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            ImageView profilePicture = findViewById(R.id.imgAvatarView);
            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
            profilePicture.setImageBitmap(bitmap);
            // Make button transparent
            getAPicture.getBackground().setAlpha(0);
        }
    }
}