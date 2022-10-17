package com.example.nika.androidchatapp.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.nika.androidchatapp.activites.MainActivity;
import com.example.nika.androidchatapp.activites.ResAudioActivity;
import com.example.nika.androidchatapp.activites.ResImageActivity;
import com.example.nika.androidchatapp.activites.ResMp4Activity;
import com.example.nika.androidchatapp.activites.ResWordActivity;
import com.example.nika.androidchatapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.nika.androidchatapp.databinding.ItemContainerSentMessageBinding;
import com.example.nika.androidchatapp.models.ChatMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    public static  final  int VIEW_TYPE_SENT =1;
    public static  final int VIEW_TYPE_RECEIVED =2;
    private FirebaseStorage storage = FirebaseStorage.getInstance();




    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.receiverProfileImage = receiverProfileImage;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType ==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
if(getItemViewType(position)==VIEW_TYPE_SENT){
    ((SentMessageViewHolder) holder ).setData(chatMessages.get(position));
}else {
    ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage);
}
        holder.itemView.setOnClickListener(l->{
            String type=chatMessages.get(position).dataType;
            String url=chatMessages.get(position).url;
            String docid=chatMessages.get(position).docid;
            if(type.equals("jpg") || type.equals("png")){
                Context context = l.getContext();
                Intent intent = new Intent(context, ResImageActivity.class);
                intent.putExtra("KEY",docid);
                context.startActivity(intent);
            }else if(type.equals("mp3")|| type.equals("wav'")){
                Context context = l.getContext();
                Intent intent = new Intent(context, ResAudioActivity.class);
                intent.putExtra("KEY",docid);
                context.startActivity(intent);
            }else if(type.equals("txt")){
                Context context = l.getContext();
                Intent intent = new Intent(context, ResWordActivity.class);
                intent.putExtra("KEY",docid);
                context.startActivity(intent);
            }else if(type.equals("text")){
            }else if(type.equals("mp4")){
                Context context = l.getContext();
                Intent intent = new Intent(context, ResMp4Activity.class);
                intent.putExtra("KEY",docid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class  SentMessageViewHolder extends RecyclerView.ViewHolder{
        private  final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding =itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDataTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private  final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage ,Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDataTime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }

    }
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }



}
