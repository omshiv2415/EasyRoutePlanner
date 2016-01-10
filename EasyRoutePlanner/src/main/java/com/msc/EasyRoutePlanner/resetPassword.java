package com.msc.EasyRoutePlanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class resetPassword extends Activity {
    //Declaration of Components such as EditText,Buttons.....
    private EditText EmailAddress;
    private Button Reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);


        // Initialising Component to the Declared Variables above
        EmailAddress = (EditText) findViewById(R.id.editTextResetPassword);
        Reset = (Button) findViewById(R.id.btnResetPassword);
        // Setting up rest button when user press this button ParseUser object will find the
        // register user in background and if the object find the register email address it will
        // send the reset information to the user's email address and if not then it will send
        // notification through Toast message "please provide correct Email"
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailAddress.getText().toString();

                ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // An email was successfully sent with reset instructions.
                            Toast.makeText(resetPassword.this, "Please check your email for reset password", Toast.LENGTH_LONG).show();
                        } else {
                            // Something went wrong. Look at the ParseException to see what's up.
                            Toast.makeText(resetPassword.this, "Please provide correct Email", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }


}

