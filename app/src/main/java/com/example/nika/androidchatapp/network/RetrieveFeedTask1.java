package com.example.nika.androidchatapp.network;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class RetrieveFeedTask1 extends AsyncTask<String , Void, String> {

        private Exception exception;
        private String modifiedSentence;
        private String fileurl;
        private String hiddentext;

        public RetrieveFeedTask1(String ifileurl){
            this.fileurl=ifileurl;
        }

        public String Gethiddentext(){
            return hiddentext;
        }



        protected String doInBackground(String... urls) {

            Socket clientSocket = null;
            try {
                clientSocket = new Socket("10.0.0.14", 10000);
                DataOutputStream outToServer =
                        new DataOutputStream(clientSocket.getOutputStream());

                BufferedReader inFromServer =
                        new BufferedReader(new
                                InputStreamReader(clientSocket.getInputStream()));

                Log.d("result" , "yes");

                outToServer.writeBytes(fileurl+ '\n');

                modifiedSentence = inFromServer.readLine();



            } catch (IOException e) {
                e.printStackTrace();
            }
            return modifiedSentence;
        }

        protected void onPostExecute(String feed) {
            hiddentext=feed;
            Log.d("after" , feed);
        }

    }
