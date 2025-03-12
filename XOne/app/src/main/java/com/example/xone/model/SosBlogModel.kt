package com.example.xone.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class SosBlogModel(
    val name: String,
    @SerializedName("des") // ✅ Maps "des" from API to "description"
    val description: String,

    @SerializedName("image") // ✅ Maps "image" from API to "imageUrl"
    val imageUrl: String,
    val details: List<SOSDetails> // ✅ Change from SOSDetails to List<SOSDetails>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(SOSDetails) ?: emptyList() // ✅ Handle list correctly
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeTypedList(details) // ✅ Handle list correctly
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SosBlogModel> {
        override fun createFromParcel(parcel: Parcel): SosBlogModel = SosBlogModel(parcel)
        override fun newArray(size: Int): Array<SosBlogModel?> = arrayOfNulls(size)
    }
}

data class SOSDetails(
    val title: String,
    val description: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SOSDetails> {
        override fun createFromParcel(parcel: Parcel): SOSDetails = SOSDetails(parcel)
        override fun newArray(size: Int): Array<SOSDetails?> = arrayOfNulls(size)
    }
}
