package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView profileImage;
    private EditText emailAddress;
    private EditText password;
    private EditText name;
    private EditText username;
    private Button loginButton;
    private Button signupButton;
    private ImageButton addImageButton;
    private Uri selectedImageUri;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImage = findViewById(R.id.profile_image);
        emailAddress = findViewById(R.id.email_editText);
        password = findViewById(R.id.password_editText);
        name = findViewById(R.id.name_editText);
        username = findViewById(R.id.username_editText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signUpButton);
        addImageButton = findViewById(R.id.addImageButton);

        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
        addImageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signUpButton) {
            registerUser();
        } else if (view.getId() == R.id.loginButton) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.addImageButton) {
            pickImageFromGallery();
        }
    }

    public void registerUser() {
        String email = emailAddress.getText().toString().trim();
        String passWord = password.getText().toString().trim();
        String Name = name.getText().toString().trim();
        String userName = username.getText().toString().trim();

        if (email.isEmpty() || passWord.isEmpty() || Name.isEmpty() || userName.isEmpty()) {
            Toast.makeText(this, "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, passWord)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // User is signed in, create a user object and store in Firestore
                                User newUser = new User(user.getUid(), email, userName, Name);
                                createUserDocument(newUser);
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createUserDocument(User user) {
        // Add a new document with a generated ID in 'users' collection
        db.collection("users")
                .document(user.getUserId())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            // Proceed to login or other activity
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed to create user document in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public Boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(data.getData())
                    .transform(new CircleCrop())
                    .into(profileImage);
        }
    }
}
