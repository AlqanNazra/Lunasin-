package com.example.lunasin.utils

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

@Composable
fun OnboardingDotsIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == currentPage) Color.Black else Color.Gray,
                        shape = CircleShape
                    )
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun OnboardingNavigationButtons(
    currentPage: Int,
    pageCount: Int,
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onFinishClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentPage > 0) {
            Button(onClick = onPreviousClicked) {
                Text("Sebelumnya")
            }
        } else {
            Spacer(modifier = Modifier.size(0.dp))
        }

        if (currentPage < pageCount - 1) {
            Button(onClick = onNextClicked) {
                Text("Selanjutnya")
            }
        } else {
            Button(onClick = onFinishClicked) {
                Text("Selesai")
            }
        }
    }
}

object OnboardingPrefs {
    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    fun hasCompletedOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, true) }
    }

    // Fungsi tambahan untuk mereset status onboarding (untuk pengujian)
    fun resetOnboarding(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, false) }
    }
}