package com.example.nika.androidchatapp.activites;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.adapters.ChatAdapter;
import com.example.nika.androidchatapp.databinding.ActivityChatBinding;
import com.example.nika.androidchatapp.models.ChatMessage;
import com.example.nika.androidchatapp.models.User;
import com.example.nika.androidchatapp.utilities.Constants;
import com.example.nika.androidchatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private OkHttpClient okHttpClient;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManger;
    private FirebaseFirestore database;
    private Boolean isReceiverAvailable = false;
    private String conversionId = null;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private int fileChose;

    boolean isPlay = false;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding =ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        setlistener();
        listenMessages();
    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManger.getString(Constants.Key_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManger.getString(Constants.Key_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener =(value , error)->{
        if(error !=null){
            return ;
        }if (value !=null){
            int count=chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId =documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId =documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message =documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.hiddenText =documentChange.getDocument().getString(Constants.KEY_HIDDEN_TEXT);
                    chatMessage.url =documentChange.getDocument().getString(Constants.KEY_URL);
                    chatMessage.docid=documentChange.getDocument().getString(Constants.KEY_IDD);
                    chatMessage.dataType =documentChange.getDocument().getString(Constants.KEY_DATA_TYPE);
                    chatMessage.dateTime =getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject =documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }

            }
            Collections.sort(chatMessages,(obj1,obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count ==0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null) {
            checkForConversion();
        }
    };

    private void init(){
        preferenceManger = new PreferenceManager(getApplicationContext());
        chatMessages =new ArrayList<>();
        chatAdapter  = new ChatAdapter(
                chatMessages,
                getBitampFromEncodedString(receiverUser.image),
                preferenceManger.getString(Constants.Key_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database =FirebaseFirestore.getInstance();

    }

    private  void listenAvailabilityofReceiver(){
        database.collection(Constants.Key_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this,(value,error)->{
            if(error != null){
                return;
            }
            if(value !=null){
                if(value.getLong(Constants.KEY_AVAILABILITY)!= null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability ==1;
                }
            }
            if(isReceiverAvailable){
                binding.textAvailability.setVisibility(View.VISIBLE);
            }else{
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    private Bitmap getBitampFromEncodedString(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private void loadReceiverDetails(){
        receiverUser =(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManger.getString(Constants.Key_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
//        message.put(Constants.KEY_HIDDEN_TEXT,"its_secret");
        message.put(Constants.KEY_DATA_TYPE,"text");
        message.put(Constants.KEY_URL,"null");
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        if (conversionId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManger.getString(Constants.Key_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManger.getString(Constants.Key_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManger.getString(Constants.Key_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }


    private void sendMessage(String url,String type,String textuser){
        DocumentReference newCityRef = database.collection(Constants.KEY_COLLECTION_CHAT).document();
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManger.getString(Constants.Key_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,"you get a "+type+" file click me for open!");
//        message.put(Constants.KEY_HIDDEN_TEXT,"its_secret");
        message.put(Constants.KEY_DATA_TYPE,type);
        message.put(Constants.KEY_URL,url);
        message.put(Constants.KEY_IDD,newCityRef.getId());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        newCityRef.set(message);
                if(isPlay) {
                    okHttpClient = new OkHttpClient().newBuilder()
                                .connectTimeout(100, TimeUnit.SECONDS)
                                .writeTimeout(100, TimeUnit.SECONDS)
                                .readTimeout(100, TimeUnit.SECONDS)
                                .build();
                    // dummyText with a name 'sample'
                    RequestBody formbody
                            = new FormBody.Builder()
                            .add("sample0", "encode")
                            .add("sample", newCityRef.getId().toString())
                            .add("sample1", textuser)
                            .build();
                    // while building request
                    // we give our form
                    // as a parameter to post()
                    Request request = new Request.Builder().url("https://textofnika.azurewebsites.net/debug")
                            .post(formbody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(
                                @NotNull Call call,
                                @NotNull IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "server down", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseData = response.body().string();
                            if (responseData!=null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String x=responseData.toString();
                                        Toast.makeText(getApplicationContext(),x , Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });

                }

        if (conversionId != null) {
            updateConversion("you get a "+type+" file click me for open!");
        }else {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManger.getString(Constants.Key_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManger.getString(Constants.Key_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManger.getString(Constants.Key_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,"you get a "+type+" file click me for open!");
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);

        }
        binding.inputMessage.setText(null);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
        binding.layoutSend.setOnClickListener(

                v->{
                    if(!isPlay){
                        sendMessage();
                    }else{
                        showToast("you need to chose a file!");
                    }

                });
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId=documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion () {
        if(chatMessages.size()!=0){
            checkForConversionRemotely(
                    preferenceManger.getString(Constants.Key_USER_ID),
                    receiverUser.id

            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManger.getString(Constants.Key_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);

    }


    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task->{
        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot =task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setlistener(){

        binding.addphoto.setOnClickListener(v->{
            if(!isPlay){
                fileChose=1;
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }else if (isPlay && (binding.inputMessage.getText().toString().isEmpty())){
                showToast("you need enter a text!");
            }else{
                fileChose=1;
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }


        });

        binding.addaudio.setOnClickListener(v->{
            if(!isPlay){
                fileChose=2;
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);}
            else if (isPlay && (binding.inputMessage.getText().toString().isEmpty())){
                showToast("you need enter a text!");
            }else{
                fileChose=2;
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
//            Intent intent = new Intent(getApplicationContext(),test.class);
//            startActivity(intent);
//            finish();
        });

        binding.addvideo.setOnClickListener(v->{
            if(!isPlay){
                fileChose=3;
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);}
            else if (isPlay && (binding.inputMessage.getText().toString().isEmpty())){
                showToast("you need enter a text!");
            }else{
                fileChose=3;
                Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });


        binding.addfile.setOnClickListener(v->{
            if(!isPlay){
                fileChose=4;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
//            intent.setType("application/pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 12);
                pickImage.launch(intent);}
            else if (isPlay && (binding.inputMessage.getText().toString().isEmpty())){
                showToast("you need enter a text!");
            }else{
                fileChose=4;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
//            intent.setType("application/pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 12);
                pickImage.launch(intent);}


        });

        binding.addsecret.setOnClickListener(v->{
            isPlay = !isPlay; // reverse
            if(isPlay){
                v.setBackgroundResource(R.drawable.ic_secret_on);
                showToast("chose media type and enter a text!");
            }else{
                v.setBackgroundResource(R.drawable.ic_scret);
            }
//            Intent intent = new Intent(getApplicationContext(),TestActivity.class);
////            Intent intent = new Intent(getApplicationContext(),EncodeActivity.class);
//
//
//
//            startActivity(intent);
//            finish();
        });
    }

    private final ActivityResultLauncher<Intent> pickImage= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()==RESULT_OK){
                    if (result.getData()!=null){
                        Uri imageUri=result.getData().getData();
                        Cursor returnCursor =
                                getContentResolver().query(imageUri, null, null, null, null);
                        Cursor cursor = getApplicationContext().getContentResolver()
                                .query(imageUri, null, null, null, null, null);


                        try {
                            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
                            // "if there's anything to look at, look at it" conditionals.
                            if (cursor != null && cursor.moveToFirst()) {

                                // Note it's called "Display Name". This is
                                // provider-specific, and might not necessarily be the file name.


                                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                                // If the size is unknown, the value stored is null. But because an
                                // int can't be null, the behavior is implementation-specific,
                                // and unpredictable. So as
                                // a rule, check if it's null before assigning to an int. This will
                                // happen often: The storage API allows for remote files, whose
                                // size might not be locally known.
                                String size = null;
                                if (!cursor.isNull(sizeIndex)) {
                                    // Technically the column stores an int, but cursor.getString()
                                    // will do the conversion automatically.
                                    size = cursor.getString(sizeIndex);

                                } else {
                                    size = "Unknown";
                                }
                                if(Long.parseLong(size)>3857809 && fileChose==2){
                                    showToast("file to big chose a audio of max one minute");
                                }else{
                                    try {
                                        InputStream inputStream= getContentResolver().openInputStream(imageUri);
                                        // inflate the layout of the popup window
                                        ContentResolver cR = getBaseContext().getContentResolver();
                                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                                        String type = mime.getExtensionFromMimeType(cR.getType(imageUri));

                                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                                        StorageReference storageRef = storage.getReference();
                                        StorageReference mountainsRef = storageRef.child(imageUri.toString());

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        if(fileChose==1){
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                                        }else if(fileChose==2){
                                        }else {

                                        }
                                        byte[] data = baos.toByteArray();

                                        UploadTask uploadTask = mountainsRef.putFile(imageUri);
                                        binding.progressBar.setVisibility(View.VISIBLE);
                                        showToast("sending...");


                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                showToast(exception.toString());
                                                binding.progressBar.setVisibility(View.GONE);

                                                // Handle unsuccessful uploads
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                                // ...
                                                mountainsRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        if (task.isSuccessful()) {
                                                            Uri downloadUri = task.getResult();
//                                        RetrieveFeedTask1 r = new RetrieveFeedTask1(downloadUri.toString());
//                                        r.execute();
//                                        try {
//                                            TimeUnit.SECONDS.sleep(10);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
                                                            sendMessage(downloadUri.toString(),type,binding.inputMessage.getText().toString());
                                                            //     showToast(r.Gethiddentext());
                                                        } else {
                                                            showToast("faild");
                                                            // Handle failures
                                                            // ...
                                                        }}}).addOnFailureListener(l->{
                                                    showToast("the problem d");
                                                    showToast(l.toString());
                                                    showToast("the problem u");
                                                });
                                                binding.progressBar.setVisibility(View.GONE);



                                            }
                                        });
                                        //final StorageReference ref = storageRef.child(imageUri.toString());

                                    }catch (FileNotFoundException e){
                                        e.printStackTrace();
                                    }

                                }


                            }
                        } finally {
                            cursor.close();
                        }



//                                showToast(String.valueOf(returnCursor.getColumnIndex(OpenableColumns.SIZE)));

                    }
                }
            }
    );

    @Override
    protected  void onResume(){
        super.onResume();
        listenAvailabilityofReceiver();
    }

    private void showToast (String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    public static String getMimeType( Uri uri) {
        String extension;

        //Check uri format to avoid null

        //If scheme is a File
        //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
        extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());



        return extension;
    }

}