package com.example.nika.androidchatapp.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import at.favre.lib.crypto.bcrypt.BCrypt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.example.nika.androidchatapp.databinding.ActivityChangePasswordBinding;
import com.example.nika.androidchatapp.databinding.ActivitySignUpBinding;
import com.example.nika.androidchatapp.databinding.ActivitySigninBinding;
import com.example.nika.androidchatapp.utilities.Constants;
import com.example.nika.androidchatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.util.Date;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivityChangePasswordBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }


    private void showToast (String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private  void setListeners(){
        binding.buttonSignin.setOnClickListener(v ->
        {
            if(binding.inputEmail.getText().toString().trim().isEmpty()){
                showToast("Enter email");
            }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
                showToast("Enter valid your email");
            }else if (!(isValidSignUpDetails())) {
                showToast("Enter strong password");
            }else{
                resetPassword();
            }


        });}

    private  void resetPassword() {
        database = FirebaseFirestore.getInstance();
        preferenceManager =new PreferenceManager(getApplicationContext());


       // showToast(database.);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(binding.inputEmail.getText().toString().trim(), binding.oldinputPassword.getText().toString().trim());

// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {



                                user.updatePassword(binding.newinputPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            database.collection(Constants.Key_COLLECTION_USERS)
                                                    .whereEqualTo(Constants.Key_EMAIL,binding.inputEmail.getText().toString().trim())
                                                    .get()
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()&& task2.getResult()!=null&&
                                                                task2.getResult().getDocuments().size()>0){
                                                            DocumentSnapshot documentSnapshot=task2.getResult().getDocuments().get(0);
                                                            //showToast(sha256(binding.newinputPassword.toString().trim()));
                                                            ;
                                                            BCrypt.Result result = BCrypt.verifyer().verify(binding.newinputPassword.getText().toString().trim().toCharArray(),documentSnapshot.getString(Constants.Key_PASSWORD));
                                                            BCrypt.Result result1 = BCrypt.verifyer().verify(binding.newinputPassword.getText().toString().trim().toCharArray(),documentSnapshot.getString(Constants.Key_PASSWORD1));
                                                            BCrypt.Result result2 = BCrypt.verifyer().verify(binding.newinputPassword.getText().toString().trim().toCharArray(),documentSnapshot.getString(Constants.Key_PASSWORD2));

                                                            if(result.verified == true || result1.verified == true || result2.verified == true){
                                                                showToast("You most chose a password that not used");
                                                            }else{
                                                                database.collection(Constants.Key_COLLECTION_USERS)
                                                                        .whereEqualTo(Constants.Key_EMAIL,binding.inputEmail.getText().toString().trim())
                                                                        .get()
                                                                        .addOnCompleteListener(task1 -> {
                                                                            if (task1.isSuccessful()&& task1.getResult()!=null&&
                                                                                    task1.getResult().getDocuments().size()>0){
                                                                                DocumentSnapshot documentSnapshot2=task1.getResult().getDocuments().get(0);
                                                                                //showToast(sha256(binding.newinputPassword.toString().trim()));
                                                                                ;

                                                                                String bcryptHashString = BCrypt.withDefaults().hashToString(12, binding.newinputPassword.getText().toString().trim().toCharArray());
                                                                                String bcryptHashString2 = BCrypt.withDefaults().hashToString(12, binding.oldinputPassword.getText().toString().trim().toCharArray());

                                                                                DocumentReference documentReference =
                                                                                        database.collection(Constants.Key_COLLECTION_USERS).document(preferenceManager.getString(Constants.Key_USER_ID));
                                                                                documentReference.update(
                                                                                        Constants.Key_PASSWORD2,documentSnapshot2.getString(Constants.Key_PASSWORD1),
                                                                                        Constants.Key_PASSWORD,bcryptHashString,
                                                                                        Constants.KEY_PTIMESTAMP,new Date(),
                                                                                        Constants.Key_PASSWORD1, bcryptHashString2).addOnFailureListener(e->{
                                                                                    showToast(e.toString());
                                                                                });
                                                                            }else{
                                                                                showToast("Unable to change the DB");
                                                                            }
                                                                        });

                                                                showToast("Password updated");
                                                                startActivity(new Intent(getApplicationContext(),MainActivity.class));

                                                            }

                                                        }else{
                                                            showToast("Unable to sign in");
                                                        }
                                                    });
                                        } else {
                                            showToast("Error password not updated");
                                        }
                                    }
                                });
                        } else {
                            showToast("Error auth failed");
                        }
                    }
                });

    }
        private Boolean isValidSignUpDetails(){
            if (binding.newinputPassword.getText().toString().trim().isEmpty()){
                showToast("Enter password");
                return false;
            }
            else if (binding.newinputPassword.getText().toString().trim().isEmpty()){
                showToast("confirm your password");
                return false;
            }

            else if (binding.newinputPassword.getText().toString().trim().length() <8){
                showToast("Week password to short, password must be 8 digits min");
                return false;
            }
            else if (!(PasswordStrength(binding.newinputPassword.getText().toString().trim()))){
                showToast("Week password");
                return false;
            }
            else {
                return true;
            }
        }


    private Boolean PasswordStrength(String password){
        int  upChars=0, lowChars=0;
        int special=0, digits=0;
        for(int i=0; i<password.length(); i++)
        {
            char ch = password.charAt(i);
            if(Character.isUpperCase(ch))
                upChars++;
            else if(Character.isLowerCase(ch))
                lowChars++;
            else if(Character.isDigit(ch))
                digits++;
            else
            {
                if(ch=='<' || ch=='>')
                {
                    //System.out.println("\nThe Password is Malicious!");
                    return false;
                }
                else
                    special++;
            }}
        if(upChars==0){
            Toast.makeText(getApplicationContext(),"\nThe Password must contain at least one uppercase character.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(lowChars==0){
            Toast.makeText(getApplicationContext(),"\nThe Password must contain at least one lowercase character.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(digits==0){
            Toast.makeText(getApplicationContext(),"\nThe Password must contain at least one digit.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(special==0){
            Toast.makeText(getApplicationContext(),"\nThe Password must contain at least one special character.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(upChars!=0 && lowChars!=0 && digits!=0 && special!=0) {
            return true;
        } else{
            return false;
        }
    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
