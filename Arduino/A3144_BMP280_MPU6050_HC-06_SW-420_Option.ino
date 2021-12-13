
/*  20211207_17:03
 *  BMP 280 + MPU6050 + HC-06 + Option sketch
 */
#include <avr/pgmspace.h>
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <SoftwareSerial.h>



/*   A3144 Setting   */
const int A3144_HALL_A0 = A0;       // A0
const int A3144_HALL_D0 = 7;        // D7

int A3144[2] = { -999, 0}; // A3144[0] = 현재 A3144 값, A3144[1] = 평균값을 저장
int i = 0;                // A3144[]의 index
int A3144_set;            // 실행시 처음 받는 값을 초기값으로 세팅
int A3144_cnt = 0;        // A3144 모듈의 값이 평균 이상일 때, A3144_cnt++
int time = 0;             // A3144_cnt 값을 약 1초마다 초기화 해주기 위해 선언

/*   BMP280 using I2C   */
Adafruit_BMP280 bme;
float BMP280_set = 0;     // BMP280의 고도를 현재위치에서 받는 값을 초기값으로 세팅
float BMP280_altitude;
float pre_BMP280 = -999;
bool sign = 0;

/*   MPU6050   */
Adafruit_MPU6050 mpu;
int MPU6050_i = 3;
double MPU6050_x[4]; // MPU6050 모듈의 X 방향 가속도 [0]=현재 값,[1]=이전 값,[2]=1초간 평균값, [3]=초기값 보정
double MPU6050_y[4]; // MPU6050 모듈의 Y 방향 가속도
double MPU6050_z[4]; // MPU6050 모듈의 Z 방향 가속도
//now, pre, ave, set

/*   HC-06   */
const int Tx = 6;     // 전송 보내는 핀
const int Rx = 5;     // 수신 받는 핀
SoftwareSerial BtSerial(Tx, Rx);

/*   SW-420   */
const int sw_420 = A1;  //A7

/*   MSG   */
int BMP280_msg;
int MPU6050_msg;

void setup() {
  Serial.begin(9600);
  /*   I2C Connect Check   */
  if (!bme.begin(0x76)) { // <========= 스캔해서 찾아낸 I2C 주소
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
//  BtSerial.println("/*   Start   */");
}



void loop() {
  /*   BMP280   */
  Serial.print("고도");
  Serial.print(':');
  if (BMP280_set == 0) {
    BMP280_set = bme.readAltitude(1013.25);
    Serial.print(0.0);
  }
  else {
    BMP280_altitude = bme.readAltitude(1013.25) - BMP280_set;
    Serial.print(BMP280_altitude);
  }

  /*   MPU6050   */
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);
  if (MPU6050_x[3] == 0) {
    MPU6050_x[3] = a.acceleration.x, 1;
    MPU6050_y[3] = a.acceleration.y, 1;
    MPU6050_z[3] = a.acceleration.z, 1;
  } else {
    MPU6050_x[0] = a.acceleration.x, 1;
    MPU6050_x[0] -= MPU6050_x[3];
    MPU6050_y[0] = a.acceleration.y, 1;
    MPU6050_y[0] -= MPU6050_y[3];
    MPU6050_z[0] = a.acceleration.z, 1;
    MPU6050_z[0] -= MPU6050_z[3];
  }

  Serial.print(',');
  Serial.print('\t');
  Serial.print("X_(m/s^2)");
  Serial.print(':');
  Serial.print(MPU6050_x[MPU6050_i]);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("Y_(m/s^2)");
  Serial.print(':');
  Serial.print(MPU6050_y[MPU6050_i]);
  Serial.print(',');
  Serial.print('\t');
  Serial.print("Z_(m/s^2)");
  Serial.print(':');
  Serial.print(MPU6050_z[MPU6050_i]);

  /*   SW-420   */
  float val = analogRead(sw_420);
  float SW_420_val = val / 1024 * 10;
  Serial.print(',');
  Serial.print('\t');
  if (SW_420_val > 9) {
    Serial.print("SHOCK");
    Serial.print(':');
    Serial.println(SW_420_val);
  } else {
    Serial.print("NOT_SHOCK");
    Serial.print(':');
    Serial.println(SW_420_val);
  }

  /*   HC-06   */
  /*===MPU6050 속도가 너무 빠를때 출력===*/
  MPU6050_SPEED_FAST('X', MPU6050_x[MPU6050_i]);
  MPU6050_SPEED_FAST('Y', MPU6050_y[MPU6050_i]);
  MPU6050_SPEED_FAST('Z', MPU6050_z[MPU6050_i]);
  /*===SW-420 충격이 감지되었을 때===*/
  if (SW_420_val > 9) {
//    BtSerial.println("SHOCK");
    /*===BMP 280===*/
    if (pre_BMP280 != -999) {
      BMP280_FLOOR((abs)(BMP280_altitude - pre_BMP280));
      /*===충격 발생시, MPU6050의 값 출력===*/
      MPU6050_SHOCK(MPU6050_x[MPU6050_i], MPU6050_y[MPU6050_i], MPU6050_z[MPU6050_i]);
      
    } else {
      BMP280_FLOOR((abs)(BMP280_altitude - BMP280_set));
      /*===충격 발생시, MPU6050의 값 출력===*/
      MPU6050_SHOCK(MPU6050_x[MPU6050_i], MPU6050_y[MPU6050_i], MPU6050_z[MPU6050_i]);
    }
  }

  delay(1000);

  // loop()함수가 10번 실행될때마다 <time = 0>, <BMP280_set = 0>으로 초기화
  if (time == 10) {
    time = 0;
    BMP280_set = 0;
  } else time++;
  
  if (MPU6050_x[3] != 0 && MPU6050_y[3] != 0 && MPU6050_z[3] != 0) {
    MPU6050_i = 0;
  }
}



