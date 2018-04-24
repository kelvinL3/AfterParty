package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class Album(@SerializedName("images")
                 val images: List<ImagesItem>?,
                 @SerializedName("artists")
                 val artists: List<ArtistsItem>?,
                 @SerializedName("release_date")
                 val releaseDate: String = "",
                 @SerializedName("name")
                 val name: String = "",
                 @SerializedName("available_markets")
                 val availableMarkets: List<String>?,
                 @SerializedName("album_type")
                 val albumType: String = "",
                 @SerializedName("release_date_precision")
                 val releaseDatePrecision: String = "",
                 @SerializedName("href")
                 val href: String = "",
                 @SerializedName("id")
                 val id: String = "",
                 @SerializedName("type")
                 val type: String = "",
                 @SerializedName("external_urls")
                 val externalUrls: ExternalUrls,
                 @SerializedName("uri")
                 val uri: String = "")