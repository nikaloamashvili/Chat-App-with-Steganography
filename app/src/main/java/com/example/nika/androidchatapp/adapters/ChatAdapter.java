package com.example.nika.androidchatapp.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.example.nika.androidchatapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.nika.androidchatapp.databinding.ItemContainerSentMessageBinding;
import com.example.nika.androidchatapp.models.ChatMessage;
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
            String UrlofFile;
            String TypeOfFile;
            //Toast.makeText(l.getContext(), getMimeType(chatMessages.get(position).message.toString()),Toast.LENGTH_SHORT).show();
            //Toast.makeText(l.getContext(), Arrays.toString((chatMessages.get(position).message.toString()).split(".")),Toast.LENGTH_SHORT).show();
            List<String> arrOfStr = Arrays.asList((chatMessages.get(position).message).split("\\."));
            //Toast.makeText(l.getContext(),chatMessages.get(position).message.toString() ,Toast.LENGTH_SHORT).show();

            TypeOfFile= arrOfStr.get(arrOfStr.size() - 1);
            UrlofFile=chatMessages.get(position).message.toString().substring(0, chatMessages.get(position).message.toString().length() -(TypeOfFile.length()+1));
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse(UrlofFile.toString()));
            String tempTitle="check";
            request.setTitle(tempTitle);
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,tempTitle+"."+TypeOfFile);
            DownloadManager downloadManager=(DownloadManager) l.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            request.setMimeType(null);
            request.allowScanningByMediaScanner();
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            downloadManager.enqueue(request);
            Toast.makeText(l.getContext(),"downloading..." ,Toast.LENGTH_SHORT).show();

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
