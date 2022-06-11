#include <ArduinoBearSSL.h>
#include <ArduinoECCX08.h>
#include <ArduinoMqttClient.h>
#include <WiFiNINA.h> // change to #include <WiFi101.h> for MKR1000
#include <Servo.h>
#include <TinyGPS.h>


#include <Adafruit_Fingerprint.h>//손꾸락


#include "arduino_secrets.h"



#include <ArduinoJson.h>
/////// Enter your sensitive data in arduino_secrets.h
const char ssid[]        = SECRET_SSID;
const char pass[]        = SECRET_PASS;
const char broker[]      = SECRET_BROKER;
const char* certificate  = SECRET_CERTIFICATE;

WiFiClient    wifiClient;            // Used for the TCP socket connection
BearSSLClient sslClient(wifiClient); // Used for SSL/TLS connection, integrates with ECC508
MqttClient    mqttClient(sslClient);

unsigned long lastMillis = 0;


#include "HX711.h" //HX711로드셀 엠프 관련함수 호출
#define calibration_factor -7050.0 // 로드셀 스케일 값 선언
#define DOUT 4 //엠프 데이터 아웃 핀 넘버 선언
#define CLK 5  //엠프 클락 핀 넘버 
#define BT 7 //버튼

HX711 scale(DOUT, CLK); //엠프 핀 선언
int getFingerprintIDez();
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&Serial2);//지문

Servo servo;
int OFF = 0;
int ON =90;

TinyGPS gps;

  
void setup() {
  Serial.begin(9600);
   // attaches the servo on pin 9 to the servo object  
  finger.begin(9600);
  servo.attach(3); 
   servo.write(0);
   pinMode(BT,INPUT);
     //weight sensor scale 설정&지정
  scale.set_scale(calibration_factor);  
  scale.tare();
  
  while (!Serial);

  if (!ECCX08.begin()) {
    Serial.println("No ECCX08 present!");
    while (1);
  }

  // Set a callback to get the current time
  // used to validate the servers certificate
  ArduinoBearSSL.onGetTime(getTime);

  // Set the ECCX08 slot to use for the private key
  // and the accompanying public certificate for it
  sslClient.setEccSlot(0, certificate);

  // Optional, set the client id used for MQTT,
  // each device that is connected to the broker
  // must have a unique client id. The MQTTClient will generate
  // a client id for you based on the millis() value if not set
  //
  // mqttClient.setId("clientId");

  // Set the message callback, this function is
  // called when the MQTTClient receives a message
  mqttClient.onMessage(onMessageReceived);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  if (!mqttClient.connected()) {
    // MQTT client is disconnected, connect
    connectMQTT();
  }

  // poll for new MQTT messages and send keep alives
  mqttClient.poll();

  // publish a message roughly every 5 seconds.
  if (millis() - lastMillis > 5000) {
    lastMillis = millis();
    char payload[512];
    getDeviceStatus(payload);
    sendMessage(payload);
  }
}

unsigned long getTime() {
  // get the current time from the WiFi module  
  return WiFi.getTime();
}

void connectWiFi() {
  Serial.print("Attempting to connect to SSID: ");
  Serial.print(ssid);
  Serial.print(" ");

  while (WiFi.begin(ssid, pass) != WL_CONNECTED) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the network");
  Serial.println();
}

