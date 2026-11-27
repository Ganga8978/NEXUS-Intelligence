package com.nexus.classic
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
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
 private var visible=false;private var w=0;private var h=0;private var temp="--°";private var sky="Finding local weather";private var humidity="--";private var wind="--";private var sunrise="--:--";private var sunset="--:--";private var rainy=false;private var updated=0L;private val start=System.currentTimeMillis();private val p=Paint(3)
 private val tick=object:Runnable{override fun run(){if(!visible)return;drawScene();handler.postDelayed(this,1200)}}
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
 val json=JSONObject(con.inputStream.bufferedReader().use{it.readText()});con.disconnect();val cur=json.getJSONObject("current");val day=json.getJSONObject("daily");val code=cur.optInt("weather_code",2)
 temp=String.format(Locale.getDefault(),"%.0f°",cur.optDouble("temperature_2m",Double.NaN));humidity=cur.optInt("relative_humidity_2m",-1).toString()+"%";wind=cur.optDouble("wind_speed_10m",0.0).roundToInt().toString()+" km/h"
 sunrise=day.getJSONArray("sunrise").optString(0).takeLast(5);sunset=day.getJSONArray("sunset").optString(0).takeLast(5)
 sky=when(code){0->"Clear sky";1,2,3->"Partly cloudy";45,48->"Misty";in 51..67->"Rain";in 71..77->"Snow";in 80..82->"Showers";in 95..99->"Thunderstorm";else->"Cloudy"}
 rainy=code in 51..67||code in 80..82||code>=95;updated=System.currentTimeMillis()
 }catch(_:Exception){sky="Weather temporarily unavailable"}}}
 private fun drawScene(){var c:Canvas?=null;try{c=surfaceHolder.lockCanvas()?:return;if(w<=0||h<=0){w=c.width;h=c.height};render(c)}catch(_:Exception){}finally{if(c!=null)try{surfaceHolder.unlockCanvasAndPost(c)}catch(_:Exception){}}}
 private fun render(c:Canvas){
 val W=w.toFloat();val H=h.toFloat();val t=(System.currentTimeMillis()-start)/1000f;val hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);val night=hour<5||hour>=19;val dusk=hour in 17..18||hour in 5..6
 val top=if(night)Color.rgb(12,25,34)else if(dusk)Color.rgb(177,112,88)else Color.rgb(102,151,169);val bottom=if(night)Color.rgb(42,61,69)else if(dusk)Color.rgb(235,173,119)else Color.rgb(205,211,194)
 p.shader=LinearGradient(0f,0f,0f,H*.72f,top,bottom,Shader.TileMode.CLAMP);c.drawRect(0f,0f,W,H,p);p.shader=null
 p.color=if(night)Color.rgb(242,236,211)else Color.argb(185,255,220,139);c.drawCircle(if(night)W*.78f else W*.18f,H*.16f,W*.038f,p)
 for(i in 0..3){val x=((i*W*.39f+t*(2f+i*.3f))%(W*1.3f))-W*.15f;val y=H*(.13f+i*.07f);p.color=if(night)Color.argb(65,195,207,207)else Color.argb(if(rainy)145 else 85,246,247,239);c.drawOval(x,y,x+W*.23f,y+H*.035f,p);c.drawCircle(x+W*.08f,y-W*.01f,W*.04f,p)}
 p.color=if(night)Color.rgb(31,55,56)else Color.rgb(76,113,91);val hill=Path().apply{moveTo(0f,H*.57f);cubicTo(W*.2f,H*.43f,W*.34f,H*.55f,W*.53f,H*.48f);cubicTo(W*.72f,H*.4f,W*.84f,H*.54f,W,H*.46f);lineTo(W,H);lineTo(0f,H);close()};c.drawPath(hill,p)
 p.color=if(night)Color.rgb(18,43,39)else Color.rgb(40,83,59);val road=Path().apply{moveTo(W*.39f,H*.59f);lineTo(W*.52f,H*.59f);cubicTo(W*.58f,H*.72f,W*.28f,H*.84f,W*.06f,H);lineTo(0f,H);cubicTo(W*.22f,H*.83f,W*.48f,H*.72f,W*.39f,H*.59f);close()};c.drawPath(road,p)
 p.color=Color.argb(95,239,227,183);c.drawLine(W*.45f,H*.61f,W*.18f,H,W*.007f,p)
 val sx=W*.63f;val sy=H*.48f;p.color=if(night)Color.rgb(24,46,40)else Color.rgb(39,79,61);c.drawRect(sx,sy+H*.06f,W,sy+H*.39f,p)
 p.color=Color.rgb(77,55,39);val roof=Path().apply{moveTo(sx-W*.04f,sy+H*.07f);lineTo(W,sy-H*.015f);lineTo(W,sy+H*.045f);lineTo(sx,sy+H*.13f);close()};c.drawPath(roof,p)
 p.color=Color.argb(220,255,190,93);c.drawCircle(W*.88f,sy+H*.15f,W*.012f,p);p.color=Color.argb(45,255,177,75);c.drawCircle(W*.88f,sy+H*.15f,W*.055f,p)
 p.color=Color.rgb(91,62,42);c.drawRect(W*.55f,H*.82f,W*.97f,H*.845f,p);c.drawRect(W*.59f,H*.845f,W*.62f,H*.94f,p);c.drawRect(W*.9f,H*.845f,W*.93f,H*.94f,p)
 p.color=Color.rgb(192,121,57);c.drawRoundRect(W*.73f,H*.77f,W*.78f,H*.82f,W*.008f,W*.008f,p)
 p.color=Color.argb(145,255,245,226);p.style=Paint.Style.STROKE;p.strokeWidth=W*.003f
 for(i in 0..2){val x=W*(.745f+i*.012f)+sin(t*1.1f+i)*W*.006f;val steam=Path().apply{moveTo(x,H*.77f);cubicTo(x-W*.02f,H*.74f,x+W*.02f,H*.73f,x,H*.70f)};c.drawPath(steam,p)};p.style=Paint.Style.FILL
 p.color=if(night)Color.rgb(16,40,29)else Color.rgb(26,70,40);c.drawCircle(-W*.03f,H*.02f,W*.19f,p);c.drawCircle(W*.19f,-H*.015f,W*.13f,p);c.drawCircle(W*.96f,H*.10f,W*.17f,p)
 if(rainy){p.color=Color.argb(85,220,235,240);p.strokeWidth=W*.0015f;for(i in 0..34){val x=(i*71f+t*12f)%W;val y=(i*113f+t*38f)%(H*.72f);c.drawLine(x,y,x-W*.006f,y+H*.018f,p)}}
 p.color=Color.argb(225,255,255,255);p.typeface=Typeface.create("sans-serif",Typeface.NORMAL);p.textSize=W*.027f;c.drawText("YOUR LOCATION",W*.085f,H*.19f,p)
 p.typeface=Typeface.create("sans-serif-light",Typeface.NORMAL);p.textSize=W*.105f;c.drawText(temp,W*.08f,H*.25f,p)
 p.typeface=Typeface.create("sans-serif",Typeface.NORMAL);p.textSize=W*.029f;c.drawText(sky,W*.085f,H*.285f,p)
 p.color=Color.argb(190,255,255,255);p.textSize=W*.022f;c.drawText("HUM "+humidity+"   WIND "+wind,W*.085f,H*.32f,p)
 p.color=Color.argb(205,255,221,143);c.drawText("SUN  "+sunrise+"     SUNSET  "+sunset,W*.085f,H*.36f,p)
 }
 }
}