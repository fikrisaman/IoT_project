#include <ArduinoRobot.h>
#include <Wire.h>

#define TRIGGER_PIN  D0  // Arduino pin tied to trigger pin on the ultrasonic sensor.
#define ECHO_PIN     D1  // Arduino pin tied to echo pin on the ultrasonic sensor.

void setup() {
  // put your setup code here, to run once:
  Robot.begin();
  Serial.begin(9600);

  pinMode(ECHO_PIN, INPUT);
  pinMode(TRIGGER_PIN, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:

  digitalWrite(TRIGGER_PIN, LOW); // Set the trigger pin to low for 2uS
  delayMicroseconds(2);
  digitalWrite(TRIGGER_PIN, HIGH); // Send a 10uS high to trigger ranging
  delayMicroseconds(10);
  digitalWrite(TRIGGER_PIN, LOW); // Send pin low again
  int distance = pulseIn(ECHO_PIN, HIGH); // Read in times pulse
  distance= distance/58; // Calculate distance from time of pulse 
  //Distance (in cm) = (elapsed time * sound velocity (340 m/s)) / 100 / 2

  Serial.print("Ping: ");
  Serial.print(distance);
  Serial.println("cm");
  delay(200);

}
