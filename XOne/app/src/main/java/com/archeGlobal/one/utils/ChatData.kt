package com.archeGlobal.one.utils

import com.archeGlobal.one.model.FAQItem

class ChatData private constructor() {
    companion object {
        val shared = ChatData()
    }

    // Main FAQs
    val faqs: List<FAQItem> = listOf(
        FAQItem(
            title = "Profile Update",
            question = "How can I update my profile information?",
            answer = "Navigate to your profile settings in the app to update your personal details, including contact information and profile picture."
        ),
        
        FAQItem(
            title = "Technical Issues",
            question = "What should I do if I encounter technical issues?",
            answer = "Restart the app, clear cache, or update to the latest version. If the issue persists, contact platform@arche.global"
        ),
        
        FAQItem(
            title = "Company Policies",
            question = "How can I access company policies and resources?",
            answer = "Navigate to the Home screen and select 'Policy' under MyArche. This service contains the updated policy documents. For further assistance, please reach out to your on-floor HR team."
        ),
        
        FAQItem(
            title = "Benefits Enrollment",
            question = "How do I access my benefits enrollment information?",
            answer = "Navigate to the Home screen and select 'Policy' under MyArche. Policy documents such as the Employee Gift Policy, Employee Referral Policy, and Employee Loan Policy are listed. If you do not find the necessary information, please reach out to your on-floor HR team."
        ),
        
        FAQItem(
            title = "Performance Management (PMS)",
            question = "How do I access my Performance Management (PMS)?",
            answer = "Navigate to the Home screen and select 'ZingHR' under MyApps. Authenticate yourself and look for PMS—this will help you understand your current progress. For further assistance, please reach out to your on-floor HR team."
        ),
        
        FAQItem(
            title = "Payslips, Form16 and Form 12A",
            question = "How can I access my Payslips, Form16 and Form 12A?",
            answer = "Navigate to the Home screen and select 'MyPay' under MyApps. You will be redirected to AzaPay/AzaTecon Authenticate yourself and look for Payslips/Form 16 and Form 12A. Additionally you can also view your Profile, Salary Computation, Tax Computation, Investment Declaration, Provident Fund and My CTC. For further assistance, please reach out to your on-floor HR team."
        ),
        
        FAQItem(
            title = "Emergency Contacts",
            question = "How do I access emergency contact information?",
            answer = "Navigate to profile settings to view 'Emergency Contact' details. To update the same, reach out to your on-floor HR team."
        ),
        
        FAQItem(
            title = "Leave & Holidays",
            question = "How can I find company holidays and leave policies?",
            answer = "Navigate to the Home screen and select 'Calendar' under MyArche. Holiday, Restricted Holiday, Global events are all listed in the Calendar service, You can also view Arche official Holiday List. Please navigate to ZingHR to access your leave balance and apply for the same."
        ),
        
        FAQItem(
            title = "Travel requests",
            question = "How do I raise a Travel request?",
            answer = """
                 Navigate to the Home screen and select 'Travel' under MyApps. You should then be able to view the request submission form.
                 
                 Things to note:
                 
                 1. Travel requests must be submitted at least two weeks before the departure date.
                 2. The approval email is sent to your Line Manager.
                 3. A valid project name and business justification must be provided.
                 
                 In case of an urgent travel request or for other queries, please reach out to traveldesk@arche.global.
                 
                 You can also check out the Revised Travel Reimbursement Policy under the Policy Service.
                 """
        ),
        
        FAQItem(
            title = "Idea Submissions",
            question = "How do I submit an idea?",
            answer = "Navigate to the Home screen and select 'IdeaVault' under MyArche. You may choose to submit an idea for ArcheOne improvement or feature addition under the categories listed. For further information regarding the same, please reach out to platform@arche.global."
        ),
        
        FAQItem(
            title = "Report an Issue",
            question = "How do I report an issue/security risk/Non Compliance anonymously?",
            answer = "Navigate to the Home screen and select 'SOS' under MyProdSuite. Choose 'Raise a Concern', select an issue category, and submit a brief description of the concern you wish to raise or report. Click 'Submit', and you will be prompted to choose whether to submit the issue with your identity or anonymously.For further assistance, please reach out to your on-floor HR team."
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
        val results = mutableListOf<Pair<FAQItem, Double>>()
        
        // Search in main FAQs with improved matching and relevance scoring
        faqs.forEach { faq ->
            val questionLower = faq.question.lowercase()
            val answerLower = faq.answer.lowercase()
            val titleLower = faq.title.lowercase()
            
            // Split question into words for word-level matching
            val questionWords = questionLower.split(Regex("\\s+")).filter { it.length > 2 }
            
            // Calculate relevance score based on different matching criteria
            var score = 0.0
            
            // Exact match gets highest score
            if (questionLower == queryLowercase) {
                score += 100.0
            }
            
            // Contains full query
            if (questionLower.contains(queryLowercase)) {
                score += 50.0
            }
            
            // Title match
            if (titleLower.contains(queryLowercase)) {
                score += 40.0
            }
            
            // Answer contains query
            if (answerLower.contains(queryLowercase)) {
                score += 20.0
            }
            
            // New word-level matching logic: Count matching words
            var matchingWordsCount = 0
            queryWords.forEach { queryWord ->
                questionWords.forEach { questionWord ->
                    // Check for exact word matches or partial matches
                    if (questionWord == queryWord || 
                        questionWord.contains(queryWord) || 
                        queryWord.contains(questionWord)) {
                        matchingWordsCount++
                    }
                }
            }
            
            // If 5 or more words match, give high score
            if (matchingWordsCount >= 5) {
                score += 80.0
            } else if (matchingWordsCount >= 3) {
                score += 40.0
            } else if (matchingWordsCount >= 2) {
                score += 20.0
            }
            
            // Original word-level matching (keeping for compatibility)
            queryWords.forEach { word ->
                if (questionLower.contains(word)) {
                    // Words at the beginning of the question get higher score
                    if (questionLower.startsWith(word)) {
                        score += 15.0
                    } else {
                        score += 10.0
                    }
                }
                
                if (titleLower.contains(word)) {
                    score += 8.0
                }
                
                if (answerLower.contains(word)) {
                    score += 5.0
                }
            }
            
            // Only add items with some relevance
            if (score > 0) {
                results.add(Pair(faq, score))
            }
        }
        
        // Search in keyword mappings with improved matching
        for ((keywordGroup, items) in keywordMappings) {
            val keywordArray = keywordGroup.split(",").map { it.trim().lowercase() }
            
            // Calculate keyword relevance
            var keywordScore = 0.0
            
            // Check full query against keywords
            keywordArray.forEach { keyword ->
                if (keyword == queryLowercase) {
                    keywordScore += 30.0
                } else if (keyword.contains(queryLowercase)) {
                    keywordScore += 20.0
                } else if (queryLowercase.contains(keyword)) {
                    keywordScore += 15.0
                }
            }
            
            // Check individual words from query against keywords
            queryWords.forEach { word ->
                keywordArray.forEach { keyword ->
                    if (keyword.contains(word)) {
                        keywordScore += 8.0
                    } else if (word.contains(keyword)) {
                        keywordScore += 5.0
                    }
                }
            }
            
            // If keywords match, score each FAQ item in this category
            if (keywordScore > 0) {
                items.forEach { faq ->
                    val questionLower = faq.question.lowercase()
                    val questionWords = questionLower.split(Regex("\\s+")).filter { it.length > 2 }
                    
                    // Base score from keyword match
                    var itemScore = keywordScore
                    
                    // Additional scoring based on question content
                    if (questionLower.contains(queryLowercase)) {
                        itemScore += 10.0
                    }
                    
                    // Apply same word matching logic here too
                    var matchingWordsCount = 0
                    queryWords.forEach { queryWord ->
                        questionWords.forEach { questionWord ->
                            if (questionWord == queryWord || 
                                questionWord.contains(queryWord) || 
                                queryWord.contains(questionWord)) {
                                matchingWordsCount++
                            }
                        }
                    }
                    
                    // If 5 or more words match, give high score
                    if (matchingWordsCount >= 5) {
                        itemScore += 60.0
                    } else if (matchingWordsCount >= 3) {
                        itemScore += 30.0
                    } else if (matchingWordsCount >= 2) {
                        itemScore += 15.0
                    }
                    
                    queryWords.forEach { word ->
                        if (questionLower.contains(word)) {
                            itemScore += 5.0
                        }
                    }
                    
                    results.add(Pair(faq, itemScore))
                }
            }
        }
        
        // Sort by relevance score (descending) and return the FAQs
        return results.sortedByDescending { it.second }.map { it.first }.distinct()
    }
}