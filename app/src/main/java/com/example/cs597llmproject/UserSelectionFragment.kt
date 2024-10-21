package com.example.cs597llmproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cs597llmproject.databinding.FragmentUserSelectionBinding

/**
 * Home fragment for asking the user to select what type of input they want to send their query in
 */
class UserSelectionFragment : Fragment() {

    private var _binding: FragmentUserSelectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserSelectionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.voiceButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_voiceInputFragment)
        }
        binding.textButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_textInputFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}