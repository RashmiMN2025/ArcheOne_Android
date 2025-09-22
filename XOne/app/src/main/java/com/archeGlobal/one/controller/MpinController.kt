package com.archeGlobal.one.controller

import android.content.Context
import com.archeGlobal.one.model.SecurityQuestion
import com.archeGlobal.one.utils.MpinManager

class MpinController(
    private val context: Context,
) {
    /** Save MPIN and security questions for reset validation */
    fun saveMpinAndQuestions(
        mpin: String,
        questions: List<SecurityQuestion>,
    ) {
        MpinManager.saveMpin(context, mpin)
        MpinManager.saveSecurityQuestions(context, questions)
    }

    /** Validate entered MPIN against stored MPIN */
    fun validateMpin(enteredMpin: String): Boolean {
        val storedMpin = MpinManager.getMpin(context)
        return storedMpin != null && storedMpin == enteredMpin
    }

    /** Validate security question answers for reset */
    fun validateSecurityAnswers(enteredQuestions: List<SecurityQuestion>): Boolean {
        val savedQuestions = MpinManager.getSecurityQuestions(context)
        return enteredQuestions.size == savedQuestions.size &&
            enteredQuestions.zip(savedQuestions).all { (entered, saved) ->
                entered.question == saved.question && entered.answer == saved.answer
            }
    }

    /** Check if MPIN is set */
    fun isMpinSet(): Boolean = MpinManager.checkMpinExists(context)

    /** Clear MPIN and security questions (for logout or reset) */
    fun clearMpinData() {
        MpinManager.clearMpin(context)
        // Optionally clear security questions if needed
    }
}
