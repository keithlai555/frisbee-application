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
import com.example.frisbeeapplication.databinding.FragmentUserHeightBinding

class UserHeightFragment : Fragment() {

    private var _binding: FragmentUserHeightBinding? = null
    private val binding get() = _binding!!
    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonUserHeight.setOnClickListener {
            val height = binding.editTextHeight.text.toString().trim()

            if (height.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a user height!", Toast.LENGTH_SHORT).show()
            } else {
                bluetoothViewModel.setUserHeight(height)
                findNavController().navigate(R.id.action_UserHeightFragment_to_AutoFragment)
            }
        }

        binding.buttonBackDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_UserHeightFragment_to_DashboardFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}