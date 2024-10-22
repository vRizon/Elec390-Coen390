#include <Arduino.h>
#include<WiFi.h>
#include<FirebaseClient.h>
#include <WiFiClientSecure.h>

#define WIFI_SSID "Your Wifi SSID"
#define WIFI_PASSWORD "Your Wifi pass"
#define API_KEY "AIzaSyCW6EV_qD-BusrijT5aButGagZm-WNaZLI"
#define DATABASE_URL "https://esp32-demo-d3ba3-default-rtdb.firebaseio.com/"

WiFiClientSecure ssl;
DefaultNetwork network;
AsyncClientClass client(ssl, getNetwork(network));
FirebaseApp app;
RealtimeDatabase Database;
AsyncResult result;
NoAuth noAuth;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial.print("Connecting to Wi-Fi...");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while(WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with the IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  // Set up Firebase client
  ssl.setInsecure();
  initializeApp(client, app, getAuth(noAuth));
  app.getApp<RealtimeDatabase>(Database);
  Database.url(DATABASE_URL);
  client.setAsyncResult(result);

  // Set an integer value in Firebase
  if (Database.set<int>(client, "/test/int", 123)) {
    Serial.println("Data sent successfully");
  } else {
    Serial.printf("Failed to send data. Error: %s\n", client.lastError().message().c_str());
  }

  // Retrieve the value
  int value = Database.get<int>(client, "/test/int");
  if (client.lastError().code() == 0) {
    Serial.printf("Value retrieved: %d\n", value);
  } else {
    Serial.printf("Failed to retrieve data. Error: %s\n", client.lastError().message().c_str());
  }
}

void loop() {
  // put your main code here, to run repeatedly:
  
}
