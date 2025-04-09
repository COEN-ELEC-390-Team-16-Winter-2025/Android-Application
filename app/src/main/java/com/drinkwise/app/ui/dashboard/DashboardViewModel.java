package com.drinkwise.app.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    // MutableLiveData is an instance that holds a string value. This allows us to change its value and observers (like UI components)
    private final MutableLiveData<String> mText;

    // Constructor that initializes the MutableLiveData.
    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    // Makes mText available in a way that lets the UI watch for updates, but without giving it the ability to change mText directly.
    public LiveData<String> getText() {
        return mText;
    }
}