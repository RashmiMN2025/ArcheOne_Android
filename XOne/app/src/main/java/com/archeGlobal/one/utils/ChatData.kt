package com.archeGlobal.one.utils

import com.archeGlobal.one.model.FAQItem

class ChatData private constructor() {
    companion object {
        val shared = ChatData()
    }

    // First 9 FAQs
    val faqs: List<FAQItem> = listOf(
        FAQItem(
            title = "Profile Update",
            question = "How can I update my profile information?",
            answer = "Go to your profile settings in the app to update your personal details, including contact information and profile picture."
        ),
        
        FAQItem(
            title = "Technical Issues",
            question = "What should I do if I encounter technical issues?",
            answer = "Restart the app, clear cache, or update to the latest version. If the issue persists, contact support@example.com."
        ),
        
        FAQItem(
            title = "Company Policies",
            question = "How can I access company policies and resources?",
            answer = "Go to the 'Resources' or 'Company Policies' section in the app to find employee handbooks and guidelines."
        ),
        
        FAQItem(
            title = "Forgot Password",
            question = "What should I do if I forget my password?",
            answer = "Click 'Forgot Password' on the login screen, enter your email, and follow the reset instructions."
        ),
        
        FAQItem(
            title = "Benefits Enrollment",
            question = "How do I access my benefits enrollment information?",
            answer = "Go to 'Benefits' in your account settings to view or modify your benefits during open enrollment."
        ),
        
        FAQItem(
            title = "Learning & Certifications",
            question = "How do I access my learning history and certifications?",
            answer = "Visit the 'Learning & Development' section to view completed courses, training history, and certifications."
        ),
        
        FAQItem(
            title = "Expense Reports",
            question = "How do I submit expense reports?",
            answer = "Go to 'Expense Reports', create a new report, enter details, attach receipts, and submit for approval."
        ),
        
        FAQItem(
            title = "Emergency Contacts",
            question = "How do I access emergency contact information?",
            answer = "Navigate to profile settings and select 'Emergency Contacts' to view or update details."
        ),
        
        FAQItem(
            title = "Leave & Holidays",
            question = "How can I find company holidays and leave policies?",
            answer = "Check 'Leave & Holidays' in the app for the holiday calendar, leave policies, and leave application steps."
        )
    )

    // Additional FAQs mapped to keywords
    val keywordMappings: Map<String, List<FAQItem>> = mapOf(
        "login, access" to listOf(
            FAQItem(
                title = "",
                question = "How do I login to the app?",
                answer = "Enter your credentials on the login screen and tap 'Sign In'."
            ),
            FAQItem(
                title = "",
                question = "How can I reset my login credentials?",
                answer = "Use the 'Forgot Password' option on the login screen."
            )
        ),
        "features, tools" to listOf(
            FAQItem(
                title = "",
                question = "What features are available in the app?",
                answer = "The app includes profile management, benefits, learning resources, and more."
            ),
            FAQItem(
                title = "",
                question = "How do I customize app tools?",
                answer = "Access settings to personalize your app experience."
            )
        )
    )

    fun searchFAQs(query: String): List<FAQItem> {
        val queryLowercase = query.lowercase().trim()
        // Split the query into individual words for better matching
        val queryWords = queryLowercase.split(Regex("\\s+")).filter { it.length > 2 } // Filter out very short words
        val results = mutableSetOf<FAQItem>()
        
        // Search in main FAQs with improved matching
        results.addAll(faqs.filter { faq ->
            val questionLower = faq.question.lowercase()
            val answerLower = faq.answer.lowercase()
            
            // Check if full query is contained
            questionLower.contains(queryLowercase) || 
            answerLower.contains(queryLowercase) ||
            // Check if any meaningful word from the query is contained
            queryWords.any { word -> 
                questionLower.contains(word) || 
                answerLower.contains(word) 
            }
        })
        
        // Search in keyword mappings with improved matching
        for ((keywordGroup, items) in keywordMappings) {
            val keywordArray = keywordGroup.split(",").map { it.trim().lowercase() }
            
            // Check full query against keywords
            val fullQueryMatches = keywordArray.any { 
                it.contains(queryLowercase) || queryLowercase.contains(it) 
            }
            
            // Check individual words from query against keywords
            val wordMatches = queryWords.any { word ->
                keywordArray.any { keyword -> 
                    keyword.contains(word) || word.contains(keyword) 
                }
            }
            
            if (fullQueryMatches || wordMatches) {
                results.addAll(items)
            }
        }
        
        return results.toList()
    }
} 