package com.example.multitoolx.ui.ble.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.multitoolx.R

/**
 * DeviceDetailFragment.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Displays detailed information about a selected Bluetooth device.
 * - Shows the device's name and address.
 */
class DeviceDetailFragment : Fragment() {

    // TextView for displaying device information
    private lateinit var deviceInfoText: TextView

    // Variables to store device name and address passed via Bundle
    private var deviceName: String? = null
    private var deviceAddress: String? = null

    companion object {
        /**
         * Creates a new instance of DeviceDetailFragment with the device's name and address.
         *
         * @param name The name of the Bluetooth device.
         * @param address The address of the Bluetooth device.
         * @return A new instance of DeviceDetailFragment with the provided arguments.
         */
        fun newInstance(name: String?, address: String?): DeviceDetailFragment {
            val fragment = DeviceDetailFragment()
            val args = Bundle()
            args.putString("deviceName", name)
            args.putString("deviceAddress", address)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * Called when the fragment is created.
     * Retrieves the device's name and address from the arguments passed during fragment initialization.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceName = arguments?.getString("deviceName")
        deviceAddress = arguments?.getString("deviceAddress")
    }

    /**
     * Called when the fragment's view is created.
     * Inflates the layout for this fragment and sets the device info text.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_detail, container, false)

        // Initialize the TextView to display the device info
        deviceInfoText = view.findViewById(R.id.deviceInfoText)

        // Set the device information text to display the name and address
        deviceInfoText.text = "Connected to:\n$deviceName\n$deviceAddress"

        return view
    }
}
