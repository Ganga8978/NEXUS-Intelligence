package com.nexus.classic
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class ThemeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(TextView(this).apply { text = "NEXUS Classic icon pack"; textSize = 20f }) }
}