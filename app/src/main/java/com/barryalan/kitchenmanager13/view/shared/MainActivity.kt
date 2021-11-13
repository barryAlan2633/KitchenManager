package com.barryalan.kitchenmanager13.view.shared

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.util.communication.*

class MainActivity : AppCompatActivity(),
    UICommunicationListener {

    private val TAG: String = "AppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onUIMessageReceived(uiMessage: UIMessage) {
        when(uiMessage.uiMessageType){
            is UIMessageType.AreYouSureDialog ->{
                areYouSureDialog(
                    uiMessage.message,
                    uiMessage.uiMessageType.callback
                )
            }
            is UIMessageType.Toast->{
                displayToast(uiMessage.message)
            }
            is UIMessageType.Dialog ->{
                displayInfoDialog(uiMessage.message)
            }

            is UIMessageType.ErrorDialog->{
                displayErrorDialog(uiMessage.message)
            }

            is UIMessageType.None ->{
                Log.i(TAG,"onUIMessageReceived: ${uiMessage.message}")
            }
        }
    }
}
