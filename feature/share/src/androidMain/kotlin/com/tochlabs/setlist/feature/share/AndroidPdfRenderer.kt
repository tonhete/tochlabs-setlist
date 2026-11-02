package com.tochlabs.setlist.feature.share

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import com.tochlabs.setlist.feature.share.pdf.PdfLayout
import com.tochlabs.setlist.feature.share.pdf.PdfLine
import com.tochlabs.setlist.feature.share.pdf.PdfPage
import com.tochlabs.setlist.feature.share.pdf.PdfTextStyle
import java.io.ByteArrayOutputStream

/** Draws a [PdfLayout] with Android's PdfDocument. Text wider than its space is shortened with "…". */
internal class AndroidPdfRenderer : PdfRenderer {
    override fun render(layout: PdfLayout): ByteArray {
        val document = PdfDocument()
        try {
            layout.pages.forEachIndexed { index, page ->
                val info =
                    PdfDocument.PageInfo
                        .Builder(
                            layout.pageWidth.toInt(),
                            layout.pageHeight.toInt(),
                            index + 1,
                        ).create()
                val pdfPage = document.startPage(info)
                drawPage(pdfPage.canvas, layout, page)
                document.finishPage(pdfPage)
            }
            return ByteArrayOutputStream().also { document.writeTo(it) }.toByteArray()
        } finally {
            document.close()
        }
    }

    private fun drawPage(
        canvas: Canvas,
        layout: PdfLayout,
        page: PdfPage,
    ) {
        val width = layout.pageWidth - 2 * layout.margin
        var top = layout.margin
        page.blocks.flatMap { it.lines }.forEach { line ->
            when (line) {
                is PdfLine.Text -> drawText(canvas, line, layout.margin, top, width)
                is PdfLine.Row -> drawRow(canvas, line, layout, top, width)
                is PdfLine.Space -> Unit
            }
            top += line.height
        }
    }

    private fun drawText(
        canvas: Canvas,
        line: PdfLine.Text,
        left: Float,
        top: Float,
        width: Float,
    ) {
        val paint = paint(line.style, if (line.accent) ACCENT else TEXT)
        canvas.drawText(fit(line.text, paint, width), left, top + line.style.sizePt, paint)
    }

    private fun drawRow(
        canvas: Canvas,
        row: PdfLine.Row,
        layout: PdfLayout,
        top: Float,
        width: Float,
    ) {
        val style = if (row.header) PdfTextStyle.TABLE_HEADER else PdfTextStyle.TABLE_CELL
        val paint = paint(style, TEXT)
        val total = layout.columnWeights.sum()
        var left = layout.margin
        row.cells.forEachIndexed { index, cell ->
            val columnWidth = width * layout.columnWeights.getOrElse(index) { 0f } / total
            canvas.drawText(fit(cell, paint, columnWidth - CELL_GAP), left, top + style.sizePt + ROW_TOP_PADDING, paint)
            left += columnWidth
        }
        val bottom = top + row.height
        val rule =
            Paint().apply {
                color = if (row.header) TEXT else RULE
                strokeWidth = if (row.header) HEADER_RULE else ROW_RULE
            }
        canvas.drawLine(layout.margin, bottom, layout.margin + width, bottom, rule)
    }

    private fun paint(
        style: PdfTextStyle,
        color: Int,
    ) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = style.sizePt
        typeface = if (style.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        this.color = color
    }

    private fun fit(
        text: String,
        paint: TextPaint,
        width: Float,
    ): String = TextUtils.ellipsize(text, paint, width.coerceAtLeast(0f), TextUtils.TruncateAt.END).toString()

    private companion object {
        const val TEXT = 0xFF000000.toInt()
        const val ACCENT = 0xFF283593.toInt()
        const val RULE = 0xFFCCCCCC.toInt()
        const val CELL_GAP = 4f
        const val ROW_TOP_PADDING = 2f
        const val HEADER_RULE = 1f
        const val ROW_RULE = 0.5f
    }
}
