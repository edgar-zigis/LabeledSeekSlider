package com.example.labeledseekslider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.example.labeledseekslider.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(LayoutInflater.from(this)).apply {
            setContentView(root)
            seekSlider.onValueChanged = { value ->
                Log.d("LabeledSeekSlider", "Current slider value: $value")
            }
        }
    }
}