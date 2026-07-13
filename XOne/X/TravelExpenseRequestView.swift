//  TravelExpenseRequestView.swift
//  ArcheOne
//
//  Created by Arche on 01/07/26.
//

import SwiftUI

// MARK: - Main View
struct TravelExpenseRequestView: View {
    let user: User
    @EnvironmentObject var coordinator: AppCoordinator
    @State private var showCreateSheet = false
    @State private var searchText: String = ""
    
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
                        sectionHeader(
                            title: "Travel Request",
                            subtitle: "Create and manage travel requests with trip details and approvals."
                        )
                        
                        // Search Bar
                        HStack {
                            Image(systemName: "magnifyingglass")
                                .foregroundColor(.gray)
                            TextField("Search travel requests...", text: $searchText)
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
                        
                        TravelRequestHistoryListView(searchText: searchText)
                    }
                    .padding(.bottom, 80)
                }
                // Floating Action Button - Bottom Right
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Button(action: {
                            showCreateSheet = true
                        }) {
                            HStack(spacing: 8) {
                                Image(systemName: "plus")
                                    .font(.system(size: 18, weight: .semibold))

                                Text("Add New")
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
            }
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text("Travel Request")
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
            .sheet(isPresented: $showCreateSheet) {
                CreateTravelRequestView()
                    .environmentObject(coordinator)
            }
    }
    
    // MARK: - Helper Views
    private func sectionHeader(title: String, subtitle: String) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(.custom("Graphik-SemiBold", size: 26))
                .foregroundColor(.black)
            Text(subtitle)
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
                .lineLimit(3)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal)
    }
}

// MARK: - History List
struct TravelRequestHistoryListView: View {
    let searchText: String
    
    var filteredRequests: [TravelRequestItem] {
        if searchText.isEmpty {
            return mockTravelRequests
        }
        return mockTravelRequests.filter { request in
            request.destination.lowercased().contains(searchText.lowercased()) ||
            request.tripID.lowercased().contains(searchText.lowercased()) ||
            request.projectID.lowercased().contains(searchText.lowercased()) ||
            request.status.lowercased().contains(searchText.lowercased())
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("All Travel Requests")
                .font(.custom("Graphik-SemiBold", size: 20))
                .foregroundColor(.black)
                .padding(.horizontal)
            
            if filteredRequests.isEmpty {
                emptyStateView
            } else {
                LazyVStack(spacing: 16) {
                    ForEach(filteredRequests) { request in
                        TravelRequestHistoryCard(request: request)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 16) {
            Image(systemName: "suitcase")
                .font(.system(size: 48))
                .foregroundColor(.gray.opacity(0.6))
            Text("No travel requests found")
                .font(.custom("Graphik-SemiBold", size: 18))
            Text(searchText.isEmpty ? "You haven't created any travel requests yet." : "No matches for \"\(searchText)\"")
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 60)
    }
}

// MARK: - History Card
struct TravelRequestHistoryCard: View {
    let request: TravelRequestItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text("#\(String(request.id.uuidString.prefix(6)))")
                    .font(.custom("Graphik-SemiBold", size: 18))
                    .foregroundColor(.black)
                Spacer()
                statusBadge(status: request.status)
            }
            
            Divider()
            
            detailRow(label: "Trip ID", value: request.tripID)
            detailRow(label: "Project ID", value: request.projectID)
            detailRow(label: "Destination", value: request.destination)
            detailRow(label: "Travel Dates", value: request.travelDates)
            detailRow(label: "Estimated Cost", value: "₹\(request.estimatedCost)")
            detailRow(label: "Mode of Travel", value: request.modeOfTravel)
            detailRow(label: "Hotel Needed", value: request.hotelNeeded ? "Yes" : "No")
            detailRow(label: "Vehicle Needed", value: request.vehicleNeeded ? "Yes" : "No")
            detailRow(label: "Advance Needed", value: request.advanceNeeded ? "Yes" : "No")
            detailRow(label: "Advance Amount", value: request.advanceAmount)
            detailRow(label: "Approved Amount", value: request.approvedAmount)
            
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
                .frame(width: 140, alignment: .leading)
            Spacer()
            Text(value)
                .font(.custom("Graphik-Medium", size: 14))
                .foregroundColor(.black)
                .multilineTextAlignment(.trailing)
        }
    }
    
    private func statusBadge(status: String) -> some View {
        Text(status.capitalized)
            .font(.custom("Graphik-Medium", size: 13))
            .foregroundColor(statusColor(status))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(statusColor(status).opacity(0.15))
            .cornerRadius(8)
    }
    
    private func statusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "approved": return .green
        case "pending": return .orange
        case "rejected": return .red
        default: return .gray
        }
    }
}

