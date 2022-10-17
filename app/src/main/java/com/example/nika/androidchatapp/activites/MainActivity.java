package com.example.nika.androidchatapp.activites;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.nika.androidchatapp.adapters.RecentConversationAdapter;
import com.example.nika.androidchatapp.databinding.ActivityMainBinding;
import com.example.nika.androidchatapp.listeners.ConversionListener;
import com.example.nika.androidchatapp.models.ChatMessage;
import com.example.nika.androidchatapp.models.User;
import com.example.nika.androidchatapp.utilities.Constants;
import com.example.nika.androidchatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations ;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager =new PreferenceManager(getApplicationContext());
        init();
        passwordTime();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }
    private void init (){
        conversations= new ArrayList<>();
        conversationAdapter= new RecentConversationAdapter(conversations,this);
        binding.converstionsRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners(){
        binding.textView3.setOnClickListener(v->{
            showToast("You need to change the password!");
            Context context = peekAvailableContext();
            Intent intent = new Intent(context, ChangePasswordActivity.class);
            intent.putExtra("KEY","yesop");
            context.startActivity(intent);
        });
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v->startActivity(new Intent(getApplicationContext(),UsersActivity.class)));

    }

    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.Key_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.Key_IMAGE), Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast (String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void listenConversations () {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.Key_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.Key_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private void passwordTime () {
        String lastPassDate;
        String thisDate;
        String mlastPassDate;
        String mthisDate;
        String ylastPassDate;
        String ythisDate;
        Integer lPD=0;
        Integer tD=0;
        Integer ylPD=0;
        Integer ytD=0;

        lastPassDate=preferenceManager.getString(Constants.KEY_PTIMESTAMP);
        thisDate=getReadableDateTime(new Date());
//        showToast(lastPassDate);

        mlastPassDate=lastPassDate.substring(0,2);
        mthisDate=thisDate.substring(0,2);
//        showToast(mlastPassDate);
//        showToast(mthisDate);

        ylastPassDate=lastPassDate.substring(7,11);
        ythisDate=thisDate.substring(7,11);

//showToast(mlastPassDate);

//        showToast("**");
//        showToast(mthisDate);
//        showToast("**");
//
//        showToast(ylastPassDate);
//        showToast("**");
//
//        showToast(ythisDate);

//        showToast("**");

        try{

            lPD = Integer.parseInt(mlastPassDate);
            tD = Integer.parseInt(mthisDate);
            ylPD = Integer.parseInt(ylastPassDate);
            ytD = Integer.parseInt(ythisDate);
        }
        catch (NumberFormatException ex){
            showToast(ex.getMessage());
        }

        if((ytD-ylPD)>1){
            showToast("You need to change the password!");
            Context context = peekAvailableContext();
            Intent intent = new Intent(context, ChangePasswordActivity.class);
            intent.putExtra("KEY","noop");
            context.startActivity(intent);

        }
        else if((ytD==ylPD)){
            if((tD-lPD)>=3){
                showToast("You need to change the password!");
                Context context = peekAvailableContext();
                Intent intent = new Intent(context, ChangePasswordActivity.class);
                intent.putExtra("KEY","noop");
                context.startActivity(intent);

            }

        }else if((((tD-12)*-1)-lPD)>=3){
//            showToast(tD.toString());
//            showToast(lPD.toString());

            showToast("You need to change the password!");
            Context context = peekAvailableContext();
            Intent intent = new Intent(context, ChangePasswordActivity.class);
            intent.putExtra("KEY","noop");
            context.startActivity(intent);
        }


    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error)->{
      if(error!=null){
          return;
      }
      if (value!=null){
          for (DocumentChange documentChange : value.getDocumentChanges()) {
              if(documentChange.getType()== DocumentChange.Type.ADDED){
                  String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                  String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                  ChatMessage chatMessage= new ChatMessage();
                  chatMessage.senderId=senderId;
                  chatMessage.receiverId=receiverId;
                  if(preferenceManager.getString(Constants.Key_USER_ID).equals(senderId)){
                      chatMessage.conversionImage= documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                      chatMessage.conversionName= documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                      chatMessage.conversionId= documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                  }else{
                      chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                      chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                      chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                  }
                  chatMessage.message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                  chatMessage.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                  conversations.add(chatMessage);

              }else if (documentChange.getType()== DocumentChange.Type.MODIFIED){
                  for (int i = 0;i<conversations.size();i++){
                      String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                      String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                      if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                          conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                          conversations.get(i).dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                           break;
                      }
                  }
              }
          }
          Collections.sort(conversations,(obj1,obj2)->obj2.dateObject.compareTo(obj1.dateObject));
          conversationAdapter.notifyDataSetChanged();
          binding.converstionsRecyclerView.smoothScrollToPosition(0);
          binding.converstionsRecyclerView.setVisibility(View.VISIBLE);
          binding.progressBar.setVisibility(View.GONE);

      }
    };

    private  void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.Key_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.Key_USER_ID)
                );
        documentReference.update(Constants.key_fcm_token,token)
                .addOnFailureListener(e-> showToast("unable to update token"));
    }

    private void signOut(){
        showToast("signing out...");
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.Key_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.Key_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.key_fcm_token, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused ->{
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(),SigninActivity.class));
                    finish();
                })
                .addOnFailureListener(e->showToast("unable to sign out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);

    }
}