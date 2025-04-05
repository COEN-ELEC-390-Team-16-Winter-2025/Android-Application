package com.drinkwise.app;

// Class representing a quick help message based on a count value.
public class QuickHelpMessage {

    // The count value that determines which message to display.
    private int count;
    // The message text determined by the count.
    private String message;
    // A reference to an EmergencyContact.
    private EmergencyContact contact;

    // Constructor that sets the count and determines the message based on the count.
    public QuickHelpMessage(int count) {
        this.count = count;

        // Determine the message based on the count.
        if(count == 1){
            this.message = "Please, check in on me.";
        } else if (count == 2 || count == 3) {
            this.message = "I'm not okay.";
        } else if (count >= 4 && count <= 6) {
            this.message = "Need help NOW";
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