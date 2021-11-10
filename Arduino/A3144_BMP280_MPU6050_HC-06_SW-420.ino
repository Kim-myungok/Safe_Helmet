
/*
* A3144 + GY BMP 280 + MPU6050 + HC-06 test sketch
*
*/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <SoftwareSerial.h>
#define SIZE 2

/*   BMP280 using I2C   */
Adafruit_BMP280 bme;
int BMP280_set;   // BMP280의 고도를 현재위치에서 받는 값을 초기값으로 세팅

/*   A3144 Setting   */
int A3144_HALL_A0 = A0;       // A0
int A3144_HALL_D0 = 7;        // D7

int min = 5000;   // A3144 모듈의 최소값
int max = 0;      // A3144 모듈의 최대값
int A3144_ave[2]; // A3144_ave[0] = 평균값을 저장, A3144_ave[1] = 현재 A3144 값
int i = 0;        // A3144_ave[]의 index
int A3144_set;    // 실행시 처음 받는 값을 초기값으로 세팅
int cnt = 0;      // A3144 모듈의 값이 평균 이상일 때, cnt++
int time = 0;     // cnt 값을 약 1초마다 초기화 해주기 위해 선언

/*   MPU6050   */
Adafruit_MPU6050 mpu;
double MPU6050_x; // MPU6050 모듈의 X 방향 가속도
double MPU6050_y; // MPU6050 모듈의 Y 방향 가속도
double MPU6050_z; // MPU6050 모듈의 Z 방향 가속도

/*   HC-06   */
int bmp280_altitude;
int Tx = 6;       // 전송 보내는핀
int Rx = 5;       // 수신 받는핀
SoftwareSerial BtSerial(Tx,Rx);

/*   SW-420   */
int sw_420 = A1;




void setup(){
  Serial.begin(9600);
  /*   I2C Connect Check   */
  if (!bme.begin(0x76)){ // <========= 스캔해서 찾아낸 I2C 주소
    Serial.println("Not_find_BMP280_sensor..."); 
  }
  /*   A3144 Pin Mode Setting   */
  pinMode(A3144_HALL_A0, INPUT);
  pinMode(A3144_HALL_D0, INPUT);
  A3144_set = analogRead(A3144_HALL_A0) * (5.0 / 1024.0) * 1000;
  /*   BMP280 Setting   */
  BMP280_set = (int)bme.readAltitude(1013.25);
  /*   MPU6050 Setting   */
  if (!mpu.begin()) {
    Serial.println("Not_find_MPU6050_sensor...");
    while (1)
      yield();
  }
  /*   HC-06 Setting   */
  BtSerial.begin(9600);
  /*   SW-420   */
  pinMode(sw_420, INPUT);
  /*   Init Serial print   */
  Serial.println("######   Start   ######");
  BtSerial.println("/*   Start   */");
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
    BtSerial.print("HIGH: ");
  } else { Serial.print("LOW");
    BtSerial.print("LOW: "); }
  Serial.print(':');
  Serial.print(A3144_D_val);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("A_Val");
  Serial.print(':');\
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
  Serial.print(',');
  Serial.print('\t');
  Serial.print("고도");
  Serial.print(':');
  bmp280_altitude = (int)bme.readAltitude(1013.25) - BMP280_set;
  Serial.print(bmp280_altitude);
  
  /*   MPU6050   */
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);
  MPU6050_x = a.acceleration.x, 1;
  MPU6050_y = a.acceleration.y, 1;
  MPU6050_z = a.acceleration.z, 1;

  Serial.print(',');
  Serial.print('\t');
  Serial.print("X_(m/s^2)");
  Serial.print(':');
  Serial.print(MPU6050_x);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("Y_(m/s^2)");
  Serial.print(':');
  Serial.print(MPU6050_y);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("Z_(m/s^2)");
  Serial.print(':');
  Serial.print(MPU6050_z);

  /*   SW-420   */
  float val = analogRead(sw_420);
  float sw_420_val = val / 1024 * 10;
  Serial.print(',');
  Serial.print('\t');
  if(sw_420_val > 9){
    Serial.write("\tSHOCK!!\t");
    Serial.print("SW_420_val");
    Serial.print(':');
    Serial.println(sw_420_val);
  }
  else{
    Serial.write("NOT_SHOCK!!\t");
    Serial.print("SW_420_val");
    Serial.print(':');
    Serial.println(sw_420_val);
  }
  /*   HC-06   */
  BtSerial.print(A3144_D_val);
  BtSerial.print(",    A_Val: ");
  BtSerial.print(A3144_A_val);
  BtSerial.print(",    A3144_AVE: ");
  BtSerial.print(A3144_ave[0]);
  BtSerial.print(",    CNT: ");
  BtSerial.print(cnt);
  BtSerial.print(",    MIN: ");
  BtSerial.print(min);
  BtSerial.print(",    MAX: ");
  BtSerial.print(max);
  BtSerial.print(",    X_(m/s^2): ");
  BtSerial.print(MPU6050_x);
  BtSerial.print(",    Y_(m/s^2): ");
  BtSerial.print(MPU6050_y);
  BtSerial.print(",    Z_(m/s^2): ");
  BtSerial.print(MPU6050_z);
  BtSerial.print(",    SW_420_val: ");
  if(sw_420_val > 9){
    BtSerial.print("SHOCK!!: ");
    BtSerial.print("SW_420_val: ");
    BtSerial.println(sw_420_val);
  }
  else{
    BtSerial.print("NOT_SHOCK!!: ");
    BtSerial.print("SW_420_val: ");
    BtSerial.println(sw_420_val);
  }

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
  if (time == 10){    // 약 1초마다 cnt와 time '0'으로 초기화
    cnt =0;
    time=0;
  }
}
