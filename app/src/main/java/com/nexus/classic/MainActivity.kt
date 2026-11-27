package com.nexus.classic
import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.widget.*
class MainActivity:Activity(){
 private val names=listOf("Phone","Messages","Camera","Photos","Settings","Calendar","Clock","Calculator","Contacts","Mail","Browser","Maps","Music","Files","Downloads","Play Store","YouTube","WhatsApp","Instagram","Facebook","Telegram","Spotify","Netflix","Amazon","Chrome","Gmail","Drive","Gallery","Notes","Weather")
 private val icons=listOf("nexus_phone","nexus_messages","nexus_camera","nexus_photos","nexus_settings","classic_calendar","classic_clock","classic_calculator","classic_contacts","classic_mail","classic_browser","classic_maps","classic_music","classic_files","classic_downloads","classic_playstore","classic_youtube","classic_whatsapp","classic_instagram","classic_facebook","classic_telegram","classic_spotify","classic_netflix","classic_amazon","classic_chrome","classic_gmail","classic_drive","classic_gallery","classic_notes","classic_weather")
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 val root=LinearLayout(this).apply{orientation=1;setPadding(22,28,22,20);setBackgroundColor(Color.rgb(235,238,243))}
 root.addView(TextView(this).apply{text="NEXUS Classic Evolution";textSize=27f;setTextColor(Color.rgb(30,39,52));typeface=Typeface.DEFAULT_BOLD})
 root.addView(TextView(this).apply{text="iPhone 4 • Metallic Edition";textSize=13f;setTextColor(Color.DKGRAY);setPadding(0,4,0,18)})
 val card=LinearLayout(this).apply{orientation=1;setPadding(18,18,18,18);setBackgroundColor(Color.rgb(28,48,39))}
 card.addView(TextView(this).apply{text="NEXUS LIVING NATURE";textSize=19f;typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE)})
 card.addView(TextView(this).apply{text="A living roadside scene following your GPS weather and daylight.";textSize=13f;setTextColor(Color.rgb(218,229,220));setPadding(0,7,0,12)})
 card.addView(Button(this).apply{text="Preview & apply live wallpaper";setOnClickListener{val i=Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(this@MainActivity,LivingNatureWallpaperService::class.java));startActivity(i)}})
 root.addView(card);root.addView(TextView(this).apply{text="ICON PACK";textSize=12f;typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.rgb(75,82,92));setPadding(0,22,0,8)})
 val grid=GridLayout(this).apply{columnCount=3}
 names.forEachIndexed{i,n->val tile=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER;setPadding(6,10,6,10)};val id=resources.getIdentifier(icons[i],"drawable",packageName);tile.addView(ImageView(this).apply{setImageResource(id);layoutParams=LinearLayout.LayoutParams(76,76)});tile.addView(TextView(this).apply{text=n;textSize=11f;setTextColor(Color.rgb(45,55,68));gravity=Gravity.CENTER});grid.addView(tile,GridLayout.LayoutParams().apply{width=0;height=GridLayout.LayoutParams.WRAP_CONTENT;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f)})}
 root.addView(grid);setContentView(ScrollView(this).apply{addView(root)})
 val f=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
 val c=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
 if(!f&&!c)requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),71)
 }
}