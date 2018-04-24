package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class ArtistsItem(@SerializedName("name")
                       val name: String = "",
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