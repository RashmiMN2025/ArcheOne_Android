//
//  ApprovalsView.swift
//  ArcheOne
//
//  Created by Arche on 01/07/26.
//

import SwiftUI

struct ApprovalsView: View {
    @EnvironmentObject var coordinator: AppCoordinator
    @State private var selectedTab: ApprovalTab = .travelRequest
    @State private var searchText: String = ""
    
    enum ApprovalTab {
        case expenses, travelRequest, mileage
    }
    
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "#E0DCD1"), Color(hex: "#C8C2CA"), Color(hex: "#4A474C")]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                VStack(alignment: .leading, spacing: 8) {
                    Text("Approvals")
                        .font(.custom("Graphik-SemiBold", size: 26))
                        .foregroundColor(.black)
                    Text("All your pending tasks, reviews, and financial approvals in one place.")
                        .font(.custom("Graphik-Regular", size: 14))
                        .foregroundColor(.gray)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal)
                .padding(.top, 16)
                
                // Tabs
                HStack(spacing: 8) {
                    ApprovalTabButton(title: "Travel Request", isSelected: selectedTab == .travelRequest) {
                        selectedTab = .travelRequest
                    }
                    ApprovalTabButton(title: "Expenses", isSelected: selectedTab == .expenses) {
                        selectedTab = .expenses
                    }
                    ApprovalTabButton(title: "Mileage Expenses", isSelected: selectedTab == .mileage) {
                        selectedTab = .mileage
                    }
                    Spacer()
                }
                .padding(.horizontal)
                .padding([.top,.bottom], 12)
                
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 20) {
                        if selectedTab == .expenses {
                            ExpenseApprovalList(searchText: searchText)
                        } else if selectedTab == .travelRequest {
                            TravelRequestApprovalList(searchText: searchText)
                        } else {
                            MileageApprovalList(searchText: searchText)
                        }
                    }
                    .padding(.bottom, 100)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Approvals")
                    .font(.custom("Graphik-Semibold", size: 18))
                    .foregroundColor(.black)
            }
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { coordinator.pop() }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.black)
                }
            }
        }
        .enableSwipeBack()
    }
}

// MARK: - Tab Button
struct ApprovalTabButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.custom("Graphik-SemiBold", size: 13))
                .foregroundColor(isSelected ? .white : .black)
                .padding(.horizontal, 24)
                .padding(.vertical, 12)
                .background(isSelected ? Color.archeColor : Color.white.opacity(0.9))
                .cornerRadius(30)
        }
    }
}

// MARK: - Data Models
struct SubmittedApprovalExpense: Identifiable {
    let id: String
    let category: String
    let description: String
    let payment: String
    let amount: String
    let date: String
}

struct TravelApprovalRequestItem: Identifiable {
    let id: String
    let tripID: String
    let projectID: String
    let destination: String
    let travelDates: String
    let estimatedCost: Int
    let modeOfTravel: String
    let hotelNeeded: Bool
    let vehicleNeeded: Bool
    let advanceNeeded: Bool
    let advanceAmount: String
    let approvedAmount: String
    let status: String
    let action: String
}

struct MileageApprovalTrip: Identifiable {
    let id: String
    let customerName: String
    let date: String
    let startPoint: String
    let endPoint: String
    let type: String
    let vehicle: String
    let amount: String
    let distance: String
    let status: String
    let action: String
}

// MARK: - Lists
struct ExpenseApprovalList: View {
    let searchText: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Teams expenses")
                .font(.custom("Graphik-SemiBold", size: 20))
                .foregroundColor(.black)
                .padding(.horizontal)
            
