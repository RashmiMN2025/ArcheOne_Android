package com.archeGlobal.one.ui.preview

import com.archeGlobal.one.navigation.Navigator

object ServiceNameMapper {
    fun mapServiceNameToId(serviceName: String): String {
        return when (serviceName.lowercase()) {
            "policy" -> "Policy"
            "asset" -> "Asset"
            "holiday calendar" -> "Holiday Calendar"
            "communique" -> "Communique"
            else -> serviceName
        }
    }
}

class PreviewNavigator : Navigator {
    override fun openPulseLogin() {}
    override fun navigateToLoginScreen() {}
    override fun navigateToOtpVerification(email: String, mobile: String, employeeId: String) {}
    override fun navigateToHome(fromOtp: Boolean, showBiometricSetup: Boolean, email: String, mobile: String, employeeId: String) {}
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
    override fun navigateToMyPay() {}
    override fun navigateToAdmin() {}
    override fun navigateToHR() {}
    override fun navigateToHolidayOptions() {} // Added missing method
    override fun navigateToHolidayCalendar() {}
    override fun navigateToClientCalendar() {}
    override fun navigateToGreetings() {}
    override fun navigateToXConnect(initialTab: String) {}
    override fun navigateToLocations(showHeader: Boolean) {}
    override fun navigateToHelpdesk() {}
    override fun navigateToAnnouncements() {}
    override fun navigateToXProfile() {}
    override fun navigateToPasswordReset() {}
    override fun navigateToPolicy() {}
    override fun navigateToSOS(showHeader: Boolean) {}
    override fun navigateToTravel() {}
    override fun navigateToTravelExpenses() {}
    override fun navigateToTravelRequestDetail() {}
    override fun navigateToTravelApprovals() {}
    override fun navigateToTravelApprovalDetail() {}
    override fun navigateToTravelApprovalConfirm() {}
    override fun navigateToTravelApprove() {}
    override fun navigateToTravelReject() {}
    override fun navigateToSAP() {}
    override fun navigateToAmple() {}
    override fun navigateToZingHR() {}
    override fun navigateToChat() {}
    override fun navigateToBusinessCard() {}
    override fun navigateToProfile() {}
    override fun navigateToAboutMe() {}
    override fun navigateToAddressDetails() {}
    override fun navigateToEmergencyContact() {}
    override fun navigateToPDFViewer(pdfUrl: String, title: String) {}
    override fun navigateToArcheOdyssey() {}
    override fun navigateToCommunique() {}
    override fun navigateToVision() {}
    override fun navigateToCoreValues() {}
    override fun navigateToAboutUs() {}

    override fun navigateToTodo() {}

    override fun navigateToIdeaVault() {}

    override fun navigateToGlobalCelebration() {}

    override fun navigateToGlobalCelebrationDetail(subcategory: com.archeGlobal.one.model.GreetingSubcategory) {
        // No-op for preview purposes
    }

    // Implementation of new methods
    override fun getCurrentRoute(): String? {
        return null // For preview purposes, we don't track a real route
    } override fun refreshCurrentScreen() {
        // No-op for preview purposes
    }

    override fun getHomeIntent(): android.content.Intent {
        // Return an empty intent for preview purposes
        return android.content.Intent()
    }

    override fun navigateToGreetingDetail(
        selectedGreetingUrl: String,
        allGreetings: List<String>,
        message: String,
        category: String
    ) {
        // No-op for preview purposes
    }

    override fun navigateToMpinSetup(email: String, mobile: String, employeeId: String, token: String) {}
    override fun navigateToTravelApprovalDetails() {}

    override fun popBackStack() {
        // No-op for preview purposes
    }
}
