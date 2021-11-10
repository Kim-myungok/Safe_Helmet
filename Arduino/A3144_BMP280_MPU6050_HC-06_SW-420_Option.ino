
/*
* A3144 + GY BMP 280 + MPU6050 + HC-06 test sketch
*
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

int A3144[2] = {-999, 0}; // A3144[0] = 현재 A3144 값, A3144[1] = 평균값을 저장
int i = 0;                // A3144[]의 index
int A3144_set;            // 실행시 처음 받는 값을 초기값으로 세팅
int A3144_cnt = 0;        // A3144 모듈의 값이 평균 이상일 때, A3144_cnt++
int time = 0;             // A3144_cnt 값을 약 1초마다 초기화 해주기 위해 선언

/*   BMP280 using I2C   */
Adafruit_BMP280 bme;
float BMP280_set;     // BMP280의 고도를 현재위치에서 받는 값을 초기값으로 세팅
float BMP280_altitude;
float pre_BMP280 = -999;
bool sign = 0;

/*   MPU6050   */
Adafruit_MPU6050 mpu;
int MPU6050_i=3;
double MPU6050_x[4]; // MPU6050 모듈의 X 방향 가속도 [0]=현재 값,[1]=이전 값,[2]=1초간 평균값, [3]=초기값 보정
double MPU6050_y[4]; // MPU6050 모듈의 Y 방향 가속도  
double MPU6050_z[4]; // MPU6050 모듈의 Z 방향 가속도
//now, pre, ave, set

/*   HC-06   */
const int Tx = 6;     // 전송 보내는 핀
const int Rx = 5;     // 수신 받는 핀
SoftwareSerial BtSerial(Tx,Rx);

/*   SW-420   */
const int sw_420 = A1;



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
  A3144[0] = A3144_A_val;
  if(A3144[0] > A3144[1]) A3144_cnt++; // A3144의 현재값이 평균보다 클때 cnt++
  
  int A3144_D_val = digitalRead(A3144_HALL_D0);
  if( A3144_D_val == HIGH ){
    Serial.print("HIGH");
  } else Serial.print("LOW");
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
  Serial.print(A3144[1]);   //A3144 1초간 데이터의 평균
  Serial.print(',');
  Serial.print('\t');
  Serial.print("A3144_cnt");
  Serial.print(':');
  Serial.print(A3144_cnt);
  
   /*   BMP280   */
  Serial.print(',');
  Serial.print('\t');
  Serial.print("고도");
  Serial.print(':');
  if(BMP280_set==0){
    BMP280_set = bme.readAltitude(1013.25);
    Serial.print(BMP280_set-BMP280_set);
  }
  else{
    BMP280_altitude = bme.readAltitude(1013.25) - BMP280_set;
    Serial.print(BMP280_altitude);
  }
  
  /*   MPU6050   */
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);
  if (MPU6050_x[3]==0){
    MPU6050_x[3] = a.acceleration.x, 1;
    MPU6050_y[3] = a.acceleration.y, 1;
    MPU6050_z[3] = a.acceleration.z, 1;
  } else{
    MPU6050_x[0] = a.acceleration.x, 1;
    MPU6050_x[0] -= MPU6050_x[3];
    MPU6050_y[0] = a.acceleration.y, 1;
    MPU6050_y[0] -= MPU6050_y[3];
    MPU6050_z[0] = a.acceleration.z, 1;
    MPU6050_z[0] -= MPU6050_z[3];
  }
  
  if(MPU6050_x[2]==0){  //평균
    MPU6050_x[2] = MPU6050_x[2] + MPU6050_x[1];
    MPU6050_y[2] = MPU6050_y[2] + MPU6050_y[1];
    MPU6050_z[2] = MPU6050_z[2] + MPU6050_z[1];
  }else{
    MPU6050_x[2] = (MPU6050_x[2] + MPU6050_x[0]) / 2;
    MPU6050_y[2] = (MPU6050_y[2] + MPU6050_y[0]) / 2;
    MPU6050_z[2] = (MPU6050_z[2] + MPU6050_z[0]) / 2;
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
  if(SW_420_val > 9){
    Serial.print("SHOCK");
    Serial.print(':');
    Serial.println(SW_420_val);
  }else{
    Serial.print("NOT_SHOCK");
    Serial.print(':');
    Serial.println(SW_420_val);
  }
  
  /*   HC-06   */
  /*===A3144 불안정 상태일때 출력===*/
  if(A3144_cnt == 10){
    BtSerial.print("!!!!_A3144_cnt: ");
    BtSerial.print(A3144_cnt);
    BtSerial.println("_!!!!");
    BtSerial.println("뇌파가_불안정합니다.");
    BtSerial.println("정서가_불안정한_상태입니다.");
    BtSerial.println();
  }
  /*===MPU6050 속도가 너무 빠를때 출력===*/
  MPU6050_SPEED_FAST('X', MPU6050_x[MPU6050_i]);
  MPU6050_SPEED_FAST('Y', MPU6050_y[MPU6050_i]);
  MPU6050_SPEED_FAST('Z', MPU6050_z[MPU6050_i]);
  /*===SW-420 충격이 감지되었을 때===*/
  if(SW_420_val > 9){
    BtSerial.println("SHOCK");
    /*===BMP 280===*/
    if(pre_BMP280 != -999){
      if(BMP280_altitude-pre_BMP280 < sign){
        BMP280_FLOOR((abs)(BMP280_altitude-pre_BMP280));
      }else BMP280_FLOOR(BMP280_altitude-pre_BMP280);
      /*===충격 발생시, MPU6050의 값 출력===*/
      MPU6050_SHOCK('X', MPU6050_x[MPU6050_i]);
      MPU6050_SHOCK('Y', MPU6050_y[MPU6050_i]);
      MPU6050_SHOCK('Z', MPU6050_z[MPU6050_i]);
    } else{
      BMP280_FLOOR((int)(BMP280_altitude - BMP280_set));
      /*===충격 발생시, MPU6050의 값 출력===*/
      MPU6050_SHOCK('X', MPU6050_x[MPU6050_i]);
      MPU6050_SHOCK('Y', MPU6050_y[MPU6050_i]);
      MPU6050_SHOCK('Z', MPU6050_z[MPU6050_i]);
    }
  } pre_BMP280 = BMP280_altitude;
  /*   A3144[] ave_calc   */
  if (A3144[0]!=-999){
    A3144[1] += A3144[0];
    if(i==sizeof(A3144)-1){
      A3144[1] = A3144[1]/sizeof(A3144);
      i=0;
    }else if(i<sizeof(A3144)-1) i++;
  }else i++;
  
  delay(100);
  
  time++;
  if (time == 10){    // 약 1초마다 A3144_cnt와 time '0'으로 초기화
    /*===1초 경과시, MPU6050를 통한 현재 사고발생 위험상태인지 출력===*/
    MPU6050_STATE(MPU6050_x[2]);
    MPU6050_STATE(MPU6050_y[2]);
    MPU6050_STATE(MPU6050_z[2]);
    
    A3144_cnt =0;
    time=0;
    
    MPU6050_x[2] = 0;
    MPU6050_y[2] = 0;
    MPU6050_z[2] = 0;
  }
  if(MPU6050_x[3] != 0 && MPU6050_y[3]!=0 && MPU6050_z[3] != 0){
    MPU6050_i = 0;
  }
}



