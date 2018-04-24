package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(@SerializedName("tracks")
                          val tracks: Tracks)