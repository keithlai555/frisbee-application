package com.example.frisbeeapplication

import BluetoothViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.frisbeeapplication.databinding.FragmentManualBinding
import android.os.Handler
import android.os.Looper

class ManualFragment : Fragment() {
    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!
    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    private var selectedSpeed = 0

    private val handler = Handler(Looper.getMainLooper())
    private var isHolding = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isBluetoothConnected = bluetoothViewModel.connectionStatus.value == true

        if (!isBluetoothConnected) {
            Toast.makeText(requireContext(), "Bluetooth NOT connected!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Bluetooth connected!", Toast.LENGTH_SHORT).show()
        }

        bluetoothViewModel.connectionStatus.observe(viewLifecycleOwner) { isConnected ->
            binding.seekbarSpeed.isEnabled = isConnected
            binding.buttonLeft.isEnabled = isConnected
            binding.buttonRight.isEnabled = isConnected
            binding.buttonUp.isEnabled = isConnected
            binding.buttonDown.isEnabled = isConnected
            binding.buttonReset.isEnabled = isConnected
        }

        binding.seekbarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedSpeed = progress
                binding.textSpeedValue.text = "Speed: $selectedSpeed"
                bluetoothViewModel.sendDataToBluetooth("Speed:$selectedSpeed;")
                Toast.makeText(requireContext(), "Sent: Speed:$selectedSpeed", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupHoldButton(binding.buttonLeft, "Left")
        setupHoldButton(binding.buttonRight, "Right")
        setupHoldButton(binding.buttonUp, "Up")
        setupHoldButton(binding.buttonDown, "Down")

        binding.buttonBack.setOnClickListener {
            bluetoothViewModel.sendDataToBluetooth("MODE:OFF")
            Toast.makeText(requireContext(), "MODE:OFF", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_ManualFragment_to_DashboardFragment)
        }

        binding.buttonReset.setOnClickListener {
            bluetoothViewModel.sendDataToBluetooth("Direction:Reset")
            Toast.makeText(requireContext(), "Sent: Direction:Reset", Toast.LENGTH_SHORT).show()
        }

        binding.buttonThrow.setOnClickListener {
            bluetoothViewModel.sendDataToBluetooth("Direction:Throw")
            Toast.makeText(requireContext(), "Sent: Direction:Throw", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHoldButton(button: View, command: String) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHolding = true
                    v.isPressed = true
                    sendRepeatedCommand(command)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isHolding = false
                    v.isPressed = false
                    handler.removeCallbacksAndMessages(null) // Stop sending data
                }
            }
            v.performClick()
            true
        }
    }

    // Function to send data repeatedly while button is held
    private fun sendRepeatedCommand(command: String) {
        if (isHolding) {
            bluetoothViewModel.sendDataToBluetooth("Direction:$command")
            Toast.makeText(requireContext(), "Sent: Direction:$command", Toast.LENGTH_SHORT).show()

            handler.postDelayed({ sendRepeatedCommand(command) }, 200) // Adjust delay as needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}