/*====================================================================
/                             FUNTION
=====================================================================*/

/*   BMP280   */
void BMP280_FLOOR (float BMP280_val){
  /*   용도 = 
  /    char c = X, Y, Z 중 하나
  /    double MPU6050_data = 판별할 속도값
  */
  BtSerial.print((int)(BMP280_val / 2.4));
  BtSerial.println("층에서_떨어짐");
  BtSerial.println();
}

/*   MPU6050   */
void MPU6050_SPEED_FAST (char c, double MPU6050_data){
  /*   용도 = 현재 속도가 기준치 이상일 시, 조건문에 맞는 유형을 출력하는 함수
  /    char c = X, Y, Z 중 하나
  /    double MPU6050_data = 판별할 속도값
  */
  float velocity = sqrt(MPU6050_data * 12960000) / 1000;
  if(velocity >= 13){
    BtSerial.print(c);
    BtSerial.print("_(km/h): ");
    BtSerial.print((int)velocity);
    BtSerial.println("(km/h)의_속도로_빠르게_이동_중...");
    if(velocity >= 100){
      BtSerial.println("지속될_시, <사망_우려>");
      BtSerial.println();
    }else if(velocity >= 60){
      BtSerial.println("지속될_시, <중상_우려>");
      BtSerial.println();
    }else{
      BtSerial.println("지속될_시, <경상_우려>");
      BtSerial.println();
    }
  }
}

void MPU6050_SHOCK (char c, double MPU6050_data){
  /*   용도 = 충격 발생시, 현재 속도 및 사고 유형 출력하는 함수
  /    char c = X, Y, Z 중 하나
  /    double MPU6050_data = 판별할 속도값
  */
  BtSerial.print(c);
  BtSerial.print("_(km/h): ");
  float velocity = sqrt(MPU6050_data * 12960000) / 1000;
  BtSerial.print((int)velocity);
  BtSerial.println("(km/h)");
  if(velocity >= 100){
    BtSerial.println("<사망_사고> 발생");
    BtSerial.println();
  }else if(velocity >= 60){
    BtSerial.println("<중상_사고> 발생");
    BtSerial.println();
  }else if(velocity >= 13){
    BtSerial.println("<경상_사고> 발생");
    BtSerial.println();
  }else{
    BtSerial.println("<안전_사고> 발생");
    BtSerial.println();
  }
}

void MPU6050_STATE (double MPU6050_data){
  /*   용도 = 1초 간 데이터 평균을 통해 현재 위험한지 출력하는 함수
  /    char c = X, Y, Z 중 하나
  /    double MPU6050_data = 판별할 속도 평균값
  */
  float velocity = sqrt(MPU6050_data * 12960000) / 1000;
  if(velocity >= 100){
      BtSerial.println("<사망_위험>");
      BtSerial.println();
    }else if(velocity >= 60){
      BtSerial.println("<중상_위험>");
      BtSerial.println();
    }else if(velocity >= 13){
      BtSerial.println("<경상_위험>");
      BtSerial.println();
    }else{
      BtSerial.println("<안전_상태>");
      BtSerial.println();
    }
}
