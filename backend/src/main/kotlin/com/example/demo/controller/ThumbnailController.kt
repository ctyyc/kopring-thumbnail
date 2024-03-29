package com.example.demo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.coobird.thumbnailator.Thumbnails
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Tag(name = "Thumbnail API", description = "Generate Thumbnail...")
@RestController
@RequestMapping("/api/v1/thumbnail")
class ThumbnailController {

    @Operation(summary = "Generate PPT", description = "Generate PPT...")
    @PostMapping("/ppt", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.IMAGE_PNG_VALUE])
    fun generatePptThumbnail(@RequestBody pptFile: MultipartFile): ResponseEntity<ByteArray> {
        println("=== start generatePptThumbnail ===")
        try {
            // Load PPT file into XMLSlideShow
            val inputStream = ByteArrayInputStream(pptFile.bytes)
            val ppt = XMLSlideShow(inputStream)

            // Get the first slide's image
            val slideImage = extractSlideImage(ppt)

            // Convert BufferedImage to byte array
            val baos = ByteArrayOutputStream()
            Thumbnails.of(slideImage).size(200, 200).outputFormat("png").toOutputStream(baos)
            val thumbnailBytes = baos.toByteArray()

            println("=== end generatePptThumbnail ===")
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(thumbnailBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @Operation(summary = "Generate PDF", description = "Generate PDF...")
    @PostMapping("/pdf", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.IMAGE_PNG_VALUE])
    fun generatePdfThumbnail(@RequestBody pdfFile: MultipartFile): ResponseEntity<ByteArray> {
        println("=== start generatePdfThumbnail ===")
        try {
            val inputStream = ByteArrayInputStream(pdfFile.bytes)
            val document = PDDocument.load(inputStream)
            val renderer = PDFRenderer(document)

            // Render the first page of the PDF (300 DPI)
            val pageImage: BufferedImage = renderer.renderImageWithDPI(0, 300f)

            // Convert BufferedImage to byte array
            val baos = ByteArrayOutputStream()
            Thumbnails.of(pageImage).size(200, 200).outputFormat("png").toOutputStream(baos)
            val thumbnailBytes = baos.toByteArray()

            document.close()

            println("=== end generatePdfThumbnail ===")
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(thumbnailBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    private fun extractSlideImage(ppt: XMLSlideShow): BufferedImage {
        val slide = ppt.slides[0]
        val dimension = Dimension(720, 540)
        val image = BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        try {
            slide.draw(graphics)
        } catch (e: Exception) {
            throw Exception("ERROR : ${e.message}")
        } finally {
            graphics.dispose()
        }

        return image
    }
}