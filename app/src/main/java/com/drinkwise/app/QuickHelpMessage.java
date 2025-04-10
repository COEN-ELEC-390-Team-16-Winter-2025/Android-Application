package com.drinkwise.app;

// class that represents a quick help message based on a count value
public class QuickHelpMessage {

    // The count value that determines which message to display
    private int count;
    // The message text determined by the count
    private String message;
    // reference to an EmergencyContact
    private EmergencyContact contact;

    // constructor
    public QuickHelpMessage(int count) {
        this.count = count;

        // message based on count
        if(count == 1){
            this.message = "Please, check in on me. \n\nSent from DrinkWise";
        } else if (count == 2 || count == 3) {
            this.message = "I'm not okay. \n\nSent from DrinkWise";
        } else if (count >= 4 && count <= 6) {
            this.message = "Need help NOW \n\nSent from DrinkWise";
        } else {
            this.message = "";
        }
    }

    public int getCount() {
        return count;
    }

    public String getMessage() {
        return message;
    }
}