package com.drinkwise.app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// This class responsible for giving data to the HomeFragment.
public class HomeViewModel extends ViewModel {





    //not used





    // holds a string that represents text to be displayed in the HomeFragment.
    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}