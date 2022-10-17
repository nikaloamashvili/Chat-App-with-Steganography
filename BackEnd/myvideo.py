# src="C:\\decode photo\\Steganography-Tools-master\\video.mp4"
# dst="C:\\decode photo\\Steganography-Tools-master\\stego.mp4"
# dst1="C:\\decode photo\\Steganography-Tools-master\\frame_.jpg"
# # dst2="C:\\decode photo\\Steganography-Tools-master\\frame_2.png"
from stegano import lsb
import cv2







def encode_vid_data(src,message,dst,dst1,dst2):
    cap=cv2.VideoCapture(src)
    vidcap = cv2.VideoCapture(src)    
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    frame_width = int(vidcap.get(3))
    frame_height = int(vidcap.get(4))

    size = (frame_width, frame_height)
    out = cv2.VideoWriter(dst,fourcc, 25.0, size)
    max_frame=0
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret == False:
            break
        max_frame+=1
    cap.release()
    print("Total number of Frame in selected Video :",max_frame)
    print("Enter the frame number where you want to embed data : ")
    n=1
    frame_number = 0
    while(vidcap.isOpened()):
        frame_number += 1
        ret, frame = vidcap.read()
        if ret == False:
            break
        if frame_number == n:    
            cv2.imwrite(dst1, frame)   
            secret=lsb.hide(dst1, message)
            secret.save(dst2)
              # save frame as JPEG file      
        out.write(frame)


def decode_vid_data(dst2):
            clear_message = lsb.reveal(dst2)
            print(clear_message)
            return clear_message


def vid_steg():
    while True:
        print("\n\t\tVIDEO STEGANOGRAPHY OPERATIONS") 
        print("1. Encode the Text message")  
        print("2. Decode the Text message")  
        print("3. Exit")  
        choice1 = int(input("Enter the Choice:"))   
        if choice1 == 1:
            a=encode_vid_data()
        elif choice1 == 2:
            decode_vid_data()
        elif choice1 == 3:
            break
        else:
            print("Incorrect Choice")
        print("\n")


