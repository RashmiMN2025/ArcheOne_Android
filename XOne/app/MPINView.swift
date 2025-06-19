//
//  MPINView.swift
//  ArcheOne
//
//  Created by Arche on 11/06/25.
//

import SwiftUI
import Security

struct MPINView: View {
    @State private var mpin = ["", "", "", ""]
    @State private var confirmMpin = ["", "", "", ""]
    @State private var selectedQuestions = ["", ""]
    @State private var questionAnswers = ["", ""]
    @State private var resetQuestionIndex = 0
    @State private var resetAnswer = ""
    @State private var showMPINInput = false
    @State private var showSecurityQuestions = true
    @State private var isButtonPressed = false
    @State private var hasExistingMPIN = false
    @State private var isResetAnswerHidden = true
    @State private var isAnswerHidden = [true, true]
    @StateObject private var toastManager = ToastManager()
    @FocusState private var focusedField: Int?
    @EnvironmentObject var navigation: NavigationManager
    @EnvironmentObject var coordinator: AppCoordinator
    let resetMpin: Bool
    let loginModel: LoginModel?
    let onCompletion: ((Bool, String?) -> Void)? // New callback property
    
    private let securityQuestions = [
        "What is the name of your first school?",
        "What is your mother’s maiden name?",
        "What was the name of your first company?",
        "What is the name of your childhood best friend?",
        "What is the name of the street you grew up on?",
        "What is the name of your favorite teacher in school?",
        "What is your favorite book or author?",
        "What was the model of your first vehicle?"
    ]
    
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "#E0DCD1"), Color(hex: "#C8C8CA"), Color(hex: "#474749")]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            ScrollView(.vertical, showsIndicators: true) {
                VStack(spacing: 24) {
                    logoView
                    headerView
                    subtitleView
                    cardContainerView
                    if (resetMpin && showMPINInput) || (!resetMpin && !showSecurityQuestions) {
                        setMPINButton
                    } else if !resetMpin && showSecurityQuestions {
                        continueButton
                    }
                    Spacer()
                    toastView
                }.onTapGesture {
                    hideKeyboard()
                }
                .padding()
            }
        }
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
        .onAppear {
            focusedField = 0
            if resetMpin {
                hasExistingMPIN = KeychainManager.checkMPINExists(forKey: "userMPIN")
                if !hasExistingMPIN {
                    toastManager.showToast(message: "No MPIN found. Please set up a new MPIN.")
                } else {
                    for index in 0..<2 {
                        if let question = KeychainManager.retrieveSecurityQuestion(forKey: "securityQuestion\(index)") {
                            selectedQuestions[index] = question
                        }
                    }
                }
            }
        }
    }
    
    private var logoView: some View {
        Image("arche_black")
            .resizable()
            .scaledToFit()
            .frame(width: 180, height: 40)
            .padding(.top, 40)
    }
    
    private var headerView: some View {
        HStack(spacing: 8) {
            Image(systemName: "lock.shield.fill")
                .foregroundColor(.archeColor)
                .font(.system(size: 24))
            Text(resetMpin ? "Reset Your MPIN" : "Set Your MPIN")
                .font(.custom("Graphik-Semibold", fixedSize: 26))
                .foregroundColor(.black)
        }
        .padding(.top, 20)
    }
    
    private var subtitleView: some View {
        Text(resetMpin ? "Verify your identity to reset your MPIN" : "Set security questions and a 4-digit PIN for secure access")
            .font(.custom("Graphik-Regular", fixedSize: 16))
            .foregroundColor(.black.opacity(0.7))
            .multilineTextAlignment(.center)
            .padding(.horizontal, 20)
    }
    
    private var cardContainerView: some View {
        VStack(spacing: 20) {
            if resetMpin && hasExistingMPIN && !showMPINInput {
                securityQuestionView
                verifyAnswerButton
                logoutButton
                backButton
            } else if !resetMpin && showSecurityQuestions {
                securityQuestionsView
            } else if (!resetMpin && !showSecurityQuestions) || (resetMpin && showMPINInput) {
                mpinInputView
                confirmMpinInputView
            }
        }
        .padding(20)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
    
    private var securityQuestionView: some View {
        VStack(spacing: 12) {
            Text("Answer Security Question")
                .font(.custom("Graphik-Medium", fixedSize: 14))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Menu {
                ForEach(0..<2, id: \.self) { index in
                    if let question = KeychainManager.retrieveSecurityQuestion(forKey: "securityQuestion\(index)") {
                        Button(question) {
                            resetQuestionIndex = index
                        }
                    }
                }
            } label: {
                HStack {
                    Text(selectedQuestions[resetQuestionIndex].isEmpty ? "Select a question" : selectedQuestions[resetQuestionIndex])
                        .font(.custom("Graphik-Regular", size: 16))
                        .multilineTextAlignment(.leading)
                        .lineSpacing(6)
                        .foregroundColor(selectedQuestions[resetQuestionIndex].isEmpty ? .gray : .black)
                    Spacer()
                    Image(systemName: "chevron.down")
                        .foregroundColor(.gray)
                }
                .padding()
                .background(Color.white)
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color(hex: "#C8C8CA"), lineWidth: 1)
                )
            }
            
            HStack {
                LimitedTextField(
                    placeholder: "Answer",
                    text: $resetAnswer,
                    maxLength: 10,
                    isSecure: isResetAnswerHidden,
                    num: false
                )
                .frame(height: 20)
                .font(.custom("Graphik-Medium", fixedSize: 16))
                
                Button(action: {
                    isResetAnswerHidden.toggle()
                }, label: {
                    Image(systemName: isResetAnswerHidden ? "eye.slash" : "eye")
                        .foregroundColor(.gray)
                        .padding(.trailing, 8)
                })
            }
            .padding()
            .background(Color.white)
            .cornerRadius(10)
        }
    }
    
    private var verifyAnswerButton: some View {
        Button(action: {
            hideKeyboard()
            withAnimation(.spring()) {
                isButtonPressed = true
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isButtonPressed = false
            }
            if let storedAnswer = KeychainManager.retrieveSecurityAnswer(forKey: "securityAnswer\(resetQuestionIndex)") {
                if resetAnswer.trimmingCharacters(in: .whitespacesAndNewlines) == storedAnswer {
                    showMPINInput = true
                } else {
                    toastManager.showToast(message: "Incorrect answer. Please try again.")
                    resetAnswer = ""
                }
            } else {
                toastManager.showToast(message: "Error retrieving security question.")
            }
        }, label: {
            Text("Verify Answer")
                .font(.custom("Graphik-Semibold", fixedSize: 18))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.archeColor, Color.archeColor.opacity(0.8)]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .scaleEffect(isButtonPressed ? 0.95 : 1.0)
                .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
        })
    }
    
    private var logoutButton: some View {
        VStack(spacing: 8) {
            (
                Text("Not remember? Then ")
                    .font(.custom("Graphik-Regular", fixedSize: 14))
                    .foregroundColor(.black)
                +
                Text("Login Again")
                    .font(.custom("Graphik-Regular", fixedSize: 14))
                    .foregroundColor(.archeColor)
                    .underline()
            )
            .multilineTextAlignment(.center)
            .onTapGesture {
                hideKeyboard()
                withAnimation(.spring()) {
                    isButtonPressed = true
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    isButtonPressed = false
                }
                onCompletion?(true, "Logged out successfully.")
            }
        }
        .padding(.horizontal)
    }
    private var backButton: some View {
        VStack(spacing: 20) {
            
                Text("OR")
                    .font(.custom("Graphik-Regular", fixedSize: 14))
                    .foregroundColor(.black)
                
            Text("Go Back")
                .font(.custom("Graphik-Regular", fixedSize: 14))
                .foregroundColor(.archeColor)
                .underline()
                .onTapGesture {
                    coordinator.pop()
                }
                .accessibilityLabel("Go back to previous screen")
            
            }
        }
    
    private var mpinInputView: some View {
        VStack(spacing: 12) {
            Text("Enter MPIN")
                .font(.custom("Graphik-Medium", fixedSize: 14))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            HStack(spacing: 12) {
                ForEach(0..<4, id: \.self) { index in
                    SecureField("", text: $mpin[index])
                        .frame(width: 64, height: 64)
                        .background(Color.white)
                        .foregroundColor(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(focusedField == index ? Color.archeColor : Color.gray.opacity(0.3), lineWidth: 2)
                        )
                        .multilineTextAlignment(.center)
                        .font(.custom("Graphik-Medium", fixedSize: 24))
                        .keyboardType(.numberPad)
                        .textContentType(.oneTimeCode)
                        .focused($focusedField, equals: index)
                        .scaleEffect(focusedField == index ? 1.05 : 1.0)
                        .animation(.easeInOut(duration: 0.2), value: focusedField)
                        .onChange(of: mpin[index]) { newValue in
                            if newValue.count == 1 && index < 3 {
                                focusedField = index + 1
                            }
                            if newValue.count > 1 {
                                mpin[index] = String(newValue.prefix(1))
                            }
                        }
                }
            }
        }
    }
    
    private var confirmMpinInputView: some View {
        VStack(spacing: 12) {
            Text("Confirm MPIN")
                .font(.custom("Graphik-Medium", fixedSize: 14))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            HStack(spacing: 12) {
                ForEach(0..<4, id: \.self) { index in
                    SecureField("", text: $confirmMpin[index])
                        .frame(width: 64, height: 64)
                        .background(Color.white)
                        .foregroundColor(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(focusedField == (index + 4) ? Color.archeColor : Color.gray.opacity(0.3), lineWidth: 2)
                        )
                        .multilineTextAlignment(.center)
                        .font(.custom("Graphik-Medium", fixedSize: 24))
                        .keyboardType(.numberPad)
                        .textContentType(.oneTimeCode)
                        .focused($focusedField, equals: index + 4)
                        .scaleEffect(focusedField == (index + 4) ? 1.05 : 1.0)
                        .animation(.easeInOut(duration: 0.2), value: focusedField)
                        .onChange(of: confirmMpin[index]) { newValue in
                            if newValue.count == 1 && index < 3 {
                                focusedField = index + 5
                            }
                            if newValue.count > 1 {
                                confirmMpin[index] = String(newValue.prefix(1))
                            }
                        }
                }
            }
        }
    }
    
    private var securityQuestionsView: some View {
        VStack(spacing: 12) {
            Text("Select Security Questions")
                .font(.custom("Graphik-Medium", fixedSize: 14))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            ForEach(0..<2, id: \.self) { index in
                VStack(spacing: 8) {
                    Menu {
                        ForEach(securityQuestions, id: \.self) { question in
                            Button(question) {
                                selectedQuestions[index] = question
                            }
                        }
                    } label: {
                        HStack {
                            Text(selectedQuestions[index].isEmpty ? "Select a question" : selectedQuestions[index])
                                .font(.custom("Graphik-Regular", size: 16))
                                .multilineTextAlignment(.leading)
                                .lineSpacing(6)
                                .foregroundColor(selectedQuestions[index].isEmpty ? .gray : .black)
                            Spacer()
                            Image(systemName: "chevron.down")
                                .foregroundColor(.gray)
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color(hex: "#C8C8CA"), lineWidth: 1)
                        )
                    }
                    
                    HStack {
                        LimitedTextField(
                            placeholder: "Answer \(index + 1)",
                            text: $questionAnswers[index],
                            maxLength: 10,
                            isSecure: isAnswerHidden[index],
                            num: false
                        )
                        .frame(height: 20)
                        .font(.custom("Graphik-Medium", fixedSize: 16))
                        
                        Button(action: {
                            isAnswerHidden[index].toggle()
                        }, label: {
                            Image(systemName: isAnswerHidden[index] ? "eye.slash" : "eye")
                                .foregroundColor(.gray)
                                .padding(.trailing, 8)
                        })
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(10)
                }
            }
        }
    }
    
    private var continueButton: some View {
        Button(action: {
            hideKeyboard()
            withAnimation(.spring()) {
                isButtonPressed = true
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isButtonPressed = false
            }
            if selectedQuestions.contains("") || questionAnswers.contains("") {
                toastManager.showToast(message: "Please select and answer both security questions!")
                return
            }
            if selectedQuestions[0] == selectedQuestions[1] {
                toastManager.showToast(message: "Please select different security questions!")
                return
            }
            showSecurityQuestions = false
        }, label: {
            Text("Continue")
                .font(.custom("Graphik-Semibold", fixedSize: 18))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.archeColor, Color.archeColor.opacity(0.8)]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .scaleEffect(isButtonPressed ? 0.95 : 1.0)
                .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
        })
        .padding(.horizontal, 40)
    }
    
    private var setMPINButton: some View {
        Button(action: {
            hideKeyboard()
            withAnimation(.spring()) {
                isButtonPressed = true
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isButtonPressed = false
            }
            let mpinString = mpin.joined()
            let confirmMpinString = confirmMpin.joined()
            
            if mpinString.isEmpty || confirmMpinString.isEmpty {
                toastManager.showToast(message: "Please enter both MPIN fields!")
                return
            }
            if mpinString.count != 4 || confirmMpinString.count != 4 {
                toastManager.showToast(message: "MPIN must be 4 digits!")
                return
            }
            if mpinString != confirmMpinString {
                toastManager.showToast(message: "MPINs do not match!")
                return
            }
            
            if !resetMpin {
                for index in 0..<2 {
                    if !KeychainManager.saveSecurityQuestion(selectedQuestions[index], answer: questionAnswers[index], forKey: "securityQuestion\(index)", answerKey: "securityAnswer\(index)") {
                        toastManager.showToast(message: "Failed to save security questions. Please try again.")
                        return
                    }
                }
            }
            
            if KeychainManager.savePassword(mpinString, forKey: "userMPIN") {
                
                toastManager.showToast(message: resetMpin ? "Your MPIN has been reset successfully." : "Your MPIN has been set successfully.")
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                    if !resetMpin {
                        UserDefaults.standard.set(loginModel?.user.name ?? "", forKey: "savedName")
                        UserDefaults.standard.set(loginModel?.user.email ?? "", forKey: "savedEmail")
                        UserDefaults.standard.set(loginModel?.user.mobile ?? "", forKey: "savedMobile")
                        UserDefaults.standard.set(loginModel?.user.employeeid ?? "", forKey: "savedEmployeeID")
                        UserDefaults.standard.set(true, forKey: "isMPINSet")
                        UserDefaults.standard.set(true, forKey: "isLoggedIn")
                        UserDefaults.standard.set(true, forKey: "isFromLogin")
                        UserDefaults.standard.set(true, forKey: "loginThroughOTP")
                        navigation.isLoggedIn = true
                        navigation.isFirstTime = false
                    }
                    coordinator.popToRoot()
                }
            } else {
                toastManager.showToast(message: "Failed to save MPIN. Please try again.")
            }
        }, label: {
            Text(resetMpin ? "Reset MPIN" : "Set MPIN")
                .font(.custom("Graphik-Semibold", fixedSize: 18))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.archeColor, Color.archeColor.opacity(0.8)]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .scaleEffect(isButtonPressed ? 0.95 : 1.0)
                .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
        })
        .padding(.horizontal, 20)
    }
    
    private var toastView: some View {
        Group {
            if toastManager.showToast {
                VStack {
                    Spacer()
                    ToastView(message: toastManager.toastMessage)
                }
            }
        }
    }
    
    private func hideKeyboard() {
        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

struct MPINView_Previews: PreviewProvider {
    static var previews: some View {
        MPINView(
            resetMpin: false,
            loginModel: LoginModel(
                message: "",
                status: 200,
                user: User(
                    email: "",
                    name: "",
                    employeeid: "",
                    designation: "",
                    employeeGrade: "",
                    department: "",
                    location: "",
                    state: "",
                    mobile: "",
                    userDetails: UserDetails(
                        reportingManager: "",
                        divisionalHead: "",
                        pan: "",
                        uan: "",
                        bloodGroup: "",
                        permanentAddress: "",
                        temporaryAddress: "",
                        emergencyContactName: "",
                        emergencyContactRelation: "",
                        emergencyContact: "",
                        reportingManagermail: "",
                        travelRequestPending: 2,
                        documents: [],
                        grade: "",
                        aadharNumber: "",
                        dob:""
                    ),
                    travelFlight: true
                ),
                profilePic: "",
                sos: "",
                services: [],
                offices: [],
                policiesList: [],
                communique: [],
                sosBlogs: [],
                assetDetails: [],
                greetingCategories: [],
                eventPopup: Eventpopup(name: "", fromDate: "", toDate: "", image: "", eventDate: "", description: "")
            ),
            onCompletion: { success, message in
                print("MPIN setup/reset completed: Success = \(success), Message = \(message ?? "No message")")
            }
        )
        .environmentObject(NavigationManager())
        .environmentObject(AppCoordinator())
    }
}

class KeychainManager {
    static func savePassword(_ password: String, forKey key: String) -> Bool {
        guard let data = password.data(using: .utf8) else {
            return false
        }
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        
        SecItemDelete(query as CFDictionary)
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }
    
    static func saveSecurityQuestion(_ question: String, answer: String, forKey questionKey: String, answerKey: String) -> Bool {
        guard let questionData = question.data(using: .utf8), let answerData = answer.data(using: .utf8) else {
            return false
        }
        
        let questionQuery: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: questionKey,
            kSecValueData as String: questionData
        ]
        SecItemDelete(questionQuery as CFDictionary)
        let questionStatus = SecItemAdd(questionQuery as CFDictionary, nil)
        
        let answerQuery: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: answerKey,
            kSecValueData as String: answerData
        ]
        SecItemDelete(answerQuery as CFDictionary)
        let answerStatus = SecItemAdd(answerQuery as CFDictionary, nil)
        
        return questionStatus == errSecSuccess && answerStatus == errSecSuccess
    }
    
    static func retrieveSecurityQuestion(forKey key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecMatchLimit as String: kSecMatchLimitOne,
            kSecReturnData as String: true
        ]
        
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess, let data = item as? Data, let question = String(data: data, encoding: .utf8) else {
            return nil
        }
        return question
    }
    
    static func retrieveSecurityAnswer(forKey key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecMatchLimit as String: kSecMatchLimitOne,
            kSecReturnData as String: true
        ]
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess, let data = item as? Data, let answer = String(data: data, encoding: .utf8) else {
            return nil
        }
        return answer
    }
    
    static func checkMPINExists(forKey key: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecMatchLimit as String: kSecMatchLimitOne,
            kSecReturnData as String: true
        ]
        
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        return status == errSecSuccess
    }
}