            if mockExpenseApprovals.isEmpty {
                ApprovalEmptyStateView(icon: "doc.text", title: "Expense not found", message: "We couldn't find any expense document for this entry.")
            } else {
                LazyVStack(spacing: 16) {
                    ForEach(mockExpenseApprovals) { item in
                        ExpenseApprovalCard(expense: item)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct TravelRequestApprovalList: View {
    let searchText: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Team travel requests")
                .font(.custom("Graphik-SemiBold", size: 20))
                .foregroundColor(.black)
                .padding(.horizontal)
            
            if mockTravelApprovals.isEmpty {
                ApprovalEmptyStateView(icon: "suitcase", title: "No travel request found", message: "No travel request found for the current filters.")
            } else {
                LazyVStack(spacing: 16) {
                    ForEach(mockTravelApprovals) { item in
                        TravelApprovalCard(travel: item)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct MileageApprovalList: View {
    let searchText: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            HStack(spacing: 12) {
                ApprovalStatCard(title: "Team Distance Traveled", value: "0.00 km", icon: "road.lanes")
                ApprovalStatCard(title: "Carbon Emissions", value: "0.00 Kg CO₂e", icon: "leaf")
                ApprovalStatCard(title: "Total Claim Amount", value: "₹0.00", icon: "indianrupeesign")
            }
            .padding(.horizontal)
            
            Text("Team mileage expenses")
                .font(.custom("Graphik-SemiBold", size: 20))
                .foregroundColor(.black)
                .padding(.horizontal)
            
            if mockMileageApprovals.isEmpty {
                ApprovalEmptyStateView(icon: "figure.walk", title: "No data", message: "You haven't spent anything yet")
            } else {
                LazyVStack(spacing: 16) {
                    ForEach(mockMileageApprovals) { item in
                        MileageApprovalCard(mileage: item)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

// MARK: - Enhanced Cards (Showing All Keys)

struct ExpenseApprovalCard: View {
    let expense: SubmittedApprovalExpense
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading) {
                    Text(expense.description)
                        .font(.custom("Graphik-SemiBold", size: 18))
                    Text("\(expense.category) • \(expense.date)")
                        .font(.custom("Graphik-Regular", size: 14))
                        .foregroundColor(.gray)
                }
                Spacer()
                statusBadge(status: "Pending")
            }
            
            Divider()
            
            VStack(alignment: .leading, spacing: 8) {
                DetailsApprovalRow(title: "Payment", value: expense.payment)
                DetailsApprovalRow(title: "Amount", value: expense.amount, isBold: true)
                DetailsApprovalRow(title: "ID", value: expense.id)
            }
            
            HStack {
                Spacer()
                Button(action: {}) {
                    HStack {
                        Image(systemName: "eye")
                        Text("Review")
                    }
                    .font(.custom("Graphik-Medium", size: 14))
                    .foregroundColor(.archeColor)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 4)
    }
    
    private func statusBadge(status: String) -> some View {
        Text(status)
            .font(.custom("Graphik-Medium", size: 13))
            .foregroundColor(.orange)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color.orange.opacity(0.15))
            .cornerRadius(8)
    }
}

struct TravelApprovalCard: View {
    let travel: TravelApprovalRequestItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading) {
                    Text("\(travel.destination) Trip")
                        .font(.custom("Graphik-SemiBold", size: 18))
                    Text("TRP-\(travel.tripID) • \(travel.travelDates)")
                        .font(.custom("Graphik-Regular", size: 14))
                        .foregroundColor(.gray)
                }
                Spacer()
                statusBadge(status: travel.status)
            }
            
            Divider()
            
            VStack(alignment: .leading, spacing: 8) {
                DetailsApprovalRow(title: "Project", value: travel.projectID)
                DetailsApprovalRow(title: "Mode", value: travel.modeOfTravel)
                DetailsApprovalRow(title: "Estimated Cost", value: "₹\(travel.estimatedCost)", isBold: true)
                DetailsApprovalRow(title: "Hotel Needed", value: travel.hotelNeeded ? "Yes" : "No")
                DetailsApprovalRow(title: "Vehicle Needed", value: travel.vehicleNeeded ? "Yes" : "No")
                DetailsApprovalRow(title: "Advance", value: travel.advanceAmount)
                DetailsApprovalRow(title: "Approved Amount", value: travel.approvedAmount)
                DetailsApprovalRow(title: "Action", value: travel.action)
            }
            
            HStack {
                Spacer()
                Button(action: {}) {
                    HStack {
                        Image(systemName: "eye")
                        Text(travel.action)
                    }
                    .font(.custom("Graphik-Medium", size: 14))
                    .foregroundColor(.archeColor)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 4)
    }
    
    private func statusBadge(status: String) -> some View {
        Text(status)
            .font(.custom("Graphik-Medium", size: 13))
            .foregroundColor(status == "Approved" ? .green : .orange)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background((status == "Approved" ? Color.green : Color.orange).opacity(0.15))
            .cornerRadius(8)
    }
}

struct MileageApprovalCard: View {
    let mileage: MileageApprovalTrip
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading) {
                    Text(mileage.customerName)
                        .font(.custom("Graphik-SemiBold", size: 18))
                    Text("\(mileage.startPoint) → \(mileage.endPoint)")
                        .font(.custom("Graphik-Regular", size: 14))
                        .foregroundColor(.gray)
                }
                Spacer()
                statusBadge(status: mileage.status)
            }
            
            Divider()
            
            VStack(alignment: .leading, spacing: 8) {
                DetailsApprovalRow(title: "Date", value: mileage.date)
                DetailsApprovalRow(title: "Distance", value: mileage.distance)
                DetailsApprovalRow(title: "Vehicle", value: mileage.vehicle)
                DetailsApprovalRow(title: "Type", value: mileage.type)
                DetailsApprovalRow(title: "Amount", value: mileage.amount, isBold: true)
                DetailsApprovalRow(title: "Action", value: mileage.action)
            }
            
            HStack {
                Spacer()
                Button(action: {}) {
                    HStack {
                        Image(systemName: "eye")
                        Text(mileage.action)
                    }
                    .font(.custom("Graphik-Medium", size: 14))
                    .foregroundColor(.archeColor)
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 4)
    }
    
    private func statusBadge(status: String) -> some View {
        Text(status)
            .font(.custom("Graphik-Medium", size: 13))
            .foregroundColor(.orange)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color.orange.opacity(0.15))
            .cornerRadius(8)
    }
}

// MARK: - Reusable Detail Row
struct DetailsApprovalRow: View {
    let title: String
    let value: String
    var isBold: Bool = false
    
    var body: some View {
        HStack {
            Text(title)
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
            Spacer()
            Text(value)
                .font(.custom(isBold ? "Graphik-SemiBold" : "Graphik-Regular", size: 14))
                .foregroundColor(.black)
        }
    }
}

// MARK: - Reusable Views
struct ApprovalEmptyStateView: View {
    let icon: String
    let title: String
    let message: String
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 48))
                .foregroundColor(.gray.opacity(0.6))
            Text(title)
                .font(.custom("Graphik-SemiBold", size: 18))
            Text(message)
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 80)
    }
}

