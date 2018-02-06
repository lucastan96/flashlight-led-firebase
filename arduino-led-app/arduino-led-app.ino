#include <ESP8266WiFi.h>
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h> 
#include <FirebaseArduino.h>

#define AP_SSID "Arduino"
#define AP_PASSWORD "arduinowifi"
#define FIREBASE_HOST "PLACEHOLDER"
#define FIREBASE_AUTH "PLACEHOLDER"
#define LED_PIN 16

void setup() {
  pinMode(LED_PIN, OUTPUT);

  Serial.begin(115200);
  Serial.println();

  WiFiManager wifiManager;
  wifiManager.resetSettings();
  wifiManager.autoConnect(AP_SSID, AP_PASSWORD);

  Serial.println("Connected to WiFi :)");
  
//  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
//  Serial.print("Connecting to WiFi");
//  while (WiFi.status() != WL_CONNECTED) {
//    Serial.print(".");
//    delay(500);
//  }
//  Serial.println();
//  Serial.print("Connected to Wifi: ");
//  Serial.println(WiFi.localIP());
  
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

int toggle = 0;

void loop() {
  FirebaseObject object = Firebase.get("flashlight");
  toggle = object.getInt("toggle");
  
  if (toggle == 0) {
    digitalWrite(LED_PIN, LOW);
  } else {
    digitalWrite(LED_PIN, HIGH);
  }
    
  Serial.println(toggle);
  delay(1000);
}
