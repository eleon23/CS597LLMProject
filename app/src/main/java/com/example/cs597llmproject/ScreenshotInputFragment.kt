package com.example.cs597llmproject

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cs597llmproject.databinding.FragmentScreenshotInputBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

class ScreenshotInputFragment : Fragment() {
    private var _binding: FragmentScreenshotInputBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScreenshotInputBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImageUri)

                            if (selectedImageBitmap != null) {
                                // Example: Save to internal storage (optional)
                                // saveBitmapToInternalStorage(selectedImageBitmap!!, "my_screenshot.png")

                                // Example: Use the bitmap for other processing (your code here)
                                // Screenshot was uploaded, we can enable the button
                                binding.submitButton.isEnabled = true
                                Toast.makeText(context, "Screenshot Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(context, "Error Uploading Screenshot", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        binding.screenshotInput.setOnClickListener {
            //Insert screenshot here and enable button
            openGallery()
        }

        binding.submitButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val userInput = binding.textInputEditText.text.toString()
                val formattedInput = convertInputUsingLLM(selectedImageBitmap, userInput)
                val bundle = Bundle().apply {
                    putString("input", formattedInput)
                }
                findNavController().navigate(
                    R.id.action_screenshotInputFragment_to_sendInfoFragment,
                    bundle
                )
            }
        }
    }

    private suspend fun convertInputUsingLLM(bitmap: Bitmap?, userInput: String): String {
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
                    content {
                        if (bitmap != null) {
                            image(bitmap)
                        }
                        text("Based on my screenshot, please help me rephrase the following question" +
                        "and return only one rephrased option: $userInput")
                    }
                ).text
            }
        }
        return result.orEmpty()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    companion object {
        private const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 100
    }

}