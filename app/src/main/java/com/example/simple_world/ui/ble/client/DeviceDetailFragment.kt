package com.example.simple_world.ui.ble.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.simple_world.R

class DeviceDetailFragment : Fragment() {

    private lateinit var deviceInfoText: TextView

    private var deviceName: String? = null
    private var deviceAddress: String? = null

    companion object {
        fun newInstance(name: String?, address: String?): DeviceDetailFragment {
            val fragment = DeviceDetailFragment()
            val args = Bundle()
            args.putString("deviceName", name)
            args.putString("deviceAddress", address)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceName = arguments?.getString("deviceName")
        deviceAddress = arguments?.getString("deviceAddress")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_detail, container, false)
        deviceInfoText = view.findViewById(R.id.deviceInfoText)
        deviceInfoText.text = "Connected to:\n$deviceName\n$deviceAddress"
        return view
    }
}
