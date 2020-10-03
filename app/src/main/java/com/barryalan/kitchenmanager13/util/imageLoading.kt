package com.barryalan.kitchenmanager13.util

import android.content.Context
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.barryalan.kitchenmanager13.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadCircleImage(uri: Uri?, progressDrawable: CircularProgressDrawable) {
    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.ic_error_black_24dp)
    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .circleCrop()
        .into(this)
}

fun ImageView.loadImage(uri: Uri?, progressDrawable: CircularProgressDrawable) {
    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.ic_error_black_24dp)
    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

fun loadCircleImage(view: ImageButton, url: Uri?){
    view.loadCircleImage(url, getProgressDrawable(view.context))
}

fun getProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply{
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
}

