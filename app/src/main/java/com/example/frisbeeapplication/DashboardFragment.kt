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
import com.example.frisbeeapplication.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTop.setOnClickListener {
            bluetoothViewModel.sendDataToBluetooth("MODE:MANUAL")
            Toast.makeText(requireContext(), "MODE:MANUAL", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_DashboardFragment_to_ManualFragment)
        }

        binding.buttonBottom.setOnClickListener {
            findNavController().navigate(R.id.action_DashboardFragment_to_UserHeightFragment)
        }

        binding.buttonReconnect.setOnClickListener {
            bluetoothViewModel.connectToManuallyPairedDevice("eric-raspberrypi")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}