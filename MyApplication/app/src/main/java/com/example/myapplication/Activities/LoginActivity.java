package com.example.myapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DataModel.User;
import com.example.myapplication.Retrofit.InterfaceAPI;
import com.example.myapplication.Retrofit.RetrofitClientInstance;
import com.example.myapplication.Session.SessionManagement;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    private FirebaseAuth mAuth;
    User user=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mAuth = FirebaseAuth.getInstance();
        email= findViewById(R.id.Email);
        password=findViewById(R.id.Password);

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            moveToHome();
        }
    }

    public void Login(View view) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String userName= String.valueOf(email.getText());
        String pass=String.valueOf(password.getText());

        byte[] salt = userName.getBytes();

        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = f.generateSecret(spec).getEncoded();
        Base64.Encoder enc = Base64.getEncoder();


        Retrofit retrofitinstance= RetrofitClientInstance.getRetrofitInstance();
        SessionManagement sessionManagement=new SessionManagement(LoginActivity.this);


        final InterfaceAPI api=retrofitinstance.create(InterfaceAPI.class);
        Map<String,String> creditentials=new HashMap<>();
        creditentials.put(userName,null);

        Call<User> call=api.loginUser(creditentials);
        mAuth.signInWithEmailAndPassword(userName, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            moveToHome();
                        } else {

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });


 call.enqueue(new Callback<User>() {
     @Override
     public void onResponse(Call<User> call, Response<User> response) {
         if(response.isSuccessful()){
//
             user= response.body();
             email.getText().clear();
             password.getText().clear();
             Switch driverSwitch=findViewById(R.id.switch1);
             if(driverSwitch.isChecked() ) {
                 moveToDrive();
             }
//
                 else {
                 moveToHome();
                 }

         }
     }

     @Override
     public void onFailure(Call<User> call, Throwable t) {
         System.out.println(t);
     }
       });



    }

    public void moveToRegister(View view ){
    Intent register=new Intent(LoginActivity.this,RegisterActivity.class);
    startActivity(register);
    }

    public void moveToHome(){
        Intent home=new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(home);
    }

    public void moveToDrive(){
        Intent drive=new Intent(LoginActivity.this,DriverMapsActivity.class);
        startActivity(drive);
    }

    @Override
    public void onBackPressed(){
    }
}
