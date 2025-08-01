// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.Actor
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.toRatingInt
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.getQualityFromName
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder

class WebteIzle : MainAPI() {
    override var mainUrl              = "https://webteizle.info"
    override var name                 = "WebteIzle"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.Movie)

    // ! CloudFlare bypass
    override var sequentialMainPage = true        // * https://recloudstream.github.io/dokka/-cloudstream/com.lagradost.cloudstream3/-main-a-p-i/index.html#-2049735995%2FProperties%2F101969414
    override var sequentialMainPageDelay       = 50L  // ? 0.05 saniye
    override var sequentialMainPageScrollDelay = 50L  // ? 0.05 saniye

    // ! CloudFlare v2
    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor      by lazy { CloudflareInterceptor(cloudflareKiller) }

    class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request  = chain.request()
            val response = chain.proceed(request)
            val doc = Jsoup.parse(response.peekBody(1024 * 1024).string())

            if (doc.text().contains("Just a moment")) {
                return cloudflareKiller.intercept(chain)
            }

            return response
        }
    }

    override val mainPage = mainPageOf(
        "${mainUrl}/film-izle/"                   to "Güncel",
        "${mainUrl}/yeni-filmler/"                to "Yeni",
        "${mainUrl}/tavsiye-filmler/"             to "Tavsiye",
        "${mainUrl}/filtre/SAYFA?tur=Aile"        to "Aile",
        "${mainUrl}/filtre/SAYFA?tur=Aksiyon"     to "Aksiyon",
        "${mainUrl}/filtre/SAYFA?tur=Animasyon"   to "Animasyon",
        "${mainUrl}/filtre/SAYFA?tur=Belgesel"    to "Belgesel",
        "${mainUrl}/filtre/SAYFA?tur=Bilim-Kurgu" to "Bilim Kurgu",
        "${mainUrl}/filtre/SAYFA?tur=Biyografi"   to "Biyografi",
        "${mainUrl}/filtre/SAYFA?tur=Dram"        to "Dram",
        "${mainUrl}/filtre/SAYFA?tur=Fantastik"   to "Fantastik",
        "${mainUrl}/filtre/SAYFA?tur=Gerilim"     to "Gerilim",
        "${mainUrl}/filtre/SAYFA?tur=Gizem"       to "Gizem",
        "${mainUrl}/filtre/SAYFA?tur=Komedi"      to "Komedi",
        "${mainUrl}/filtre/SAYFA?tur=Korku"       to "Korku",
        "${mainUrl}/filtre/SAYFA?tur=Macera"      to "Macera",
        "${mainUrl}/filtre/SAYFA?tur=Romantik"    to "Romantik",
        "${mainUrl}/filtre/SAYFA?tur=Spor"        to "Spor",
        "${mainUrl}/filtre/SAYFA?tur=Tarihi"      to "Tarihi",
        "${mainUrl}/filtre/SAYFA?tur=Western"     to "Western"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if ("SAYFA" in request.data) request.data.replace("SAYFA", "$page") else "${request.data}$page"
        val document = app.get(url).document
        val home = document.select("div.golgever").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.filmname")?.text() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        @Suppress("NAME_SHADOWING", "BlockingMethodInNonBlockingContext")
        val query = URLEncoder.encode(query, "ISO-8859-9")

        val document = app.get(
            "${mainUrl}/filtre?a=${query}",
            referer = "${mainUrl}/",
            interceptor = interceptor
        ).document

        return document.select("div.golgever").mapNotNull { it.toSearchResult() }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("[property='og:title']")?.attr("content")?.substringBefore(" izle") ?: return null
        val poster = fixUrlNull(document.selectFirst("div.card img")?.attr("data-src"))
        val year = document.selectXpath("//td[contains(text(), 'Vizyon')]/following-sibling::td").text().trim().split(" ").last().toIntOrNull()
        val description = document.selectFirst("blockquote")?.text()?.trim()
        val tags = document.selectXpath("//a[@itemgroup='genre']").map { it.text() }
        val rating = document.selectFirst("div.detail")?.text()?.trim()?.replace(",", ".").toRatingInt()
        val duration = document.selectXpath("//td[contains(text(), 'Süre')]/following-sibling::td").text().trim().split(" ").first().toIntOrNull()
        val trailer = document.selectFirst("button#fragman")?.attr("data-ytid")
        val actors = document.selectXpath("//div[@data-tab='oyuncular']//a").map {
            Actor(it.selectFirst("span")!!.text().trim(), fixUrlNull(it.selectFirst("img")!!.attr("data-src")))
        }

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.year = year
            this.plot = description
            this.tags = tags
            this.rating = rating
            this.duration = duration
            addTrailer("https://www.youtube.com/embed/${trailer}")
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        Log.d("WBTI", "data » $data")
        val document = app.get(data).document

        val filmId = document.selectFirst("button#wip")?.attr("data-id") ?: return false
        Log.d("WBTI", "filmId » $filmId")

        val dilList = mutableListOf<String>()
        if (document.selectFirst("div.golge a[href*=dublaj]") != null) {
            dilList.add("0")
        }

        if (document.selectFirst("div.golge a[href*=altyazi]") != null) {
            dilList.add("1")
        }

        dilList.forEach {
            val dilAd = if (it == "0") "Dublaj" else "Altyazı"

            val playerApi = app.post(
                "${mainUrl}/ajax/dataAlternatif3.asp",
                headers = mapOf("X-Requested-With" to "XMLHttpRequest"),
                data = mapOf(
                    "filmid" to filmId,
                    "dil" to it,
                    "s" to "",
                    "b" to "",
                    "bot" to "0"
                )
            ).text
            val playerData = AppUtils.tryParseJson<DataAlternatif>(playerApi) ?: return@forEach

            for (thisEmbed in playerData.data) {
                val embedApi = app.post(
                    "${mainUrl}/ajax/dataEmbed.asp",
                    headers = mapOf("X-Requested-With" to "XMLHttpRequest"),
                    data = mapOf("id" to thisEmbed.id.toString())
                ).document

                var iframe = fixUrlNull(embedApi.selectFirst("iframe")?.attr("src"))

if (iframe == null) {
    val scriptSource = embedApi.html()

    // Önce vidmoly gibi doğrudan eşleşmeyi dene
    val matchResult = Regex("""(vidmoly)\('([\d\w]+)','""").find(scriptSource)

    if (matchResult != null) {
        val platform = matchResult.groupValues[1]
        val vidId = matchResult.groupValues[2]

        iframe = when (platform) {
            "vidmoly" -> "https://vidmoly.to/embed-${vidId}.html"
            else -> null
        }
    } else {
        // Eğer vidmoly yoksa, _0x5c93 tanımı var mı diye kontrol et
        val hasDzen = Regex("""var\s+_0x5c93\s*=""").containsMatchIn(scriptSource)
        if (hasDzen) {
            val dzenMatch = Regex("""var\s+vid\s*=\s*['"]([^'"]+)['"]""").find(scriptSource)
            val videoId = dzenMatch?.groupValues?.get(1)
            if (videoId != null) {
                iframe = "https://dzen.ru/embed/$videoId"
            }
        } else {
            Log.d("WBTI", "scriptSource » $scriptSource")
        }
    }
}

                    if (iframe != null) {
                    Log.d("WBTI", "iframe » $iframe")
                    loadExtractor(iframe, "${mainUrl}/", subtitleCallback, callback)
                    }
            }
        }
        return true
    }
}
