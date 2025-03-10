package com.example.frisbeeapplication

import BluetoothViewModel
import android.content.Context
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
import android.view.inputmethod.InputMethodManager

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

        binding.inputHorizontalTilt.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                sendHorizontalData()
            }
        }

        binding.inputHorizontalTilt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                sendHorizontalData()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.inputVerticalTilt.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                sendVerticalData()
            }
        }

        binding.inputVerticalTilt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                sendVerticalData()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        setupHoldButton(binding.buttonLeft, "Left")
        setupHoldButton(binding.buttonRight, "Right")

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
            val horizontalAngleText = binding.inputHorizontalTilt.text.toString().trim()
            val verticalAngleText = binding.inputVerticalTilt.text.toString().trim()

            val horizontalAngle = if (horizontalAngleText.isEmpty()) 0.0 else horizontalAngleText.toDouble()
            val verticalAngle = if (verticalAngleText.isEmpty()) 0.0 else verticalAngleText.toDouble()

            val horizontalMin = -20.0
            val horizontalMax = 20.0
            val verticalMin = 0.0
            val verticalMax = 16.0

            if (horizontalAngle < horizontalMin || horizontalAngle > horizontalMax) {
                Toast.makeText(requireContext(), "Horizontal Angle must be between $horizontalMin and $horizontalMax", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (verticalAngle < verticalMin || verticalAngle > verticalMax) {
                Toast.makeText(requireContext(), "Vertical Angle must be between $verticalMin and $verticalMax", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bluetoothViewModel.sendDataToBluetooth("Direction:Throw;HorizontalAngle:$horizontalAngle;VerticalAngle:$verticalAngle")
            Toast.makeText(requireContext(), "Sent: Direction:Throw;HorizontalAngle:$horizontalAngle;VerticalAngle:$verticalAngle", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendHorizontalData() {
        val horizontalAngleText = binding.inputHorizontalTilt.text.toString().trim()
        val horizontalAngle = if (horizontalAngleText.isEmpty()) 0.0 else horizontalAngleText.toDouble()

        val horizontalMin = -20.0
        val horizontalMax = 20.0

        if (horizontalAngle < horizontalMin || horizontalAngle > horizontalMax) {
            Toast.makeText(requireContext(), "Horizontal Angle must be between $horizontalMin and $horizontalMax", Toast.LENGTH_SHORT).show()
        } else {
            bluetoothViewModel.sendDataToBluetooth("Direction:HorizontalAngle:$horizontalAngle")
            Toast.makeText(requireContext(), "Sent: Horizontal Angle: $horizontalAngle", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to send vertical angle data to Bluetooth
    private fun sendVerticalData() {
        val verticalAngleText = binding.inputVerticalTilt.text.toString().trim()
        val verticalAngle = if (verticalAngleText.isEmpty()) 0.0 else verticalAngleText.toDouble()

        val verticalMin = 0.0
        val verticalMax = 16.0

        if (verticalAngle < verticalMin || verticalAngle > verticalMax) {
            Toast.makeText(requireContext(), "Vertical Angle must be between $verticalMin and $verticalMax", Toast.LENGTH_SHORT).show()
        } else {
            bluetoothViewModel.sendDataToBluetooth("Direction:VerticalAngle:$verticalAngle")
            Toast.makeText(requireContext(), "Sent: Vertical Angle: $verticalAngle", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity?.currentFocus
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
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

            handler.postDelayed({ sendRepeatedCommand(command) }, 100)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}