package com.archeGlobal.one.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseExtractionDetailViewScreenTest {
    @Test
    fun detectsPdfUrlsFromRemoteDocuments() {
        assertTrue(isPdfUrl("https://example.com/file.pdf"))
        assertTrue(isPdfUrl("https://example.com/download?file=report.pdf"))
        assertFalse(isPdfUrl("https://example.com/file.png"))
    }

    @Test
    fun detectsImageUrlsFromRemoteDocuments() {
        assertTrue(isImageUrl("https://example.com/photo.jpg"))
        assertTrue(isImageUrl("https://example.com/IMAGE.PNG"))
        assertFalse(isImageUrl("https://example.com/document.pdf"))
    }
}
