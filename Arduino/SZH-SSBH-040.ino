/*
 * read_switch_
 */
 
int ReadSwitch = A3;

void setup (){
  Serial.begin(9600);
  Serial.println("##### START #####");
  pinMode (ReadSwitch, INPUT);
}
void loop (){
  int D_val = digitalRead (ReadSwitch);
  
  Serial.print("Digital");
  Serial.print(':');
  Serial.println(D_val);
  
  delay(100);
}
