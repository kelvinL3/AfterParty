package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class ImagesItem(@SerializedName("width")
                      val width: Int = 0,
                      @SerializedName("url")
                      val url: String = "",
                      @SerializedName("height")
                      val height: Int = 0)