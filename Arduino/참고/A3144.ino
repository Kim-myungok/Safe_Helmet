#define SIZE 2

int A3144_HALL_A0 = A0;
int A3144_HALL_D0 = 7;

int min = 5000;
int max = 0;
int ave[2];
int i = 0;
int set;
int cnt = 0;
int time = 0;

void setup(){
  pinMode(A3144_HALL_A0, INPUT);
  pinMode(A3144_HALL_D0, INPUT);
  Serial.begin(9600);
  Serial.println("=============== Start ===============");
  set = analogRead(A3144_HALL_A0) * (5.0 / 1024.0) * 1000;
}

void loop(){
  /*   아날로그 값을 전압0~5000mV 변환   */
  int A3144_A_val = analogRead(A3144_HALL_A0) * (5.0 / 1024.0) * 1000 - set;
  if(min > A3144_A_val){
    min = A3144_A_val;
  }
  if(max < A3144_A_val){
    max = A3144_A_val;
  }
  ave[i] = A3144_A_val;
  if(ave[i] > ave[0]) cnt++;
  
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
  Serial.print("AVE");
  Serial.print(':');
  Serial.print(ave[0]);
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
  Serial.println(max);
 
 
  if (i==SIZE-1){
    for(int temp=1; temp<SIZE; temp++){
      ave[0] += ave[temp];
      if(temp==SIZE-1) ave[0] = ave[0]/SIZE;
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
