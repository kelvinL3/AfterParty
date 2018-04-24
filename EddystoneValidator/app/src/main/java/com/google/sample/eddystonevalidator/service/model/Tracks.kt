package com.google.sample.eddystonevalidator.service.model

import com.google.gson.annotations.SerializedName

data class Tracks(@SerializedName("next")
                  val next: String = "",
                  @SerializedName("total")
                  val total: Int = 0,
                  @SerializedName("offset")
                  val offset: Int = 0,
                  @SerializedName("limit")
                  val limit: Int = 0,
                  @SerializedName("href")
                  val href: String = "",
                  @SerializedName("items")
                  val items: List<ItemsItem>?)