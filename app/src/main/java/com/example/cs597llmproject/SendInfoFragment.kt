package com.example.cs597llmproject

import android.app.Activity.RESULT_OK
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.cs597llmproject.databinding.FragmentSendInfoBinding
import com.example.cs597llmproject.databinding.FragmentTextInputBinding

class SendInfoFragment : Fragment() {

    private var _binding: FragmentSendInfoBinding? = null
    private val REQUEST_SELECT_CONTACT = 1
    private var userInput = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSendInfoBinding.inflate(inflater, container, false)
        userInput = arguments?.getString("input").toString()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, userInput)
            }
            startActivity(intent)
        }

        binding.sendButton.setOnClickListener {
            val pickContactIntent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            startActivityForResult(pickContactIntent, REQUEST_SELECT_CONTACT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
            data?.data?.let { contactUri ->
                // Query the selected contact for the phone number
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                context?.contentResolver?.query(contactUri, projection, null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val numberIndex =
                                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val phoneNumber = cursor.getString(numberIndex)
                            // Now launch the SMS intent
                            sendSms(phoneNumber, userInput)
                        }
                    }
            }
        }
    }

    private fun sendSms(phoneNumber: String, userInput: String) {
        val message = userInput
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        if (requireContext().packageManager.resolveActivity(smsIntent, 0) != null) {
            startActivity(smsIntent)
        } else {
            Toast.makeText(requireContext(), "No messaging app found!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}