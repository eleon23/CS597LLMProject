package com.example.cs597llmproject

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cs597llmproject.databinding.FragmentVoiceInputFragmentBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Fragment used for gathing voice input from user
 */
class VoiceInputFragment : Fragment() {

    private var _binding: FragmentVoiceInputFragmentBinding? = null
    private var userInput = ""
    private lateinit var voiceLauncher: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVoiceInputFragmentBinding.inflate(inflater, container, false)
        // Initialize the ActivityResultLauncher
        voiceLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                activityResult.data?.let { data ->
                    val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    userInput = results?.get(0) ?: "None"
                    viewLifecycleOwner.lifecycleScope.launch {
                        val formattedInput = convertInputUsingLLM(userInput)
                        val bundle = Bundle().apply {
                            putString("input", formattedInput)
                        }
                        findNavController().navigate(
                            R.id.action_voiceInputFragment_to_sendInfoFragment,
                            bundle
                        )
                    }
                }
            }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.voiceInputButton.setOnClickListener {
            setUpVoiceInput()
        }
    }

    private fun setUpVoiceInput() {

        val voiceIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "You can speak now"
            )
        }
        // Launch the intent using the ActivityResultLauncher
        voiceLauncher.launch(voiceIntent)
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
                    "Rephrase the question in a way that is clear enough for the user to copy and past " +
                            "into Google Search Bar and be able to find the answer. If you think there are multiple questions with different solutions being asked, separately " +
                            "rephrase them. If you are less than 90% confident in your understanding of the user's intent and problem just provide only the rephrase questions " +
                            "without additional text. Please provide on response and here is the user's question: $userInput"
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