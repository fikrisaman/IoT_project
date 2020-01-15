#include "esp_camera.h"
#include "Arduino.h"
#include "soc/soc.h"           // Disable brownour problems
#include "soc/rtc_cntl_reg.h"  // Disable brownour problems
#include "driver/rtc_io.h"
#include <Wire.h>

 
RTC_DATA_ATTR int bootCount = 0;

// Pin definition for CAMERA_MODEL_AI_THINKER
#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27
#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22
 

#define uS_TO_S_FACTOR 1000000
#define heaterSelPin 16 
#define AL_PIN 14
#define MQ_PIN 2                               //define which analog input channel you are going to use
#define RL_VALUE 5                                     //define the load resistance on the board, in kilo ohms
#define RO_CLEAN_AIR_FACTOR 9.83                    //RO_CLEAR_AIR_FACTOR=(Sensor resistance in clean air)/RO,
#define Ro 9.23                                                 
#define RS_air 1.24 
/***********************Software Related Macros************************************/
#define CALIBARAION_SAMPLE_TIMES 50                    //define how many samples you are going to take in the calibration phase
#define CALIBRATION_SAMPLE_INTERVAL 500                //define the time interal(in milisecond) between each samples in the
                                                    //cablibration phase
#define READ_SAMPLE_INTERVAL 50                        //define how many samples you are going to take in normal operation
#define READ_SAMPLE_TIMES 5                            //define the time interal(in milisecond) between each samples in 
                                                    //normal operation
                                                 
char buffer[100];
camera_fb_t * fb;
void convertDecToBin(char Dec, boolean  Bin[]);
  
void setup() {
  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector
  Serial.begin(115200);

  Serial.setDebugOutput(true);
  analogReadResolution(12);
  PIN_FUNC_SELECT(GPIO_PIN_MUX_REG[12], PIN_FUNC_GPIO);
  PIN_FUNC_SELECT(GPIO_PIN_MUX_REG[13], PIN_FUNC_GPIO);
  PIN_FUNC_SELECT(GPIO_PIN_MUX_REG[15], PIN_FUNC_GPIO);
  pinMode(4, OUTPUT);   // Turns off the ESP32-CAM white on-board LED (flash) connected to GPIO 4
  digitalWrite(4, LOW);   // sets the LED on      
  pinMode(15, OUTPUT);
  digitalWrite(15, LOW);   // sets the LED on      
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);   // sets the LED on     
  pinMode(12, OUTPUT);
  digitalWrite(12, LOW);   // sets the LED on  
  pinMode(heaterSelPin,OUTPUT);   // set the heaterSelPin as digital output.
  digitalWrite(heaterSelPin,LOW); // Start to heat the sensor

  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
 
  if(psramFound()){
    config.frame_size = FRAMESIZE_QVGA; // FRAMESIZE_ + QVGA|CIF|VGA|SVGA|XGA|SXGA|UXGA
    config.jpeg_quality = 10;
    config.fb_count = 2;
  } else {
    config.frame_size = FRAMESIZE_QVGA;
    config.jpeg_quality = 12;
    config.fb_count = 1;
  }
 
  // Init Camera
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }
} 
 
void loop() {
    if (Serial.available()) {
      char value = (char)Serial.read() - '0'; 
      if (value == 1){    
            fb = NULL;
            fb = esp_camera_fb_get();  
            if(!fb) {
              Serial.println("Camera capture failed");
               return;
            }
            Serial.write(fb->buf , fb->len); // payload (image), payload length
            esp_camera_fb_return(fb);
      } 
      else if (value == 2){
          float ratioGaz = 0.0;
          float ratioAlcool = 0.0;
          ratioGaz = GazRead();
          ratioAlcool =  AlcoolRead();
          snprintf(buffer,sizeof(buffer),"Gaz: %f Alcool: %f",ratioGaz,ratioAlcool);
          Serial.write(buffer);
      }
      else if (value == 3){
        //stop        
        digitalWrite(15,1);
        digitalWrite(13,0);
        digitalWrite(12,0);
      }
      else if (value == 4){
        //forward
        digitalWrite(15,1);
        digitalWrite(13,0);
        digitalWrite(12,1);
      }
      else if (value == 5){
        //right
        digitalWrite(15,1);
        digitalWrite(13,1);
        digitalWrite(12,0);
      }
      else if (value == 6){
        //left
        digitalWrite(15,1);
        digitalWrite(13,1);
        digitalWrite(12,1);
      }
      else if (value == 7 || value == 0){
        digitalWrite(4,0);
        digitalWrite(15,0);
        digitalWrite(13,0);
        digitalWrite(12,0);        
      }
      else if (value == 8){
        //turn on flash
        digitalWrite(4,0);
      }
      else if (value == 9){
        //turn off flash
        digitalWrite(4,1);
      }
      else if(value == 0 or value > 2 and value < 16){
        boolean  Bin[] = {0,0,0,0}; 
        convertDecToBin(value,Bin);
        digitalWrite(4,  Bin[0]);
        digitalWrite(15, Bin[1]);
        digitalWrite(13, Bin[2]);
        digitalWrite(12,Bin[3]);
      }
   }
}


//Conversion d'un nombre décimal de 0 à 16 en mot binaire de 4 bits
// MSB : Flash, MSB - 1 : Auto/Manuel , LSB+1-LSB : Direction
void convertDecToBin(char Dec, boolean  Bin[]) {
  for(int i = 3 ; i >= 0 ; i--) {
    if(pow(2, i)<=Dec) {
      Dec = Dec - pow(2, i);
      Bin[4-(i+1)] = 1;
    } else {
    }
  }
}

float analogReadPin(int pin){
    float sensor_volt;
    int sensorValue = analogRead(pin);
    sensor_volt=(float)sensorValue/4095*5.0;
    return sensor_volt;
}


float AlcoolRead()
{
    float RS_gas; // Get value of RS in a GAS
    float ratio; // Get ratio RS_GAS/RS_air
    float sensor_volt=analogReadPin(AL_PIN);
    RS_gas = sensor_volt/(5.0-sensor_volt); // omit *R16
    ratio = RS_gas/RS_air;                                //according to the chart in the datasheet 
    return ratio;
}

float GazRead()
{
    float RS_gas; // Get value of RS in a GAS
    float ratio; // Get ratio RS_GAS/RS_air
    float sensor_volt = analogReadPin(MQ_PIN);
    Serial.print(sensor_volt);
    RS_gas = (5.0-sensor_volt)/sensor_volt; // omit * RL
    ratio = RS_gas/Ro;  // ratio = RS/R0
    return ratio;  
}
