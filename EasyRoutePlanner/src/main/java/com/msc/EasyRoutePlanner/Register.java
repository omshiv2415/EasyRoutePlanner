package com.msc.EasyRoutePlanner;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends ActionBarActivity {

    //Declaration of Components such as EditText,Buttons.....
    protected EditText CreateUserName;
    protected EditText Email;
    protected EditText Password;
    protected EditText retypePassword;
    protected Button RegisterButton;
    private TextToSpeech speech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialising Component to the Declared Variables above
        CreateUserName = (EditText) findViewById(R.id.editTextUserName);
        Email = (EditText) findViewById(R.id.editTextEmail);
        Password = (EditText) findViewById(R.id.editTextPassword);
        retypePassword = (EditText) findViewById(R.id.editTextRetypePassword);
        RegisterButton = (Button) findViewById(R.id.buttonRegister);

        //setting up register button to click when user click this button it
        // will take all the information from the editText and send it to the
        // parse cloud database.
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String mUserName = CreateUserName.getText().toString().trim();
                String mEmail = Email.getText().toString().trim();
                String mPassword = Password.getText().toString().trim();
                String mRetypPassowrd = retypePassword.getText().toString().trim();

                ParseUser user = new ParseUser();

                if (mUserName.equals("") || mEmail.equals("")
                        || mPassword.equals("") || mRetypPassowrd.equals("")) {

                    speech = new TextToSpeech(Register.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                speech.setLanguage(Locale.UK);
                                String toSpeak = ("Please provide all the Details");
                                speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                Toast.makeText(Register.this, toSpeak,
                                        Toast.LENGTH_SHORT).show();


                            }
                        }
                    });

                } else if (!isValidEmail(mEmail)) {
                    speech = new TextToSpeech(Register.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                speech.setLanguage(Locale.UK);
                                String toSpeak = ("Please provide correct Email");
                                speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                Toast.makeText(Register.this, toSpeak,
                                        Toast.LENGTH_SHORT).show();


                            }
                        }
                    });

                } else if (!isValidPassword(mPassword)) {

                    speech = new TextToSpeech(Register.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                speech.setLanguage(Locale.UK);
                                String toSpeak = ("Password must be between 7 and 21");
                                speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                Toast.makeText(Register.this, toSpeak,
                                        Toast.LENGTH_SHORT).show();


                            }
                        }
                    });


                } else if (!mPassword.equals(mRetypPassowrd)) {

                    speech = new TextToSpeech(Register.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                speech.setLanguage(Locale.UK);
                                String toSpeak = ("Re-type Password must be match with Password");
                                speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                Toast.makeText(Register.this, toSpeak,
                                        Toast.LENGTH_SHORT).show();


                            }
                        }
                    });


                } else {

                   // storing user registration data to parse cloud database
                    user.setPassword(mPassword);
                    user.setUsername(mUserName);
                    user.setEmail(mEmail);


                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // user signed up successfully
                                Toast.makeText(Register.this, "Success ! Welcome", Toast.LENGTH_LONG).show();
                                // take user homepage
                                Intent takeUserHome = new Intent(Register.this, Login.class);
                                startActivity(takeUserHome);

                            } else {
                                //there was an error signing up user.
                                Toast.makeText(Register.this, "Please try again", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }
            }
        });
    }


    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        if (pass != null && pass.length() >= 7 && pass.length() <= 21) {
            return true;
        }
        return false;
    }

}

