AI Troubleshooting App in Android

This Android Mobile Application levareages Google Gemnini to provide a Troubleshooting app for user's who need help with rephrasing their questions to improve clarity and efficiency.
Users are then able to share their newly updated question with family, friends or send it to Google for querying.

Here users can type their question, can use speech to text and can submit screenshots.


** Developer Notes **

Please refer to the Build Config when you need to update your API key.

This is built using Google Gemini but can be update to use whatever AI API tooling such as Open AI. 

In TextInputFragment, VoiceInputFragment, ScreenshotInputFragment:

Please update the method: "convertInputUsingLLM" to use any LLM you need.
You can also change the query we are sending. Currently its defaulted to return one rephrased question.

  
