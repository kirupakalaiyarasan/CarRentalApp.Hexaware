package com.carrentalapp.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.carrental.entity.Customer;
import com.carrental.entity.Lease;
import com.carrental.entity.Payment;
import com.carrental.entity.Vehicle;
import com.carrental.util.DBConnectionUtil;
import com.carrentalapp.exception.CarNotFoundException;
import com.carrentalapp.exception.CustomerNotFoundException;
import com.carrentalapp.exception.LeaseNotFoundException;

public class CarLeaseRepositoryImpl implements ICarLeaseRepository {

    private Connection connection;

    public CarLeaseRepositoryImpl() {
        this.connection = DBConnectionUtil.getConnection();
    }

    // --- Car Management ---

    @Override
    public void addCar(Vehicle car) {
        String query = "INSERT INTO Vehicle (make, model, year, dailyRate, status, passengerCapacity, engineCapacity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, car.getMake());
            stmt.setString(2, car.getModel());
            stmt.setInt(3, car.getYear());
            stmt.setDouble(4, car.getDailyRate());
            stmt.setString(5, car.getStatus());
            stmt.setInt(6, car.getPassengerCapacity());
            stmt.setDouble(7, car.getEngineCapacity());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCar(int carID) {
        String query = "DELETE FROM Vehicle WHERE vehicleID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, carID);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new CarNotFoundException("Car with ID " + carID + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Vehicle> listAvailableCars() {
        List<Vehicle> cars = new ArrayList<>();
        String query = "SELECT * FROM Vehicle WHERE status = 'available'";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cars.add(mapVehicle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }
    @Override
    public List<Vehicle> listRentedCars() {
        List<Vehicle> rentedCars = new ArrayList<>();
        String query = "SELECT * FROM Vehicle WHERE status = 'notAvailable'";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rentedCars.add(new Vehicle(
                    rs.getInt("vehicleID"),
                    rs.getString("make"),
                    rs.getString("model"),
                    rs.getInt("year"),
                    rs.getDouble("dailyRate"),
                    rs.getString("status"),
                    rs.getInt("passengerCapacity"),
                    rs.getDouble("engineCapacity")
                ));
            }

            // Check if no cars are rented
            if (rentedCars.isEmpty()) {
                System.out.println("No cars are currently rented.");
            }
        } catch (SQLException e) {
        	System.err.println("Error while fetching rented cars: " + e.getMessage());
            e.printStackTrace();
        }
        return rentedCars;
    }