void connectMQTT() {
  Serial.print("Attempting to MQTT broker: ");
  Serial.print(broker);
  Serial.println(" ");

  while (!mqttClient.connect(broker, 8883)) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the MQTT broker");
  Serial.println();

  // subscribe to a topic
  mqttClient.subscribe("$aws/things/Samul/shadow/update/delta");
}
//디바이스 정보 저장.
void getDeviceStatus(char* payload) {
  // Read temperature as Celsius (the default)
  bool newData = false;
  float LAT,LON;
  //gps 가져오는 코드
  for (unsigned long start = millis(); millis() - start < 1000;)
  {
    while (Serial1.available())
    {
      char c = Serial1.read();
      // Serial.write(c); // uncomment this line if you want to see the GPS data flowing
      if (gps.encode(c)) // Did a new valid sentence come in?
        newData = true;
    }
  }
  if (newData)
  {//위도경도설정
    float flat, flon;
    unsigned long age;
    gps.f_get_position(&flat, &flon, &age);
    LAT =flat;
    LON =flon;
  }
  float t = -((scale.get_units()*200)-30); 
    if((servo.read()==0) && (digitalRead(BT) == HIGH))//서보모터가 0도이고 버튼이 눌리면 잠김
  {
    servo.write(90);
             }
       getFingerprintIDez();//지문확인 코드가 돌아감
      delay(50);

  const char* servo1 = (servo.read() == 90)? "ON" : "OFF";//서보모터 on,off 문자열로 만들어서 aws로 보냄
  // make payload for the device update topic ($aws/things/Samul/shadow/update)
  sprintf(payload,"{\"state\":{\"reported\":{\"WEIGHT\":\"%0.2f\",\"SERVO\":\"%s\",\"LAT\":\"%0.6f\",\"LON\":\"%0.6f\"}}}",t,servo1,LAT,LON);
  //sprintf(payload,"{\"state\":{\"reported\":{\"temperature\":\"%0.2f\",\"LED\":\"%s\"}}}",t,led);
}

void sendMessage(char* payload) {
  char TOPIC_NAME[]= "$aws/things/Samul/shadow/update";
  
  Serial.print("Publishing send message:");
  Serial.println(payload);
  mqttClient.beginMessage(TOPIC_NAME);
  mqttClient.print(payload);
  mqttClient.endMessage();
}


void onMessageReceived(int messageSize) {
  // we received a message, print out the topic and contents
  Serial.print("Received a message with topic '");
  Serial.print(mqttClient.messageTopic());
  Serial.print("', length ");
  Serial.print(messageSize);
  Serial.println(" bytes:");

  // store the message received to the buffer
  char buffer[512] ;
  int count=0;
  while (mqttClient.available()) {
     buffer[count++] = (char)mqttClient.read();
  }
  buffer[count]='\0'; // 버퍼의 마지막에 null 캐릭터 삽입
  Serial.println(buffer);
  Serial.println();

  // JSon 형식의 문자열인 buffer를 파싱하여 필요한 값을 얻어옴.
  // 디바이스가 구독한 토픽이 $aws/things/Sabo/shadow/update/delta 이므로,
  // JSon 문자열 형식은 다음과 같다.
  // {
  //    "version":391,
  //    "timestamp":1572784097,
  //    "state":{
  //        "LED":"ON"
  //    },
  //    "metadata":{
  //        "LED":{
  //          "timestamp":15727840
  //         }
  //    }
  // }
  //
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, buffer);
  JsonObject root = doc.as<JsonObject>();
  JsonObject state = root["state"];
  const char* servo2 = state["SERVO"];

  char payload[512];

  if (strcmp(servo2,"ON")==0) {
     servo.write(ON);
    sprintf(payload,"{\"state\":{\"desired\":{\"SERVO\":\"%s\"}}}","on");
    sendMessage(payload);
        
    
  } else if (strcmp(servo2,"OFF")==0) {
    servo.write(OFF);
    sprintf(payload,"{\"state\":{\"desired\":{\"SERVO\":\"%s\"}}}","off");
    sendMessage(payload);
  }
}
int getFingerprintIDez() {//지문인식코드
  uint8_t p = finger.getImage();
  if (p != FINGERPRINT_OK)  return -1;

  p = finger.image2Tz();
  if (p != FINGERPRINT_OK)  return -1;

  p = finger.fingerFastSearch();
  if (p != FINGERPRINT_OK)  return -1;
  
    myservo.write(0);              // tell servo to go to position in variable 'pos'
    delay(5000);                       // waits 15ms for the servo to reach the position
  
  return finger.fingerID; 
}