struct ApprovalStatCard: View {
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.custom("Graphik-Regular", size: 13))
                    .foregroundColor(.gray)
                Spacer()
                Image(systemName: icon)
                    .foregroundColor(.gray)
            }
            Text(value)
                .font(.custom("Graphik-SemiBold", size: 19))
                .foregroundColor(.black)
        }
        .padding(14)
        .frame(maxWidth: .infinity)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 8)
    }
}

// MARK: - Dummy Data
let mockExpenseApprovals: [SubmittedApprovalExpense] = [
    SubmittedApprovalExpense(id: "EXP-3601", category: "Accommodation", description: "Hotel stay - Bangalore trip", payment: "Bank Transfer", amount: "₹12,500", date: "Jun 21, 2026"),
    SubmittedApprovalExpense(id: "EXP-3598", category: "Travel", description: "Flight - Mumbai to Delhi", payment: "Corporate Card", amount: "₹8,750", date: "Jun 19, 2026")
]

let mockTravelApprovals: [TravelApprovalRequestItem] = [
    TravelApprovalRequestItem(id: "TRP-001", tripID: "TRP-001", projectID: "PROJ-001", destination: "Bangalore", travelDates: "12-15 Apr 2026", estimatedCost: 28500, modeOfTravel: "Flight", hotelNeeded: true, vehicleNeeded: false, advanceNeeded: true, advanceAmount: "₹5,000", approvedAmount: "₹5,000", status: "Approved", action: "Review & Approve"),
    TravelApprovalRequestItem(id: "TRP-002", tripID: "TRP-002", projectID: "PROJ-002", destination: "Mumbai", travelDates: "28-30 Mar 2026", estimatedCost: 12400, modeOfTravel: "Train", hotelNeeded: true, vehicleNeeded: true, advanceNeeded: false, advanceAmount: "-", approvedAmount: "-", status: "Pending", action: "Review Request")
]

let mockMileageApprovals: [MileageApprovalTrip] = [
    MileageApprovalTrip(id: "MLG-1002", customerName: "Priya Nair", date: "Jun 25, 2026", startPoint: "Hyderabad", endPoint: "Secunderabad", type: "Personal", vehicle: "Bike", amount: "₹480", distance: "28 km", status: "Pending", action: "Review Claim")
]
