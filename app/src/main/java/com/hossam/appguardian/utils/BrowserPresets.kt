package com.hossam.appguardian.utils

/**
 * A hand-maintained list of common web browsers on Android, keyed by package name.
 *
 * This is deliberately NOT claimed to be exhaustive: there is no offline, server-free way to
 * enumerate "every browser ever published to Google Play" from inside an app -- that would
 * require querying an online catalog, which conflicts with the fully-offline requirement. What
 * follows covers the browsers the vast majority of users actually have installed. New/obscure
 * browsers can always be added individually from the normal "Installed Apps" tab.
 */
object BrowserPresets {

    data class Entry(val packageName: String, val displayName: String)

    val ALL: List<Entry> = listOf(
        Entry("com.android.chrome", "Google Chrome"),
        Entry("org.mozilla.firefox", "Firefox"),
        Entry("org.mozilla.focus", "Firefox Focus"),
        Entry("com.sec.android.app.sbrowser", "Samsung Internet"),
        Entry("com.microsoft.emmx", "Microsoft Edge"),
        Entry("com.opera.browser", "Opera"),
        Entry("com.opera.mini.native", "Opera Mini"),
        Entry("com.opera.gx", "Opera GX"),
        Entry("com.brave.browser", "Brave"),
        Entry("com.vivaldi.browser", "Vivaldi"),
        Entry("com.duckduckgo.mobile.android", "DuckDuckGo"),
        Entry("com.UCMobile.intl", "UC Browser"),
        Entry("com.ucmobile.lite", "UC Browser Mini"),
        Entry("com.kiwibrowser.browser", "Kiwi Browser"),
        Entry("com.CloudMosa.puffinFree", "Puffin Browser"),
        Entry("mobi.mgeek.TunnyBrowser", "Dolphin Browser"),
        Entry("com.ecosia.android", "Ecosia"),
        Entry("com.yandex.browser", "Yandex Browser"),
        Entry("com.mi.globalbrowser", "Mi Browser"),
        Entry("com.huawei.browser", "Huawei Browser"),
        Entry("com.naver.whale", "Naver Whale"),
        Entry("org.torproject.torbrowser", "Tor Browser"),
        Entry("com.amazon.cloud9", "Amazon Silk"),
        Entry("mark.via.gp", "Via Browser"),
        Entry("com.jio.web", "JioPages"),
        Entry("acr.browser.lightning", "Lightning Browser")
    )
}
