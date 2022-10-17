# Steganography Messaging app

## Introduction

### Background
In the world, there is a variety of social networks and expanded use of platforms for instant messaging, photos, documents, videos and voice. With the development of technology and the advancement of computing science, the field of steganography has also entered the computerized communication channels and today it is possible to find references to the subjects of hidden messages in communication protocols, images, videos, audio and more Steganography. The name means – 'stegano' is covering, and 'graphically' it's writing. This is the art of writing covered messages and conveying information in a secret way. What do I mean by that? In cryptography, we encrypt the information but do not hide it. Anyone can see that I am transmitting encrypted information and the purpose of cryptography is not to hide that fact of the existence of the information, just to prevent people from rummaging through there without the original cipher key. The purpose of steganography is to hide the fact of the transfer of information.

### The guiding goals (Targets)
Today, the need to send messages safely and covertly between people is complex and complex actions must be performed, if it is in looking for the appropriate algorithm for the same type of format of the file in which we want to hide the information and it will be necessary in the same type of algorithm to decipher the message that exists in that file. For different files like text, image, video, audio, etc. for each one of them the user will have to look for the right algorithm. Which led us to develop a messaging app that will allow users to send and decode hidden messages in the various format files in a messaging app in an Android environment, by running certain algorithms on files such as documents, videos and voice files sent between users of the application and allowing them to send hidden content in files and images and in addition to decoding the messages with the hidden content received.  The user will choose the file that he wants to add to him the hidden information and the system will run the required algorithm and only the target user will receive the file and decrypt it.

### Glossary Table
|Terms   | Explanation  |
| ------------ | ------------ |
|  Client Based Application  | An application that runs on a work station or personal computer in a network and is not available to others in the network  |
|  Real-time chat |  Uses Web-based apps, which permit communication that is usually addressed directly but is anonymous among users in a multi-user environment. |
| Database (DB)  | In computing, a database is an organized collection of data stored and accessed electronically. Small databases can be stored on a file system, while large databases are hosted on computer clusters or cloud storage  |
| Firebase  |  Firebase is a set of hosting services for any type of application. It offers NoSQL and real-time hosting of databases, content, social authentication, and notifications, or services, such as a real-time communication server |
|  Android OS |  Android is a mobile operating system based on a modified version of the Linux kernel and other open source software, designed primarily for touchscreen mobile devices such as smartphones and tablets |
| API  | API stands for Application Programming Interface. In the context of APIs, the word Application refers to any software with a distinct function. Interface can be thought of as a contract of service between two applications. This contract defines how the two communicate with each other using requests and responses  |
| Flask Python  |  Flask is a micro web framework written in Python. It is classified as a microframework because it does not require particular tools or libraries. It has no database abstraction layer, form validation, or any other components where pre-existing third-party libraries provide common function |

## Requirements

### Android Client OS
•	Android Studio version 7.0+.
•	Java 8 or higher.
•	Familiarity with the Gradle programming language.
•	Network Connection
•	Android Libraries
'androidx.appcompat:appcompat:1.4.1','com.google.android.material:material:1.5.0','androidx.constraintlayout:constraintlayout:2.1.3','com.google.firebase:firebase-messaging:23.0.2','com.google.firebase:firebase-firestore:24.1.0','com.google.firebase:firebase-analytics:20.1.2','com.google.firebase:firebase-auth:21.0.4','group: 'at.favre.lib', name: 'bcrypt',version: '0.9.0','com.google.firebase:firebase-storage:20.0.1','com.google.firebase:firebase-database:20.0.5','junit:junit:4.+','androidx.test.ext:junit:1.1.3','androidx.test.espresso:espresso-core:3.4.0','com.intuit.sdp:sdp-android:1.0.6','com.intuit.ssp:ssp-android:1.0.6',("com.squareup.okhttp3:okhttp:4.9.0"),'com.makeramen:roundedimageview:2.3.0',"androidx.multidex:multidex:2.0.1",'com.squareup.retrofit2:retrofit:2.9.0','com.squareup.retrofit2:converter-scalars:2.9.0'

### FireBase DB
•	Targets API level 19 (KitKat) or higher
•	Uses Android 4.4 or higher
•	Cloud Service

### Python Server
•	System requirements for Python Installation: 
1. Operating system: Linux- Ubuntu 16.04 to 17.10, or Windows 7 to 10, with 2GB RAM (4GB preferable)
 2. You must install Python 3.10.2 and related packages, please follow the installation instructions given below as per your operating system.
•	Python and Matplotlib Libraries
•	Python Libraries
 flask, firebase_admin, myaudio, mytext, random,  string, requests, uuid, os, wave, argparse ,pydub ,pyplot
•	Steganography Python Libraries
Stegano, HiddenWave, Priyansh

## Installation(for end user)
you can to install the app on your android from [here](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/installation.apk "here").

