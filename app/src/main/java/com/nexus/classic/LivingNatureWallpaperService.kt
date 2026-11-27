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
  val W=w.toFloat();val H=h.toFloat();val t=(System.currentTimeMillis()-start)/1000f
  val hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);val night=hour<5||hour>=19;val dusk=hour in 17..18||hour in 5..6
  val breeze=(wind.removeSuffix(" km/h").toFloatOrNull()?:8f).coerceIn(0f,45f)/45f
  val sway=sin(t*1.35f)*W*(.006f+.018f*breeze)
  val skyTop=if(night)Color.rgb(11,24,36)else if(dusk)Color.rgb(176,104,83)else Color.rgb(83,145,170)
  val skyBottom=if(night)Color.rgb(45,61,72)else if(dusk)Color.rgb(239,174,118)else Color.rgb(207,220,202)
  p.shader=LinearGradient(0f,0f,0f,H*.73f,skyTop,skyBottom,Shader.TileMode.CLAMP);c.drawRect(0f,0f,W,H,p);p.shader=null
  // distant atmospheric ridges
  for(i in 0..3){p.color=Color.argb(if(night)75 else 95,if(night)67 else 95,if(night)91 else 128,if(night)101 else 112)
   val yy=H*(.39f+i*.045f);val ridge=Path().apply{moveTo(0f,yy+H*.12f);cubicTo(W*.22f,yy-H*.10f,W*.38f,yy+H*.08f,W*.58f,yy-H*.04f);cubicTo(W*.78f,yy-H*.13f,W*.88f,yy+H*.03f,W,yy-H*.06f);lineTo(W,H);lineTo(0f,H);close()};c.drawPath(ridge,p)}
  // sun / moon glow
  val orbX=if(night)W*.77f else W*.19f;val orbY=if(dusk)H*.23f else H*.15f
  p.color=Color.argb(25,255,220,160);c.drawCircle(orbX,orbY,W*.095f,p)
  p.color=if(night)Color.rgb(235,236,215)else Color.rgb(255,219,147);c.drawCircle(orbX,orbY,W*.032f,p)
  if(weatherCode==45||weatherCode==48||rainy||hour in 5..7){for(i in 0..5){val drift=((t*W*.008f+i*W*.23f)%(W*1.4f))-W*.2f;val fy=H*(.43f+i*.055f);p.color=Color.argb(if(night)12 else 25,235,239,225);c.drawOval(drift,fy,drift+W*.42f,fy+H*.045f,p)}}
  // clouds drift with the real wind
  for(i in 0..4){val x=((i*W*.31f+t*W*(.006f+.018f*breeze))%(W*1.35f))-W*.15f;val y=H*(.11f+(i%3)*.055f)
   p.color=Color.argb(if(night)35 else if(rainy)155 else 75,245,247,239);c.drawOval(x,y,x+W*.20f,y+H*.025f,p);c.drawCircle(x+W*.07f,y,W*.033f,p);c.drawCircle(x+W*.12f,y-H*.012f,W*.04f,p)}
  // layered green valley
  p.color=if(night)Color.rgb(24,49,43)else Color.rgb(57,104,73)
  val hills=Path().apply{moveTo(0f,H*.53f);cubicTo(W*.18f,H*.39f,W*.33f,H*.55f,W*.52f,H*.46f);cubicTo(W*.72f,H*.37f,W*.83f,H*.53f,W,H*.43f);lineTo(W,H);lineTo(0f,H);close()};c.drawPath(hills,p)
  p.color=if(night)Color.rgb(14,38,32)else Color.rgb(33,78,49)
  val foreground=Path().apply{moveTo(0f,H*.66f);cubicTo(W*.2f,H*.55f,W*.36f,H*.65f,W*.55f,H*.56f);cubicTo(W*.76f,H*.53f,W*.87f,H*.65f,W,H*.56f);lineTo(W,H);lineTo(0f,H);close()};c.drawPath(foreground,p)
  // small distant trees
  for(i in 0..24){val x=i*W/24f;val y=H*(.56f+(i%4)*.025f);p.color=if(night)Color.rgb(12,35,28)else Color.rgb(27,67,40);c.drawRect(x,y,x+W*.009f,y+H*.09f,p);c.drawCircle(x,y,W*.025f,p);c.drawCircle(x+W*.02f,y-H*.018f,W*.03f,p)}
  // winding road: dry by default; wet reflections only when rain is active
  val road=Path().apply{moveTo(W*.40f,H*.59f);lineTo(W*.53f,H*.59f);cubicTo(W*.60f,H*.72f,W*.32f,H*.84f,W*.06f,H);lineTo(0f,H);cubicTo(W*.22f,H*.83f,W*.49f,H*.71f,W*.40f,H*.59f);close()}
  p.color=if(rainy)Color.rgb(48,61,62)else if(night)Color.rgb(31,47,45)else Color.rgb(82,91,78);c.drawPath(road,p)
  if(rainy){p.color=Color.argb(55,221,230,216);c.drawOval(W*.12f,H*.91f,W*.39f,H*.96f,p);c.drawOval(W*.28f,H*.82f,W*.42f,H*.85f,p)}
  p.color=Color.argb(95,237,220,177);p.strokeWidth=W*.003f;c.drawLine(W*.46f,H*.61f,W*.17f,H,p)
  // roadside grass
  for(i in 0..45){val x=i*W/45f;val y=H*(.72f+(i%7)*.04f);p.color=if(night)Color.rgb(28,62,39)else Color.rgb(55,104,54);p.strokeWidth=W*.002f;c.drawLine(x,y,x+sin(t+i)*W*.008f,y-H*.025f,p)}
  // tea stall wooden structure
  val sx=W*.62f;val sy=H*.43f
  p.color=if(night)Color.rgb(30,45,38)else Color.rgb(55,75,53);c.drawRect(sx,sy+H*.10f,W,sy+H*.42f,p)
  p.color=Color.rgb(91,58,37);val roof=Path().apply{moveTo(sx-W*.05f,sy+H*.10f);lineTo(W,sy-H*.02f);lineTo(W,sy+H*.045f);lineTo(sx,sy+H*.17f);close()};c.drawPath(roof,p)
  p.color=Color.rgb(130,91,57);for(i in 0..5){val x=sx+W*.035f+i*W*.055f;c.drawLine(x,sy+H*.08f,x,sy+H*.39f,p)}
  // stall counter, kettle, cups, biscuit jars
  p.color=Color.rgb(105,67,42);c.drawRect(sx-W*.015f,sy+H*.27f,W,sy+H*.31f,p)
  p.color=Color.rgb(190,126,70);c.drawRoundRect(W*.80f,sy+H*.18f,W*.87f,sy+H*.27f,W*.012f,W*.012f,p)
  p.color=Color.rgb(66,71,67);c.drawRoundRect(W*.82f,sy+H*.14f,W*.86f,sy+H*.19f,W*.015f,W*.015f,p)
  p.color=Color.rgb(201,154,91);c.drawCircle(W*.91f,sy+H*.22f,W*.025f,p);c.drawCircle(W*.96f,sy+H*.22f,W*.025f,p)
  val glow=(sin(t*3.1f)*.5f+.5f)
  p.color=Color.argb((28+glow*22).toInt(),255,171,73);c.drawCircle(W*.89f,sy+H*.12f,W*(.055f+glow*.012f),p);p.color=Color.rgb(255,187,87);c.drawRoundRect(W*.878f,sy+H*.09f,W*.902f,sy+H*.15f,W*.008f,W*.008f,p)
  // animated kettle steam
  drawSteam(c,W*.84f,sy+H*.15f,W*.020f,t,1.2f)
  // leafy tree canopy and branch
  p.color=Color.rgb(63,47,32);p.strokeWidth=W*.025f;p.strokeCap=Paint.Cap.ROUND;c.drawLine(W*.94f,0f,W*.77f,H*.28f,p);c.drawLine(W*.85f,H*.11f,W*.68f,H*.18f,p)
  for(i in 0..48){val x=(i%9)*W*.045f+W*.68f+sway*(i%3);val y=(i/9)*H*.035f-H*.025f+sin(t*.8f+i)*H*.003f;p.color=if(night)Color.rgb(23,58,37)else if(i%3==0)Color.rgb(93,132,49)else Color.rgb(43,102,46);c.save();c.rotate(sin(t*1.3f+i)*5f*breeze,x,y);c.drawOval(x-W*.022f,y-H*.012f,x+W*.022f,y+H*.012f,p);c.restore()}
  // seated tea drinker, with breathing, sipping motion and wind-tossed hair
  drawTeaDrinker(c,W,H,t,breeze)
  // floating leaves carried by wind
  for(i in 0..9){val x=((i*W*.137f+t*W*(.018f+.04f*breeze))%(W*1.1f))-W*.05f;val y=H*(.30f+((i*37)%55)/100f)+sin(t*1.8f+i)*H*.018f;p.color=if(i%2==0)Color.rgb(133,153,65)else Color.rgb(74,123,54);c.save();c.rotate(sin(t+i)*45f,x,y);c.drawOval(x-W*.009f,y-H*.004f,x+W*.009f,y+H*.004f,p);c.restore()}
  // birds gliding in the distance
  for(i in 0..2){val x=((i*W*.29f+t*W*.025f)%(W*1.15f))-W*.05f;val y=H*(.34f+i*.035f)+sin(t*1.7f+i)*H*.012f;p.color=Color.argb(190,29,37,31);p.style=Paint.Style.STROKE;p.strokeWidth=W*.002f;val wing=Path().apply{moveTo(x-W*.018f,y);quadTo(x-W*.008f,y-H*.012f,x,y);quadTo(x+W*.008f,y-H*.012f,x+W*.018f,y)};c.drawPath(wing,p);p.style=Paint.Style.FILL}
  if(night){for(i in 0..13){val fx=((i*W*.091f+t*W*(.004f+(i%3)*.001f))%(W*1.08f))-W*.03f;val fy=H*(.42f+(i%7)*.055f)+sin(t*1.5f+i)*H*.012f;val pulse=(sin(t*2.2f+i)*.5f+.5f);p.color=Color.argb((70+pulse*120).toInt(),255,218,129);c.drawCircle(fx,fy,W*.0025f,p);p.color=Color.argb((12+pulse*20).toInt(),255,198,93);c.drawCircle(fx,fy,W*.012f,p)}}
  // rain only in real rain; animated diagonal drops and puddle ripples
  if(rainy){p.color=Color.argb(115,213,231,240);p.strokeWidth=W*.0017f;for(i in 0..44){val x=(i*53f+t*W*.16f)%W;val y=(i*91f+t*H*.34f)%(H*.78f);c.drawLine(x,y,x-W*.008f,y+H*.022f,p)};for(i in 0..5){val x=W*(.12f+i*.075f);val y=H*(.91f+(i%2)*.025f);val r=((t*W*.012f+i*W*.015f)% (W*.04f));p.color=Color.argb(85,224,235,224);p.style=Paint.Style.STROKE;c.drawCircle(x,y,r,p);p.style=Paint.Style.FILL}}
  // weather and local conditions overlay
  p.color=Color.argb(225,255,255,255);p.typeface=Typeface.create("sans-serif",Typeface.NORMAL);p.textSize=W*.026f;c.drawText("NEXUS  •  LIVING NATURE",W*.075f,H*.17f,p)
  p.typeface=Typeface.create("sans-serif-light",Typeface.NORMAL);p.textSize=W*.092f;c.drawText(temp,W*.07f,H*.235f,p)
  p.typeface=Typeface.create("sans-serif",Typeface.NORMAL);p.textSize=W*.027f;c.drawText(sky,W*.075f,H*.27f,p)
  p.color=Color.argb(195,255,255,255);p.textSize=W*.021f;c.drawText("HUM  "+humidity+"    WIND  "+wind,W*.075f,H*.305f,p)
  p.color=Color.argb(190,255,221,151);c.drawText("SUNRISE  "+sunrise+"     SUNSET  "+sunset,W*.075f,H*.34f,p)
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