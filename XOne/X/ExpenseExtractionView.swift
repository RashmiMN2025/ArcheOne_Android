//
//  ExpenseExtractionView.swift
//  ArcheOne
//
//  Created by Arche on 01/07/26.
//

import SwiftUI

// MARK: - Main View
struct ExpenseExtractionView: View {
    @EnvironmentObject var coordinator: AppCoordinator
    @State private var selectedTab: ExpenseTab = .drafts
    @State private var showAddPopup = false
    @State private var showCreateManualSheet = false
    @State private var searchText: String = ""
    
    enum ExpenseTab {
        case drafts, submitted
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
                    Text("Expense Extraction")
                        .font(.custom("Graphik-SemiBold", size: 26))
                        .foregroundColor(.black)
                    Text("Keep track of expenses with real-time metrics")
                        .font(.custom("Graphik-Regular", size: 14))
                        .foregroundColor(.gray)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal)
                .padding(.top, 16)
                
                // Search Bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("Search expenses...", text: $searchText)
                        .font(.custom("Graphik-Regular", size: 16))
                        .foregroundColor(.black)
                }
                .padding(12)
                .background(Color.white)
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                )
                .padding(.horizontal)
                .padding(.top, 8)
                
                // Tab Buttons
                HStack(spacing: 8) {
                    TabButton(title: "Drafts", isSelected: selectedTab == .drafts) {
                        selectedTab = .drafts
                    }
                    TabButton(title: "Submitted", isSelected: selectedTab == .submitted) {
                        selectedTab = .submitted
                    }
                    Spacer()
                }
                .padding(.horizontal)
                .padding([.top,.bottom], 16)
                
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 20) {
                        if selectedTab == .drafts {
                            DraftsListView(searchText: searchText)
                        } else {
                            SubmittedListView(searchText: searchText)
                        }
                    }
                    .padding(.bottom, 100)
                }
            }
            
            // Floating Add Expense Button
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(action: { showAddPopup = true }) {
                        HStack(spacing: 8) {
                            Image(systemName: "plus")
                                .font(.system(size: 18, weight: .semibold))
                            Text("Add Expense")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 20)
                        .frame(height: 56)
                        .background(Color.archeColor)
                        .clipShape(Capsule())
                        .shadow(color: Color.black.opacity(0.2), radius: 8, x: 0, y: 4)
                    }
                    .padding(20)
                }
            }
            
            // Centered Popup for Add Expense
            if showAddPopup {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture { showAddPopup = false }
                
                VStack(spacing: 24) {
                    Text("Add Expense")
                        .font(.custom("Graphik-SemiBold", size: 22))
                        .padding(.top, 20)
                    
                    VStack(spacing: 12) {
                        PopupOptionButton(icon: "camera.fill", title: "Use Camera") {
                            showAddPopup = false
                            print("Camera selected")
                        }
                        PopupOptionButton(icon: "photo.on.rectangle", title: "Add photos and files") {
                            showAddPopup = false
                            print("Files selected")
                        }
                        PopupOptionButton(icon: "pencil", title: "Enter manually") {
                            showAddPopup = false
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                showCreateManualSheet = true
                            }
                        }
                    }
                    .padding(.horizontal, 30)
                    .padding(.bottom, 20)
                }
                .frame(width: 320)
                .background(Color.white)
                .cornerRadius(20)
                .shadow(radius: 20)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Expense Extraction")
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
        .sheet(isPresented: $showCreateManualSheet) {
            ManualExpenseEntryView()
                .environmentObject(coordinator)
        }
    }
}

// MARK: - Popup Option Button
struct PopupOptionButton: View {
    let icon: String
    let title: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .font(.title2)
                    .frame(width: 36)
                Text(title)
                    .font(.custom("Graphik-Medium", size: 17))
                Spacer()
            }
            .padding()
            .background(Color.gray.opacity(0.08))
            .cornerRadius(12)
        }
        .foregroundColor(.black)
    }
}

// MARK: - Tab Button
struct TabButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.custom("Graphik-SemiBold", size: 16))
                .foregroundColor(isSelected ? .white : .black)
                .padding(.horizontal, 32)
                .padding(.vertical, 12)
                .background(isSelected ? Color.archeColor : Color.white)
                .cornerRadius(30)
                .shadow(color: Color.black.opacity(isSelected ? 0.15 : 0.05), radius: 6)
        }
    }
}

// MARK: - Drafts List
struct DraftsListView: View {
    let searchText: String
    
