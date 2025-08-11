package com.fkhrayef.sharge.Repository;

import com.fkhrayef.sharge.Model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Invoice findInvoiceById(Integer id);
    Invoice findInvoiceByBookingId(Integer bookingId);
    
    List<Invoice> findByIsPaidFalse();

    // Charger Financial Statistics
    @Query("SELECT SUM(i.totalCost) FROM Invoice i WHERE i.bookingId IN (SELECT b.id FROM Booking b WHERE b.chargerId = :chargerId)")
    Double sumRevenueByChargerId(Integer chargerId);

    @Query("SELECT SUM(i.kwhDelivered) FROM Invoice i WHERE i.bookingId IN (SELECT b.id FROM Booking b WHERE b.chargerId = :chargerId)")
    Double sumEnergyByChargerId(Integer chargerId);

    @Query("SELECT AVG(i.totalCost) FROM Invoice i WHERE i.bookingId IN (SELECT b.id FROM Booking b WHERE b.chargerId = :chargerId)")
    Double avgSessionValueByChargerId(Integer chargerId);

    // User Financial Statistics
    @Query("SELECT SUM(i.totalCost) FROM Invoice i WHERE i.bookingId IN (SELECT b.id FROM Booking b WHERE b.userId = :userId)")
    Double sumSpendingByUserId(Integer userId);

}
