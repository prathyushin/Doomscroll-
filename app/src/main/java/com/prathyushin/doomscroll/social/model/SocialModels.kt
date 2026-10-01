package com.prathyushin.doomscroll.social.model

enum class SocialProvider { INSTAGRAM, THREADS }
data class SocialPost(val id:String,val provider:SocialProvider,val author:String,val text:String?,val mediaUrl:String?,val thumbnailUrl:String?,val permalink:String?,val timestamp:String?)
data class Page<T>(val items:List<T>,val nextCursor:String?)
data class ProviderState<T>(val loading:Boolean=false,val items:List<T> = emptyList(),val nextCursor:String?=null,val error:String?=null)
