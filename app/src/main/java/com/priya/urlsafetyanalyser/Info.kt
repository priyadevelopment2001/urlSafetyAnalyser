package shivam.sycodes.securepeek.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

class Info(private val context: Context) {

    fun privacyPolicy(){
        val dialog = AlertDialog.Builder(context).setTitle("Privacy Policy")
            .setMessage("Thank you for using Secure Peek! This privacy policy explains how the app handles your data and outlines important disclaimers regarding its functionality and usage.\n" +
                    "\n" +
                    "1. Data Collection\n\n" +
                    "Secure Peek does not collect or store any personal data from users.\n" +
                    "URLs provided for analysis are sent to external services, such as the Google Safe Browsing API, for threat analysis. These services may process the data in accordance with their own privacy policies.\n" +
                    "\n\n2. Data Usage\n\n" +
                    "The app only uses the URLs you input to provide threat analysis, metadata information, and screenshot previews.\n" +
                    "No user data is retained by the app or shared with third parties beyond the services used for analysis.\n" +
                    "\n\n3. Third-Party Services\n\n" +
                    "Secure Peek integrates with third-party services (e.g., Google Safe Browsing API) to perform its analysis. By using this app, you agree to their respective terms and privacy policies.\n" +
                    "\n" +
                    "4. Disclaimer of Results\n\n" +
                    "No Guarantee of Safety: Secure Peek aims to provide helpful insights about URLs, but it cannot guarantee absolute accuracy or safety.\n" +
                    "Unintended Results: The app may occasionally produce incorrect or unexpected results due to limitations in the underlying technologies. Users should exercise their own judgment when evaluating URLs.\n" +
                    "\n\n5. Limitation of Liability\n\n" +
                    "Secure Peek is provided on an \"as-is\" basis without any warranties, express or implied.\n" +
                    "By using the app, you acknowledge that the developer is not responsible for any harm, loss, or damage arising from the use of the app, including reliance on its analysis or actions taken based on its results.\n" +
                    "\n\n6. Changes to the Privacy Policy\n\n" +
                    "This policy may be updated from time to time. Users are encouraged to review it periodically for any changes. Continued use of the app signifies your acceptance of the updated policy.")
            .setPositiveButton("Close"){dialog, _ ->
                dialog.dismiss()
            }.create()
        dialog.show()
    }
}