// MARK: - Model
struct TravelRequestItem: Identifiable {
    let id = UUID()
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
}

let mockTravelRequests: [TravelRequestItem] = [
    TravelRequestItem(
        tripID: "TRP-001", projectID: "PROJ-001", destination: "Bangalore",
        travelDates: "12-15 Apr 2026", estimatedCost: 28500, modeOfTravel: "Flight",
        hotelNeeded: true, vehicleNeeded: false, advanceNeeded: true,
        advanceAmount: "₹5,000", approvedAmount: "₹5,000", status: "Approved"
    ),
    TravelRequestItem(
        tripID: "TRP-002", projectID: "PROJ-002", destination: "Mumbai",
        travelDates: "28-30 Mar 2026", estimatedCost: 12400, modeOfTravel: "Train",
        hotelNeeded: true, vehicleNeeded: true, advanceNeeded: false,
        advanceAmount: "-", approvedAmount: "-", status: "Pending"
    )
]

// MARK: - Create Sheet
struct CreateTravelRequestView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var coordinator: AppCoordinator
    
    @State private var projectID = ""
    @State private var description = ""
    @State private var destination = ""
    @State private var startDate = Date()
    @State private var endDate = Date()
    @State private var estimatedCost = 0
    @State private var modeOfTravel = ""
    @State private var hotelNeeded = false
    @State private var vehicleNeeded = false
    @State private var advanceNeeded = false
    @State private var advanceAmount = ""
    @State private var advanceType = "Cash"
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                ScrollView {
                    
                    VStack(alignment: .leading, spacing: 20) {
                        HStack(spacing: 16) {
                            Image(systemName: "airplane.departure")
                                .font(.system(size: 40))
                                .foregroundColor(.archeColor)
                                .padding(12)
                                .background(Color.archeColor.opacity(0.1))
                                .clipShape(Circle())

                            VStack(alignment: .leading, spacing: 4) {
                                Text("Travel Request")
                                    .font(.custom("Graphik-SemiBold", size: 24))

                                Text("Submit a new travel request for approval and track your travel plans.")
                                    .font(.custom("Graphik-Regular", size: 14))
                                    .foregroundColor(.gray)
                            }

                            Spacer()
                        }
                        .padding(.horizontal)
                        customTextField(placeholder: "Project ID *", text: $projectID)
                        customTextField(placeholder: "Description (Optional)", text: $description)
                        customTextField(placeholder: "Destination *", text: $destination)
                        
                        HStack(spacing: 16) {
                            customDateField(label: "Start Date *", date: $startDate)
                            customDateField(label: "End Date *", date: $endDate)
                        }
                        
                        customTextField(placeholder: "Estimated Total Cost *", text: Binding(
                            get: { String(estimatedCost) },
                            set: { estimatedCost = Int($0) ?? 0 }
                        )).keyboardType(.numberPad)
                        
                        customDropdown(title: "Mode Of Travel *", selection: $modeOfTravel, options: ["Flight", "Train", "Bus", "Own vehicle"])
                        
                        Toggle("Hotel Accommodation Needed ?", isOn: $hotelNeeded).padding(.horizontal)
                        Toggle("Vehicle Needed ?", isOn: $vehicleNeeded).padding(.horizontal)
                        Toggle("Advance Needed ?", isOn: $advanceNeeded).padding(.horizontal)
                        
                        if advanceNeeded {
                            VStack(spacing: 16) {
                                customTextField(placeholder: "Advance Amount *", text: $advanceAmount)
                                    .keyboardType(.numberPad)
                                customDropdown(title: "Advance Type *", selection: $advanceType, options: ["Cash", "Bank Transfer"])
                            }
                            .padding(.horizontal)
                        }
                        // Create Request Button at Bottom
                        Button(action: {
                            // TODO: Add your save logic here
                            dismiss()
                        }) {
                            Text("Create Request")
                                .font(.custom("Graphik-SemiBold", size: 18))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 18)
                                .background(Color.archeColor)
                                .cornerRadius(16)
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 15)
                    }
                    .padding()
                }
                
            }
            .navigationTitle("Create New Travel Request")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
    }
    
    // Reusable components (same as before)
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
    
    private func customDateField(label: String, date: Binding<Date>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.custom("Graphik-Medium", size: 14))
                .foregroundColor(.black)
            DatePicker("", selection: date, displayedComponents: .date)
                .datePickerStyle(.compact)
                .labelsHidden()
                .padding()
                .background(Color.white)
                .cornerRadius(10)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#C8C8CA"), lineWidth: 1))
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
                    Text(selection.wrappedValue.isEmpty ? "Select" : selection.wrappedValue)
                        .foregroundColor(selection.wrappedValue.isEmpty ? .gray : .black)
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
