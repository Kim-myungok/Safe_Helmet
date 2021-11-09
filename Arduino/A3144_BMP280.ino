
/*
* A3144 + GY BMP 280 test sketch
*
*/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BMP280.h>
#define SIZE 2

/*   BMP280 using I2C   */
Adafruit_BMP280 bme;
int BMP280_set;

/*   A3144 Setting   */
int A3144_HALL_A0 = A0;
int A3144_HALL_D0 = 7;

int min = 5000;
int max = 0;
int A3144_ave[2];
int i = 0;
int A3144_set;
int cnt = 0;
int time = 0;


void setup(){z
  Serial.begin(9600);
  /*   I2C Connect Check   */
  if (!bme.begin(0x76)){ // <========= 스캔해서 찾아낸 I2C 주소
    Serial.println("Could not find a valid BMP280 sensor, check wiring!"); 
  }
  
  /*   A3144 Pin Mode Setting   */
  pinMode(A3144_HALL_A0, INPUT);
  pinMode(A3144_HALL_D0, INPUT);
  A3144_set = analogRead(A3144_HALL_A0) * (5.0 / 1024.0) * 1000;
  /*   BMP280 Setting   */
  BMP280_set = (int)bme.readAltitude(1013.25);
  /*   Init Serial Print   */
  Serial.println("=============== Start ===============");
}

void loop(){
  /*   A3144__아날로그 값을 전압0~5000mV 변환   */
  int A3144_A_val = analogRead(A3144_HALL_A0) * (5.0 / 1024.0) * 1000 - A3144_set;
  if(min > A3144_A_val){
    min = A3144_A_val;
  }
  if(max < A3144_A_val){
    max = A3144_A_val;
  }
  A3144_ave[i] = A3144_A_val;
  if(A3144_ave[i] > A3144_ave[0]) cnt++;
  
  int A3144_D_val = digitalRead(A3144_HALL_D0);
  if( A3144_D_val == HIGH ){
    Serial.print("HIGH");
  } else { Serial.print("LOW"); }
  Serial.print(':');
  Serial.print(A3144_D_val);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("A_Val");
  Serial.print(':');
  Serial.print(A3144_A_val);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("A3144_AVE");
  Serial.print(':');
  Serial.print(A3144_ave[0]);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("CNT");
  Serial.print(':');
  Serial.print(cnt);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("MIN");
  Serial.print(':');
  Serial.print(min);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("MAX");
  Serial.print(':');
  Serial.print(max);
  
  /*   BMP280   */
//  Serial.print("온도_(*C)");
//  Serial.print(':');
//  Serial.print(bme.readTemperature());
//  Serial.print('\t');
//  Serial.print("대기압_(bm)");
//  Serial.print(':');
//  Serial.print(bme.readPressure() / 100); // 100 Pa = 1 millibar
//  Serial.print('\t');
  Serial.print(',');
  Serial.print('\t');
  Serial.print("고도");
  Serial.print(':');
  Serial.println((int)bme.readAltitude(1013.25) - BMP280_set);

  /*   A3144_ave[] ave_calc   */
  if (i==SIZE-1){
    for(int temp=1; temp<SIZE; temp++){
      A3144_ave[0] += A3144_ave[temp];
      if(temp==SIZE-1) A3144_ave[0] = A3144_ave[0]/SIZE;
    }
    i = 1;
  } else  i++;
  
  delay(100);
  time++;
  if (time == 10){
    cnt =0;
    time=0;
  }
}
