package com.altf4.ourfinance.data.model

data class DownloadInvoiceResponse(
    val pdfBase64: String? = null,
    val fileName: String? = null,
    val status: String? = null,
    val message: String? = null
)