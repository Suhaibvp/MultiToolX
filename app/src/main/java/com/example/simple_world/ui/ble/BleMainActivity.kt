package com.example.simple_world.ui.ble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.simple_world.ui.ble.advertise.AdvertiseActivity
//import com.example.simple_world.ui.ble.ble_advertise.AdvertiseActivity
import com.example.simple_world.databinding.ActivityBleMainBinding
import com.example.simple_world.ui.ble.client.ClientMainActivity

class BleMainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding=ActivityBleMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btn_advertise=binding.btnAdvertise
        val btn_client=binding.btnClient
        btn_advertise.setOnClickListener {
            println("Btn Advertise clicked")
            val intent= Intent(this, AdvertiseActivity::class.java)
            startActivity(intent)
        }
        btn_client.setOnClickListener {
println("Btn Client clicked ")
            val intent=Intent(this,ClientMainActivity::class.java)
            startActivity(intent)
        }

    }
}