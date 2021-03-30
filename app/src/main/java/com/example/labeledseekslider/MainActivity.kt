package com.example.labeledseekslider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seekSlider.onValueChanged = { value ->
            Log.d("LabeledSeekSlider", "Current slider value: $value")
        }
    }
}