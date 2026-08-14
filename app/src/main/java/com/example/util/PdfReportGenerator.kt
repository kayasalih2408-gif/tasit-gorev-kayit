package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DriverProfile
import com.example.data.model.DutyRecord
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfReportGenerator {

    // Standard A4 dimensions in points (72 DPI): 595 x 842
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    /**
     * Generates a single official "Taşıt Görev Fişi" PDF and returns its File.
     */
    fun generateDutySlipPdf(
        context: Context,
        duty: DutyRecord,
        profile: DriverProfile? = null
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            drawDutySlip(canvas, duty, profile)
            pdfDocument.finishPage(page)

            // Save file
            val outputDir = File(context.cacheDir, "pdf_reports").apply { mkdirs() }
            val fileName = "Tasit_Gorev_Fisi_${duty.date.replace(".", "_")}_ID${duty.id}.pdf"
            val outputFile = File(outputDir, fileName)
            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Generates an official "Aylık Taşıt Görev Çizelgesi" summary PDF for a given month.
     */
    fun generateMonthlyReportPdf(
        context: Context,
        duties: List<DutyRecord>,
        year: Int,
        month: Int,
        profile: DriverProfile? = null
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            drawMonthlyReport(canvas, duties, year, month, profile)
            pdfDocument.finishPage(page)

            val outputDir = File(context.cacheDir, "pdf_reports").apply { mkdirs() }
            val monthName = DateUtils.getMonthName(month)
            val fileName = "Aylik_Gorev_Cizelgesi_${year}_${monthName}.pdf"
            val outputFile = File(outputDir, fileName)
            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawDutySlip(canvas: Canvas, duty: DutyRecord, profile: DriverProfile?) {
        val forestGreen = Color.rgb(27, 67, 50)
        val lightGreenBg = Color.rgb(235, 245, 238)
        val tableHeaderBg = Color.rgb(216, 243, 220)
        val darkGrey = Color.rgb(40, 40, 40)
        val lightBorder = Color.rgb(180, 200, 190)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer Page Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = forestGreen
        canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), paint)

        paint.strokeWidth = 0.8f
        canvas.drawRect(28f, 28f, (PAGE_WIDTH - 28).toFloat(), (PAGE_HEIGHT - 28).toFloat(), paint)

        // Top Header Banner
        paint.style = Paint.Style.FILL
        paint.color = lightGreenBg
        canvas.drawRoundRect(RectF(35f, 35f, (PAGE_WIDTH - 35).toFloat(), 125f), 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = forestGreen
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(RectF(35f, 35f, (PAGE_WIDTH - 35).toFloat(), 125f), 6f, 6f, paint)

        // Header Titles
        paint.style = Paint.Style.FILL
        paint.color = forestGreen
        paint.textAlign = Paint.Align.CENTER

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("T.C.", (PAGE_WIDTH / 2).toFloat(), 52f, paint)
        canvas.drawText("TARIM VE ORMAN BAKANLIĞI", (PAGE_WIDTH / 2).toFloat(), 66f, paint)

        paint.textSize = 13f
        canvas.drawText("ORMAN GENEL MÜDÜRLÜĞÜ", (PAGE_WIDTH / 2).toFloat(), 82f, paint)

        paint.textSize = 11f
        paint.color = Color.rgb(45, 106, 79)
        canvas.drawText("57 NOLU ORMAN KADASTRO BAŞMÜHENDİSLİĞİ", (PAGE_WIDTH / 2).toFloat(), 98f, paint)

        paint.textSize = 14f
        paint.color = forestGreen
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RESMİ TAŞIT GÖREV FİŞİ", (PAGE_WIDTH / 2).toFloat(), 117f, paint)

        // Fiş No & Tarih Bar
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.color = darkGrey
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val slipNumber = if (duty.recordNumber.isNotBlank()) duty.recordNumber else "57-OKB-${duty.id}"
        canvas.drawText("Fiş No: $slipNumber", 40f, 142f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Düzenleme Tarihi: ${duty.date}", (PAGE_WIDTH - 40).toFloat(), 142f, paint)

        // SECTION 1: ARAÇ VE ŞOFÖR BİLGİLERİ
        var currentY = 152f
        currentY = drawSectionHeader(canvas, "1. ARAÇ VE ŞOFÖR BİLGİLERİ", currentY)

        val sec1Top = currentY
        val sec1Height = 52f
        drawBox(canvas, 35f, sec1Top, (PAGE_WIDTH - 35).toFloat(), sec1Top + sec1Height, Color.WHITE, lightBorder)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9.5f
        paint.color = darkGrey

        val driver = duty.driverName.ifBlank { profile?.driverName ?: "Salih Kaya" }
        val plate = duty.vehiclePlate.ifBlank { profile?.vehiclePlate ?: "57 OG 057" }
        val model = duty.vehicleModel.ifBlank { profile?.vehicleModel ?: "Dacia Duster 4x4" }
        val dept = duty.departmentName.ifBlank { profile?.departmentName ?: "57 Nolu Orman Kadastro Başmühendisliği" }

        // Left Col
        drawLabelValue(canvas, 45f, sec1Top + 18f, "Taşıt Plakası:", plate)
        drawLabelValue(canvas, 45f, sec1Top + 38f, "Marka / Model:", model)

        // Right Col
        val midX = 310f
        drawLabelValue(canvas, midX, sec1Top + 18f, "Taşıt Şoförü:", driver)
        drawLabelValue(canvas, midX, sec1Top + 38f, "Bağlı Birim:", dept)

        currentY = sec1Top + sec1Height + 12f

        // SECTION 2: GÖREVLENDİRME DETAYLARI
        currentY = drawSectionHeader(canvas, "2. GÖREVLENDİRME VE GÜZERGAH BİLGİLERİ", currentY)

        val sec2Top = currentY
        val sec2Height = 58f
        drawBox(canvas, 35f, sec2Top, (PAGE_WIDTH - 35).toFloat(), sec2Top + sec2Height, Color.WHITE, lightBorder)

        drawLabelValue(canvas, 45f, sec2Top + 18f, "Görev Konusu:", duty.dutyType.ifBlank { "Orman Kadastro Sahası İnceleme ve Parsel Ölçümü" })
        drawLabelValue(canvas, 45f, sec2Top + 38f, "Gidilecek Yer / Güzergah:", duty.destination.ifBlank { "Kadastro Çalışma Sahası" })
        drawLabelValue(canvas, 45f, sec2Top + 53f, "Görev Durumu:", if (duty.isCompleted) "GÖREV TAMAMLANDI" else "DEVAM EDEN AKTİF GÖREV", isHighlighted = true)

        currentY = sec2Top + sec2Height + 12f

        // SECTION 3: GÖREVLİ HEYET / PERSONEL LİSTESİ
        currentY = drawSectionHeader(canvas, "3. GÖREVLİ HEYET (PERSONEL LİSTESİ)", currentY)

        val tableTop = currentY
        val rowHeight = 22f
        val colWidths = floatArrayOf(35f, 40f, 220f, 180f, 85f) // Total: 525 (width = 525)

        // Table Header
        paint.style = Paint.Style.FILL
        paint.color = tableHeaderBg
        canvas.drawRect(35f, tableTop, (PAGE_WIDTH - 35).toFloat(), tableTop + rowHeight, paint)

        paint.style = Paint.Style.STROKE
        paint.color = forestGreen
        paint.strokeWidth = 1f
        canvas.drawRect(35f, tableTop, (PAGE_WIDTH - 35).toFloat(), tableTop + rowHeight, paint)

        paint.style = Paint.Style.FILL
        paint.color = forestGreen
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("S.No", 55f, tableTop + 15f, paint)
        canvas.drawText("Görevli Adı Soyadı", 185f, tableTop + 15f, paint)
        canvas.drawText("Unvanı / Görevi", 385f, tableTop + 15f, paint)
        canvas.drawText("İmza", 517f, tableTop + 15f, paint)

        val personnelList = listOf(
            Pair(duty.personnel1Name.ifBlank { "—" }, duty.personnel1Title.ifBlank { "—" }),
            Pair(duty.personnel2Name.ifBlank { "—" }, duty.personnel2Title.ifBlank { "—" }),
            Pair(duty.personnel3Name.ifBlank { "—" }, duty.personnel3Title.ifBlank { "—" })
        )

        var rY = tableTop + rowHeight
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = darkGrey

        for (i in personnelList.indices) {
            val p = personnelList[i]
            paint.style = Paint.Style.FILL
            paint.color = if (i % 2 == 0) Color.WHITE else Color.rgb(248, 252, 249)
            canvas.drawRect(35f, rY, (PAGE_WIDTH - 35).toFloat(), rY + rowHeight, paint)

            paint.style = Paint.Style.STROKE
            paint.color = lightBorder
            canvas.drawRect(35f, rY, (PAGE_WIDTH - 35).toFloat(), rY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.color = darkGrey
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${i + 1}", 55f, rY + 15f, paint)

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(p.first, 85f, rY + 15f, paint)
            canvas.drawText(p.second, 295f, rY + 15f, paint)

            rY += rowHeight
        }

        currentY = rY + 12f

        // SECTION 4: HAREKET VE KİLOMETRE CETVELİ
        currentY = drawSectionHeader(canvas, "4. HAREKET VE KİLOMETRE CETVELİ", currentY)

        val kmTableTop = currentY
        val kmRowH = 24f

        // 6 columns: Çıkış Saati | Dönüş Saati | Çıkış KM | Dönüş KM | Net KM | Yakıt/Açıklama
        paint.style = Paint.Style.FILL
        paint.color = tableHeaderBg
        canvas.drawRect(35f, kmTableTop, (PAGE_WIDTH - 35).toFloat(), kmTableTop + kmRowH, paint)

        paint.style = Paint.Style.STROKE
        paint.color = forestGreen
        paint.strokeWidth = 1f
        canvas.drawRect(35f, kmTableTop, (PAGE_WIDTH - 35).toFloat(), kmTableTop + kmRowH, paint)

        paint.style = Paint.Style.FILL
        paint.color = forestGreen
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("Çıkış Saati", 78f, kmTableTop + 16f, paint)
        canvas.drawText("Dönüş Saati", 165f, kmTableTop + 16f, paint)
        canvas.drawText("Çıkış KM", 255f, kmTableTop + 16f, paint)
        canvas.drawText("Dönüş KM", 345f, kmTableTop + 16f, paint)
        canvas.drawText("Net KM", 435f, kmTableTop + 16f, paint)
        canvas.drawText("Görev Durumu", 515f, kmTableTop + 16f, paint)

        // Values Row
        val valRowTop = kmTableTop + kmRowH
        val valRowH = 28f
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRect(35f, valRowTop, (PAGE_WIDTH - 35).toFloat(), valRowTop + valRowH, paint)

        paint.style = Paint.Style.STROKE
        paint.color = lightBorder
        canvas.drawRect(35f, valRowTop, (PAGE_WIDTH - 35).toFloat(), valRowTop + valRowH, paint)

        paint.style = Paint.Style.FILL
        paint.color = darkGrey
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText(duty.startTime, 78f, valRowTop + 18f, paint)
        canvas.drawText(duty.endTime ?: "—", 165f, valRowTop + 18f, paint)
        canvas.drawText("${duty.startKm} km", 255f, valRowTop + 18f, paint)
        canvas.drawText(if (duty.endKm != null) "${duty.endKm} km" else "—", 345f, valRowTop + 18f, paint)

        val netKmText = if (duty.netKm != null) "${duty.netKm} KM" else if (duty.endKm != null) "${duty.endKm - duty.startKm} KM" else "—"
        paint.color = forestGreen
        canvas.drawText(netKmText, 435f, valRowTop + 18f, paint)

        paint.textSize = 8.5f
        paint.color = if (duty.isCompleted) Color.rgb(27, 120, 50) else Color.rgb(180, 80, 0)
        canvas.drawText(if (duty.isCompleted) "Tamamlandı" else "Görevde", 515f, valRowTop + 18f, paint)

        currentY = valRowTop + valRowH + 12f

        // SECTION 5: NOTLAR VE AÇIKLAMA (Varsa)
        if (duty.notes.isNotBlank()) {
            currentY = drawSectionHeader(canvas, "5. AÇIKLAMA VE NOTLAR", currentY)
            val noteBoxTop = currentY
            val noteBoxH = 34f
            drawBox(canvas, 35f, noteBoxTop, (PAGE_WIDTH - 35).toFloat(), noteBoxTop + noteBoxH, Color.WHITE, lightBorder)
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 9f
            paint.color = darkGrey
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(duty.notes, 45f, noteBoxTop + 20f, paint)
            currentY = noteBoxTop + noteBoxH + 12f
        }

        // SECTION 6: İMZA VE ONAY ALANLARI
        currentY = Math.max(currentY + 10f, 650f)
        currentY = drawSectionHeader(canvas, "İMZA VE ONAY BÖLÜMÜ", currentY)

        val sigTop = currentY
        val sigBoxHeight = 85f
        val sigColW = (PAGE_WIDTH - 70f) / 3f

        for (i in 0..2) {
            val left = 35f + (i * sigColW)
            val right = left + sigColW
            drawBox(canvas, left, sigTop, right, sigTop + sigBoxHeight, Color.WHITE, lightBorder)

            paint.style = Paint.Style.FILL
            paint.color = forestGreen
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            paint.textAlign = Paint.Align.CENTER

            val title = when (i) {
                0 -> "TAŞIT ŞOFÖRÜ"
                1 -> "GÖREVLİ HEYET BAŞKANI"
                else -> "ONAYLAYAN"
            }
            val subtitle = when (i) {
                0 -> driver
                1 -> duty.personnel1Name.ifBlank { "Mühendis / Heyet Bşk." }
                else -> profile?.chiefEngineerName ?: "Orman Kadastro Başmühendisi"
            }

            canvas.drawText(title, left + (sigColW / 2), sigTop + 18f, paint)

            paint.color = darkGrey
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText(subtitle, left + (sigColW / 2), sigTop + 33f, paint)

            paint.color = Color.GRAY
            paint.textSize = 8f
            canvas.drawText("(İmza)", left + (sigColW / 2), sigTop + 72f, paint)
        }

        // Footer note
        paint.color = Color.GRAY
        paint.textSize = 7.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Bu belge 57 Nolu Orman Kadastro Başmühendisliği Taşıt Görev Kayıt ve Raporlama Sistemi tarafından üretilmiştir.",
            (PAGE_WIDTH / 2).toFloat(),
            (PAGE_HEIGHT - 35).toFloat(),
            paint
        )
    }

    private fun drawMonthlyReport(
        canvas: Canvas,
        duties: List<DutyRecord>,
        year: Int,
        month: Int,
        profile: DriverProfile?
    ) {
        val forestGreen = Color.rgb(27, 67, 50)
        val lightGreenBg = Color.rgb(235, 245, 238)
        val tableHeaderBg = Color.rgb(216, 243, 220)
        val darkGrey = Color.rgb(40, 40, 40)
        val lightBorder = Color.rgb(180, 200, 190)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Borders
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = forestGreen
        canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), paint)

        // Header
        paint.style = Paint.Style.FILL
        paint.color = lightGreenBg
        canvas.drawRoundRect(RectF(35f, 35f, (PAGE_WIDTH - 35).toFloat(), 110f), 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = forestGreen
        paint.textAlign = Paint.Align.CENTER

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("T.C. TARIM VE ORMAN BAKANLIĞI - ORMAN GENEL MÜDÜRLÜĞÜ", (PAGE_WIDTH / 2).toFloat(), 52f, paint)

        paint.textSize = 12f
        canvas.drawText("57 NOLU ORMAN KADASTRO BAŞMÜHENDİSLİĞİ", (PAGE_WIDTH / 2).toFloat(), 70f, paint)

        paint.textSize = 13f
        val monthName = DateUtils.getMonthName(month).uppercase(Locale("tr", "TR"))
        canvas.drawText("AYLIK TAŞIT GÖREV VE KİLOMETRE ÇİZELGESİ (${monthName} ${year})", (PAGE_WIDTH / 2).toFloat(), 95f, paint)

        // Summary bar
        var currentY = 125f
        val totalDuties = duties.size
        val totalKm = duties.sumOf { it.netKm ?: ((it.endKm ?: it.startKm) - it.startKm) }

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        drawBox(canvas, 35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + 30f, Color.WHITE, lightBorder)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.color = darkGrey
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val plate = profile?.vehiclePlate ?: "57 OG 057"
        val driver = profile?.driverName ?: "Salih Kaya"
        canvas.drawText("Plaka: $plate  |  Şoför: $driver", 45f, currentY + 19f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = forestGreen
        canvas.drawText("Toplam Görev: $totalDuties Adet  |  Toplam Yapılan: $totalKm KM", (PAGE_WIDTH - 45).toFloat(), currentY + 19f, paint)

        currentY += 40f

        // Table of Duties
        val rowH = 18f
        paint.style = Paint.Style.FILL
        paint.color = tableHeaderBg
        canvas.drawRect(35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + rowH, paint)

        paint.style = Paint.Style.STROKE
        paint.color = forestGreen
        canvas.drawRect(35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + rowH, paint)

        paint.style = Paint.Style.FILL
        paint.color = forestGreen
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("Tarih", 65f, currentY + 12f, paint)
        canvas.drawText("Gidilen Yer / Güzergah", 175f, currentY + 12f, paint)
        canvas.drawText("Görev Konusu", 310f, currentY + 12f, paint)
        canvas.drawText("Çıkış KM", 395f, currentY + 12f, paint)
        canvas.drawText("Dönüş KM", 450f, currentY + 12f, paint)
        canvas.drawText("Net KM", 515f, currentY + 12f, paint)

        currentY += rowH
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val maxRows = Math.min(duties.size, 25)
        for (i in 0 until maxRows) {
            val d = duties[i]
            val rColor = if (i % 2 == 0) Color.WHITE else Color.rgb(248, 252, 249)
            paint.style = Paint.Style.FILL
            paint.color = rColor
            canvas.drawRect(35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + rowH, paint)

            paint.style = Paint.Style.STROKE
            paint.color = lightBorder
            canvas.drawRect(35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + rowH, paint)

            paint.style = Paint.Style.FILL
            paint.color = darkGrey
            paint.textSize = 7.5f

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(d.date, 65f, currentY + 12f, paint)

            paint.textAlign = Paint.Align.LEFT
            val destText = if (d.destination.length > 24) d.destination.substring(0, 22) + "..." else d.destination
            canvas.drawText(destText, 105f, currentY + 12f, paint)

            val typeText = if (d.dutyType.length > 20) d.dutyType.substring(0, 18) + "..." else d.dutyType
            canvas.drawText(typeText, 250f, currentY + 12f, paint)

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${d.startKm}", 395f, currentY + 12f, paint)
            canvas.drawText(if (d.endKm != null) "${d.endKm}" else "-", 450f, currentY + 12f, paint)

            val net = d.netKm ?: if (d.endKm != null) (d.endKm - d.startKm) else 0
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = forestGreen
            canvas.drawText("$net KM", 515f, currentY + 12f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = darkGrey

            currentY += rowH
        }

        if (duties.isEmpty()) {
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            canvas.drawRect(35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + 30f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = lightBorder
            canvas.drawRect(35f, currentY, (PAGE_WIDTH - 35).toFloat(), currentY + 30f, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.GRAY
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Seçilen aya ait kayıtlı taşıt görevi bulunamadı.", (PAGE_WIDTH / 2).toFloat(), currentY + 18f, paint)
            currentY += 30f
        }

        // Signatures at bottom
        val sigTop = Math.max(currentY + 20f, (PAGE_HEIGHT - 130).toFloat())
        val sigBoxH = 70f
        val sigColW = (PAGE_WIDTH - 70f) / 2f

        for (i in 0..1) {
            val left = 35f + (i * sigColW)
            val right = left + sigColW
            drawBox(canvas, left, sigTop, right, sigTop + sigBoxH, Color.WHITE, lightBorder)

            paint.style = Paint.Style.FILL
            paint.color = forestGreen
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.CENTER

            val title = if (i == 0) "DÜZENLEYEN (TAŞIT ŞOFÖRÜ)" else "ONAYLAYAN (BAŞMÜHENDİS)"
            val sub = if (i == 0) (profile?.driverName ?: "Salih Kaya") else (profile?.chiefEngineerName ?: "Orman Kadastro Başmühendisi")
            canvas.drawText(title, left + (sigColW / 2), sigTop + 18f, paint)
            paint.color = darkGrey
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(sub, left + (sigColW / 2), sigTop + 32f, paint)
            paint.color = Color.GRAY
            canvas.drawText("(İmza / Mühür)", left + (sigColW / 2), sigTop + 60f, paint)
        }
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(27, 67, 50)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(title, 35f, y + 10f, paint)

        // Underline
        paint.color = Color.rgb(82, 183, 136)
        paint.strokeWidth = 1f
        canvas.drawLine(35f, y + 14f, (PAGE_WIDTH - 35).toFloat(), y + 14f, paint)

        return y + 20f
    }

    private fun drawBox(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, fill: Int, stroke: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint.color = fill
        canvas.drawRoundRect(RectF(left, top, right, bottom), 4f, 4f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = stroke
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(RectF(left, top, right, bottom), 4f, 4f, paint)
    }

    private fun drawLabelValue(
        canvas: Canvas,
        x: Float,
        y: Float,
        label: String,
        value: String,
        isHighlighted: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textAlign = Paint.Align.LEFT

        // Label
        paint.textSize = 8.5f
        paint.color = Color.rgb(90, 90, 90)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(label, x, y, paint)

        val labelBounds = Rect()
        paint.getTextBounds(label, 0, label.length, labelBounds)
        val valueX = x + labelBounds.width() + 6f

        // Value
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (isHighlighted) Color.rgb(27, 67, 50) else Color.rgb(30, 30, 30)
        canvas.drawText(value, valueX, y, paint)
    }

    fun sharePdfFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Taşıt Görev Fişini Paylaş"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF paylaşılırken hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun openPdfFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to share chooser if no direct viewer installed
            sharePdfFile(context, file)
        }
    }
}
