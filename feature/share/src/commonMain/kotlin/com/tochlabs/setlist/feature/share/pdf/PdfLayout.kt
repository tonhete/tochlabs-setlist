package com.tochlabs.setlist.feature.share.pdf

// Platform-neutral description of a PDF (design.md, decision 7): what goes on each page is decided
// and tested here; the platform renderer only draws it.

/** A4 in PDF points (1/72 inch). */
const val PAGE_WIDTH = 595f
const val PAGE_HEIGHT = 842f
const val PAGE_MARGIN = 40f

private const val LINE_SPACING = 1.3f

enum class PdfTextStyle(
    val sizePt: Float,
    val bold: Boolean,
) {
    TITLE(20f, true),
    SUBTITLE(11f, false),
    STAGE_TITLE(26f, true),
    STAGE_DETAIL(15f, false),
    STAGE_STOP(18f, true),
    TABLE_HEADER(9f, true),
    TABLE_CELL(9f, false),
    FOOTER(11f, true),
    ;

    val lineHeight: Float get() = sizePt * LINE_SPACING
}

sealed interface PdfLine {
    val height: Float

    /** One line of text; text that does not fit the width is shortened by the renderer. */
    data class Text(
        val text: String,
        val style: PdfTextStyle,
        /** Highlighted (change markers, links), drawn in the accent colour. */
        val accent: Boolean = false,
    ) : PdfLine {
        override val height: Float get() = style.lineHeight
    }

    /** A table row; cells follow [PdfLayout.columnWeights]. */
    data class Row(
        val cells: List<String>,
        val header: Boolean = false,
    ) : PdfLine {
        private val style get() = if (header) PdfTextStyle.TABLE_HEADER else PdfTextStyle.TABLE_CELL
        override val height: Float get() = style.lineHeight + ROW_PADDING
    }

    data class Space(
        override val height: Float,
    ) : PdfLine
}

private const val ROW_PADDING = 6f

/** Lines that stay together on one page, such as one setlist item. */
data class PdfBlock(
    val lines: List<PdfLine>,
) {
    val height: Float get() = lines.sumOf { it.height.toDouble() }.toFloat()
}

data class PdfPage(
    val blocks: List<PdfBlock>,
) {
    val height: Float get() = blocks.sumOf { it.height.toDouble() }.toFloat()
}

data class PdfLayout(
    val pages: List<PdfPage>,
    /** Relative column widths of the table rows; empty when there is no table. */
    val columnWeights: List<Float> = emptyList(),
    val pageWidth: Float = PAGE_WIDTH,
    val pageHeight: Float = PAGE_HEIGHT,
    val margin: Float = PAGE_MARGIN,
) {
    val contentHeight: Float get() = pageHeight - 2 * margin
}

/**
 * Splits [blocks] into pages of [contentHeight] without splitting a block. [pageHeader] starts every
 * page after the first one (the table header of the list PDF).
 */
internal fun paginate(
    blocks: List<PdfBlock>,
    contentHeight: Float = PAGE_HEIGHT - 2 * PAGE_MARGIN,
    pageHeader: PdfBlock? = null,
): List<PdfPage> {
    val pages = mutableListOf<PdfPage>()
    var current = mutableListOf<PdfBlock>()
    var used = 0f
    blocks.forEach { block ->
        if (current.isNotEmpty() && used + block.height > contentHeight) {
            pages += PdfPage(current)
            current = mutableListOf()
            used = 0f
            if (pageHeader != null) {
                current += pageHeader
                used += pageHeader.height
            }
        }
        current += block
        used += block.height
    }
    if (current.isNotEmpty() || pages.isEmpty()) pages += PdfPage(current)
    return pages
}
