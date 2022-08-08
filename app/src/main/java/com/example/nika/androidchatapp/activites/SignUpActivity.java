package com.example.nika.androidchatapp.activites;
import com.example.nika.androidchatapp.databinding.ActivitySignUpBinding;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import at.favre.lib.crypto.bcrypt.BCrypt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.databinding.ActivitySignUpBinding;
import com.example.nika.androidchatapp.utilities.Constants;
import com.example.nika.androidchatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());

        setlistener();
    }

    private void setlistener(){
        binding.textSignIn.setOnClickListener(v->onBackPressed() );
        binding.buttonSignUp.setOnClickListener(v->{
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v->{
            Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
    private void signUp(){
        loading(true);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constants.Key_NAME,binding.inputName.getText().toString());
        user.put(Constants.Key_EMAIL,binding.inputEmail.getText().toString());
        String bcryptHashString = BCrypt.withDefaults().hashToString(12, binding.inputPassword.getText().toString().trim().toCharArray());
        user.put(Constants.Key_PASSWORD,bcryptHashString);
        user.put(Constants.Key_PASSWORD1,"null");
        user.put(Constants.Key_PASSWORD2,"null");
        user.put(Constants.KEY_PTIMESTAMP, getReadableDateTime(new Date()));

        user.put(Constants.Key_IMAGE,encodedImage);
        firebaseAuth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(),binding.inputPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,"reg ok!",Toast.LENGTH_LONG).show();
                            database.collection(Constants.Key_COLLECTION_USERS)
                                    .add(user)
                                    .addOnSuccessListener(documentReference ->{
                                        loading(false);
                                        preferenceManager.putBoolean(Constants.Key_IS_SIGN_IN,true);
                                        preferenceManager.putString(Constants.Key_USER_ID,documentReference.getId());
                                        preferenceManager.putString(Constants.Key_NAME,binding.inputName.getText().toString());
                                        preferenceManager.putString(Constants.Key_IMAGE, encodedImage);
                                        preferenceManager.putString(Constants.KEY_PTIMESTAMP,  getReadableDateTime(new Date()));
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    } )
                                    .addOnFailureListener(exception->{
                                        loading(false);
                                        showToast(exception.getMessage());
                                    });
                        } else {
                            task.addOnFailureListener(exception-> {
                                loading(false);
                                showToast(exception.getMessage());
                            });
                            Toast.makeText(SignUpActivity.this,"reg faild!",Toast.LENGTH_LONG).show();
                        }
                    }
                });



    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte [] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()==RESULT_OK){
                    if (result.getData()!=null){
                        Uri imageUri=result.getData().getData();
                        try {
                            InputStream inputStream= getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage=encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(encodedImage==null){
            showToast("Select proflie image");
            return false;
        }else if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }
        else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid Email");
            return false;
        }
        else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("confirm your password");
            return false;
        }
        else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Password and confirm must be same");
            return false;}
        else if (binding.inputPassword.getText().toString().trim().length() <8){
            showToast("Week password to short, password must be 8 digits min");
            return false;
        }
        else if (!(PasswordStrength(binding.inputPassword.getText().toString().trim()))){
            showToast("Week password");
            return false;
        }
        else {
            return true;
        }

    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
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



//    private String generateHashedPass(String pass) {
//        // hash a plaintext password using the typical log rounds (10)
//        return BCrypt.hashpw(pass, BCrypt.gensalt());
//    }
//
//    private boolean isValid(String clearTextPassword, String hashedPass) {
//        // returns true if password matches hash
//        return BCrypt.checkpw(clearTextPassword, hashedPass);
//    }


}

