#include <EEPROM.h> //Arduino library
#include <Servo.h>  //Arduino library

#define FIRMWARE_VERSION 3 //Firmware version

#define STATUS_LED_PIN  13
#define FEEDER_SERVO_PIN  9
#define LIGHT_SENSOR_PIN  2
#define MANUAL_FEED_PIN	12

#define START_POSITION  0
#define MID_POSITION  70
#define END_POSITION  150

#define LONG_DELAY  1000
#define SHORT_DELAY 150

#define STATUS_STARTING  0
#define STATUS_NORMAL  1
#define STATUS_FEEDING 2

#define FC_STARTING  0
#define FC_WAITING   1

#define DEFAULT_FEED_TIMES 2
#define DEFAULT_FEED_INTERVAL 28800 //seconds
#define DEFAULT_LIGHT_THRESHOLD  1000 //near of max value 1023
#define DEFAULT_FEED_ON_NIGHT  0 // 0: feeding cycle not allowed if light level if over threshold (night level)

//EEPROM CONTROL
#define  DATA_SUM			120
#define  MEM_ADDR_BASE			0
#define  MEM_ADDR_DT_SUM_0              MEM_ADDR_BASE //Indicates if user has recorded data into eeprom
#define  MEM_ADDR_DT_SUM_1		MEM_ADDR_BASE+1
#define  MEM_ADDR_FEEDER_SERVO_PIN	MEM_ADDR_BASE+2
#define  MEM_ADDR_START_POSITION      	MEM_ADDR_BASE+3
#define  MEM_ADDR_MID_POSITION          MEM_ADDR_BASE+4
#define  MEM_ADDR_END_POSITION          MEM_ADDR_BASE+5
#define  MEM_ADDR_LONG_DELAY            MEM_ADDR_BASE+6 // 2 bytes
#define  MEM_ADDR_SHORT_DELAY           MEM_ADDR_BASE+8 // 2 bytes
#define  MEM_ADDR_FEED_TIMES            MEM_ADDR_BASE+10
#define  MEM_ADDR_FEED_INTERVAL         MEM_ADDR_BASE+11 // 2 bytes
#define  MEM_ADDR_LIGHT_THRESHOLD       MEM_ADDR_BASE+13 // 2 bytes
#define  MEM_ADDR_FEED_ON_NIGHT         MEM_ADDR_BASE+15 // 1 byte
#define  MEM_ADDR_LIGHT_SENSOR_PIN      MEM_ADDR_BASE+16 // 1 byte

//Servo control variables
int feederServoPin = FEEDER_SERVO_PIN;
int startPosition = START_POSITION;
int midPosition = MID_POSITION;
int endPosition = END_POSITION;
unsigned int longDelay = LONG_DELAY;
unsigned int shortDelay = SHORT_DELAY;

//Feeding cycle control variables
unsigned int feedInterval = DEFAULT_FEED_INTERVAL;
unsigned int nextFeedingCycle = DEFAULT_FEED_INTERVAL;
unsigned long lastFeedTime = 0;
int feedTimes = DEFAULT_FEED_TIMES;
//Light sensor
int lightSensorPin = LIGHT_SENSOR_PIN;
unsigned int lightThreshold = DEFAULT_LIGHT_THRESHOLD;
int feedOnNight = DEFAULT_FEED_ON_NIGHT;

//Feeder status
int status = STATUS_STARTING;
int feedCycleMaxDuration = 0;
int feedCycleTime = 0;
int feedCycleState = FC_STARTING;
int feedCyclePosition = 0;
int feedCycleCount = 0;
int feedCycleDelay = 0;

