// ! https://github.com/hexated/cloudstream-extensions-hexated/blob/master/Hdfilmcehennemi/src/main/kotlin/com/hexated/Hdfilmcehennemi.kt

package com.keyiflerolsun

import android.util.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.Jsoup

class HDFilmCehennemi : MainAPI() {
    override var mainUrl              = "https://www.hdfilmcehennemi.nl"
    override var name                 = "HDFilmCehennemi"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = true
    override val supportedTypes       = setOf(TvType.Movie, TvType.TvSeries)

    override val mainPage = mainPageOf(
        mainUrl to "Yeni Eklenen Filmler",
        "${mainUrl}/yabancidiziizle-2"                    to "Yeni Eklenen Diziler",
        "${mainUrl}/category/tavsiye-filmler-izle2"       to "Tavsiye Filmler",
        "${mainUrl}/imdb-7-puan-uzeri-filmler"            to "IMDB 7+ Filmler",
        "${mainUrl}/en-cok-yorumlananlar-1"               to "En Çok Yorumlananlar",
        "${mainUrl}/en-cok-begenilen-filmleri-izle"       to "En Çok Beğenilenler",
        "${mainUrl}/tur/aile-filmleri-izleyin-6"          to "Aile Filmleri",
        "${mainUrl}/tur/aksiyon-filmleri-izleyin-3"       to "Aksiyon Filmleri",
        "${mainUrl}/tur/animasyon-filmlerini-izleyin-4"   to "Animasyon Filmleri",
        "${mainUrl}/tur/belgesel-filmlerini-izle-1"       to "Belgesel Filmleri",
        "${mainUrl}/tur/bilim-kurgu-filmlerini-izleyin-2" to "Bilim Kurgu Filmleri",
        "${mainUrl}/tur/komedi-filmlerini-izleyin-1"      to "Komedi Filmleri",
        "${mainUrl}/tur/korku-filmlerini-izle-2/"         to "Korku Filmleri",
        "${mainUrl}/tur/romantik-filmleri-izle-1"         to "Romantik Filmleri"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document

        val home: List<SearchResponse>?

        home = document.select("div.section-content a.poster").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title     = this.selectFirst("strong.poster-title")?.text() ?: return null
        val href      = fixUrlNull(this.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun search(query: String): List<SearchResponse> {
        val response      = app.get(
            "${mainUrl}/search?q=${query}",
            headers = mapOf("X-Requested-With" to "fetch")
        ).parsedSafe<Results>() ?: return emptyList()
        val searchResults = mutableListOf<SearchResponse>()

        response.results.forEach { resultHtml ->
            val document = Jsoup.parse(resultHtml)

            val title     = document.selectFirst("h4.title")?.text() ?: return@forEach
            val href      = fixUrlNull(document.selectFirst("a")?.attr("href")) ?: return@forEach
            val posterUrl = fixUrlNull(document.selectFirst("img")?.attr("src")) ?: fixUrlNull(document.selectFirst("img")?.attr("data-src"))

            searchResults.add(
                newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = posterUrl?.replace("/thumb/", "/list/") }
            )
        }

        return searchResults
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title       = document.selectFirst("h1.section-title")?.text()?.substringBefore(" izle") ?: return null
        val poster      = fixUrlNull(document.select("aside.post-info-poster img.lazyload").lastOrNull()?.attr("data-src"))
        val tags        = document.select("div.post-info-genres a").map { it.text() }
        val year        = document.selectFirst("div.post-info-year-country a")?.text()?.trim()?.toIntOrNull()
        val tvType      = if (document.select("div.seasons").isEmpty()) TvType.Movie else TvType.TvSeries
        val description = document.selectFirst("article.post-info-content > p")?.text()?.trim()
        val rating      = document.selectFirst("div.post-info-imdb-rating span")?.text()?.substringBefore("(")?.trim()?.toRatingInt()
        val actors      = document.select("div.post-info-cast a").map {
            Actor(it.selectFirst("strong")!!.text(), it.select("img").attr("data-src"))
        }

        val recommendations = document.select("div.section-slider-container div.slider-slide").mapNotNull {
                val recName      = it.selectFirst("a")?.attr("title") ?: return@mapNotNull null
                val recHref      = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
                val recPosterUrl = fixUrlNull(it.selectFirst("img")?.attr("data-src")) ?: fixUrlNull(it.selectFirst("img")?.attr("src"))

                newTvSeriesSearchResponse(recName, recHref, TvType.TvSeries) {
                    this.posterUrl = recPosterUrl
                }
            }

        return if (tvType == TvType.TvSeries) {
            val trailer  = document.selectFirst("div.post-info-trailer button")?.attr("data-modal")?.substringAfter("trailer/", "")?.let { if (it.isNotEmpty()) "https://www.youtube.com/watch?v=$it" else null }
            Log.d("HDCH", "Trailer: $trailer")
            val episodes = document.select("div.seasons-tab-content a").mapNotNull {
                val epName    = it.selectFirst("h4")?.text()?.trim() ?: return@mapNotNull null
                val epHref    = fixUrlNull(it.attr("href")) ?: return@mapNotNull null
                val epEpisode = Regex("""(\d+)\. ?Bölüm""").find(epName)?.groupValues?.get(1)?.toIntOrNull()
                val epSeason  = Regex("""(\d+)\. ?Sezon""").find(epName)?.groupValues?.get(1)?.toIntOrNull() ?: 1

                newEpisode(epHref) {
                    this.name = epName
                    this.season = epSeason
                    this.episode = epEpisode
                }
            }

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl       = poster
                this.year            = year
                this.plot            = description
                this.tags            = tags
                this.rating          = rating
                this.recommendations = recommendations
                addActors(actors)
                addTrailer(trailer)
            }
        } else {
            val trailer = document.selectFirst("div.post-info-trailer button")?.attr("data-modal")?.substringAfter("trailer/", "")?.let { if (it.isNotEmpty()) "https://www.youtube.com/watch?v=$it" else null }
            Log.d("HDCH", "Trailer: $trailer")
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl       = poster
                this.year            = year
                this.plot            = description
                this.tags            = tags
                this.rating          = rating
                this.recommendations = recommendations
                addActors(actors)
                addTrailer(trailer)
            }
        }
    }

    private suspend fun invokeLocalSource(source: String, url: String, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit ) {
        val script    = app.get(url, referer = "${mainUrl}/").document.select("script").find { it.data().contains("sources:") }?.data() ?: return
        Log.d("HDCH", "script » $script")
        val videoData = getAndUnpack(script).substringAfter("file_link=\"").substringBefore("\";")
        Log.d("HDCH", "videoData » $videoData")
        val subData   = script.substringAfter("tracks: [").substringBefore("]")
        Log.d("HDCH", "subData » $subData")

        callback.invoke(
            newExtractorLink(
                source  = source,
                name    = source,
                url     = base64Decode(videoData),
                type    = INFER_TYPE
            ) {
                headers = mapOf("Referer" to "${mainUrl}/")
                quality = Qualities.Unknown.value
            }
        )

        AppUtils.tryParseJson<List<SubSource>>("[${subData}]")?.filter { it.kind == "captions" }?.map {
            subtitleCallback.invoke(
                SubtitleFile(it.label.toString(), fixUrl(it.file.toString()))
            )
        }
    }

