package dao;

import org.junit.jupiter.api.*;

import com.carrental.entity.Lease;
import com.carrental.entity.Payment;
import com.carrental.entity.Vehicle;
import com.carrental.util.DBConnectionUtil;
import com.carrentalapp.dao.CarLeaseRepositoryImpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures test methods run in order
public class CarLeaseRepositoryImplTest {

    private static Connection connection;
    private static CarLeaseRepositoryImpl repository;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        // Initialize the database connection and repository
        connection = DBConnectionUtil.getConnection();
        repository = new CarLeaseRepositoryImpl();
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Clear previous test data before each test case runs
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Payment");
            stmt.execute("DELETE FROM Lease");
            stmt.execute("DELETE FROM Vehicle");
            stmt.execute("DELETE FROM Customer");

            // Insert sample customers and vehicles
            stmt.execute("INSERT INTO Customer (customerID, firstName, lastName, email, phoneNumber) VALUES (1, 'John', 'Doe', 'john.doe@example.com', '1234567890')");
            stmt.execute("INSERT INTO Customer (customerID, firstName, lastName, email, phoneNumber) VALUES (2, 'Jane', 'Smith', 'jane.smith@example.com', '0987654321')");
            stmt.execute("INSERT INTO Vehicle (vehicleID, make, model, year, dailyRate, status, passengerCapacity, engineCapacity) " +
                         "VALUES (1, 'Toyota', 'Camry', 2020, 100.0, 'available', 5, 2.5)");
            stmt.execute("INSERT INTO Vehicle (vehicleID, make, model, year, dailyRate, status, passengerCapacity, engineCapacity) " +
                         "VALUES (2, 'Honda', 'Civic', 2021, 80.0, 'available', 5, 1.8)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up after each test
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Payment");
            stmt.execute("DELETE FROM Lease");
            stmt.execute("DELETE FROM Vehicle");
            stmt.execute("DELETE FROM Customer");
        }
    }

    // Test for recording a payment
    @Test
    @Order(1)
    void testRecordPayment() throws Exception {
        // Step 1: Create a lease (ensure it exists in the database)
        Lease lease = repository.createLease(1, 1, Date.valueOf("2024-11-01"), Date.valueOf("2024-11-10"));
        assertNotNull(lease, "Lease creation failed");

        // Step 2: Record a payment for the created lease
        repository.recordPayment(lease.getLeaseID(), 200.0);

        // Step 3: Verify the payment was recorded
        List<Payment> payments = repository.getPaymentHistory(1);
        assertEquals(1, payments.size());
        assertEquals(200.0, payments.get(0).getAmount());
    }

    // Test for calculating total revenue
    @Test
    @Order(2)
    void testCalculateTotalRevenue() throws Exception {
        // Step 1: Create a lease and record payments
        Lease lease = repository.createLease(1, 1, Date.valueOf("2024-11-01"), Date.valueOf("2024-11-10"));
        assertNotNull(lease, "Lease creation failed");
        repository.recordPayment(lease.getLeaseID(), 100.0);
        repository.recordPayment(lease.getLeaseID(), 200.0);

        // Step 2: Calculate total revenue
        double totalRevenue = repository.calculateTotalRevenue();
        assertEquals(300.0, totalRevenue, "Total revenue calculation failed");
    }

    // Test for getting payment history
    @Test
    @Order(3)
    void testGetPaymentHistory() throws Exception {
        // Step 1: Create a lease and record payments
        Lease lease = repository.createLease(1, 1, Date.valueOf("2024-11-01"), Date.valueOf("2024-11-10"));
        assertNotNull(lease, "Lease creation failed");
        repository.recordPayment(lease.getLeaseID(), 100.0);
        repository.recordPayment(lease.getLeaseID(), 200.0);

        // Step 2: Get payment history for customer 1
        List<Payment> payments = repository.getPaymentHistory(1);

        // Verify payment history
        assertNotNull(payments);
        assertEquals(2, payments.size()); // Ensure the number of payments is correct
        assertEquals(100.0, payments.get(0).getAmount()); // Check the payment amount
        assertEquals(200.0, payments.get(1).getAmount()); // Check the payment amount
    }

    // Test for creating and returning a lease
    @Test
    @Order(4)
    void testCreateAndReturnLease() throws Exception {
        // Step 1: Create a lease for customer 2 and vehicle 2
        Lease lease = repository.createLease(2, 2, Date.valueOf("2024-12-01"), Date.valueOf("2024-12-10"));
        assertNotNull(lease, "Lease creation failed");

        // Step 2: Verify the car's status is 'notAvailable' after leasing
        Vehicle vehicle = repository.findCarById(2);
        assertEquals("notAvailable", vehicle.getStatus(), "Car status should be 'notAvailable' after lease.");

        // Step 3: Return the car
        repository.returnCar(lease.getLeaseID());

        // Step 4: Verify the car's status is 'available' after return
        vehicle = repository.findCarById(2); // Fetch the latest data after return
        assertEquals("available", vehicle.getStatus(), "Car status should be 'available' after return.");
    }
}
