package com.example.cs597llmproject

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.cs597llmproject.databinding.FragmentVoiceInputFragmentBinding

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
                    // Handle the extracted text here, for example update the UI
                    //findViewById<TextView>(R.id.textView).text = textForVoiceInput
                    Toast.makeText(context, userInput, Toast.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}