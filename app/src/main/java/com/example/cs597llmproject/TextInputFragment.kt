package com.example.cs597llmproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.cs597llmproject.databinding.FragmentTextInputBinding
import com.example.cs597llmproject.databinding.FragmentVoiceInputFragmentBinding


class TextInputFragment : Fragment() {

    private var _binding: FragmentTextInputBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var userInput = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTextInputBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitButton.setOnClickListener {
            userInput = binding.textInputEditText.text.toString()
            val bundle = Bundle().apply {
                putString("input", userInput)
            }
            findNavController().navigate(R.id.action_textInputFragment_to_sendInfoFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}