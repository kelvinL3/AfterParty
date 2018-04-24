package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class ExternalIds(@SerializedName("isrc")
                       val isrc: String = "")