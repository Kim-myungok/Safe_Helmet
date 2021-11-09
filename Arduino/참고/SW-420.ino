int sum = 0;

/*   SW-420   */
int sw_420 = A1;

void setup() { 
  Serial.begin(9600); 
  pinMode(sw_420, INPUT);
} 
void loop(){
  float val = analogRead(sw_420);
  float val_pct = val / 1024 * 100;
  if(val_pct > 90){
    Serial.write("SHOCK!!");
    Serial.println();
    Serial.print('\t');
    Serial.print("SW_420_val");
    Serial.print(':');
    Serial.print(val);
    Serial.print('\t');
    Serial.print("val_pct");
    Serial.print(':');
    Serial.println(val_pct);
  }
  else{
    Serial.write("NOT_SHOCK!!");
    Serial.println();
    Serial.print("\tSW_420_val");
    Serial.print(':');
    Serial.println(val);
    Serial.print('\t');
    Serial.print("val_pct");
    Serial.print(':');
    Serial.println(val_pct);
  }
  delay(500);
}
