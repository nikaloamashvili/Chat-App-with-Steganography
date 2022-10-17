# from turtle import st
from flask import Flask
import firebase_admin
from firebase_admin import db
from firebase_admin import firestore
from myaudio import *
from mytext import *
from myvideo import *
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from firebase_admin import storage
from stegano import lsb
import random
import string
import requests
from uuid import uuid4
import pyrebase
import os
from pydub import AudioSegment


cred = credentials.Certificate(".json")
firebase_admin.initialize_app(cred,{"storageBucket": ""})

config = {
  "apiKey": "",
  "authDomain": "",
  "databaseURL": "",
  "projectId": "",
  "storageBucket": "",
  "messagingSenderId": "",
  "appId": "",
  "measurementId": ""
}

db = firestore.client()
letters = string.ascii_lowercase

firebase_storage1 = pyrebase.initialize_app(config)
storage1 = firebase_storage1.storage()

# import request
from flask import request
app = Flask(__name__)

def encode(str,str2):
  print(1)
  doc=db.collection("chat").document(str).get().to_dict()
  token = uuid4()
  filename= ( ''.join(random.choice(letters) for i in range(5)) )
  print(doc.get('dataType'))

  if(doc.get('dataType')=="jpg"):
    response = requests.get(doc.get('url'))
    src=filename+".JPEG"
    file = open(src, "wb")
    file.write(response.content)
    file.close()
    secret = lsb.hide(src, str2)
    fileName1 = filename+".png"
    secret.save(fileName1)
    print (fileName1)
    bucket = storage.bucket()
    blob = bucket.blob(fileName1)
    metadata = {"firebaseStorageDownloadTokens": token}
  # Assign the token as metadata
    blob.metadata = metadata
    blob.upload_from_filename(fileName1)
    print ("***")

    # Opt : if you want to make public access from the URL
    blob.make_public()
    print("your file url", blob.public_url)
    db.collection("chat").document(str).update({
      "url":blob.public_url,
      "dataType":"png"
      })
    os.remove(src)
    os.remove(fileName1)


  elif(doc.get('dataType')=="mp3"):

    src = filename+".mp3"
    dst = filename+".wav"
    dst1= filename+"o.wav"

    response = requests.get(doc.get('url'))
    file = open(src, "wb")
    file.write(response.content)
    file.close()

    # convert wav to mp3
    sound = AudioSegment.from_mp3(src)
    sound.export(dst, format="wav")
    em_audio(dst, str2,dst1)
    fileName1 = dst1
    imgurl=storage1.child(filename+"o.wav").put(fileName1)
    public_url=storage1.child(filename+"o.wav").get_url(imgurl['downloadTokens'])
    print(public_url)
    db.collection("chat").document(str).update({
      "url":public_url,
      "dataType":"wav"
      })
    os.remove(src)
    os.remove(dst)
    os.remove(dst1)

  elif(doc.get('dataType')=="mp4"):

    src=filename+".mp4"
    dstv=filename+".png"
    dststego=filename+".mp4"
    dstjpg=filename+".jpg"

    print('mp4')
    response = requests.get(doc.get('url'))
    file = open(src, "wb")
    file.write(response.content)
    file.close()



    encode_vid_data(src,str2,dststego,dstjpg,dstv)
    bucket = storage.bucket()
    blob = bucket.blob(dstv)    
    metadata = {"firebaseStorageDownloadTokens": token}
  # Assign the token as metadata
    blob.metadata = metadata
    blob.upload_from_filename(dstv)
    # Opt : if you want to make public access from the URL
    blob.make_public()
    print("your file url", blob.public_url)
    db.collection("chat").document(str).update({
      "frame":blob.public_url
      })
    os.remove(src)
    os.remove(dstv)
    os.remove(dststego)
    os.remove(dstjpg)



  elif(doc.get('dataType')=="txt"):
    response = requests.get(doc.get('url'))
    src=filename+".txt"
    dst=filename+"o.txt"

    file = open(src, "wb")
    file.write(response.content)
    file.close()
    # token = uuid4()
    encode_txt_data(src,str2,dst)
    fileName1=dst
    bucket = storage.bucket()
    blob = bucket.blob(fileName1)    
    metadata = {"firebaseStorageDownloadTokens": token}
  # Assign the token as metadata
    blob.metadata = metadata
    blob.upload_from_filename(fileName1)
    # Opt : if you want to make public access from the URL
    blob.make_public()
    print("your file url", blob.public_url)
    db.collection("chat").document(str).update({
      "url":blob.public_url
      })
    os.remove(fileName1)
    os.remove(src)



def decode(str):
  doc=db.collection("chat").document(str).get().to_dict()
  filename= ( ''.join(random.choice(letters) for i in range(5)) )
  print(doc)
  clear_message=None
  # if(doc.get('hiddenText')=="its_secret"):
  if(doc.get('dataType')=="png"):
      url=doc.get('url')
      response = requests.get(url)
      src=filename+".png"

      file = open(src, "wb")
      file.write(response.content)
      file.close()
      clear_message = lsb.reveal(src)
      os.remove(src)
      print (clear_message)

  elif(doc.get('dataType')=="wav"):
      url=doc.get('url')
      response = requests.get(url)
      src=filename+".wav"

      file = open(src, "wb")
      file.write(response.content)
      file.close()
      clear_message = ex_msg(src)
      os.remove(src)

  elif(doc.get('dataType')=="txt"):
      url=doc.get('url')
      response = requests.get(url)
      src=filename+".txt"

      file = open(src, "wb")
      file.write(response.content)
      file.close()
      clear_message = decode_txt_data(src)
      print(clear_message)
      os.remove(src)

  elif(doc.get('dataType')=="mp4"):
      url=doc.get('frame')
      if(not(url==None)):
          
        response = requests.get(url)
        src=filename+".jpg"

        file = open(src, "wb")
        file.write(response.content)
        file.close()
        clear_message = decode_vid_data(src)
        print(clear_message)
        os.remove(src)

  if(clear_message==""):
        return None
  elif(clear_message==None):
        return None
  else:
        return "the hidden text is "+clear_message

@app.route("/")
def showHomePage():
  return "This is BackEnd of ChatApp of N.L"
@app.route("/debug", methods=["POST"])
def debug():
  text0 = request.form["sample0"]
  text = request.form["sample"]
  text1 = request.form["sample1"]
  print(text)
  print(text1)
  if(text0=="encode"):
    encode(text,text1)
    return "the text was encoded"
  elif(text0=="decode"):

    sectext=decode(text)
    if(sectext==None):
      return "not found hidden text"
    else:
      return sectext
  return "received"

# # if __name__ == "__main__":
# #   app.run(host="0.0.0.0")



  


