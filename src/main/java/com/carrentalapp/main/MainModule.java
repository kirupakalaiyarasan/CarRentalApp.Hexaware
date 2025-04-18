package com.carrentalapp.main;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

import com.carrental.entity.Customer;
import com.carrental.entity.Lease;
import com.carrental.entity.Payment;
import com.carrental.entity.Vehicle;
import com.carrental.util.DBConnectionUtil;
import com.carrentalapp.dao.CarLeaseRepositoryImpl;
import com.carrentalapp.exception.CarNotFoundException;
import com.carrentalapp.exception.CustomerNotFoundException;
import com.carrentalapp.exception.LeaseNotFoundException;

public class MainModule {
    public static void main(String[] args) {
        // Establishing connection to the database
        Connection connection = DBConnectionUtil.getConnection();
        CarLeaseRepositoryImpl repository = new CarLeaseRepositoryImpl();
        Scanner scanner = new Scanner(System.in);

        // Main loop for menu-driven application
        while (true) {
            System.out.println("\nCar Rental System Menu");
            System.out.println("1. Add Car");
            System.out.println("2. Remove Car");
            System.out.println("3. List Available Cars");
            System.out.println("4. Find Car by ID");
            System.out.println("5. Add Customer");
            System.out.println("6. Remove Customer");
            System.out.println("7. List All Customers");
            System.out.println("8. Create Lease");
            System.out.println("9. List Active Leases");
            System.out.println("10. List Lease History");
            System.out.println("11. Return Car");
            System.out.println("12. Record Payment");
            System.out.println("13. List Rented Cars");
            System.out.println("14. Payment History");
            System.out.println("15. Calculate Total Revenue");
            System.out.println("16. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            try {
                switch (choice) {
                    case 1:
                        // Add a new car
                        System.out.print("Enter Make: ");
                        String make = scanner.next();
                        System.out.print("Enter Model: ");
                        String model = scanner.next();
                        System.out.print("Enter Year: ");
                        int year = scanner.nextInt();
                        System.out.print("Enter Daily Rate: ");
                        double dailyRate = scanner.nextDouble();
                        System.out.print("Enter Passenger Capacity: ");
                        int passengerCapacity = scanner.nextInt();
                        System.out.print("Enter Engine Capacity: ");
                        double engineCapacity = scanner.nextDouble();
                        System.out.print("Enter Status (available/notAvailable): ");
                        String status = scanner.next();
                        Vehicle car = new Vehicle(0, make, model, year, dailyRate, status, passengerCapacity, engineCapacity);
                        repository.addCar(car);
                        System.out.println("Car added successfully.");
                        break;

                    case 2:
                        // Remove a car
                        System.out.print("Enter Car ID: ");
                        int carID = scanner.nextInt();
                        repository.removeCar(carID);
                        System.out.println("Car removed successfully.");
                        break;

                    case 3:
                        // List available cars
                        List<Vehicle> availableCars = repository.listAvailableCars();
                        System.out.println("Available Cars:");
                        for (Vehicle v : availableCars) {
                            System.out.println(v.getVehicleID() + ": " + v.getMake() + " " + v.getModel());
                        }
                        break;

                    case 4:
                        // Find a car by ID
                        System.out.print("Enter Car ID: ");
                        carID = scanner.nextInt();
                        Vehicle foundCar = repository.findCarById(carID);
                        System.out.println("Car found: " + foundCar.getMake() + " " + foundCar.getModel());
                        break;

                    case 5:
                        // Add a new customer
                        System.out.print("Enter First Name: ");
                        String firstName = scanner.next();
                        System.out.print("Enter Last Name: ");
                        String lastName = scanner.next();
                        System.out.print("Enter Email: ");
                        String email = scanner.next();
                        System.out.print("Enter Phone Number: ");
                        String phoneNumber = scanner.next();
                        Customer customer = new Customer(0, firstName, lastName, email, phoneNumber);
                        repository.addCustomer(customer);
                        System.out.println("Customer added successfully.");
                        break;

                    case 6:
                        // Remove a customer
                        System.out.print("Enter Customer ID: ");
                        int customerID = scanner.nextInt();
                        repository.removeCustomer(customerID);
                        System.out.println("Customer removed successfully.");
                        break;

                    case 7:
                        // List all customers
                        List<Customer> customers = repository.listCustomers();
                        System.out.println("Customers:");
                        for (Customer c : customers) {
                            System.out.println(c.getCustomerID() + ": " + c.getFirstName() + " " + c.getLastName());
                        }
                        break;

                    case 8:
                        // Create a lease
                        System.out.print("Enter Customer ID: ");
                        customerID = scanner.nextInt();
                        System.out.print("Enter Car ID: ");
                        carID = scanner.nextInt();
                        System.out.print("Enter Start Date (yyyy-mm-dd): ");
                        String startDateStr = scanner.next();
                        Date startDate = Date.valueOf(startDateStr);
                        System.out.print("Enter End Date (yyyy-mm-dd): ");
                        String endDateStr = scanner.next();
                        Date endDate = Date.valueOf(endDateStr);
                        Lease lease = repository.createLease(customerID, carID, startDate, endDate);
                        System.out.println("Lease created successfully. Lease ID: " + lease.getLeaseID());
                        break;

                    case 9:
                        // List active leases
                        List<Lease> activeLeases = repository.listActiveLeases();
                        System.out.println("Active Leases:");
                        for (Lease l : activeLeases) {
                            System.out.println("Lease ID: " + l.getLeaseID() + " | Customer ID: " + l.getCustomerID());
                        }
                        break;

                    case 10:
                        // List lease history
                        List<Lease> leaseHistory = repository.listLeaseHistory();
                        System.out.println("Lease History:");
                        for (Lease l : leaseHistory) {
                            System.out.println("Lease ID: " + l.getLeaseID() + " | Customer ID: " + l.getCustomerID());
                        }
                        break;

                    case 11:
                        // Return a car
                        System.out.print("Enter Lease ID: ");
                        int leaseID = scanner.nextInt();
                        repository.returnCar(leaseID);
                        System.out.println("Car returned successfully.");
                        break;

                    case 12:
                        // Record a payment
                        System.out.print("Enter Lease ID: ");
                        leaseID = scanner.nextInt();
                        System.out.print("Enter Payment Amount: ");
                        double amount = scanner.nextDouble();
                        repository.recordPayment(leaseID, amount);
                        System.out.println("Payment recorded successfully.");
                        break;

                    case 13:
                        try {
                            List<Vehicle> rentedCars = repository.listRentedCars();
                            System.out.println("Rented Cars:");
                            if (rentedCars.isEmpty()) {
                                System.out.println("No cars are currently rented.");
                            } else {
                                for (Vehicle car1 : rentedCars) {
                                    System.out.println(car1);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error while retrieving rented cars: " + e.getMessage());
                        }
                        break;

                    case 14:
                        // Payment history
                        System.out.print("Enter Customer ID: ");
                        customerID = scanner.nextInt();
                        List<Payment> paymentHistory = repository.getPaymentHistory(customerID);
                        System.out.println("Payment History:");
                        for (Payment p : paymentHistory) {
                            System.out.println("Payment ID: " + p.getPaymentID() + " | Amount: " + p.getAmount() + " | Date: " + p.getPaymentDate());
                        }
                        break;

                    case 15:
                        // Calculate total revenue
                        double totalRevenue = repository.calculateTotalRevenue();
                        System.out.println("Total Revenue: " + totalRevenue);
                        break;

                    case 16:
                        // Exit
                        System.out.println("Exiting the system...");
                        scanner.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (CarNotFoundException | CustomerNotFoundException | LeaseNotFoundException e) {
            	System.err.println("Error: " + e.getMessage());
            } catch (Exception e) {
            	System.err.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
