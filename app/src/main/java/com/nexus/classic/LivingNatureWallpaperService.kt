package com.nexus.classic
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.*
class LivingNatureWallpaperService:WallpaperService(){
 override fun onCreateEngine():Engine=NatureEngine()
 inner class NatureEngine:Engine(){
 private val handler=Handler(Looper.getMainLooper());private val worker=Executors.newSingleThreadExecutor()
 private var visible=false;private var w=0;private var h=0;private var temp="--°";private var sky="Finding local weather";private var humidity="--";private var wind="--";private var sunrise="--:--";private var sunset="--:--";private var rainy=false;private var updated=0L;private var weatherCode=2;private val start=System.currentTimeMillis();private val p=Paint(3)
 private val powerManager by lazy { this@LivingNatureWallpaperService.getSystemService(POWER_SERVICE) as PowerManager }
 private val tick=object:Runnable{override fun run(){if(!visible)return;drawScene();val interval=if(powerManager.isPowerSaveMode)66L else 33L;handler.postDelayed(this,interval)}}
 override fun onVisibilityChanged(v:Boolean){visible=v;if(v){if(System.currentTimeMillis()-updated>1800000)loadWeather();handler.removeCallbacks(tick);handler.post(tick)}else handler.removeCallbacks(tick)}
 override fun onSurfaceChanged(holder:SurfaceHolder,format:Int,width:Int,height:Int){super.onSurfaceChanged(holder,format,width,height);w=width;h=height;drawScene()}
 override fun onDestroy(){visible=false;handler.removeCallbacks(tick);worker.shutdownNow();super.onDestroy()}
 private fun loadWeather(){worker.execute{try{
 val lm=this@LivingNatureWallpaperService.getSystemService(LOCATION_SERVICE) as LocationManager
 val fine=this@LivingNatureWallpaperService.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
 val coarse=this@LivingNatureWallpaperService.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
 var loc:Location?=null
 if(fine||coarse)for(provider in lm.getProviders(true)){try{val x=lm.getLastKnownLocation(provider);if(x!=null&&(loc==null||x.time>loc!!.time))loc=x}catch(_:SecurityException){}}
 if(loc==null){sky="Enable location permission";return@execute}
 val endpoint="https://api.open-meteo.com/v1/forecast?latitude="+loc!!.latitude+"&longitude="+loc!!.longitude+"&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=sunrise,sunset&timezone=auto&forecast_days=1"
 val con=URL(endpoint).openConnection() as HttpURLConnection;con.connectTimeout=9000;con.readTimeout=9000
 val json=JSONObject(con.inputStream.bufferedReader().use{it.readText()});con.disconnect();val cur=json.getJSONObject("current");val day=json.getJSONObject("daily");val code=cur.optInt("weather_code",2);weatherCode=code
 temp=String.format(Locale.getDefault(),"%.0f°",cur.optDouble("temperature_2m",Double.NaN));humidity=cur.optInt("relative_humidity_2m",-1).toString()+"%";wind=cur.optDouble("wind_speed_10m",0.0).roundToInt().toString()+" km/h"
 sunrise=day.getJSONArray("sunrise").optString(0).takeLast(5);sunset=day.getJSONArray("sunset").optString(0).takeLast(5)
 sky=when(code){0->"Clear sky";1,2,3->"Partly cloudy";45,48->"Misty";in 51..67->"Rain";in 71..77->"Snow";in 80..82->"Showers";in 95..99->"Thunderstorm";else->"Cloudy"}
 rainy=code in 51..67||code in 80..82||code>=95;updated=System.currentTimeMillis()
 }catch(_:Exception){sky="Weather temporarily unavailable"}}}
 private fun drawScene(){var c:Canvas?=null;try{c=surfaceHolder.lockCanvas()?:return;if(w<=0||h<=0){w=c.width;h=c.height};render(c)}catch(_:Exception){}finally{if(c!=null)try{surfaceHolder.unlockCanvasAndPost(c)}catch(_:Exception){}}}
 private fun render(c:Canvas){
  val W=w.toFloat();val H=h.toFloat();val t=(System.currentTimeMillis()-startTime)/1000f
  val hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
  val sceneName=when { rainy -> "nexus_rain"; hour in 5..7 || hour in 17..18 -> "nexus_dusk"; hour<5 || hour>=19 -> "nexus_dusk"; else -> "nexus_day" }
  val resId=resources.getIdentifier(sceneName,"drawable",packageName)
  val bitmap=if(resId!=0) BitmapFactory.decodeResource(resources,resId) else null
  if(bitmap!=null){
   val scale=max(W/bitmap.width,H/bitmap.height)
   val bw=W/scale;val bh=H/scale
   val pan=sin(t*.10f)*bitmap.width*.012f
   val left=((bitmap.width-bw)/2f+pan).coerceIn(0f,(bitmap.width-bw).coerceAtLeast(0f))
   val top=((bitmap.height-bh)/2f).coerceIn(0f,(bitmap.height-bh).coerceAtLeast(0f))
   val src=Rect(left.toInt(),top.toInt(),(left+bw).toInt().coerceAtMost(bitmap.width),(top+bh).toInt().coerceAtMost(bitmap.height))
   p.color=Color.WHITE;p.alpha=255;c.drawBitmap(bitmap,src,Rect(0,0,w,h),p)
  } else { p.shader=LinearGradient(0f,0f,0f,H,Color.rgb(80,130,150),Color.rgb(30,65,48),Shader.TileMode.CLAMP);c.drawRect(0f,0f,W,H,p);p.shader=null }
  // Atmospheric animation layered over the photographic scene.
  if(rainy){
   p.color=Color.argb(105,224,237,242);p.strokeWidth=max(1f,W*.0015f)
   for(i in 0..62){val x=(i*W*.071f+t*W*.20f)%W;val y=(i*H*.137f+t*H*.38f)%(H*1.05f);c.drawLine(x,y,x-W*.006f,y+H*.018f,p)}
   for(i in 0..5){val x=W*(.08f+i*.16f);val y=H*(.83f+(i%2)*.045f);val radius=((t*W*.018f+i*W*.011f)%(W*.035f));p.color=Color.argb(65,232,241,237);p.style=Paint.Style.STROKE;c.drawCircle(x,y,radius,p);p.style=Paint.Style.FILL}
  }
  // Leaves drift across the foreground in response to measured wind.
  val breeze=((wind.removeSuffix(" km/h").toFloatOrNull()?:8f).coerceIn(0f,45f))/45f
  for(i in 0..7){val x=((i*W*.173f+t*W*(.012f+.025f*breeze))%(W*1.12f))-W*.06f;val y=H*(.20f+(i%5)*.13f)+sin(t*1.2f+i)*H*.012f;p.color=if(i%2==0)Color.argb(190,115,151,67)else Color.argb(180,69,112,54);c.save();c.rotate(sin(t*1.4f+i)*32f,x,y);c.drawOval(x-W*.009f,y-H*.003f,x+W*.009f,y+H*.003f,p);c.restore()}
  // Gentle mist is reserved for fog codes; it never makes a dry road look wet.
  if(weatherCode==45||weatherCode==48){for(i in 0..3){val x=((t*W*.009f+i*W*.31f)%(W*1.3f))-W*.15f;val y=H*(.40f+i*.10f);p.color=Color.argb(24,239,243,233);c.drawOval(x,y,x+W*.45f,y+H*.035f,p)}}
  // Compact, unobtrusive live weather label.
  p.color=Color.argb(185,12,22,20);c.drawRoundRect(W*.045f,H*.035f,W*.42f,H*.145f,W*.018f,W*.018f,p)
  p.color=Color.WHITE;p.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);p.textSize=W*.030f;c.drawText("NEXUS  •  LIVING NATURE",W*.065f,H*.075f,p)
  p.typeface=Typeface.create("sans-serif-light",Typeface.NORMAL);p.textSize=W*.066f;c.drawText(temp,W*.065f,H*.125f,p)
  p.typeface=Typeface.create("sans-serif",Typeface.NORMAL);p.textSize=W*.022f;c.drawText("$sky  •  $humidity  •  $wind",W*.065f,H*.165f,p)
 }
 private fun drawSteam(c:Canvas,x:Float,y:Float,width:Float,t:Float,speed:Float){
  val old=p.color;p.color=Color.argb(135,255,246,226);p.style=Paint.Style.STROKE;p.strokeWidth=width*.12f;p.strokeCap=Paint.Cap.ROUND
  for(i in 0..3){val phase=t*speed+i*.72f;val drift=sin(phase)*width*.8f;val rise=(phase%(2.8f))/2.8f;val path=Path().apply{moveTo(x+drift,y-rise*width*5f);cubicTo(x-width+drift,y-width*2f-rise*width*4f,x+width+drift,y-width*3f-rise*width*3f,x+drift,y-width*5f-rise*width*2f)};c.drawPath(path,p)}
  p.style=Paint.Style.FILL;p.color=old
 }
 private fun drawTeaDrinker(c:Canvas,W:Float,H:Float,t:Float,breeze:Float){
  val breath=sin(t*1.25f)*H*.0035f;val lean=sin(t*.42f)*W*.002f
  val cx=W*.57f+lean;val headY=H*.66f+breath
  // bench behind body
  p.color=Color.rgb(103,68,43);c.drawRoundRect(W*.39f,H*.79f,W*.76f,H*.815f,W*.008f,W*.008f,p);c.drawRect(W*.42f,H*.81f,W*.44f,H*.94f,p);c.drawRect(W*.70f,H*.81f,W*.72f,H*.94f,p)
  // legs, relaxed seated pose
  p.color=Color.rgb(35,43,39);val legs=Path().apply{moveTo(cx-W*.045f,H*.75f);cubicTo(cx-W*.11f,H*.81f,cx-W*.12f,H*.85f,cx-W*.20f,H*.89f);lineTo(cx-W*.19f,H*.92f);cubicTo(cx-W*.10f,H*.91f,cx-W*.04f,H*.85f,cx+W*.005f,H*.82f);lineTo(cx+W*.08f,H*.78f);close()};c.drawPath(legs,p)
  p.color=Color.rgb(73,70,58);c.drawRoundRect(cx-W*.22f,H*.89f,cx-W*.14f,H*.925f,W*.012f,W*.012f,p)
  // torso and shoulder
  p.color=Color.rgb(103,78,54);val torso=Path().apply{moveTo(cx-W*.055f,headY+H*.045f);cubicTo(cx-W*.12f,H*.70f,cx-W*.10f,H*.78f,cx-W*.06f,H*.80f);lineTo(cx+W*.10f,H*.80f);cubicTo(cx+W*.13f,H*.73f,cx+W*.08f,H*.69f,cx+W*.035f,headY+H*.05f);close()};c.drawPath(torso,p)
  // neck and head in three-quarter profile
  p.color=Color.rgb(151,105,73);c.drawRoundRect(cx-W*.025f,headY+H*.015f,cx+W*.025f,headY+H*.09f,W*.012f,W*.012f,p);c.drawOval(cx-W*.047f,headY-H*.045f,cx+W*.043f,headY+H*.065f,p)
  // ear and face profile
  p.color=Color.rgb(177,125,89);c.drawCircle(cx+W*.036f,headY+H*.01f,W*.012f,p)
  // wind-lifted hair strands, moving opposite wind direction
  p.color=Color.rgb(28,27,25);c.drawOval(cx-W*.05f,headY-H*.065f,cx+W*.045f,headY+H*.005f,p)
  p.style=Paint.Style.STROKE;p.strokeWidth=W*.006f;p.strokeCap=Paint.Cap.ROUND
  for(i in 0..6){val root=cx-W*.035f+i*W*.011f;val lift=sin(t*2.0f+i*.8f)*W*.012f*breeze;val hair=Path().apply{moveTo(root,headY-H*.04f);quadTo(root-W*.015f-lift,headY-H*.10f,root-W*.035f-lift,headY-H*(.085f+.018f*breeze))};c.drawPath(hair,p)}
  p.style=Paint.Style.FILL
  // arm reaches toward tea cup, gently moving as he sips
  val sipCycle=(sin(t*.42f)*.5f+.5f);val sip=if(sipCycle>.88f) -(sipCycle-.88f)*H*.30f else sin(t*.75f)*H*.002f;p.color=Color.rgb(151,105,73);p.strokeWidth=W*.026f;p.strokeCap=Paint.Cap.ROUND
  c.drawLine(cx+W*.035f,H*.715f,cx+W*.09f,H*.68f+sip,p);c.drawLine(cx+W*.09f,H*.68f+sip,cx+W*.07f,H*.635f+sip,p)
  // cup in hand with hot tea
  p.color=Color.rgb(207,143,78);c.drawRoundRect(cx+W*.048f,H*.61f+sip,cx+W*.085f,H*.65f+sip,W*.006f,W*.006f,p)
  p.color=Color.rgb(247,205,132);c.drawRect(cx+W*.052f,H*.615f+sip,cx+W*.081f,H*.622f+sip,p)
  p.color=Color.rgb(185,128,74);p.style=Paint.Style.STROKE;p.strokeWidth=W*.004f;c.drawCircle(cx+W*.09f,H*.63f+sip,W*.012f,p);p.style=Paint.Style.FILL
  drawSteam(c,cx+W*.066f,H*.608f+sip,W*.013f,t,1.55f)
  // subtle shoulder breathing and hair wind response
 }
 }
}