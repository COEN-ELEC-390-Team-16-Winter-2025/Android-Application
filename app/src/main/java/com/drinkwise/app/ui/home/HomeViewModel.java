package com.drinkwise.app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// HomeViewModel is responsible for providing data to the HomeFragment.
// It extends ViewModel so that data can survive configuration changes.
public class HomeViewModel extends ViewModel {





    //not used





    // This MutableLiveData holds a String that represents text to be displayed in the HomeFragment.
    // LiveData allows the UI to observe changes.
    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}