## Installation & First Steps (for developers)
### Android Studio 
The first stage we started to build our development environment, we knew that we would have to develop in an Android environment and therefore after testing the best development environment for the Android environment is Android studio in JAVA, in addition the team members underwent one and two Android courses so that there was familiarity with the development environment. Its shortcomings are the system that it requires a lot of resources.
### Firebase Cloud
Step 2 – After we started developing the messaging application in the Android environment Studio we were looking for a database that would support us both saving files and identification to the application, Firebase is a cloud service that serves as a database solution and has full integration with Android applications, it is a Google service Additional advantages it allows full support for Realtime applications and allows enforcing and identification of users at a high security level and encrypts communication between the client and the server It has a built-in API that allowed us to run code from our Python server on the Firebase DB , a disadvantage of the system is that it is a cloud service and cannot be a solution without connecting to the Internet. We connected Firebase to develop our application and through this we were able to build functions for sending messages in real time, registering and identifying to the application securely by using Google's built-in hash algorithm, in addition to saving three passwords back in the database using sha256
### Python Server + Flask Library
Step 3 – After setting up our messaging app and the database, we needed a solution that would allow us to run steganographic algorithms, so we were looking for something that would interface us with a database i.e. firebase and in addition that could also communicate with the Android app. The solution will run the appropriate algorithm according to the requested file format. In addition, we had to make updates to the database running the algorithms we wanted to save the file with the hidden message or decode the hidden message from the file (encode/decode). After testing, we realized that it is correct to use the Python environment because it is very common and can be run on ready-made steganography algorithms that were very common in the Python environment, the next step is also to run the Docker server in a cloud environment (Azure). We then had to find a solution that would integrate our Python server with the firebase in order to have the ability to run functions on our Python server in the database and make changes to the firebase, as well as receive information from Firebase to our Python server so that we could successfully execute the algorithms and we could hide and discover messages, after investigation we found that Flask gives the ability to interface with the Firebase API easily to receive and update information in the database, Which made it very easy for us to run code functions we created 
### Steganography Algorithms
Step 4 – The last part was very challenging, it took to research and find for each file which algorithms were suitable for each type of format. For image and text type files it was not a problem but for video and audio files it was very complex because there are a variety of audio and video formats, we found algorithms developed in the Python environment and made adjustments to our backend server, disadvantages that some algorithms we had to make adjustments and improve the algorithms in order to get optimal support In addition we would have to update the system when we wanted to support additional files, The advantages that it is very easy in the Python environment to install and make adjustments.

## Video demonstration of the App:

[click here to watch](https://youtu.be/tKHeQ_uIAaM "click here to watch").

## User Case Diagram
[![user case diagram](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/Use%20case%20diagram.jpg "user case diagram")](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/Use%20case%20diagram.jpg "user case diagram")



Figure 1: User Case Diagram. The diagram shows all the functions that the Application can do for the user, from creating a new instance till sending a file with a hidden content.  


## Android app Class Diagram

[![class diagram](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/Class%20diagram.svg "class diagram")](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/Class%20diagram.svg "class diagram")

Figure 2: Class Diagram. The diagram shows all the functions that run behind the user interface. The functions are written in JAVA. 

## ERD

[![erd](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/ERD.png "erd")](https://github.com/nikaloamashvili/Chat-App-with-Steganography/blob/main/ERD.png "erd")


Figure 3: ERD. The model describes interrelated things of interest in a curtain field. It is composed of entities that specifies relationships between the entities. 

## Summary

### Stages of the project

In the early stages of the project, none of the team members knew exactly how steganography was used to hide messages in code, and what are the advantages and disadvantages between the existing algorithms on the Internet. We noticed that there is a wide variety of algorithms for all kinds of formats and this gave us the idea to learn how everything works. We realized very quickly that there is no order in the field and this led us to the idea to develop a friendly messaging app for the user that will allow sending messages in real time between friends and also allow to hide and decode messages in files and images. Because we lacked knowledge in the field of API, and the development of mobile messaging applications, we started learning the following worlds so that we could reach a final product .We focused on developing in Java environment and Python. Our database environment was a Firebase that helped us to develop more quickly, by interfacing with the Firebase API we could use the flask python library to access the database to perform actions such as running algorithms and changes on the database and already to make updates. We divided the tasks between us so that everyone took part in the project, developing the messaging application, setting up a Python server to run algorithms and adding the appropriate algorithms according to the type of format required .The testing environment was performed on a Python server whose task is to run the various algorithms, developing a messaging application in the Android studio environment and using google's database cloud solution called Firebase to support the messaging application we developed .


## Conclusion

In each part we did research to implement the development in the best possible way. First, we began to understand how to develop a messaging application that is secure in a secure form with saving the user's data encrypted in the database, for example, encrypting login details and the password in encryption scrypt. We made sure that the communication between the application and Firebase was secured after the end of the implementation of the application and the establishment of a database. We learned how to run different algorithms that perform Decode and Encode in different formats. And the last step was to enable the ability to run algorithms by a third-party server that required from the user only to select the file and information which we want to hide, and the corresponding algorithms will run. The system we have developed will allow users to hide messages and decrypt. That will be sent between people without the need to search for a specific application that will do everything. It will be in one messaging app Realtime. With the possibility of adding additional algorithm capabilities in the future by the developers so that part of updating the application of the user will not see any changes.


## Bibliography


פרופ' אהוד גודס, דניאל גרפונקל "מימוש והדגמת שיטות בתחום הסטגנוגרפיה בקבצי תוכן", האוניברסיטה הפתוחה. https://www.openu.ac.il/Lists/MediaServer_Documents/Academic/CS/DanielGarfunkel.pdf


### Technical Bibliography
Firebase
https://firebase.google.com/docs

Flask
https://flask.palletsprojects.com/en/2.2.x

Python
https://docs.python.org/3/contents.html

Android Studio
https://developer.android.com/docs
https://www.youtube.com/watch?v=ENK4ONrRm8s&list=PLam6bY5NszYOhXkY7jOS4EQAKcQwkXrp4

GitHub
https://github.com/techchipnet/HiddenWave
https://github.com/Priyansh-15/Steganography-Tools
https://github.com/cedricbonhomme/Stegano

