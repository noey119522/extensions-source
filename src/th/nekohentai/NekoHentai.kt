package eu.kanade.tachiyomi.extension.th.nekohentai

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

class NekoHentai : ParsedHttpSource() {

    override val name = "Neko Hentai"
    override val baseUrl = "https://neko-hentai.net"
    override val lang = "th"
    override val supportsLatest = true

    // 1. หน้าหลัก / มังงะยอดนิยม
    // สมมติว่าหน้าแรกมีรายการมังงะ
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/page/$page/", headers)

    // Selector: กล่องคลุมของแต่ละเรื่องในหน้าแรก
    override fun popularMangaSelector() = "div.box" 

    override fun popularMangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            // ดึงชื่อเรื่อง
            title = element.select("div.title h2").text()
            // ดึงรูปปก
            thumbnail_url = element.select("div.img img").attr("src")
            // ดึงลิงก์ (สำคัญมาก)
            setUrlWithoutDomain(element.select("a").attr("href"))
        }
    }

    override fun popularMangaNextPageSelector() = "a.next" // ปุ่มถัดไป

    // 2. อัปเดตล่าสุด (มักจะใช้โครงสร้างเดียวกับ Popular)
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/page/$page/", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // 3. ค้นหา
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/page/$page/?s=$query", headers)
    }
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // 4. หน้ารายละเอียดมังงะ (เมื่อกดเข้าไปในเรื่อง)
    override fun mangaDetailsParse(document: Document): SManga {
        return SManga.create().apply {
            title = document.select("h1.title").text()
            description = document.select("div.imptdt").text() // เนื้อเรื่องย่อ
            author = document.select("div.author i").text()
            thumbnail_url = document.select("div.thumb img").attr("src")
            status = SManga.UNKNOWN
        }
    }

    // 5. รายการตอน (Chapter List)
    // Selector: กล่องรายการตอน
    override fun chapterListSelector() = "div.cl ul li"

    override fun chapterFromElement(element: Element): SChapter {
        return SChapter.create().apply {
            name = element.select("span.leftoff").text() // ชื่อตอน
            setUrlWithoutDomain(element.select("a").attr("href"))
            // วันที่อัปโหลด (ถ้าไม่มีให้ใส่ 0L)
            date_upload = 0L 
        }
    }

    // 6. หน้าอ่าน (ดึงรูปภาพ)
    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        
        // ตรงนี้สำคัญ: ต้อง Inspect ดูว่ารูปภาพอยู่ใน div ไหน
        // ปกติเว็บแนวนี้มักจะ id="readerarea" หรือ class="reading-content"
        document.select("div#readerarea img").forEachIndexed { index, element ->
            val url = element.attr("src")
            // เช็คว่าไม่ใช่รูปเสียหรือ icon
            if (url.isNotEmpty()) {
                pages.add(Page(index, "", url))
            }
        }
        return pages
    }

    override fun imageUrlParse(document: Document) = ""
}
