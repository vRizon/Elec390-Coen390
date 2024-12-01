/*
This code is for the Sensors ESP32, which captures temperature and distance 
readings and sends it to the Firebase. Functionality is inspired by Rui Santos, 
from their Random Nerd Tutorials website, with their copyright message below.
*/

/*********
  Rui Santos
  Complete instructions at: https://RandomNerdTutorials.com/esp32-cam-save-picture-firebase-storage/
  
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files.
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

  Based on the example provided by the ESP Firebase Client Library
*********/




#include <Arduino.h>
#include <WiFi.h>
#include <FirebaseClient.h>
#include <WiFiClientSecure.h>
#include <Wire.h>
#include <Adafruit_MLX90614.h>

#define TRIG_PIN 13
#define ECHO_PIN 12
#define BUTTON_PIN 33

#define SOUND_SPEED 0.034
#define CM_TO_INCH 0.393701

#define WIFI_SSID "KPNetwork"
#define WIFI_PASSWORD "beetstew"
#define API_KEY "AIzaSyCW6EV_qD-BusrijT5aButGagZm-WNaZLI"
#define DATABASE_URL "https://esp32-demo-d3ba3-default-rtdb.firebaseio.com/"

// Initialize the Firebase section
WiFiClientSecure ssl;
DefaultNetwork network;
AsyncClientClass client(ssl, getNetwork(network));
FirebaseApp app;
RealtimeDatabase Database;
AsyncResult result;
NoAuth noAuth;

// Initialize the sensor variables and temperature sensor object
Adafruit_MLX90614 mlx = Adafruit_MLX90614();
long duration;
float distanceCm, temp;
int buttonState;
int prevState = HIGH;  // = buttonState;
bool send = false;

// Create the path Strings for Firebase 
String path = "/profiles/4t34RojIIuNPeJ79j1OKWZJ75EJ2";
String TempPath = path + "/Temperature/";
String buttonpath = path + "/Button/";
String DistPath = path + "/Distance/";


void setup() {
  Serial.begin(115200);              
  pinMode(TRIG_PIN, OUTPUT);          
  pinMode(ECHO_PIN, INPUT);           
  pinMode(BUTTON_PIN, INPUT_PULLUP);  // Set the Button Pin as input pullup
  
  mlx.begin();

  Serial.print("Connecting to Wi-Fi...");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }

  // Set up Firebase client
  ssl.setInsecure();
  initializeApp(client, app, getAuth(noAuth));
  app.getApp<RealtimeDatabase>(Database);
  Database.url(DATABASE_URL);
  client.setAsyncResult(result);
}

void loop() {

  buttonState = digitalRead(BUTTON_PIN);

  Serial.println(buttonState); // debugging, 1 means button is open
  if (buttonState == LOW) {  // LOW is button pressed

    Database.set<bool>(client, buttonpath, true); // sends the state of the button to the Firebase 

    // Clear the trig Pin
    digitalWrite(TRIG_PIN, LOW);
    delayMicroseconds(2);

    // Pulse the trig Pin for 10ms
    digitalWrite(TRIG_PIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIG_PIN, LOW);

    // Reads the echo Pin, returns the sound wave travel time in ms
    duration = pulseIn(ECHO_PIN, HIGH);

    // Calculate the distance
    distanceCm = duration * SOUND_SPEED / 2;

    Serial.print("Distance (cm): ");
    Serial.println(distanceCm); // for debugging

    temp = mlx.readObjectTempC();
    Serial.print("*C\tObject = ");
    Serial.print(temp);
    Serial.println("*C");

    // Set a float value for temp in Firebase
    if (Database.set<float>(client, TempPath, temp)) {
      Serial.println("Data sent successfully");
    } else {
      Serial.printf("Failed to send data. Error: %s\n", client.lastError().message().c_str());
    }

    delay(10);

    // Set a float value for distance to Firebase
    if (Database.set<float>(client, DistPath, distanceCm)) {
      Serial.println("Data sent successfully");
    } else {
      Serial.printf("Failed to send data. Error: %s\n", client.lastError().message().c_str());
    }

    Serial.println();
    delay(350);

  } else {
    // Set the button state to false, send to Firebase and restart the ESP
    // Restarting because the ESP32 occasionally freezes, liekly due to internal timer issues
    Database.set<bool>(client, buttonpath, false);
    if ((prevState == LOW) && (buttonState == HIGH)) {  // LOW is button pressed
      Serial.println("IM A RESTARTING");
      ESP.restart();
    }
  }

  prevState = buttonState;  // HIGH is open
  delay(10);
}