    var filteredExpenses: [ExpenseItem] {
        if searchText.isEmpty { return mockDraftExpenses }
        return mockDraftExpenses.filter { $0.id.lowercased().contains(searchText.lowercased()) }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("Uploaded files (\(filteredExpenses.count))")
                .font(.custom("Graphik-SemiBold", size: 20))
                .foregroundColor(.black)
                .padding(.horizontal)
            
            if filteredExpenses.isEmpty {
                EmptyStateView(message: "No draft expenses found")
            } else {
                LazyVStack(spacing: 16) {
                    ForEach(filteredExpenses) { expense in
                        ExpenseHistoryCard(expense: expense)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct ExpenseHistoryCard: View {
    let expense: ExpenseItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text(expense.id)
                    .font(.custom("Graphik-SemiBold", size: 18))
                    .foregroundColor(.black)
                Spacer()
                statusBadge(status: expense.status)
            }
            Divider()
            detailRow(label: "Bill Date", value: expense.billDate)
            detailRow(label: "Vendor Name", value: expense.vendorName)
            detailRow(label: "Category", value: expense.category)
            detailRow(label: "Amount", value: expense.amount)
            detailRow(label: "Uploaded At", value: expense.uploadedAt)
            Divider()
            HStack {
                Spacer()
                Button(action: {}) {
                    HStack {
                        Image(systemName: "eye")
                        Text("View Details")
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
    
    private func detailRow(label: String, value: String) -> some View {
        HStack(alignment: .top) {
            Text(label)
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
                .frame(width: 130, alignment: .leading)
            Spacer()
            Text(value)
                .font(.custom("Graphik-Medium", size: 14))
                .foregroundColor(.black)
                .multilineTextAlignment(.trailing)
        }
    }
    
    private func statusBadge(status: String) -> some View {
        Text(status)
            .font(.custom("Graphik-Medium", size: 13))
            .foregroundColor(.white)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color.blue)
            .cornerRadius(8)
    }
}

// MARK: - Submitted List (Detailed Cards)
struct SubmittedListView: View {
    let searchText: String
    
    var filteredExpenses: [SubmittedExpense] {
        if searchText.isEmpty { return mockSubmittedExpenses }
        return mockSubmittedExpenses.filter {
            $0.id.lowercased().contains(searchText.lowercased()) ||
            $0.category.lowercased().contains(searchText.lowercased())
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("My reimbursements")
                .font(.custom("Graphik-SemiBold", size: 20))
                .foregroundColor(.black)
                .padding(.horizontal)
            
            if filteredExpenses.isEmpty {
                EmptyStateView(message: "No ERP Ready Expenses found")
            } else {
                LazyVStack(spacing: 16) {
                    ForEach(filteredExpenses) { expense in
                        SubmittedExpenseCard(expense: expense)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct SubmittedExpenseCard: View {
    let expense: SubmittedExpense
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text(expense.id)
                    .font(.custom("Graphik-SemiBold", size: 18))
                    .foregroundColor(.black)
                Spacer()
                statusBadge(status: expense.status)
            }
            Divider()
            detailRow(label: "Category", value: expense.category)
            detailRow(label: "Description", value: expense.description)
            detailRow(label: "Payment", value: expense.payment)
            detailRow(label: "Amount", value: expense.amount)
            detailRow(label: "Date", value: expense.date)
            Divider()
            HStack {
                Spacer()
                Button(action: {}) {
                    HStack {
                        Image(systemName: "eye")
                        Text("View Details")
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
    
    private func detailRow(label: String, value: String) -> some View {
        HStack(alignment: .top) {
            Text(label)
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
                .frame(width: 130, alignment: .leading)
            Spacer()
            Text(value)
                .font(.custom("Graphik-Medium", size: 14))
                .foregroundColor(.black)
                .multilineTextAlignment(.trailing)
        }
    }
    
    private func statusBadge(status: String) -> some View {
        Text(status)
            .font(.custom("Graphik-Medium", size: 13))
            .foregroundColor(.green)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color.green.opacity(0.15))
            .cornerRadius(8)
    }
}

// MARK: - Manual Expense Entry
struct ManualExpenseEntryView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var coordinator: AppCoordinator
    
    @State private var expenseID = ""
    @State private var billDate = Date()
    @State private var vendorName = ""
    @State private var category = "Others"
    @State private var amount = ""
    @State private var description = ""
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack(spacing: 16) {
                        Image(systemName: "square.and.pencil")
                            .font(.system(size: 40))
                            .foregroundColor(.archeColor)
                            .padding(12)
                            .background(Color.archeColor.opacity(0.1))
                            .clipShape(Circle())

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Enter Expense Manually")
                                .font(.custom("Graphik-SemiBold", size: 24))

                            Text("Manually enter your expense details for reimbursement and expense tracking.")
                                .font(.custom("Graphik-Regular", size: 14))
                                .foregroundColor(.gray)
                        }

                        Spacer()
                    }
                    .padding(.horizontal)
                    customTextField(placeholder: "Expense ID *", text: $expenseID)
                    customTextField(placeholder: "Vendor Name *", text: $vendorName)
                    customTextField(placeholder: "Amount *", text: $amount).keyboardType(.decimalPad)
                    
                    customDropdown(title: "Category", selection: $category, options: ["Others", "Travel", "Food", "Accommodation", "Transport"])
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Bill Date").font(.custom("Graphik-Medium", size: 14))
                        DatePicker("", selection: $billDate, displayedComponents: .date)
                            .datePickerStyle(.compact)
                            .labelsHidden()
                            .padding()
                            .background(Color.white)
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#C8C8CA"), lineWidth: 1))
                    }
                    
                    customTextField(placeholder: "Description (Optional)", text: $description)
                    
                    Button(action: { dismiss() }) {
                        Text("Submit Expense")
                            .font(.custom("Graphik-SemiBold", size: 18))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 18)
                            .background(Color.archeColor)
                            .cornerRadius(16)
                    }
                    .padding(.top, 20)
                }
                .padding()
            }
            .navigationTitle("Enter Expense Manually")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
    
    private func customTextField(placeholder: String, text: Binding<String>) -> some View {
        ZStack(alignment: .leading) {
            TextField("", text: text)
                .font(.custom("Graphik-Regular", size: 16))
                .foregroundColor(.black)
                .padding(15)
                .background(Color.white)
                .cornerRadius(10)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#C8C8CA"), lineWidth: 1))
            
            if text.wrappedValue.isEmpty {
                Text(placeholder)
                    .font(.custom("Graphik-Regular", size: 16))
                    .foregroundColor(.gray.opacity(0.7))
                    .padding(.horizontal, 15)
                    .padding(.vertical, 15)
                    .allowsHitTesting(false)
            }
        }
    }
    
    private func customDropdown(title: String, selection: Binding<String>, options: [String]) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.custom("Graphik-Medium", size: 14))
                .foregroundColor(.black)
            Menu {
                ForEach(options, id: \.self) { option in
                    Button(option) { selection.wrappedValue = option }
                }
            } label: {
                HStack {
                    Text(selection.wrappedValue)
                        .foregroundColor(.black)
                    Spacer()
                    Image(systemName: "chevron.down")
                }
                .padding()
                .background(Color.white)
                .cornerRadius(10)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#C8C8CA"), lineWidth: 1))
            }
        }
    }
}

// MARK: - Empty State
struct EmptyStateView: View {
    let message: String
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "doc.text")
                .font(.system(size: 48))
                .foregroundColor(.gray.opacity(0.6))
            Text(message)
                .font(.custom("Graphik-Regular", size: 16))
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 60)
    }
}

// MARK: - Models & Dummy Data
struct ExpenseItem: Identifiable {
    let id: String
    let billDate: String
    let vendorName: String
    let category: String
    let amount: String
    let status: String
    let uploadedAt: String
}

struct SubmittedExpense: Identifiable {
    let id: String
    let category: String
    let description: String
    let payment: String
    let amount: String
    let date: String
    let status: String = "Submitted"
}

let mockDraftExpenses: [ExpenseItem] = [
    ExpenseItem(id: "EXP-3668", billDate: "--", vendorName: "--", category: "Others", amount: "₹2,450", status: "Extracted", uploadedAt: "Jun 25, 2026 09:49 AM"),
    ExpenseItem(id: "EXP-3642", billDate: "--", vendorName: "--", category: "Others", amount: "₹1,890", status: "Extracted", uploadedAt: "Jun 22, 2026 03:46 PM")
]

let mockSubmittedExpenses: [SubmittedExpense] = [
    SubmittedExpense(id: "EXP-3601", category: "Accommodation", description: "Hotel stay - Bangalore trip", payment: "Bank Transfer", amount: "₹12,500", date: "Jun 21, 2026"),
    SubmittedExpense(id: "EXP-3598", category: "Travel", description: "Flight - Mumbai to Delhi", payment: "Corporate Card", amount: "₹8,750", date: "Jun 19, 2026")
]
