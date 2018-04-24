package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class ItemsItem(@SerializedName("disc_number")
                     val discNumber: Int = 0,
                     @SerializedName("album")
                     val album: Album,
                     @SerializedName("available_markets")
                     val availableMarkets: List<String>?,
                     @SerializedName("type")
                     val type: String = "",
                     @SerializedName("external_ids")
                     val externalIds: ExternalIds,
                     @SerializedName("uri")
                     val uri: String = "",
                     @SerializedName("duration_ms")
                     val durationMs: Int = 0,
                     @SerializedName("explicit")
                     val explicit: Boolean = false,
                     @SerializedName("artists")
                     val artists: List<ArtistsItem>?,
                     @SerializedName("popularity")
                     val popularity: Int = 0,
                     @SerializedName("name")
                     val name: String = "",
                     @SerializedName("track_number")
                     val trackNumber: Int = 0,
                     @SerializedName("href")
                     val href: String = "",
                     @SerializedName("id")
                     val id: String = "",
                     @SerializedName("external_urls")
                     val externalUrls: ExternalUrls)