package com.example.nika.androidchatapp.activites;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.nika.androidchatapp.adapters.ChatAdapter;
import com.example.nika.androidchatapp.databinding.ActivityChatBinding;
import com.example.nika.androidchatapp.models.ChatMessage;
import com.example.nika.androidchatapp.models.User;
import com.example.nika.androidchatapp.network.RetrieveFeedTask1;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManger;
    private FirebaseFirestore database;
    private Boolean isReceiverAvailable = false;
    private String conversionId = null;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private int fileChose;



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
        message.put(Constants.KEY_HIDDEN_TEXT,"null");
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


    private void sendMessage(String url,String type){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManger.getString(Constants.Key_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,"you get a "+type+" file click me for open!");
        message.put(Constants.KEY_HIDDEN_TEXT,"null");
        message.put(Constants.KEY_DATA_TYPE,type);
        message.put(Constants.KEY_URL,url);
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
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
        binding.layoutSend.setOnClickListener(v->sendMessage());
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
            fileChose=1;
            Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });

        binding.addaudio.setOnClickListener(v->{
            fileChose=2;
            Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });

        binding.addvideo.setOnClickListener(v->{
            fileChose=3;
            Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });


        binding.addfile.setOnClickListener(v->{
            fileChose=4;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/msword");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 12);
            pickImage.launch(intent);

        });




    }

    private final ActivityResultLauncher<Intent> pickImage= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()==RESULT_OK){
                    if (result.getData()!=null){
                        showToast(result.getData().getClass().toString());
                        Uri imageUri=result.getData().getData();
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

                            UploadTask uploadTask = mountainsRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    showToast("cccc");
                                    showToast(exception.toString());
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                    // ...
                                }
                            });
                            //final StorageReference ref = storageRef.child(imageUri.toString());
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
                                        sendMessage(downloadUri.toString(),type);
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
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
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