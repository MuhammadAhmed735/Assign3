package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailAddress;
    private EditText password;
    private Button loginButton;
    private Button signupButton;
    private Button forgotPasswordButton;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailAddress = findViewById(R.id.email_editText);
        password     = findViewById(R.id.password_editText);
        loginButton  = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signUpButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.loginButton) //login Button clicked
        {
            loginUser();

        }
        else if (view.getId() == R.id.signUpButton) //signup button clicked
        {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
        else if (view.getId() == R.id.forgotPasswordButton) //forgot pass clicked
        {
            if(forgetPassword())
            {
                Toast.makeText(this,"Password Reset successfully",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loginUser() {
        String email = emailAddress.getText().toString().trim();
        String passWord = password.getText().toString().trim();

        if (email.isEmpty() || passWord.isEmpty()) {
            Toast.makeText(this, "Kindly enter email and password", Toast.LENGTH_SHORT).show();
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
        }
        mAuth.signInWithEmailAndPassword(email, passWord)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Email and Password. Please enter again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public Boolean forgetPassword()
    {
        return true;
    }
    public Boolean isValidEmail(String email)
    {
        //built in method for validating email format
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

}