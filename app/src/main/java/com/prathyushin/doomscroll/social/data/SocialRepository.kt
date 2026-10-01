package com.prathyushin.doomscroll.social.data

import com.prathyushin.doomscroll.social.api.MetaApi
import com.prathyushin.doomscroll.social.model.*
import java.util.concurrent.Executors

class SocialRepository(private val api:MetaApi=MetaApi()){
 private val executor=Executors.newFixedThreadPool(2)
 fun load(provider:SocialProvider,token:String,cursor:String?,ok:(Page<SocialPost>)->Unit,fail:(Throwable)->Unit){executor.execute{runCatching{if(provider==SocialProvider.INSTAGRAM)api.instagram(token,cursor) else api.threads(token,cursor)}.onSuccess(ok).onFailure(fail)}}
}
