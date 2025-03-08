package com.example.frisbeeapplication

import BluetoothViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.frisbeeapplication.databinding.FragmentAutoBinding


class AutoFragment : Fragment() {
    private var _binding: FragmentAutoBinding? = null
    private val binding get() = _binding!!
    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAutoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothViewModel.connectionStatus.observe(viewLifecycleOwner) { isConnected ->
            binding.toggleButton.isEnabled = isConnected
        }

        if (bluetoothViewModel.connectionStatus.value != true) {
            showToastAtTop("Bluetooth not connected")
        } else {
            showToastAtTop("Bluetooth connected!")
        }

        binding.buttonBack.setOnClickListener {
            bluetoothViewModel.sendDataToBluetooth("MODE:OFF")
            showToastAtTop("MODE:OFF")
            findNavController().navigate(R.id.action_AutoFragment_to_UserHeightFragment)
        }

        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            val userHeight = bluetoothViewModel.userHeight ?: ""
            val modeState = if (isChecked) "1" else "0"
            if (isChecked) {
                binding.toggleButton.text = "AUTO MODE ON"
            } else {
                binding.toggleButton.text = "AUTO MODE OFF"
            }
            bluetoothViewModel.sendDataToBluetooth("MODE:AUTO;State:$modeState;Height:$userHeight")
            showToastAtTop("MODE:AUTO;State:$modeState;Height:$userHeight")
        }
    }

    private fun showToastAtTop(message: String) {
        val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        toast.setGravity(android.view.Gravity.TOP, 100, 200) // Position at top with slight offset
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}