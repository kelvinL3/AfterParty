package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class ExternalUrls(@SerializedName("spotify")
                        val spotify: String = "")