package de.triplet.gradle.play

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.model.Image
import java.io.File

internal const val APPLICATION_NAME = "gradle-play-publisher"
internal const val PLAY_STORE_GROUP = "Play Store"

internal const val MIME_TYPE_APK = "application/vnd.android.package-archive"
internal const val MIME_TYPE_IMAGE = "image/*"

internal const val LISTING_PATH = "listing/"
internal const val RESOURCES_OUTPUT_PATH = "build/outputs/play"

internal const val MAX_SCREENSHOTS_SIZE = 8

internal enum class ListingDetails(val fileName: String, val maxLength: Int = -1) {
    ContactEmail("contactEmail"),
    ContactPhone("contactPhone"),
    ContactWebsite("contactWebsite"),
    DefaultLanguage("defaultLanguage"),
    Title("title", 50),
    ShortDescription("shortdescription", 80),
    FullDescription("fulldescription", 4000),
    Video("video"),
    WhatsNew("whatsnew", 500);

    fun saveText(dir: File, value: String) = File(dir, fileName).writeText(value)
}

internal val JSON_FACTORY by lazy { JacksonFactory.getDefaultInstance() }
internal val HTTP_TRANSPORT by lazy { GoogleNetHttpTransport.newTrustedTransport() }

internal val TRACKS = arrayOf("alpha", "beta", "rollout", "production")
internal val IMAGE_EXTENSIONS = arrayOf("png", "jpg")

//Min length for any side: 320px. Max length for any side: 3840px.
internal data class ImageSize(val minWidth: Int, val minHeight: Int, val maxWidth: Int = minWidth, val maxHeight: Int = minHeight)
internal val screenshotSize = ImageSize(320, 320, 3840, 3840)
internal enum class ImageTypes(val fileName: String, val constraints: ImageSize, val max: Int = MAX_SCREENSHOTS_SIZE) {
    Icon("icon",  ImageSize(512, 512), 1),
    FeatureGraphic("featureGraphic", ImageSize(1024, 500), 1),
    PhoneScreenshots("phoneScreenshots", screenshotSize),
    SevenInchScreenshots("sevenInchScreenshots", screenshotSize),
    TenInchScreenshots("tenInchScreenshots", screenshotSize),
    PromoGraphic("promoGraphic", ImageSize(180, 120),  1),
    TVBanner("tvBanner", ImageSize(1280, 720), 1),
    TVScreenshots("tvScreenshots", screenshotSize),
    WearScreenshots("wearScreenshots", screenshotSize);

    fun getImages(listingDir: File): List<AbstractInputStreamContent>? {
        val graphicDir = File(listingDir, fileName)
        if (graphicDir.exists()) {
            return graphicDir.listFiles(ImageFileFilter())
                    .asList()
                    .sorted()
                    .map { FileContent(MIME_TYPE_IMAGE, it) }
        }
        return null
    }

    fun saveImages(listingDir: File, images: List<Image>?) {
        @Suppress("UNUSED_VARIABLE")
        val imageFolder = listingDir.validSubFolder(fileName) ?: return

        if (images == null) {
            return
        }

        // TODO: Disabled for now as we have only access to preview-versions with the current API.
        /*
        for (image in images) {
            File(imageFolder, "${image.id}.png").outputStream().use { os ->
                URL(image.url).openStream().use { it.copyTo(os) }
            }
        }
        */
    }


}

// region '419' is a special case in the play store that represents latin america
// 'fil' is a special case in the play store that represents Filipino
internal val LOCALE_REGEX = Regex("^(fil|[a-z]{2}(-([A-Z]{2}|419))?)\\z")
