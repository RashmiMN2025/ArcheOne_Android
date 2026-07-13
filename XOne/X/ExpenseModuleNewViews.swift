import SwiftUI

struct ExpenseModuleDashboardView: View {
    let user: User?
    @EnvironmentObject var coordinator: AppCoordinator
    @StateObject private var toastManager = ToastManager()
    
    private let columns = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]
    
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "#E0DCD1"), Color(hex: "#C8C2CA"), Color(hex: "#4A474C")]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            ScrollView(showsIndicators: false) {
                VStack(spacing: 24) {
                    header

                    LazyVGrid(columns: columns, spacing: 16) {
                        moduleTile(
                            title: "Travel Request",
                            iconName: "airplane",
                            description: "Submit new travel expense requests"
                        ) {
                            coordinator.push(.travelExpenseRequest(user ?? sampleUser))
                        }
                        
                        
                        moduleTile(
                                title: "My Expense",
                                iconName: "bookmark",
                                description: "View and manage my expenses"
                        ) {
                            coordinator.push(.expenseReport(user ?? sampleUser))
                        }

                        moduleTile(
                            title: "Mileage Calculator",
                            iconName: "car.fill",
                            description: "Estimate trip reimbursement"
                        ) {
                            coordinator.push(.mileageCalculator(user ?? sampleUser))
                        }

                        moduleTile(
                            title: "Approvals",
                            iconName: "checkmark.seal.fill",
                            description: "Review pending approvals"
                        ) {
                            coordinator.push(.expenseApproval(user ?? sampleUser))
                        }

                    }
                    .padding(.horizontal)
                    .padding(.bottom, 24)
                }
                .padding(.top, 20)
            }

            if toastManager.showToast {
                VStack {
                    Spacer()
                    ToastView(message: toastManager.toastMessage)
                }
                .padding(.horizontal, 20)
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { coordinator.pop() }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.black)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("Expense Desk")
                    .font(.custom("Graphik-SemiBold", size: 18))
                    .foregroundColor(.black)
            }
        }
        .enableSwipeBack()
    }
    
    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Expense Desk")
                .font(.custom("Graphik-SemiBold", size: 28))
                .foregroundColor(.black)

            Text("Manage travel expenses, mileage, approvals and reports from one place.")
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
                .lineLimit(3)

            HStack(alignment: .center, spacing: 12) {
                Image(systemName: "sparkles")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                    .padding(10)
                    .background(Color.archeColor)
                    .cornerRadius(12)

                VStack(alignment: .leading, spacing: 4) {
                    Text(user?.name ?? "Guest User")
                        .font(.custom("Graphik-Medium", size: 16))
                        .foregroundColor(.black)
                    Text("Employee Id:\(user?.employeeid ?? "")")
                        .font(.custom("Graphik-Regular", size: 13))
                        .foregroundColor(.gray)
                    Text("Grade:\(user?.employeeGrade ?? "")")
                        .font(.custom("Graphik-Regular", size: 13))
                        .foregroundColor(.gray)
                    Text("Ready to file a new request?")
                        .font(.custom("Graphik-Regular", size: 13))
                        .foregroundColor(.gray)
                }
            }
            .padding()
            .background(Color.white)
            .cornerRadius(18)
            .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 6)
        }
        .padding(.horizontal)
    }
    
    @ViewBuilder
    private func moduleTile(title: String, iconName: String, description: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(spacing: 16) {
                ZStack {
                    Circle()
                        .fill(Color.archeColor.opacity(0.12))
                        .frame(width: 58, height: 58)
                    Image(systemName: iconName)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24, height: 24)
                        .foregroundColor(.archeColor)
                }

                Text(title)
                    .font(.custom("Graphik-SemiBold", size: 15))
                    .foregroundColor(.black)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                Text(description)
                    .font(.custom("Graphik-Regular", size: 12))
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
            }
            .padding()
            .frame(minHeight: 180)
            .background(Color.white)
            .cornerRadius(18)
            .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 6)
        }
        .buttonStyle(PlainButtonStyle())
    }
}



private let sampleUser = User(
    email: "sample@arche.com",
    name: "Sample User",
    employeeid: "EMP000",
    designation: "Analyst",
    employeeGrade: "G3",
    department: "Operations",
    location: "Bengaluru",
    state: "Karnataka",
    mobile: "0000000000",
    userDetails: UserDetails(
        reportingManager: "",
        divisionalHead: "",
        pan: "",
        uan: "",
        bloodGroup: "",
        permanentAddress: "",
        divisionalHeadmail: "",
        temporaryAddress: "",
        emergencyContactName: "",
        emergencyContactRelation: "",
        emergencyContact: "",
        reportingManagermail: "",
        travelRequestPending: 0,
        documents: [],
        grade: "",
        aadharNumber: "",
        dob: "",
        access: ""
    ),
    travelFlight: true
)

struct ExpenseModuleNewViews_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            ExpenseModuleDashboardView(user: nil)
                .environmentObject(AppCoordinator(navigationManager: NavigationManager()))
            TravelExpenseRequestView(user: sampleUser)
                .environmentObject(AppCoordinator(navigationManager: NavigationManager()))
            TravelExpenseRequestView(user: sampleUser)
                .environmentObject(AppCoordinator(navigationManager: NavigationManager()))
            MileageOverviewView()
                .environmentObject(AppCoordinator(navigationManager: NavigationManager()))
            ApprovalsView()
                .environmentObject(AppCoordinator(navigationManager: NavigationManager()))
            ExpenseExtractionView()
                .environmentObject(AppCoordinator(navigationManager: NavigationManager()))
        }
    }
}
