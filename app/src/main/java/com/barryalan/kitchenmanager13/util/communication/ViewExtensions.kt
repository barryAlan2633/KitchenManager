package com.barryalan.kitchenmanager13.util.communication

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.barryalan.kitchenmanager13.R

fun Activity.displayToast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.displayToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.displaySuccessDialog(message: String?) {
    MaterialDialog(this)
        .cornerRadius(12f)
        .show {
            title(R.string.dialog_title_success)
            message(text = message)
            positiveButton(R.string.dialog_button_positive)
        }
}

fun Activity.displayErrorDialog(errorMessage: String?) {
    MaterialDialog(this)
        .cornerRadius(12f)
        .show {
            title(R.string.dialog_title_error)
            message(text = errorMessage)
            positiveButton(R.string.dialog_button_positive)
        }
}

fun Activity.displayInfoDialog(message: String?) {
    MaterialDialog(this)
        .cornerRadius(12f)
        .show {
            title(R.string.dialog_title_info)
            message(text = message)
            positiveButton(R.string.dialog_button_positive)
        }
}

fun Activity.areYouSureDialog(message: String, callback: AreYouSureCallBack) {
    MaterialDialog(this)
        .cornerRadius(12f)
        .show {
            title(R.string.dialog_title_are_you_sure)
            message(text = message)
            negativeButton(R.string.dialog_button_negative) {
                callback.cancel()
            }
            positiveButton(R.string.dialog_button_positive){
                callback.proceed()
            }
        }
}



interface AreYouSureCallBack {

    fun proceed()

    fun cancel()
}

