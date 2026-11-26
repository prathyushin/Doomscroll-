package com.prathyushin.doomscroll.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadsPolicyTest {

    @Test
    fun allowsThreadsAndMetaAuthHosts() {
        val policy = ThreadsPolicy()
        assertEquals(ThreadsRouteAction.ALLOW, policy.evaluateUrl("https://www.threads.net/@user").action)
        assertEquals(ThreadsRouteAction.ALLOW, policy.evaluateUrl("https://www.threads.com/").action)
        assertEquals(
            ThreadsRouteAction.ALLOW_AUTH_REDIRECT,
            policy.evaluateUrl("https://www.instagram.com/accounts/login/").action
        )
    }

    @Test
    fun blocksAppHandoffDeepLinks() {
        val policy = ThreadsPolicy()
        assertEquals(
            ThreadsRouteAction.BLOCK_APP_HANDOFF,
            policy.evaluateUrl("intent://threads.net/#Intent;package=com.instagram.barcelona;end").action
        )
        assertEquals(
            ThreadsRouteAction.BLOCK_APP_HANDOFF,
            policy.evaluateUrl("barcelona://open").action
        )
    }

    @Test
    fun blocksConsecutiveRedirectLoops() {
        val policy = ThreadsPolicy()
        val url = "https://www.threads.com/login"
        repeat(6) { policy.evaluateUrl(url) }
        assertEquals(ThreadsRouteAction.BLOCK_LOOP_REDIRECT, policy.evaluateUrl(url).action)
    }

    @Test
    fun opensExternalLinksInBrowser() {
        val policy = ThreadsPolicy()
        assertEquals(
            ThreadsRouteAction.OPEN_EXTERNAL_BROWSER,
            policy.evaluateUrl("https://news.ycombinator.com/").action
        )
    }
}
