package com.nexus.classic
import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.widget.*
class MainActivity : Activity() {
    private val names = listOf("Phone","Messages","Camera","Photos","Settings","Calendar","Clock","Calculator","Contacts","Mail","Browser","Maps","Music","Files","Downloads","Play Store","YouTube","WhatsApp","Instagram","Facebook","Telegram","Spotify","Netflix","Amazon","Chrome","Gmail","Drive","Gallery","Notes","Weather")
    private val drawables = listOf("nexus_phone","nexus_messages","nexus_camera","nexus_photos","nexus_settings","ic_calendar","ic_clock","ic_calculator","ic_contacts","ic_mail","ic_browser","ic_maps","ic_music","ic_files","ic_downloads","ic_playstore","ic_youtube","ic_whatsapp","ic_instagram","ic_facebook","ic_telegram","ic_spotify","ic_netflix","ic_amazon","ic_chrome","ic_gmail","ic_drive","ic_gallery","ic_notes","ic_weather")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,28,22,20); setBackgroundColor(Color.rgb(235,238,243)) }
        root.addView(TextView(this).apply { text="NEXUS Classic Evolution"; textSize=27f; setTextColor(Color.rgb(30,39,52)); typeface=Typeface.DEFAULT_BOLD })
        root.addView(TextView(this).apply { text="iPhone 4 • Metallic Edition"; textSize=13f; setTextColor(Color.DKGRAY); setPadding(0,4,0,18) })
        val grid=GridLayout(this).apply { columnCount=3 }
        names.forEachIndexed { i,n ->
            val tile=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(6,10,6,10) }
            val id=resources.getIdentifier(drawables[i],"drawable",packageName)
            tile.addView(ImageView(this).apply { setImageResource(id); layoutParams=LinearLayout.LayoutParams(76,76) })
            tile.addView(TextView(this).apply { text=n; textSize=11f; setTextColor(Color.rgb(45,55,68)); gravity=Gravity.CENTER; setPadding(0,5,0,0) })
            grid.addView(tile,GridLayout.LayoutParams().apply { width=0; height=GridLayout.LayoutParams.WRAP_CONTENT; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f) })
        }
        root.addView(grid)
        root.addView(TextView(this).apply { text="Apply from your launcher's Icon Pack / Theme settings. Nova Launcher and ADW-compatible launchers are supported. Black Wind support depends on its own third-party icon-pack integration."; textSize=13f; setTextColor(Color.rgb(75,82,92)); setPadding(0,18,0,0) })
        setContentView(ScrollView(this).apply { addView(root) })
    }
}