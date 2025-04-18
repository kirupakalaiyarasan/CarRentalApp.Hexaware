package com.carrentalapp.dao;

import java.util.List;

import com.carrental.entity.Customer;
import com.carrental.entity.Lease;
import com.carrental.entity.Payment;
import com.carrental.entity.Vehicle;

public interface ICarLeaseRepository {

    // Car Management
    void addCar(Vehicle car);
    void removeCar(int carID);
    List<Vehicle> listAvailableCars();
    List<Vehicle> listRentedCars();
    Vehicle findCarById(int carID);

    // Customer Management
    void addCustomer(Customer customer);
    void removeCustomer(int customerID);
    List<Customer> listCustomers();
    Customer findCustomerById(int customerID);

    // Lease Management
    Lease createLease(int customerID, int carID, java.sql.Date startDate, java.sql.Date endDate);
    void returnCar(int leaseID);
    List<Lease> listActiveLeases();
    List<Lease> listLeaseHistory();

    // Payment Handling
    void recordPayment(int leaseID, double amount);
    List<Payment> getPaymentHistory(int customerID);
    double calculateTotalRevenue();
}
