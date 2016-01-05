#include <Servo.h>

#define FIRMWARE_VERSION 1 //Firmware version

#define FEEDER_SERVO_PIN  9

#define START_POSITION  0
#define MID_POSITION  70
#define END_POSITION  150

#define LONG_DELAY  1000
#define SHORT_DELAY 150

#define STATUS_STARTING  0;
#define STATUS_NORMAL  1;
#define STATUS_FEEDING 2;

#define DEFAULT_FEED_TIMES 2
#define DEFAULT_FEED_INTERVAL 28800; //seconds

int feedInterval = DEFAULT_FEED_INTERVAL;
unsigned long lastFeedTime = 0;
int lastFeedTimes = DEFAULT_FEED_TIMES;

int status = STATUS_STARTING;

//Commands
String VERSION = "VE";
String FEED = "FE";
String FEED_INTERVAL = "FI";
String STATUS = "ST";

boolean cmdReceived = false; //Indicates command available to parse and process
String inputString = ""; //String received from serial line

Servo feederServo;

void setup() 
{ 
    // initialize serial:
  Serial.begin(9600);
  feederServo.attach(FEEDER_SERVO_PIN);
    
  //Put feeder in initial state
  moveTo(START_POSITION,LONG_DELAY);
  
  //First feeding
  feed(DEFAULT_FEED_TIMES);
} 

void loop() 
{
  if (cmdReceived) {
    
    if (inputString.startsWith(FEED)) {
      int times = DEFAULT_FEED_TIMES;
      if(inputString.length() >= FEED.length() + 1) {
        inputString.replace(FEED,"");
        times = inputString.toInt();
      }            
      feed(times);
    } else if (inputString.startsWith(FEED_INTERVAL) && inputString.length() >= FEED.length() + 5) {
      inputString.replace(FEED_INTERVAL,"");
      feedInterval = inputString.toInt();
      Serial.print(FEED_INTERVAL);
      Serial.println(feedInterval);
    } else if (inputString.startsWith(VERSION)) {
      Serial.print(VERSION);
      Serial.println(FIRMWARE_VERSION);
    } else if (inputString.startsWith(STATUS)) {
      sendStatus();
    } else {
      Serial.println("ER0");
    }
    
    inputString = "";
    cmdReceived = false;
  }
 
  if((millis()-lastFeedTime)/1000>=feedInterval) {
    feed(DEFAULT_FEED_TIMES);
  }
}
/*
   Servo movement control
*/
void moveTo(int position, int delay_time) {
  feederServo.write(position);
  delay(delay_time);
}
/*
   Feeding operation cycle
*/
void feed(int times) {
  lastFeedTime = millis();
  lastFeedTimes = times;
  status = STATUS_FEEDING;
  sendStatus();  
  for(int t = 0;t<times;t++) 
  {
      moveTo(END_POSITION,LONG_DELAY);
      for(int pos = END_POSITION;pos>MID_POSITION;pos--) {
         moveTo(pos,SHORT_DELAY);
      }
      moveTo(START_POSITION,LONG_DELAY);
  }
  status = STATUS_NORMAL;
}
/*
    Status command response
*/
void sendStatus() {
  Serial.print(STATUS);
  Serial.print(status);
  Serial.print(";");
  Serial.print(FEED_INTERVAL);
  Serial.print(feedInterval);
  Serial.print(";");
  Serial.print("LF");
  Serial.println(lastFeedTime);
}
/*
Process incomming serial data
*/
void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read(); 
    if (inChar == '\n') {
      cmdReceived = true;
    }else{
      inputString += inChar;
      inputString.toUpperCase();
    }
  }
}
