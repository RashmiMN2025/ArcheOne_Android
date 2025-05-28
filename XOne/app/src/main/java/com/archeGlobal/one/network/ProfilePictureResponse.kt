package com.archeGlobal.one.network

/**
 * Response model for profile picture upload API
 * Format as per actual server response:
 * {
 *   "status": 200,
 *   "message": "Profile picture uploaded successfully",
 *   "filePath": "https://pulse.netcon.in:7000/download_doc/user@example.com?fileName=1366-profile_pic.jpg"
 * }
 */
data class ProfilePictureResponse(
    val status: Int,
    val message: String,
    val filePath: String? = null
)
