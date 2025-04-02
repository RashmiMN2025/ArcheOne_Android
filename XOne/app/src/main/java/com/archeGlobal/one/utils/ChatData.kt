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
        ),
        "company news, announcements" to listOf(
            FAQItem(
                title = "Company News",
                question = "How do I stay updated with company news and announcements?",
                answer = "To access company-related content through the application, please follow these steps:\n" +
                        "1. Click 'Connect': Find this in the app's main menu.\n" +
                        "2. Access Content: Navigate to case studies, blogs, and company posts.\n" +
                        "3. Stay Updated: Explore and engage with the latest updates and insights."
            )
        ),
        "anonymous feedback, sos" to listOf(
            FAQItem(
                title = "Anonymous Feedback",
                question = "How can I provide anonymous feedback?",
                answer = "To provide anonymous feedback through the application, please follow these steps:\n" +
                        "1. Go to 'SOS': Click the 'SOS' option in the app menu.\n" +
                        "2. Raise a Concern: Select 'Raise a Concern' under 'SOS.'\n" +
                        "3. Pick a Category: Choose a category for your concern.\n" +
                        "4. Describe: Provide a detailed issue description.\n" +
                        "5. Submit Anonymously: Use the anonymous submission option if preferred."
            )
        ),
        "workplace issues, concerns" to listOf(
            FAQItem(
                title = "Workplace Issues",
                question = "What is the process for reporting workplace issues or concerns?",
                answer = "To report an issue through the application, please follow these steps:\n" +
                        "1. Go to 'SOS': Click on 'SOS' in the app menu.\n" +
                        "2. Raise a Concern: Choose 'Raise a Concern.'\n" +
                        "3. Pick a Category: Select a relevant category.\n" +
                        "4. Describe: Provide details about your issue or feedback.\n" +
                        "5. Submit:\n" +
                        "   - With Name: Skip the anonymous option to include your details.\n" +
                        "   - Anonymously: Choose 'Submit Anonymously' to stay anonymous."
            )
        ),
        "work schedule, manage schedule" to listOf(
            FAQItem(
                title = "Work Schedule",
                question = "How can I manage my work schedule?",
                answer = "Thanks for letting us know. We're working on it and appreciate your patience."
            )
        ),
        "project assignments, deadlines" to listOf(
            FAQItem(
                title = "Project Assignments",
                question = "How do I access my project assignments and deadlines?",
                answer = "Thanks for letting us know. We're working on it and appreciate your patience."
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