/*====================================================================
  /                             FUNTION
  =====================================================================*/

/*   BMP280   */
void BMP280_FLOOR (float BMP280_val) {
  /*   용도 = BMP280센서값으로 낙상사고인지 여부를 판별함
   *   double MPU6050_data = 판별할 속도값
   */
  int BMP280_floor = (abs)((abs)(BMP280_val)-(abs)(pre_BMP280)) / 2.5;
  if (BMP280_floor >= 1) {//낙상o
    BMP280_msg = 0;
  } else {                //낙상x(충돌)
    BMP280_msg = 3;
  }
}


/*   MPU6050   */
void MPU6050_SPEED_FAST (char c, double MPU6050_data) {
  /*   용도 = 현재 속도가 기준치 이상일 시, 조건문에 맞는 유형을 출력하는 함수
   *   char c = X, Y, Z 중 하나
   *   double MPU6050_data = 판별할 속도값
   */
  float velocity = sqrt(MPU6050_data * 12960000) / 1000;
  if (velocity >= 13) {
    pre_BMP280 = BMP280_altitude;
  }
}

void MPU6050_SHOCK (double MPU6050_x, double MPU6050_y, double MPU6050_z) {
  /*   용도 = 충격 발생시, 현재 속도 및 사고 유형 출력하는 함수
   *   double MPU6050_x, double MPU6050_y, double MPU6050_z = 판별할  xyz 속도값
   */
  int index = 0;
  double max_temp[3] = {MPU6050_x, MPU6050_y, MPU6050_z};
  for (int i = 1; i < 3; i++) {
    if (max_temp[0] < max_temp[i]) {
      max_temp[0] = max_temp[i];
      index = i;
    }
  }
  double velocity = sqrt(max_temp[0] * 12960000) / 1000;
  if (velocity >= 13){
    MPU6050_msg = (int)velocity;
    
    char st[20];
    if (velocity >= 100) {
      MPU6050_msg = 3;
    } else if (velocity >= 60) {
      MPU6050_msg = 2;
    } else {
      MPU6050_msg = 1;
    }
    int msg = BMP280_msg+MPU6050_msg;
    for(int i=0; i<msg; i++)
      BtSerial.print(1);
    BtSerial.println();
    BMP280_msg = 0;
    MPU6050_msg = 0;
  } else{
    pre_BMP280 = BMP280_altitude;
    BMP280_msg = 0;
    MPU6050_msg = 0;
  }
}