override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    Log.d("HDCH", "data » $data")
    val document = app.get(data).document

    document.select("div.alternative-links").map { element ->
        element to element.attr("data-lang").uppercase()
    }.forEach { (element, langCode) ->
        element.select("button.alternative-link").map { button ->
            button.text().replace("(HDrip Xbet)", "").trim() + " $langCode" to button.attr("data-video")
        }.forEach { (source, videoID) ->
            val apiGet = app.get(
                "${mainUrl}/video/$videoID/",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "X-Requested-With" to "fetch"
                ),
                referer = data
            ).text
            Log.d("HDCH", "Found videoID: $videoID")
            var iframe = Regex("""data-src=\\"([^"]+)""").find(apiGet)?.groupValues?.get(1)!!.replace("\\", "")
            Log.d("HDCH", "$iframe » $iframe")
            if (iframe.contains("rapidrame")) {
                iframe = "${mainUrl}/playerr/" + iframe.substringAfter("?rapidrame_id=")
            } else if (iframe.contains("rplayer")) {
                iframe = "${mainUrl}/playerr/" + iframe.substringAfter("/rplayer/")
            }
            Log.d("HDCH", "$source » $videoID » $iframe")
            invokeLocalSource(source, iframe, subtitleCallback, callback)
        }
    }
    return true
}
    private data class SubSource(
        @JsonProperty("file")  val file: String?  = null,
        @JsonProperty("label") val label: String? = null,
        @JsonProperty("kind")  val kind: String?  = null
    )

    data class Results(
        @JsonProperty("results") val results: List<String> = arrayListOf()
    )
}