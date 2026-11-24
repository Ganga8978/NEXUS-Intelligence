package com.nexus.classic
import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.graphics.drawable.GradientDrawable
class MainActivity : Activity() {
    private val icons = listOf("Phone","Messages","Camera","Photos","Settings","Calendar","Clock","Calculator","Contacts","Mail","Browser","Maps","Music","Files","Downloads","Play Store","YouTube","WhatsApp","Instagram","Facebook","Telegram","Spotify","Netflix","Amazon","Chrome","Gmail","Drive","Gallery","Notes","Weather")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,28,22,20); setBackgroundColor(Color.rgb(235,238,243)) }
        root.addView(TextView(this).apply { text="NEXUS Classic"; textSize=28f; setTextColor(Color.rgb(30,39,52)); typeface=Typeface.create("sans-serif",Typeface.BOLD) })
        root.addView(TextView(this).apply { text="iPhone 4 Evolution  •  Metallic Edition"; textSize=13f; setTextColor(Color.DKGRAY); setPadding(0,4,0,18) })
        root.addView(TextView(this).apply { text="iPhone 4 Evolution • Core icon redesign"; textSize=17f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Color.rgb(45,55,68)); setPadding(0,8,0,12) })
        val grid=GridLayout(this).apply { columnCount=3 }
        icons.forEachIndexed { i,n ->
            val tile=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(6,10,6,10) }
            val icon=ImageView(this).apply { setImageResource(resources.getIdentifier("ic_"+listOf("phone","messages","camera","photos","settings","calendar","clock","calculator","contacts","mail","browser","maps","music","files","downloads","playstore","youtube","whatsapp","instagram","facebook","telegram","spotify","netflix","amazon","chrome","gmail","drive","gallery","notes","weather")[i],"drawable",packageName)); layoutParams=LinearLayout.LayoutParams(68,68) }
            tile.addView(icon); tile.addView(TextView(this).apply { text=n; textSize=11f; setTextColor(Color.rgb(45,55,68)); gravity=Gravity.CENTER; setPadding(0,5,0,0) })
            grid.addView(tile,GridLayout.LayoutParams().apply { width=0; height=GridLayout.LayoutParams.WRAP_CONTENT; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f) })
        }
        root.addView(grid)
        root.addView(TextView(this).apply { text="Install, then open Black Wind → Settings → Icon pack. Select NEXUS Classic.\n\nThe pack uses the Nova / ADW theme format recognised by Black Wind."; textSize=13f; setTextColor(Color.rgb(75,82,92)); setPadding(0,18,0,0) })
        setContentView(ScrollView(this).apply { addView(root) })
    }
}