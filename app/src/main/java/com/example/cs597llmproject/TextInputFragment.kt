package com.example.cs597llmproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cs597llmproject.databinding.FragmentTextInputBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
            viewLifecycleOwner.lifecycleScope.launch {
                val formattedInput = convertInputUsingLLM(userInput)
                val bundle = Bundle().apply {
                    putString("input", formattedInput)
                }
                findNavController().navigate(
                    R.id.action_textInputFragment_to_sendInfoFragment,
                    bundle
                )
            }
        }
    }

    private suspend fun convertInputUsingLLM(userInput: String): String {
        var result: String? = null
        val model = GenerativeModel(
            modelName = "gemini-1.5-flash-001",
            apiKey = BuildConfig.API_KEY,
            generationConfig = generationConfig {
                temperature = 0.15f
                topK = 32
                topP = 1f
                maxOutputTokens = 4096
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
            )
        )

        coroutineScope {
            launch {
                result = model.generateContent(
                    "Rephrase the question in a way that is clear enough for the user to copy and paste " +
                            "into Google Search Bar and be able to find the answer. Please provide one response and here is the user's question: $userInput"
                ).text
            }
        }
        return result.orEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}