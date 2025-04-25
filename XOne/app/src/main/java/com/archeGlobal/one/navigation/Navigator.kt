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
    fun navigateToAdmin()
    fun navigateToHR()
    fun navigateToHolidayCalendar()
    fun navigateToClientCalendar()
    fun navigateToGreetings()
    fun navigateToXConnect()
    fun navigateToXConnect(initialTab: String = "All Posts")
    fun navigateToLocations(showHeader: Boolean = true)
    fun navigateToHelpdesk()
    fun navigateToAnnouncements()
    fun navigateToXProfile()
    fun navigateToPasswordReset()
    fun navigateToPolicy()
    fun navigateToSOS(showHeader: Boolean = true)
    fun navigateToTravelExpenses()
    fun navigateToSAP()
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
    
    // New methods to support immediate UI updates
    fun getCurrentRoute(): String?
    fun refreshCurrentScreen()

} 