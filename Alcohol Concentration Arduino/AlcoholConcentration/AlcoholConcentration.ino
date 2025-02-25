
#define MQ_PIN A0

//Optimal R0 = 1230.37

const int RL = 200000; //200k ohms for RL as per datasheet
const float m = -0.62;
const float b = 0.38;

float R0_air;

float getVoltage(){
  int adc_value = analogRead(MQ_PIN);
  float voltage = (adc_value*5.0)/1024;
  return voltage;
}

float getRS(float voltage){
  float RS = ((5.0*RL)/voltage) - RL;
  return RS;
} 

float getR0(float RS_air, float voltage){
  return RS_air/60;
}

void setR0(float R0){
  R0_air = R0;
}

double getAlcoholConcentration(float RS_gas, float R0_air){
  
  float ratio = RS_gas / R0_air;
  double log_concentration = (log10(ratio)-b)/m;
  double concentration = pow(10, log_concentration);

  return concentration;
}

double getBAC(double concentration){
  return concentration*0.21;
}

void setup()
{
  Serial.begin(115200);
  Serial.println("Preparing the sensor...");
  delay(20000); //waits for 20 seconds to heat up the sensor
  Serial.println("Sensor heated.");


  //calibration
  float voltage = 0;
  for(int i =0; i<1000; i++){
    voltage += getVoltage();
  }
  voltage = voltage/1000;
  float RS_air = getRS(voltage); 

  R0_air = getR0(RS_air, voltage);

  Serial.print("R0 value in clean air: ");
  Serial.println(R0_air);
}

void loop()
{
  float voltage = getVoltage(); //gets voltage values from the sensor
  float RS_gas = getRS(voltage); //calculates Rs from the voltage
  
  double concentration = getAlcoholConcentration(RS_gas, R0_air); //calculates the alcohol concentration mg/l

  //filters out false positives
  if(concentration < 0.05){
    concentration = 0;
  }
  /*
  Serial.print("Alcohol Concentration (mg/L): ");
  Serial.println(concentration);

  Serial.print("BAC%: ");
  */
  
  Serial.println(getBAC(concentration), 4);
  
  delay(500);

}