//Commands
String VERSION = "VE";
String FEED = "FE";
String FEED_INTERVAL = "FI";
String FEED_TIMES = "FT";
String FEED_ON_NIGHT = "FN";
String STATUS = "ST";
String SAVE_DATA = "SD";
String RESET_DATA = "RD";
String SWITCH_STATUS_LIGHT="SS";
String CHANGE_SERVO_PIN = "CSPIN";
String CHANGE_LIGHT_SENSOR_PIN = "CLSPIN";
String CHANGE_LIGHT_THRESHOLD = "CLTHR";
String CHANGE_START_POSITION = "CSPOS";
String CHANGE_MID_POSITION = "CMPOS";
String CHANGE_END_POSITION = "CEPOS";
String CHANGE_LONG_DELAY = "CLDLY";
String CHANGE_SHORT_DELAY = "CSDLY";

boolean cmdReceived = false; //Indicates command available to parse and process
String inputString = ""; //String received from serial line

Servo feederServo;

void setup() 
{ 
    // initialize serial:
  Serial.begin(9600);

  //Status led
  pinMode(STATUS_LED_PIN, OUTPUT);

  //Force feeding cycle button
  pinMode(MANUAL_FEED_PIN, INPUT);

  if(isDataStored()) {
    //Load variables from eeprom
    loadDataFromEEPROM();
    nextFeedingCycle = feedInterval;
  } else {
    //Default values
    lightSensorPin = LIGHT_SENSOR_PIN;
    feederServoPin = FEEDER_SERVO_PIN;
    startPosition = START_POSITION;
    midPosition = MID_POSITION;
    endPosition = END_POSITION;
    longDelay = LONG_DELAY;
    shortDelay = SHORT_DELAY;
    feedInterval = DEFAULT_FEED_INTERVAL;
    nextFeedingCycle = DEFAULT_FEED_INTERVAL;
    lastFeedTime = 0;
    feedTimes = DEFAULT_FEED_TIMES;
    lightThreshold = DEFAULT_LIGHT_THRESHOLD;
    feedOnNight = DEFAULT_FEED_ON_NIGHT;    
    status = STATUS_STARTING;    
  }
  feederServo.attach(feederServoPin);
  feederServo.write(startPosition);
  delay(shortDelay);
} 

void loop() 
{  
  if(status == STATUS_STARTING) {
    startFeedingCycle();
  } else if (status == STATUS_FEEDING) {     
    status = feedCycle();    
  } else if (digitalRead(MANUAL_FEED_PIN) == LOW) {
    startFeedingCycle();
  } else {       
    if (cmdReceived) {    
      processCommand();
      inputString = "";
      cmdReceived = false;
    }    
    //Regular feeding cycle
    nextFeedingCycle = (millis()-lastFeedTime)/1000;
    if(nextFeedingCycle>=feedInterval) {
	if(analogRead(lightSensorPin) >= lightThreshold) {
            if(feedOnNight){
                startFeedingCycle();   
            }
        } else {
            startFeedingCycle();
        }
    }
  }

}

