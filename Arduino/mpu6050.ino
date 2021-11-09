
/*
* A3144 + GY BMP 280 + MPU6050 test sketch
*
*/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#define SIZE 2

/*   MPU6050   */
Adafruit_MPU6050 mpu;

void setup(){
  Serial.begin(9600);
  /*   MPU6050 Setting   */
  if (!mpu.begin()) {
    Serial.println("Sensor init failed");
    while (1)
      yield();
  }
  Serial.println("Found a MPU-6050 sensor");
  /*   Init Serial Print   */
  Serial.println("=============== Start ===============");
}

void loop(){
  /*   MPU6050   */
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  //Serial.print("Accelerometer ");
  Serial.print("X_(m/s^2)");
  Serial.print(':');
  Serial.print(a.acceleration.x, 1);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("Y_(m/s^2)");
  Serial.print(':');
  Serial.print(a.acceleration.y, 1);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("Z_(m/s^2)");
  Serial.print(':');
  Serial.println(a.acceleration.z, 1);
}
