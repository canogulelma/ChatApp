package com.example.chatappone.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatappone.databinding.ActivityMainBinding;
import com.example.chatappone.utilities.Constanst;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SharedPreferences preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }
    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constanst.KEY_NAME, ""));
        byte [] bytes = Base64.decode(preferenceManager.getString(Constanst.KEY_IMAGE, ""), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void showToasts(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);

    }
    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constanst.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constanst.KEY_USER_ID, ""));
        documentReference.update(Constanst.KEY_FCM_TOKEN, token)

                .addOnFailureListener(e -> showToasts("Unable to send token: "));

    }
    private void signOut() {
        showToasts("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constanst.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constanst.KEY_USER_ID, "")
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constanst.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(this::onSuccess)
                .addOnFailureListener(e -> showToasts("Unable to sign out"));


    }

    private void onSuccess(Void unused) {
        preferenceManager.edit().clear().apply();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }
}