/*
 Non blocking feeding cycle
*/
int feedCycle() {
    int output = STATUS_FEEDING;    
      if(feedCycleState == FC_STARTING) {
        feederServo.write(feedCyclePosition);
        feedCycleDelay = feedCycleDelay+longDelay;
        feedCycleState = FC_WAITING;
      } else if((feedCycleState == FC_WAITING) && feedCycleTime>feedCycleDelay) {                        
         if (feedCyclePosition <= startPosition) {
          feedCycleCount++;
          if(feedCycleCount<feedTimes) {
            feedCyclePosition = endPosition;
            feedCycleState = FC_STARTING;
          } else {            
            output = STATUS_NORMAL;
            //Normal state - Status LED OFF
            digitalWrite(STATUS_LED_PIN,LOW);
          }          
        } else if(feedCyclePosition<=midPosition) {
          feedCyclePosition = startPosition;
          feedCycleDelay = feedCycleDelay+longDelay;
        } 
        else {
          feedCycleDelay = feedCycleDelay+shortDelay;
          feedCyclePosition--;
        }
        feederServo.write(feedCyclePosition);
      } else {
        if (cmdReceived) {    
          sendStatus(false);
          inputString = "";
          cmdReceived = false;
        } 
      }     
  feedCycleTime = millis() - lastFeedTime; 
  return output;
}
/*
  start feeding cycle
*/
void startFeedingCycle() {
  lastFeedTime = millis();
  nextFeedingCycle = 0;
  feedCycleTime = 0;
  feedCycleCount = 0;
  feedCycleState = FC_STARTING;
  feedCyclePosition = endPosition;
  feedCycleDelay = 0;  
  feedCycleMaxDuration = feedTimes*(longDelay+longDelay+shortDelay * (endPosition - midPosition));  
  status = STATUS_FEEDING;
  //Status LED ON
  digitalWrite(STATUS_LED_PIN,HIGH);
  sendStatus(false);
}
/*
  Command proccesor
*/
void processCommand() {
  //Parse and process received command
    if (inputString.startsWith(FEED)) {
        startFeedingCycle();
    } else if (inputString.startsWith(FEED_TIMES) && inputString.length() >= FEED_TIMES.length() + 1) {
        inputString.replace(FEED_TIMES,"");
        feedTimes = inputString.toInt();        
    } else if (inputString.startsWith(FEED_INTERVAL) && inputString.length() >= FEED.length() + 5) {
      inputString.replace(FEED_INTERVAL,"");
      feedInterval = inputString.toInt();
    } else if (inputString.startsWith(FEED_ON_NIGHT) && inputString.length() >= FEED_ON_NIGHT.length() + 1) {
        inputString.replace(FEED_ON_NIGHT,"");
        feedOnNight = inputString.toInt();        
    } else if (inputString.startsWith(VERSION)) {
      Serial.print(VERSION);
      Serial.println(FIRMWARE_VERSION);
    } else if (inputString.startsWith(STATUS)) {
      sendStatus(true);
    } else if (inputString.startsWith(SAVE_DATA)) {
      loadDataToEEPROM();
    } else if (inputString.startsWith(RESET_DATA)) {
      resetDataEEPROM();
    } else if (inputString.startsWith(CHANGE_SERVO_PIN) && inputString.length() >= CHANGE_SERVO_PIN.length() + 1) {
      inputString.replace(CHANGE_SERVO_PIN,"");
      feederServoPin = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_LIGHT_SENSOR_PIN) && inputString.length() >= CHANGE_LIGHT_SENSOR_PIN.length() + 1) {
      inputString.replace(CHANGE_LIGHT_SENSOR_PIN,"");
      lightSensorPin = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_LIGHT_THRESHOLD) && inputString.length() >= CHANGE_LIGHT_THRESHOLD.length() + 4) {
      inputString.replace(CHANGE_LIGHT_THRESHOLD,"");
      lightThreshold = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_START_POSITION) && inputString.length() >= CHANGE_START_POSITION.length() + 3) {
      inputString.replace(CHANGE_START_POSITION,"");
      startPosition = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_MID_POSITION) && inputString.length() >= CHANGE_MID_POSITION.length() + 3) {
      inputString.replace(CHANGE_MID_POSITION,"");
      midPosition = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_END_POSITION) && inputString.length() >= CHANGE_END_POSITION.length() + 3) {
      inputString.replace(CHANGE_END_POSITION,"");
      endPosition = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_LONG_DELAY) && inputString.length() >= CHANGE_LONG_DELAY.length() + 5) {
      inputString.replace(CHANGE_LONG_DELAY,"");
      longDelay = inputString.toInt();
    } else if (inputString.startsWith(CHANGE_SHORT_DELAY) && inputString.length() >= CHANGE_SHORT_DELAY.length() + 5) {
      inputString.replace(CHANGE_SHORT_DELAY,"");
      shortDelay = inputString.toInt();
    } else if (inputString.startsWith(SWITCH_STATUS_LIGHT)) {      
      if(digitalRead(STATUS_LED_PIN) == HIGH) {
          digitalWrite(STATUS_LED_PIN,LOW);  
      } else {
          digitalWrite(STATUS_LED_PIN,HIGH);
      }
    } else {
      //Return help
      help();
    }
}

