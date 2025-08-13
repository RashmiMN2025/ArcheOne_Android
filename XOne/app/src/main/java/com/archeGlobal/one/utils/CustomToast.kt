package com.archeGlobal.one.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast

object CustomToast {

    // Universal toast method - replaces all Toast.makeText() calls
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            val toast = Toast(context)
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(android.R.layout.simple_list_item_1, null)

            val textView = layout.findViewById<TextView>(android.R.id.text1)
            textView.apply {
                text = message
                textSize = 14f
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
                maxLines = 10

                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 10f
                    setColor(Color.parseColor("#DC000000")) // Standard toast gray background (87% opacity)
                }
                background = drawable
            }

            toast.apply {
                view = layout
                this.duration = duration
                setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 64)
            }

            toast.show()
        } catch (e: Exception) {
            // Fallback to regular toast if custom toast fails
            Toast.makeText(context, message, duration).show()
        }
    }

    fun showLongToast(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }

    fun showErrorToast(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }
}
