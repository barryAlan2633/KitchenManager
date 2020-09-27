package com.barryalan.kitchenmanager13.view.shared

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.barryalan.kitchenmanager13.util.communication.UICommunicationListener
import java.lang.ClassCastException

abstract class BaseFragment : Fragment() {
    lateinit var uiCommunicationListener: UICommunicationListener
    private val TAG: String = "AppDebug"


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implements UICommunicationListener")
        }
    }
}

