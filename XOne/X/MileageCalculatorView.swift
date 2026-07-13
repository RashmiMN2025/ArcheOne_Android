//
//  MileageOverviewView.swift
//  ArcheOne
//
//  Created by Arche on 01/07/26.
//

import SwiftUI

// MARK: - Main View
struct MileageOverviewView: View {
    @EnvironmentObject var coordinator: AppCoordinator
    @State private var searchText: String = ""
    @State private var showAddMileageSheet = false
    
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
                    // Header
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Mileage Overview")
                            .font(.custom("Graphik-SemiBold", size: 26))
                            .foregroundColor(.black)
                        Text("Track and review all travel-based mileage submissions in one place.")
                            .font(.custom("Graphik-Regular", size: 14))
                            .foregroundColor(.gray)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal)
                    
                    // Stats Cards - Compact
                    HStack(spacing: 12) {
                        StatCard(title: "Total distance logged", value: "0.00 km", icon: "road.lanes")
                        StatCard(title: "Total Carbon Emission", value: "0.00 Kg CO₂e", icon: "leaf")
                        StatCard(title: "Total claim amount", value: "₹0.00", icon: "indianrupeesign")
                    }
                    .padding(.horizontal)
                    
                    // Recent Trips
                    VStack(alignment: .leading, spacing: 16) {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("My recent trips")
                                .font(.custom("Graphik-SemiBold", size: 20))
                                .foregroundColor(.black)
                            
                            HStack(spacing: 8) {
                                Image(systemName: "magnifyingglass")
                                    .foregroundColor(.gray)
                                
                                TextField("Search trips...", text: $searchText)
                                    .font(.custom("Graphik-Regular", size: 15))
                            }
                            .padding(10)
                            .background(Color.white)
                            .cornerRadius(12)
                        }.padding(.horizontal,10)
                        if mockMileageTrips.isEmpty {
                            EmptyStatesView(message: "You haven't spent anything yet")
                        } else {
                            LazyVStack(spacing: 16) {
                                ForEach(mockMileageTrips) { trip in
                                    MileageTripCard(trip: trip)
                                }
                            }
                            .padding(.horizontal)
                        }
                    }
                }
                .padding(.bottom, 100)
            }
            
            // Floating Add Button
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(action: { showAddMileageSheet = true }) {
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
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Mileage Overview")
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
        .sheet(isPresented: $showAddMileageSheet) {
            AddMileageExpenseView()
                .environmentObject(coordinator)
        }
    }
}

// MARK: - Compact Stat Card
struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                Text(title)
                    .font(.custom("Graphik-Regular", size: 12))
                    .foregroundColor(.gray)
                    .lineLimit(3)
                    .minimumScaleFactor(0.85)
                Spacer()
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(.gray)
            }
            
            Spacer(minLength: 2)
            
            Text(value)
                .font(.custom("Graphik-SemiBold", size: 17))
                .foregroundColor(.black)
                .lineLimit(4)
        }
        .padding(16)
        .frame(maxWidth: .infinity, minHeight: 120) // Fixed minimum height for equal cards
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 8)
    }
}

// MARK: - Mileage Trip Card
struct MileageTripCard: View {
    let trip: MileageTrip
    
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text(trip.id)
                    .font(.custom("Graphik-SemiBold", size: 18))
                    .foregroundColor(.black)
                Spacer()
                statusBadge(status: trip.status)
            }
            Divider()
            detailRow(label: "Customer Name", value: trip.customerName)
            detailRow(label: "Date", value: trip.date)
            detailRow(label: "Route", value: "\(trip.startPoint) → \(trip.endPoint)")
            detailRow(label: "Type", value: trip.type)
            detailRow(label: "Vehicle", value: trip.vehicle)
            detailRow(label: "Amount", value: trip.amount)
            detailRow(label: "Distance", value: trip.distance)
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

// MARK: - Add Mileage Expense Form (same as previous)
// MARK: - Add Mileage Expense Form (Improved with CreateTravelRequestView style)
struct AddMileageExpenseView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var coordinator: AppCoordinator
    
    @State private var customerName = ""
    @State private var projectID = ""
    @State private var fromDate = Date()
    @State private var toDate = Date()
    @State private var startLocation = ""
    @State private var destination = ""
    @State private var vehicleType = ""
    @State private var vehicle = ""
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // Header
                        HStack(spacing: 16) {
                            Image(systemName: "fuelpump.fill")
                                .font(.system(size: 40))
                                .foregroundColor(.archeColor)
                                .padding(12)
                                .background(Color.archeColor.opacity(0.1))
                                .clipShape(Circle())
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Add Mileage Expense")
                                    .font(.custom("Graphik-SemiBold", size: 24))
                                Text("Add and manage mileage expenses for travel reimbursements.")
                                    .font(.custom("Graphik-Regular", size: 14))
                                    .foregroundColor(.gray)
                            }
                        }
                        .padding(.horizontal)
                        
                        // Form Fields
                        customTextField(placeholder: "Enter customer name", text: $customerName)
                        
                        customDropdown(title: "Project ID", selection: $projectID, options: ["PROJ-001", "PROJ-002", "PROJ-003"])
                        
                        HStack(spacing: 16) {
                            customDateField(label: "From Date", date: $fromDate)
                            customDateField(label: "To Date", date: $toDate)
                        }
                        
                        customTextField(placeholder: "Select starting location", text: $startLocation)
                        customTextField(placeholder: "Select destination", text: $destination)
                        
                        customTextField(placeholder: "Distance will be calculated automatically", text: .constant(""))
                        
                        HStack(spacing: 16) {
                            customDropdown(title: "Vehicle Type", selection: $vehicleType, options: ["PERSONAL", "COMPANY"])
                            customDropdown(title: "Vehicle", selection: $vehicle, options: ["CAR", "BIKE"])
                        }
                        
                        customTextField(placeholder: "Price will be calculated automatically", text: .constant(""))
                        
                        // Save Button
                        Button(action: {
                            dismiss()
                        }) {
                            Text("Save Expense")
                                .font(.custom("Graphik-SemiBold", size: 18))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 18)
                                .background(Color.archeColor)
                                .cornerRadius(16)
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 20)
                    }
                    .padding()
                }
            }
            .navigationTitle("Add Mileage Expense")
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
    
    // MARK: - Reusable Components (Same style as CreateTravelRequestView)
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
                        .foregroundColor(.gray.opacity(0.7))
                    
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
struct EmptyStatesView: View {
    let message: String
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "figure.walk")
                .font(.system(size: 48))
                .foregroundColor(.gray.opacity(0.6))
            Text("No data")
                .font(.custom("Graphik-SemiBold", size: 18))
            Text(message)
                .font(.custom("Graphik-Regular", size: 14))
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 80)
    }
}

// MARK: - Models
struct MileageTrip: Identifiable {
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
}

let mockMileageTrips: [MileageTrip] = [
    MileageTrip(id: "MLG-1001", customerName: "Rahul Sharma", date: "Jun 28, 2026",
                startPoint: "Bangalore", endPoint: "Mysore", type: "Business",
                vehicle: "Car", amount: "₹1,250", distance: "142 km", status: "Approved"),
    MileageTrip(id: "MLG-1002", customerName: "Priya Nair", date: "Jun 25, 2026",
                startPoint: "Hyderabad", endPoint: "Secunderabad", type: "Personal",
                vehicle: "Bike", amount: "₹480", distance: "28 km", status: "Pending")
]
