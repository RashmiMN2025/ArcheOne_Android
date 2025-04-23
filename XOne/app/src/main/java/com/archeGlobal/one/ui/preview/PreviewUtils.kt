package com.archeGlobal.one.ui.preview

import com.archeGlobal.one.navigation.Navigator

class PreviewNavigator : Navigator {
    override fun openPulseLogin() {}
    override fun navigateToLoginScreen() {}
    override fun navigateToOtpVerification(email: String, mobile: String, employeeId: String) {}
    override fun navigateToHome(
        fromOtp: Boolean,
        showBiometricSetup: Boolean,
        email: String,
        mobile: String,
        employeeId: String
    ) {}
    override fun navigateToID() {}
    override fun navigateToAsset() {}
    override fun navigateToTimesheet() {}
    override fun navigateToLeave() {}
    override fun navigateToMyDocuments() {}
    override fun navigateToUserDocuments() {}
    override fun navigateToMyCareer() {}
    override fun navigateToELearning() {}
    override fun navigateToGoalSetting() {}
    override fun navigateToXCard() {}
    override fun navigateToMedical() {}
    override fun navigateToFinance() {}
    override fun navigateToAdmin() {}
    override fun navigateToHR() {}
    override fun navigateToHolidayCalendar() {}
    override fun navigateToClientCalendar() {}
    override fun navigateToGreetings() {}
    override fun navigateToXConnect() {}
    override fun navigateToLocations(showHeader: Boolean) {}
    override fun navigateToHelpdesk() {}
    override fun navigateToAnnouncements() {}
    override fun navigateToXProfile() {}
    override fun navigateToPasswordReset() {}
    override fun navigateToPolicy() {}
    override fun navigateToSOS(showHeader: Boolean) {}
    override fun navigateToTravelExpenses() {}
    override fun navigateToSAP() {}
    override fun navigateToZingHR() {}
    override fun navigateToChat() {}
    override fun navigateToBusinessCard() {}
    override fun navigateToProfile() {}
    override fun navigateToAboutMe() {}
    override fun navigateToAddressDetails() {}
    override fun navigateToEmergencyContact() {}
    override fun navigateToPDFViewer(pdfUrl: String, title: String) {}
    
    // Implementation of new methods
    override fun getCurrentRoute(): String? {
        return null // For preview purposes, we don't track a real route
    }
    
    override fun refreshCurrentScreen() {
        // No-op for preview purposes
    }
} 