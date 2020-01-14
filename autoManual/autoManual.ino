#include <ArduinoRobot.h> // include the robot library
#include <Wire.h>
#include <I2C_Anything.h>

//TRIGGER et ECHO peuvent etre relies ensemble

#define TRIGGER_PIN_FRONT  D1
#define ECHO_PIN_FRONT     D1
#define TRIGGER_PIN_LEFT  D2
#define ECHO_PIN_LEFT     D2
#define TRIGGER_PIN_RIGHT  D0
#define ECHO_PIN_RIGHT     D0

#define pin_autoManuel D3
#define pin_cmd1 D4
#define pin_cmd2 D5

int speedcar = 128;
int rotateDeg = 90;
int command, maxDist = 20;
long distance,duration,FrontSensor;
float RightSensor,BackSensor,LeftSensor;

//by default is 0 = auto 

int test = 0;

void setup() {
  
  Robot.begin();
  Robot.beginSpeaker();
  Wire.begin();
  Serial.begin(9600);

  pinMode(pin_autoManuel, INPUT);
  pinMode(pin_cmd1, INPUT);
  pinMode(pin_cmd2, INPUT);

  //ECHO first then TRIGGER//
  pinMode(ECHO_PIN_FRONT, INPUT);
  pinMode(TRIGGER_PIN_FRONT, OUTPUT);

  //pinMode(ECHO_PIN_RIGHT, INPUT);
  //pinMode(TRIGGER_PIN_RIGHT, OUTPUT);
  //pinMode(ECHO_PIN_LEFT, INPUT);
  //pinMode(TRIGGER_PIN_LEFT, OUTPUT);
  
}

void loop() {
  if(digitalRead(pin_autoManuel) == 0)
  {
    /*Serial.println("Auto on");
    Robot.beep(BEEP_SIMPLE);
    delay(1000);
    Robot.beep(BEEP_DOUBLE);
    delay(1000);*/
    //Robot.beep(BEEP_LONG);
    //delay(1000);
    if(digitalRead(pin_cmd1) == 0 && digitalRead(pin_cmd2) == 0)
    {
      //stopRobot();
      Robot.beep(BEEP_LONG);
      delay(1000);
    }
    else if(digitalRead(pin_cmd1) == 0 && digitalRead(pin_cmd2) == 1)
    {
      //goForward();
      Robot.beep(BEEP_LONG);
      delay(3000);
    }
    else if(digitalRead(pin_cmd1) == 1 && digitalRead(pin_cmd2) == 0)
    {
      //rotateRight();
      Robot.beep(BEEP_LONG);
      delay(5000);
    }
    else if(digitalRead(pin_cmd1) == 1 && digitalRead(pin_cmd2) == 1)
    {
      //rotateLeft();
      Robot.beep(BEEP_DOUBLE);
      delay(7000);
    }
  }
  else
  {
    Robot.beep(BEEP_SIMPLE);
    delay(1000);
    Robot.beep(BEEP_SIMPLE);
    delay(1000);
    
    //RightSensor=SonarSensor(TRIGGER_PIN_RIGHT, ECHO_PIN_RIGHT);
    //RightSensor = distance;
    //LeftSensor=SonarSensor(TRIGGER_PIN_LEFT, ECHO_PIN_LEFT);
    //LeftSensor = distance;
    SonarSensor(TRIGGER_PIN_FRONT, ECHO_PIN_FRONT);
    FrontSensor = distance;
    Serial.print(FrontSensor);

    /*if(FrontSensor<=maxDist && LeftSensor<=maxDist)
    {
      Serial.println("Turn right");
      rotateRight();
    }
    else if(FrontSensor<=maxDist && RightSensor<=maxDist)
    {
      Serial.println("Turn left");
      rotateLeft();
    }
    else
    {
      Serial.println("Forward");
      goForward();
    }
    delay(5);*/
    if(FrontSensor <= maxDist)
    {
      Serial.println("Turn right"); 
      //rotateRight();
      delay(5);
    }
    else
    {
      Serial.println("Forward");
      //goForward();
    }
  }
}

void goForward(){
  Robot.motorsWrite(speedcar, speedcar);
}

void goBackward(){
  Robot.motorsWrite(-speedcar, -speedcar);
}

void rotateLeft(){
  Robot.turn(rotateDeg);
}

void rotateRight(){
  Robot.turn(rotateDeg);
}

void stopRobot(){
  Robot.motorsWrite(0,0);
}

void SonarSensor(int trigPin,int echoPin)
{
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = duration/58.2;
  delay(250); 
}
