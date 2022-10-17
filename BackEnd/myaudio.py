# import os
import wave
# import argparse
from os import path
# from pydub import AudioSegment
# AudioSegment.converter = "C:\\ffmpeg-5.1.1-essentials_build\\ffmpeg-5.1.1-essentials_build\\bin\\ffmpeg.exe"
# AudioSegment.ffmpeg ="C:\\ffmpeg-5.1.1-essentials_build\\ffmpeg-5.1.1-essentials_build\\bin\\ffmpeg.exe"
# AudioSegment.ffprobe ="C:\\ffmpeg-5.1.1-essentials_build\\ffmpeg-5.1.1-essentials_build\\bin\\ffmpeg.exe"

# # files
# src = "C:\\decode photo\\HiddenWave-main\\anika.mp3"
# dst = "C:\\decode photo\\HiddenWave-main\\Demo.wav"

# # convert wav to mp3
# sound = AudioSegment.from_mp3("C:\\decode photo\\HiddenWave-main\\anika.mp3")
# sound.export(dst, format="wav")

def em_audio(af, string, output):

      print ("Please wait...")
      waveaudio = wave.open(af, mode='rb')
      frame_bytes = bytearray(list(waveaudio.readframes(waveaudio.getnframes())))
      string = string + int((len(frame_bytes)-(len(string)*8*8))/8) *'#'
      bits = list(map(int, ''.join([bin(ord(i)).lstrip('0b').rjust(8,'0') for i in string])))
      for i, bit in enumerate(bits):
        frame_bytes[i] = (frame_bytes[i] & 254) | bit
      frame_modified = bytes(frame_bytes)
      with wave.open(output, 'wb') as fd:
        fd.setparams(waveaudio.getparams())
        fd.writeframes(frame_modified)
      waveaudio.close()
      print ("Done...")



# em_audio("C:\\decode photo\\HiddenWave-main\\Demo.wav","dfgdfgd","C:\\decode photo\\HiddenWave-main\\Output.wav")

def ex_msg(af):

        print ("Please wait...")
        waveaudio = wave.open(af, mode='rb')
        frame_bytes = bytearray(list(waveaudio.readframes(waveaudio.getnframes())))
        extracted = [frame_bytes[i] & 1 for i in range(len(frame_bytes))]
        string = "".join(chr(int("".join(map(str,extracted[i:i+8])),2)) for i in range(0,len(extracted),8))
        msg = string.split("###")[0]
        print(msg)
        return msg
        waveaud
        #ex_msg("C:\\Users\\Public\\Documents\\AndroidChatApp\\Chat-App-with-Steganography\\BackEnd\\Demo.wav")