    public Vehicle findCarById(int carID) {
        String query = "SELECT * FROM Vehicle WHERE vehicleID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, carID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Vehicle(
                    rs.getInt("vehicleID"),
                    rs.getString("make"),
                    rs.getString("model"),
                    rs.getInt("year"),
                    rs.getDouble("dailyRate"),
                    rs.getString("status"),  // Ensure the latest status is fetched
                    rs.getInt("passengerCapacity"),
                    rs.getDouble("engineCapacity")
                );
            } else {
                throw new CarNotFoundException("Car with ID " + carID + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    // --- Customer Management ---

    @Override
    public void addCustomer(Customer customer) {
        String query = "INSERT INTO Customer (firstName, lastName, email, phoneNumber) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCustomer(int customerID) {
        // Step 1: Delete the payments associated with the customer
        String deletePaymentsQuery = "DELETE FROM Payment WHERE leaseID IN (SELECT leaseID FROM Lease WHERE customerID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(deletePaymentsQuery)) {
            stmt.setInt(1, customerID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting payments: " + e.getMessage());
            e.printStackTrace();
        }

        // Step 2: Delete the leases associated with the customer
        String deleteLeasesQuery = "DELETE FROM Lease WHERE customerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteLeasesQuery)) {
            stmt.setInt(1, customerID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting leases: " + e.getMessage());
            e.printStackTrace();
        }

        // Step 3: Now delete the customer
        String query = "DELETE FROM Customer WHERE customerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerID);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new CustomerNotFoundException("Customer with ID " + customerID + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error while deleting customer: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @Override
    public List<Customer> listCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM Customer";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public Customer findCustomerById(int customerID) {
        String query = "SELECT * FROM Customer WHERE customerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapCustomer(rs);
            } else {
                throw new CustomerNotFoundException("Customer with ID " + customerID + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Lease Management ---

    @Override
    public Lease createLease(int customerID, int carID, Date startDate, Date endDate) {
        String leaseQuery = "INSERT INTO Lease (vehicleID, customerID, startDate, endDate, type) VALUES (?, ?, ?, ?, ?)";
        String updateCarStatusQuery = "UPDATE Vehicle SET status = 'notAvailable' WHERE vehicleID = ?";

        try {
            // Start a transaction
            connection.setAutoCommit(false);

            // Create the lease
            try (PreparedStatement stmt = connection.prepareStatement(leaseQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, carID);
                stmt.setInt(2, customerID);
                stmt.setDate(3, startDate);
                stmt.setDate(4, endDate);
                stmt.setString(5, calculateLeaseType(startDate, endDate));
                stmt.executeUpdate();

                // Retrieve the generated leaseID
                ResultSet rs = stmt.getGeneratedKeys();
                int leaseID = 0;
                if (rs.next()) {
                    leaseID = rs.getInt(1);
                }

                // Update the car status to 'notAvailable'
                try (PreparedStatement updateStmt = connection.prepareStatement(updateCarStatusQuery)) {
                    updateStmt.setInt(1, carID);
                    updateStmt.executeUpdate();
                }

                // Commit the transaction
                connection.commit();

                return new Lease(leaseID, carID, customerID, startDate, endDate, calculateLeaseType(startDate, endDate));
            } catch (SQLException e) {
                connection.rollback(); // Rollback if any exception occurs
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public void returnCar(int leaseID) {
        // Update the vehicle's status to 'available'
        String query = "UPDATE Vehicle SET status = 'available' WHERE vehicleID = (SELECT vehicleID FROM Lease WHERE leaseID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, leaseID);
            int rowsAffected = stmt.executeUpdate();
            
            // If no rows were affected, the lease was not found
            if (rowsAffected == 0) {
                throw new LeaseNotFoundException("Lease with ID " + leaseID + " not found.");
            }
            
            // Commit the transaction to make sure changes are saved
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Error while returning car: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public List<Lease> listActiveLeases() {
        List<Lease> activeLeases = new ArrayList<>();
        String query = "SELECT * FROM Lease WHERE endDate >= CURDATE()";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                activeLeases.add(mapLease(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeLeases;
    }

    @Override
    public List<Lease> listLeaseHistory() {
        List<Lease> leases = new ArrayList<>();
        String query = "SELECT * FROM Lease";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                leases.add(mapLease(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leases;
    }

    // --- Payment Handling ---

    @Override
    public void recordPayment(int leaseID, double amount) {
        // Validate if the leaseID exists
        String validateLeaseQuery = "SELECT leaseID FROM Lease WHERE leaseID = ?";
        try (PreparedStatement validateStmt = connection.prepareStatement(validateLeaseQuery)) {
            validateStmt.setInt(1, leaseID);
            ResultSet rs = validateStmt.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException("Lease ID " + leaseID + " does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Error while validating lease ID: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Insert the payment record
        String query = "INSERT INTO Payment (leaseID, paymentDate, amount) VALUES (?, CURDATE(), ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, leaseID);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
            System.out.println("Payment of " + amount + " recorded successfully for Lease ID: " + leaseID);
        } catch (SQLException e) {
            System.out.println("Error while recording payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Payment> getPaymentHistory(int customerID) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payment WHERE leaseID IN (SELECT leaseID FROM Lease WHERE customerID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public double calculateTotalRevenue() {
        String query = "SELECT SUM(amount) AS totalRevenue FROM Payment";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("totalRevenue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- Helper Methods ---

    private Vehicle mapVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("vehicleID"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getDouble("dailyRate"),
                rs.getString("status"),
                rs.getInt("passengerCapacity"),
                rs.getDouble("engineCapacity")
        );
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customerID"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("email"),
                rs.getString("phoneNumber")
        );
    }

    private Lease mapLease(ResultSet rs) throws SQLException {
        return new Lease(
                rs.getInt("leaseID"),
                rs.getInt("vehicleID"),
                rs.getInt("customerID"),
                rs.getDate("startDate"),
                rs.getDate("endDate"),
                rs.getString("type")
        );
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("paymentID"),
                rs.getInt("leaseID"),
                rs.getDate("paymentDate"),
                rs.getDouble("amount")
        );
    }

    private String calculateLeaseType(Date startDate, Date endDate) {
        long difference = endDate.getTime() - startDate.getTime();
        long days = difference / (1000 * 60 * 60 * 24);
        return days > 30 ? "Monthly" : "Daily";
    }
}
