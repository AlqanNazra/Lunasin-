package com.example.lunasin.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import coil.decode.ImageSource
import kotlin.reflect.KClass

class PopupUtils {

    fun showSimplePopup(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "OK",
        negativeText: String? = null,
        neutralText: String? = null,
        positiveAction: (() -> Unit)? = null,
        negativeAction: (() -> Unit)? = null,
        neutralAction: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        if (positiveText.isNotEmpty()) {
            builder.setPositiveButton(positiveText) { dialog, _ ->
                positiveAction?.invoke()
                dialog.dismiss()
            }
        }

        negativeText?.let {
            if (it.isNotEmpty()) {
                builder.setNegativeButton(it) { dialog, _ ->
                    negativeAction?.invoke()
                    dialog.dismiss()
                }
            }
        }

        neutralText?.let {
            if (it.isNotEmpty()) {
                builder.setNeutralButton(it) { dialog, _ ->
                    neutralAction?.invoke()
                    dialog.dismiss()
                }
            }
        }

        builder.show()
    }

    fun showNavigationPopup(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Ya",
        negativeText: String = "Tidak",
        positiveDestination: KClass<*>? = null,
        negativeDestination: KClass<*>? = null
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(positiveText) { dialog, _ ->
            positiveDestination?.let {
                val intent = Intent(context, it.java)
                context.startActivity(intent)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(negativeText) { dialog, _ ->
            negativeDestination?.let {
                val intent = Intent(context, it.java)
                context.startActivity(intent)
            }
            dialog.dismiss()
        }

        builder.show()
    }

    fun Popupbergambar (
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Ya",
        negativeText: String = "Tidak",
        positiveDestination: KClass<*>? = null,
        imageResourceId: Int)
    {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(200,200)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageResource(imageResourceId)
            setPadding(0, 0, 0, 16)
        }

        val messageText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = message
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        layout.addView(imageView)
        layout.addView(messageText)
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setView(layout)

        builder.setPositiveButton(positiveText) { dialog, _ ->
            positiveDestination?.let {
                val intent = Intent(context, it.java)
                context.startActivity(intent)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(negativeText) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // End Class
}