package com.drinkwise.app.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// DashboardViewModel holds UI-related data for the DashboardFragment.
public class DashboardViewModel extends ViewModel {

    // A MutableLiveData instance holding a String value.
    // MutableLiveData allows us to change its value, and observers (like UI components)
    // will be notified when it changes.
    private final MutableLiveData<String> mText;

    // Constructor initializes the MutableLiveData.
    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        // Set the initial value for mText. This value can be seen in the UI.
        mText.setValue("This is dashboard fragment");
    }

    // Expose mText as LiveData so that UI components can see changes without modifying it.
    public LiveData<String> getText() {
        return mText;
    }
}