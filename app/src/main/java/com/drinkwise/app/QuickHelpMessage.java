package com.drinkwise.app;

public class QuickHelpMessage {

    private int count;
    private String message;

    private EmergencyContact contact;

    public QuickHelpMessage(int count) {
        this.count = count;

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
