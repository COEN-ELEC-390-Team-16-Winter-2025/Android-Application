#include <SoftwareSerial.h>

#define MQ_PIN A0
#define RX_PIN 10  // Bluetooth RX (Arduino TX)
#define TX_PIN 11  // Bluetooth TX (Arduino RX)

SoftwareSerial BTSerial(RX_PIN, TX_PIN); // HC-05 Bluetooth module

const int RL = 200000; // 200k ohms as per datasheet
const float m = -0.62;
const float b = 0.38;

float R0_air;
float weight_kg = 70; // Default values (overwritten by Bluetooth)
float height_cm = 175;

float getVoltage() {
    int adc_value = analogRead(MQ_PIN);
    return (adc_value * 5.0) / 1024;
}

float getRS(float voltage) {
    return ((5.0 * RL) / voltage) - RL;
}

float getR0(float RS_air, float voltage) {
    return RS_air / 60;
}

double getAlcoholConcentration(float RS_gas, float R0_air) {
    float ratio = RS_gas / R0_air;
    double log_concentration = (log10(ratio) - b) / m;
    return pow(10, log_concentration);
}

// BAC calculation using height and weight
double getBAC(double concentration) {
    double TBW = 2.447 + (0.3362 * weight_kg) + (0.1074 * height_cm);
    return (concentration * 0.21) / TBW;
}

void setup() {
    Serial.begin(115200);
    BTSerial.begin(9600);  // Start Bluetooth

    Serial.println("Waiting for user data...");

    // Wait for height & weight from Bluetooth
    while (!BTSerial.available()) {
        delay(100);
    }

    String receivedData = BTSerial.readStringUntil('\n');
    weight_kg = receivedData.substring(0, receivedData.indexOf(',')).toFloat();
    height_cm = receivedData.substring(receivedData.indexOf(',') + 1).toFloat();

    Serial.print("Weight: "); Serial.print(weight_kg);
    Serial.print(", Height: "); Serial.println(height_cm);

    // Sensor warm-up
    Serial.println("Preparing the sensor...");
    delay(20000);
    Serial.println("Sensor heated.");

    // Calibration
    float voltage = 0;
    for (int i = 0; i < 1000; i++) {
        voltage += getVoltage();
    }
    voltage /= 1000;
    float RS_air = getRS(voltage);
    R0_air = getR0(RS_air, voltage);

    Serial.print("R0 value in clean air: ");
    Serial.println(R0_air);
}

// Take multiple readings and calculate stable BAC
double getStableBAC() {
    double readings[5];
    for (int i = 0; i < 5; i++) {
        float voltage = getVoltage();
        float RS_gas = getRS(voltage);
        readings[i] = getBAC(getAlcoholConcentration(RS_gas, R0_air));
        delay(200);
    }

    // Sort readings to remove outliers
    for (int i = 0; i < 4; i++) {
        for (int j = i + 1; j < 5; j++) {
            if (readings[i] > readings[j]) {
                double temp = readings[i];
                readings[i] = readings[j];
                readings[j] = temp;
            }
        }
    }

    // Take the middle three values
    return (readings[1] + readings[2] + readings[3]) / 3.0;
}

void loop() {
    if (BTSerial.available()) {
        String command = BTSerial.readStringUntil('\n');

        if (command == "SCAN_NOW") {
            double stableBAC = getStableBAC();
            Serial.print("Stable BAC%: ");
            Serial.println(stableBAC, 4);

            BTSerial.println(stableBAC, 4);  // Send BAC to Android
        }
    }
}