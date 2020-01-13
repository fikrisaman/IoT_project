import serial
import time
import os
import paho.mqtt.client as mqtt
import threading
import sys
import base64
import datetime
import json
import numpy
import math


manuel = 0
flash = 0
move = 0


def convetButane(ratioAlcool):
    butane = 10**((math.log10(ratioAlcool)-1.9149)/(-0.8485))
    if butane >= 100 and butane <= 10000:
        return butane
    else:
        return -1

def convertHydrogen(ratioAlcool):
    hydrogen = 10**((math.log10(ratioAlcool)-0.2376)/(-0.3691))
    if hydrogen >= 20 and hydrogen <= 10000:
        return hydrogen
    else:
        return -1

def convertEthanol(ratioAlcool):
    ethanol = 10**((math.log10(ratioAlcool)-1.153)/(-0.3954))
    if ethanol >= 20 and ethanol <= 10000:
        return ethanol
    else:
        return -1

def convertLPG(ratioGaz):
    LPG = 10**((math.log10(ratioGaz)-1.3083)/(-0.4684))
    if LPG >= 100 and LPG <= 10000:
        return LPG
    else:
        return -1

def convertCO(ratioGaz):
    CO = 10**((math.log10(ratioGaz)-1.4355)/(-0.3088))
    if CO >= 100 and CO <= 10000:
        return CO
    else:
        return -1

def convertSmoke(ratioGaz):
    Smoke = 10**((math.log10(ratioGaz)-1.3038)/(-0.3422))
    if Smoke >= 100 and Smoke <= 10000:
        return Smoke
    else:
        return -1

def on_message_msgs(client, userdata, message):
    global move, flash, manuel
    print("message received ", str(message.payload.decode("utf-8")))
    msg = str(message.payload.decode("utf-8")).split(" ")
    if msg[0] == "manuel":
        manuel = int(msg[1])
    if msg[0] == "flash":
        flash = int(msg[1])
    if msg[0] == "move":
        move = int(msg[1])
    if manuel == 0:
        move=0

    command = 8*flash + 4*manuel + move
    ser.write(str(command).encode())


mqttc = mqtt.Client("client1", clean_session=False)
mqttc.username_pw_set("jvjdxvzg", "wA51fzMgsqJ8")

mqttc.message_callback_add("robot/+", on_message_msgs)
mqttc.connect("farmer.cloudmqtt.com", 15408)

ser = serial.Serial()
ser.baudrate = 115200
ser.port = '/dev/cu.usbmodem141201'
ser.timeout = 2

mqttc.loop_start()
ser.open()
while(1):
    imageString = ''
    msg = ''
    command = '1'
    ser.write(command.encode())
    time.sleep(0.5)
    while ser.inWaiting():
        imageString = imageString + ser.read()

    imageString = base64.b64encode(imageString)
    command = '2'
    ser.write(command.encode())
    time.sleep(0.2)
    while ser.inWaiting():
        msg = msg + ser.read()
    msg = msg.split(' ')
    payload = {
        "time": datetime.datetime.now().strftime("%H:%M:%S"),
        "butane": convetButane(float(msg[3])) if len(msg) ==4 and  msg[3] != 'inf' and float(msg[3]) > 0.0 else 0.0,
        "lgp": convertLPG(float(msg[1])) if len(msg) ==4 and  msg[1] != 'inf' and float(msg[1]) > 0.0 else 0.0,
        "ethanol": convertEthanol(float(msg[3])) if len(msg) ==4 and   msg[3] != 'inf' and float(msg[3]) > 0.0 else 0.0,
        "smoke": convertSmoke(float(msg[1])) if len(msg) ==4 and  msg[1] != 'inf' and float(msg[1]) > 0.0 else 0.0,
        "co": convertCO(float(msg[1])) if len(msg) ==4 and  msg[1] != 'inf' and float(msg[1]) > 0.0 else 0.0,
        "hydrogene": convertHydrogen(float(msg[3])) if len(msg) ==4 and  msg[3] != 'inf' and float(msg[3]) > 0.0 else 0.0,
        "image": imageString
    }
    payload = json.dumps(payload)
    mqttc.publish("sensor/temp", payload=payload, qos=0,retain=True)
    mqttc.subscribe("robot/+", qos=0)

