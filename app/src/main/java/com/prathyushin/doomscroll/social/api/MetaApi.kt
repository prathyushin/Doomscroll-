package com.prathyushin.doomscroll.social.api

import com.prathyushin.doomscroll.social.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MetaApi(private val client:OkHttpClient=OkHttpClient()) {
 fun instagram(token:String,cursor:String?):Page<SocialPost>{
  val accounts=get("https://graph.facebook.com/v23.0/me/accounts?fields=id,name,instagram_business_account&access_token=$token")
  val ig=accounts.optJSONArray("data")?.optJSONObject(0)?.optJSONObject("instagram_business_account")?.optString("id").orEmpty()
  if(ig.isBlank()) return Page(emptyList(),null)
  val u="https://graph.facebook.com/v23.0/$ig/media?fields=id,caption,media_type,media_url,permalink,timestamp,thumbnail_url,username&limit=15&access_token=$token"+(cursor?.let{"&after=$it"}?:"")
  return parse(get(u),SocialProvider.INSTAGRAM)
 }
 fun threads(token:String,cursor:String?):Page<SocialPost>{
  val u="https://graph.threads.net/v1.0/me/threads?fields=id,username,text,media_type,media_url,thumbnail_url,permalink,timestamp&limit=15&access_token=$token"+(cursor?.let{"&after=$it"}?:"")
  return parse(get(u),SocialProvider.THREADS)
 }
 private fun parse(j:JSONObject,p:SocialProvider):Page<SocialPost>{
  val a=j.optJSONArray("data")?:return Page(emptyList(),null); val out=mutableListOf<SocialPost>()
  for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=SocialPost(x.optString("id"),p,x.optString("username",p.name),x.optString(if(p==SocialProvider.THREADS)"text" else "caption").takeIf{it.isNotBlank()},x.optString("media_url").takeIf{it.isNotBlank()},x.optString("thumbnail_url").takeIf{it.isNotBlank()},x.optString("permalink").takeIf{it.isNotBlank()},x.optString("timestamp").takeIf{it.isNotBlank()})}
  return Page(out,j.optJSONObject("paging")?.optJSONObject("cursors")?.optString("after")?.takeIf{it.isNotBlank()})
 }
 private fun get(url:String):JSONObject{client.newCall(Request.Builder().url(url).build()).execute().use{r->val b=r.body?.string().orEmpty();if(!r.isSuccessful)throw IllegalStateException(runCatching{JSONObject(b).optJSONObject("error")?.optString("message")}.getOrNull()?:"Meta request failed (${r.code})");return JSONObject(b)}}
}
