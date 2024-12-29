package com.example.myapplication.presentation

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedScreenshotViewModel : ViewModel() {
    val bitmap: MutableLiveData<Bitmap> = MutableLiveData(Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888))
}