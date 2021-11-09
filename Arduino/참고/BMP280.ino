
/*
* GY BMP 280 test sketch
*
*/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BMP280.h>

// using I2C
Adafruit_BMP280 bme;

void setup(){
  Serial.begin(9600);
  if (!bme.begin(0x76)){ // <========= 스캔해서 찾아낸 I2C 주소
    Serial.println("Could not find a valid BMP280 sensor, check wiring!"); 
    Serial.print("---- GY BMP 280 ----------------\n");
    Serial.println("--------------------------------\n\n");
  }
}

void loop(){
  
//  Serial.print("온도_(*C)");
//  Serial.print(':');
//  Serial.print(bme.readTemperature());
//  Serial.print('\t');
//  Serial.print("대기압_(bm)");
//  Serial.print(':');
//  Serial.print(bme.readPressure() / 100); // 100 Pa = 1 millibar
//  Serial.print('\t');
  Serial.print("고도");
  Serial.print(':');
  Serial.println(bme.readAltitude(1013.25));
  
  delay(500);
}
