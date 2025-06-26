package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class SocialContent(
    @SerializedName("jobs") val jobs: List<Job> = emptyList(),
    @SerializedName("case_studies") val caseStudies: List<CaseStudy> = emptyList(),
    @SerializedName("blogs") val blogs: List<BlogPost> = emptyList()
)

data class Job(
    @SerializedName("Slug") val Slug: String = "",
    @SerializedName("Title") val Title: String = "",
    @SerializedName("Image") val Image: String = "",
    @SerializedName("Description") val Description: String = "",
    @SerializedName("Content") val Content: String? = null
)

data class CaseStudy(
    @SerializedName("Slug") val Slug: String = "",
    @SerializedName("Title") val Title: String = "",
    @SerializedName("Image") val Image: String = "",
    @SerializedName("Content") val Content: String? = null,
    @SerializedName("Description") val Description: String = ""
)

data class SocialArticle(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val content: String? = null
)

data class BlogPost(
    @SerializedName("Slug") val Slug: String = "",
    @SerializedName("Title") val Title: String = "",
    @SerializedName("Image") val Image: String = "",
    @SerializedName("Description") val Description: String = "",
    @SerializedName("Content") val Content: String? = null
)
