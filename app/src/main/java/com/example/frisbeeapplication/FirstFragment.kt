package com.example.frisbeeapplication

import BluetoothViewModel
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.frisbeeapplication.databinding.FragmentFirstBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun checkAndEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(requireContext(), "Please enable Bluetooth and connect to pi", Toast.LENGTH_SHORT).show()
            val enableBtIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(enableBtIntent)
        } else {
            Toast.makeText(requireContext(), "Attempting to connect to Raspberry Pi...", Toast.LENGTH_SHORT).show()
            bluetoothViewModel.connectToManuallyPairedDevice("eric-raspberrypi")

            bluetoothViewModel.connectionStatus.observe(viewLifecycleOwner) { isConnected ->
                if (isConnected) {
                    Toast.makeText(requireContext(), "Connected! Navigating to the dashboard.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_FirstFragment_to_DashboardFragment)
                }
                else {
                    Toast.makeText(requireContext(), "Unable to connect. Try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

//        binding.buttonFirst.setOnClickListener {
//            //findNavController().navigate(R.id.action_FirstFragment_to_DashboardFragment)
//            checkAndEnableBluetooth()
//        }
        binding.buttonFirst.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                val permissions = arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )

                val missingPermissions = permissions.filter {
                    ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
                }.toTypedArray()

                if (missingPermissions.isNotEmpty()) {
                    ActivityCompat.requestPermissions(requireActivity(), missingPermissions, 1)
                    return@setOnClickListener
                }
            }

            checkAndEnableBluetooth()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
