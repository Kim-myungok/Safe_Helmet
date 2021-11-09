/* 기울기 센서 GY-521(MPU6050) 기초 실습 #1 (RAW 데이터 얻기)                            */ 
/* 아래 코드관련 실습에 대한 설명과 회로도 및 자료는 https://rasino.tistory.com/ 에 있습니다 */
#include<Wire.h>
#include <SoftwareSerial.h> //시리얼통신 라이브러리 호출
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>


int sw_420 = A0; //충격모듈센서 정의
//블루투스 통신 정의
int blueTx=3;   //Tx (보내는핀 설정)at
int blueRx=4;   //Rx (받는핀 설정)
int buttonPin = 5; // 푸시버튼이 연결된 번호
int buttonState = 0; // 입력핀의 상태를 저장
double angleAcX, angleAcY, angleAcZ;  //각도
double angleGyX, angleGyY, angleGyZ;  //각도

const double RADIAN_TO_DEGREE = 180 / 3.14159;  
const double DEG_PER_SEC = 32767 / 250;    // 1초에 회전하는 각도
// GyX, GyY, GyZ 값의 범위 : -32768 ~ +32767 (16비트 정수범위)

unsigned long now = 0;   // 현재 시간 저장용 변수
unsigned long past = 0;  // 이전 시간 저장용 변수
double dt = 0;           // 한 사이클 동안 걸린 시간 변수 

double averAcX, averAcY, averAcZ;
double averGyX, averGyY, averGyZ;
SoftwareSerial mySerial(blueRx, blueTx);  //시리얼 통신을 위한 객체선언

const int MPU_ADDR = 0x68;    // I2C통신을 위한 MPU6050의 주소
int16_t AcX, AcY, AcZ, Tmp, GyX, GyY, GyZ,t=0;   // 가속도(Acceleration) 와 
void getRawData();  // 센서값 얻는 서브함수의 프로토타입 선언 
double m=70,g,s,v,a_x, a_y, a_z;

void setup() {
  initSensor();
  pinMode(buttonPin, INPUT); // 푸시버튼은 입력으로 설정
  Serial.begin(9600);
  mySerial.begin(9600); //블루투스 시리얼
  caliSensor();   //  초기 센서 캘리브레이션 함수 호출
  past = millis(); // past에 현재 시간 저장  
  delay(20);
}

void loop() {
  buttonState = digitalRead(buttonPin); //입력 값을 읽고 저장
// 버튼이 눌렸는지 확인, 버튼이 눌렸으면 입력핀의 상태는 HIGH가 됨
if (buttonState == HIGH) {
digitalWrite(sw_420, HIGH); //충격센서 켬
}
else {
digitalWrite(sw_420, LOW); //충격센서 끔
}
  
  getRawData();          // 센서값 얻어오는 함수 호출
  
  float val = analogRead(sw_420);
  float val_pct = val/1024*100;
  if(val_pct>90){
    Serial.write("\tSHOCK!!\t");
    Serial.print(val);
    Serial.write("\t");
    Serial.println(val_pct);
//    mySerial.println(val);
  }
  else{
    Serial.write("NOT_SHOCK!!\t");
    Serial.print(val);
    Serial.write("\t");
    Serial.println(val_pct);
  }
  g = 9.8;              //중력가속도
   v = ((AcX*t)+(AcY*t)+(AcZ*t))/3;             //순간속도
  s = ((0.5*AcX*t*t)+(0.5*AcY*t*t)+(0.5*AcZ*t*t))/3;        //낙하거리
  
  
  if (abs(a_x - AcX) > 25000 || abs(a_y - AcY) > 25000 || abs(a_z - AcZ) > 25000 || val_pct>90){
    mySerial.write("7"); //True
    delay(1);
    a_x=AcX;
    a_y=AcY;
    a_z=AcZ;
  }
//  Serial.print("t=");
//  Serial.println(t);
//  Serial.print("v=");
//  Serial.println(v);
//  Serial.print("s=");
//  Serial.println(s);
//  Serial.print("a=");
//  Serial.println(a);
//  delay(1000);
//  t++;
  
//  
 
  getData(); 
  getDT();
  angleGyX += ((GyX - averGyX) / DEG_PER_SEC) * dt;
  angleGyY += ((GyY - averGyY) / DEG_PER_SEC) * dt;
  angleGyZ += ((GyZ - averGyZ) / DEG_PER_SEC) * dt;
  
//  Serial.print("Angle Gyro X:");
//  Serial.print(angleGyX);
//  Serial.print("\t\t Angle Gyro y:");
//  Serial.print(angleGyY);  
//  Serial.print("\t\t Angle Gyro Z:");
//  Serial.println(angleGyZ);  
//  delay(400);
}

