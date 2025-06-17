package com.archeGlobal.one.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.archeGlobal.one.model.SecurityQuestion

object MpinManager {
    private const val PREF_NAME = "mpin_prefs"
    private const val KEY_MPIN = "user_mpin"
    private const val KEY_QUESTION_1 = "security_question_1"
    private const val KEY_ANSWER_1 = "security_answer_1"
    private const val KEY_QUESTION_2 = "security_question_2"
    private const val KEY_ANSWER_2 = "security_answer_2"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        PREF_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveMpin(context: Context, mpin: String) {
        prefs(context).edit().putString(KEY_MPIN, mpin).apply()
    }

    fun getMpin(context: Context): String? = prefs(context).getString(KEY_MPIN, null)

    fun clearMpin(context: Context) {
        prefs(context).edit().remove(KEY_MPIN).apply()
    }

    fun saveSecurityQuestions(context: Context, questions: List<SecurityQuestion>) {
        prefs(context).edit()
            .putString(KEY_QUESTION_1, questions[0].question)
            .putString(KEY_ANSWER_1, questions[0].answer)
            .putString(KEY_QUESTION_2, questions[1].question)
            .putString(KEY_ANSWER_2, questions[1].answer)
            .apply()
    }

    fun getSecurityQuestions(context: Context): List<SecurityQuestion> {
        val prefs = prefs(context)
        val q1 = prefs.getString(KEY_QUESTION_1, "") ?: ""
        val a1 = prefs.getString(KEY_ANSWER_1, "") ?: ""
        val q2 = prefs.getString(KEY_QUESTION_2, "") ?: ""
        val a2 = prefs.getString(KEY_ANSWER_2, "") ?: ""
        return listOf(
            SecurityQuestion(q1, a1),
            SecurityQuestion(q2, a2)
        )
    }

    fun checkMpinExists(context: Context): Boolean = getMpin(context) != null
}