/*
    Status command response
*/
void sendStatus(boolean isLong) {
  Serial.print(STATUS);
  Serial.print(status);
  
  if(status==STATUS_FEEDING) {
    Serial.print(";");    
    Serial.print((float)(millis() - lastFeedTime)/feedCycleMaxDuration);
  }
  if(isLong) {
      Serial.print(";");
      Serial.print(VERSION);
      Serial.print(FIRMWARE_VERSION);
      Serial.print(";");
      Serial.print(FEED_INTERVAL);
      Serial.print(feedInterval);
      Serial.print(";");
      Serial.print(FEED_TIMES);
      Serial.print(feedTimes); 
      Serial.print(";LF");
      Serial.print(lastFeedTime);
      Serial.print(";NF");
      Serial.print(nextFeedingCycle);
      Serial.print(";FN");
      Serial.print(feedOnNight);
      Serial.print(";LV");
      Serial.print(analogRead(lightSensorPin));      
      Serial.print(";SRV[");
      Serial.print(feederServoPin);  
      Serial.print("|");
      Serial.print(startPosition);  
      Serial.print("|");
      Serial.print(midPosition);  
      Serial.print("|");
      Serial.print(endPosition);  
      Serial.print("|");
      Serial.print(longDelay);  
      Serial.print("|");
      Serial.print(shortDelay);  
      Serial.print("]");
      Serial.print(";LSN[");
      Serial.print(lightSensorPin);
      Serial.print("|");
      Serial.print(lightThreshold);
      Serial.println("]");
  } else {
      Serial.println("");
  }

}
/*
    Help
*/
void help() {
  Serial.print("\nV");
  Serial.print(FIRMWARE_VERSION);
  Serial.println("----------------------------------------------------------------------------------");
  Serial.print(FEED);
  Serial.println(" Force to feeding cycle");
  Serial.print(FEED_TIMES);
  Serial.println("X Set feed times to new value");
  Serial.print(FEED_INTERVAL);
  Serial.println("XXXXX Set feed interval to new value");
  Serial.print(FEED_ON_NIGHT);
  Serial.println("X Allow feeding at night");
  Serial.print(SAVE_DATA);
  Serial.println(" Save all parameters into EEPROM");
  Serial.print(RESET_DATA);
  Serial.println(" Reset feeder to default values and delete EEPROM");
  Serial.print(STATUS);
  Serial.println(" Show feeder status (status,feed interval,feed times, last feed time, servo vars)");
  Serial.print(SWITCH_STATUS_LIGHT);
  Serial.println(" Turn on/off status light");  
  Serial.print(CHANGE_LIGHT_SENSOR_PIN);
  Serial.println("X Change light sensor pin");
  Serial.print(CHANGE_LIGHT_THRESHOLD);
  Serial.println("XXXX Change light sensor threshold");  
  Serial.print(CHANGE_SERVO_PIN);
  Serial.println("X Change servo pin");
  Serial.print(CHANGE_START_POSITION);
  Serial.println("XXX Change servo start position");
  Serial.print(CHANGE_MID_POSITION);
  Serial.println("XXX Change servo mid position");
  Serial.print(CHANGE_END_POSITION);
  Serial.println("XXX Change servo end position");
  Serial.print(CHANGE_LONG_DELAY);
  Serial.println("XXXXX Change servo long delay");
  Serial.print(CHANGE_SHORT_DELAY);
  Serial.println("XXXXX Change servo short delay");
  Serial.print(VERSION);
  Serial.println(" Show firmware version");
  Serial.println("----------------------------------------------------------------------------------");
}
////////EEPROM///////
bool isDataStored() {
    return ((EEPROM.read(MEM_ADDR_DT_SUM_0)+EEPROM.read(MEM_ADDR_DT_SUM_1)) == (DATA_SUM+DATA_SUM));
}
/*
  Load all parameters from EEPROM without validation
*/
void loadDataFromEEPROM() {
    feederServoPin = EEPROM.read(MEM_ADDR_FEEDER_SERVO_PIN);
    startPosition = EEPROM.read(MEM_ADDR_START_POSITION);
    midPosition = EEPROM.read(MEM_ADDR_MID_POSITION);
    endPosition = EEPROM.read(MEM_ADDR_END_POSITION);
    longDelay = EEPROMReadInt(MEM_ADDR_LONG_DELAY);
    shortDelay = EEPROMReadInt(MEM_ADDR_SHORT_DELAY);
    feedInterval = EEPROMReadInt(MEM_ADDR_FEED_INTERVAL);
    feedTimes = EEPROM.read(MEM_ADDR_FEED_TIMES);
    lightSensorPin = EEPROM.read(MEM_ADDR_LIGHT_SENSOR_PIN);
    lightThreshold = EEPROMReadInt(MEM_ADDR_LIGHT_THRESHOLD);
    feedOnNight = EEPROM.read(MEM_ADDR_FEED_ON_NIGHT);
}
/*
  Load data into EEPROM
*/
void loadDataToEEPROM() {
    EEPROM.write(MEM_ADDR_FEEDER_SERVO_PIN,feederServoPin);
    EEPROM.write(MEM_ADDR_START_POSITION,startPosition);
    EEPROM.write(MEM_ADDR_MID_POSITION,midPosition);
    EEPROM.write(MEM_ADDR_END_POSITION,endPosition);
    EEPROM.write(MEM_ADDR_FEED_TIMES,feedTimes);
    EEPROMWriteInt(MEM_ADDR_LONG_DELAY,longDelay);
    EEPROMWriteInt(MEM_ADDR_SHORT_DELAY,shortDelay);
    EEPROMWriteInt(MEM_ADDR_FEED_INTERVAL,feedInterval);
    EEPROM.write(MEM_ADDR_DT_SUM_0,DATA_SUM-10);
    EEPROM.write(MEM_ADDR_DT_SUM_1,DATA_SUM+10);
    EEPROMWriteInt(MEM_ADDR_LIGHT_THRESHOLD,lightThreshold);
    EEPROM.write(MEM_ADDR_LIGHT_SENSOR_PIN,lightSensorPin);
    EEPROM.write(MEM_ADDR_FEED_ON_NIGHT,feedOnNight);  
}
/*
   Reset dato from EEPROM and load default values
*/
void resetDataEEPROM() {
    //write indalid data sum combination to avoid was reload from EEPROM
    EEPROM.write(MEM_ADDR_DT_SUM_0,DATA_SUM);
    EEPROM.write(MEM_ADDR_DT_SUM_1,DATA_SUM+10);
    //call setup to force reload all data
    setup();
}

/*
   Writes 2 byte integer to EEPROM
*/
void EEPROMWriteInt(int p_address, int p_value) {
    byte lowByte = ((p_value >> 0) & 0xFF);
    byte highByte = ((p_value >> 8) & 0xFF);
    EEPROM.write(p_address, lowByte);
    EEPROM.write(p_address + 1, highByte);
}

/* 
  Read a 2 byte integer EEPROM
*/
unsigned int EEPROMReadInt(int p_address) {
    byte lowByte = EEPROM.read(p_address);
    byte highByte = EEPROM.read(p_address + 1);
    return ((lowByte << 0) & 0xFF) + ((highByte << 8) & 0xFF00);
}
/////////////////////

/*
Process incomming serial data
*/
void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read(); 
    if (inChar == '\n') {
      inputString.toUpperCase();
      cmdReceived = true;     
    }else{
      inputString += inChar;
    }
  }
}
