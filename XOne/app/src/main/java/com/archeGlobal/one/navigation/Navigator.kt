package com.archeGlobal.one.navigation

interface Navigator {
    fun openPulseLogin()
    fun navigateToLoginScreen()
    fun navigateToOtpVerification(email: String, mobile: String, employeeId: String)
    fun navigateToHome(
        fromOtp: Boolean = false,
        showBiometricSetup: Boolean = false,
        email: String = "",
        mobile: String = "",
        employeeId: String = ""
    )
    fun getHomeIntent(): android.content.Intent
    fun navigateToID()
    fun navigateToAsset()
    fun navigateToTimesheet()
    fun navigateToLeave()
    fun navigateToMyDocuments()
    fun navigateToUserDocuments()
    fun navigateToMyCareer()
    fun navigateToELearning()
    fun navigateToGoalSetting()
    fun navigateToXCard()
    fun navigateToMedical()
    fun navigateToFinance()
    fun navigateToMyPay()
    fun navigateToAdmin()
    fun navigateToHR()
    fun navigateToHolidayOptions()
    fun navigateToHolidayCalendar()
    fun navigateToClientCalendar()
    fun navigateToGreetings()
    fun navigateToGlobalCelebration()
    fun navigateToXConnect(initialTab: String = "All Posts")
    fun navigateToLocations(showHeader: Boolean = true)
    fun navigateToHelpdesk()
    fun navigateToAnnouncements()
    fun navigateToXProfile()
    fun navigateToPasswordReset()
    fun navigateToPolicy()
    fun navigateToSOS(showHeader: Boolean = true)
    fun navigateToTravel()
    fun navigateToTravelExpenses()
    fun navigateToTravelRequestDetail()
    fun navigateToTravelApprovals()
    fun navigateToTravelApprovalDetail()
    fun navigateToTravelApprovalDetails()
    fun navigateToTravelApprovalConfirm()
    fun navigateToTravelApprove()
    fun navigateToTravelReject()
    fun navigateToSAP()
    fun navigateToAmple()
    fun navigateToZingHR()
    fun navigateToChat()
    fun navigateToBusinessCard()
    fun navigateToProfile()
    fun navigateToAboutMe()
    fun navigateToAddressDetails()
    fun navigateToEmergencyContact()
    fun navigateToPDFViewer(pdfUrl: String, title: String)
    fun navigateToArcheOdyssey()
    fun navigateToCommunique()
    fun navigateToVision()
    fun navigateToCoreValues()
    fun navigateToAboutUs()
    fun navigateToTodo()
    fun navigateToIdeaVault()
    // New methods to support immediate UI updates
    fun getCurrentRoute(): String?
    fun refreshCurrentScreen()
    fun popBackStack()
    
    // Navigation method for greeting detail screen
    fun navigateToGreetingDetail(
        selectedGreetingUrl: String,
        allGreetings: List<String>,
        message: String,
        category: String
    )
    fun navigateToGlobalCelebrationDetail(subcategory: com.archeGlobal.one.model.GreetingSubcategory)
}