void initSensor() {
  Wire.begin();
  Wire.beginTransmission(MPU_ADDR);   // I2C 통신용 어드레스(주소)
  Wire.write(0x6B);    // MPU6050과 통신을 시작하기 위해서는 0x6B번지에    
  Wire.write(0);       // MPU6050을 동작 대기 모드로 변경
  Wire.endTransmission(true);
}
void getData() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B);   // AcX 레지스터 위치(주소)를 지칭합니다
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDR, 14, true);  // AcX 주소 이후의 14byte의 데이터를 요청
  AcX = Wire.read() << 8 | Wire.read(); //두 개의 나뉘어진 바이트를 하나로 이어 붙여서 각 변수에 저장
  AcY = Wire.read() << 8 | Wire.read();
  AcZ = Wire.read() << 8 | Wire.read();
  Tmp = Wire.read() << 8 | Wire.read();
  GyX = Wire.read() << 8 | Wire.read();
  GyY = Wire.read() << 8 | Wire.read();
  GyZ = Wire.read() << 8 | Wire.read();
}

void getRawData() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B);   // AcX 레지스터 위치(주소)를 지칭합니다
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDR, 14, true);  // AcX 주소 이후의 14byte의 데이터를 요청

  AcX = Wire.read() << 8 | Wire.read(); //두 개의 나뉘어진 바이트를 하나로 이어 붙여서 각 변수에 저장
  AcY = Wire.read() << 8 | Wire.read();
  AcZ = Wire.read() << 8 | Wire.read();
  Tmp = Wire.read() << 8 | Wire.read();
  GyX = Wire.read() << 8 | Wire.read();
  GyY = Wire.read() << 8 | Wire.read();
  GyZ = Wire.read() << 8 | Wire.read();

//  Serial.print("AcX:");
//  Serial.print(AcX);
//  Serial.print("   AcY:");
//  Serial.print(AcY);
//  Serial.print("   AcZ:");
//  Serial.print(AcZ);
//  Serial.print("   GyX:");
//  Serial.print(GyX);
//  Serial.print("   GyY:");
//  Serial.print(GyY);
//  Serial.print("   GyZ:");
//  Serial.print(GyZ);
//  Serial.println();
//  delay(150);  
}
// loop 한 사이클동안 걸리는 시간을 알기위한 함수
void getDT() {
  now = millis();   
  dt = (now - past) / 1000.0;  
  past = now;
}

// 센서의 초기값을 10회 정도 평균값으로 구하여 저장하는 함수
void caliSensor() {
  double sumAcX = 0 , sumAcY = 0, sumAcZ = 0;
  double sumGyX = 0 , sumGyY = 0, sumGyZ = 0;
  getData();
  for (int i=0;i<10;i++) {
    getData();
    sumAcX+=AcX;  sumAcY+=AcY;  sumAcZ+=AcZ;
    sumGyX+=GyX;  sumGyY+=GyY;  sumGyZ+=GyZ;
    delay(200);
  }
  averAcX=sumAcX/10;  averAcY=sumAcY/10;  averAcZ=sumAcY/10;
  averGyX=sumGyX/10;  averGyY=sumGyY/10;  averGyZ=sumGyZ/10;
}
