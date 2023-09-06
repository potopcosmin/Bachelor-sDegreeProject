package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DataModel.User;
import com.example.myapplication.Retrofit.InterfaceAPI;
import com.example.myapplication.Retrofit.RetrofitClientInstance;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText emailView      ;
    EditText passwordView   ;
    EditText firstnameView  ;
    EditText lastNameView   ;
    EditText usernameView   ;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emailView= findViewById(R.id.Email);
        passwordView= findViewById(R.id.Password);
        firstnameView= findViewById(R.id.FirstName);
        lastNameView= findViewById(R.id.LastName);
        usernameView= findViewById(R.id.UserName);
        mAuth = FirebaseAuth.getInstance();

    }


    public void register(View view) throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecureRandom random = new SecureRandom();
        byte[] salt = usernameView.getText().toString().getBytes();

        KeySpec spec = new PBEKeySpec(String.valueOf(passwordView.getText()).toCharArray(), salt, 65536, 128);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = f.generateSecret(spec).getEncoded();
        Base64.Encoder enc = Base64.getEncoder();
        System.out.printf("salt: %s%n", enc.encodeToString(salt));
        System.out.printf("hash: %s%n", enc.encodeToString(hash));


        Retrofit retrofitinstance= RetrofitClientInstance.getRetrofitInstance();

        final InterfaceAPI api=retrofitinstance.create(InterfaceAPI.class);
        Switch driverSwitch=findViewById(R.id.switch1);
        String type;
        if(driverSwitch.isChecked()){
            type="driver";
        }
        else type="normal";


        Call<String>  call=api.registerUser(new User(String.valueOf(usernameView.getText()),
                String.valueOf(emailView.getText()),String.valueOf(firstnameView.getText()),
                String.valueOf(lastNameView.getText()),type));



        mAuth.createUserWithEmailAndPassword(String.valueOf(emailView.getText()), String.valueOf(passwordView.getText()))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(RegisterActivity.this, "Register succes.Please , Log in.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent login=new Intent(RegisterActivity.this,LoginActivity.class);
                            startActivity(login);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Register failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
      call.enqueue(new Callback<String>() {
          @Override
          public void onResponse(Call<String> call, Response<String> response) {
              if(response.isSuccessful()){
                  if(response.body().matches("User succesfully registered")){
                      System.out.println(response.body());
                      Toast.makeText(getApplicationContext(),"Register successfully ",Toast.LENGTH_LONG).show();
                      Intent login=new Intent(RegisterActivity.this,LoginActivity.class);
                      startActivity(login);
                  }
              }
              else {
                  Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
              }
          }
//
          @Override
          public void onFailure(Call<String> call, Throwable t) {
              System.out.println(t);
          }
      });



    }
}