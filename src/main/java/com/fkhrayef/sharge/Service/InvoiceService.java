package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.Invoice;
import com.fkhrayef.sharge.Repository.BookingRepository;
import com.fkhrayef.sharge.Repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceByBookingId(Integer bookingId) {
        if (bookingRepository.findBookingById(bookingId) == null) {
            throw new ApiException("Booking not found");
        }
        return invoiceRepository.findInvoiceByBookingId(bookingId);
    }

    public void addInvoice(Invoice invoice) {
        if (bookingRepository.findBookingById(invoice.getBookingId()) == null) {
            throw new ApiException("Booking not found");
        }

        invoice.setPaymentMethod("CASH"); // Default
        invoice.setIsPaid(false);
        invoiceRepository.save(invoice);
    }

    public void updateInvoice(Integer id, Invoice invoice) {
        Invoice oldInvoice = invoiceRepository.findInvoiceById(id);
        if (oldInvoice == null) {
            throw new ApiException("Invoice not found");
        }
        if (bookingRepository.findBookingById(invoice.getBookingId()) == null) {
            throw new ApiException("Booking not found");
        }
        if (oldInvoice.getIsPaid() && !invoice.getIsPaid()) {
            throw new ApiException("You cannot change the payment status of an invoice from this endpoint");
        }
        if (!oldInvoice.getPaymentMethod().equals(invoice.getPaymentMethod())) {
            throw new ApiException("You cannot change the payment method of an invoice from this endpoint");
        }

        oldInvoice.setBookingId(invoice.getBookingId());
        invoiceRepository.save(oldInvoice);
    }

    public void deleteInvoice(Integer id) {
        Invoice oldInvoice = invoiceRepository.findInvoiceById(id);
        if (oldInvoice == null) {
            throw new ApiException("Invoice not found");
        }

        if (oldInvoice.getIsPaid()) {
            throw new ApiException("You cannot delete an invoice that has already been paid");
        }

        invoiceRepository.delete(oldInvoice);
    }

    public void changePaymentMethod(Integer bookingId, String paymentMethod) {
        Invoice invoice = invoiceRepository.findInvoiceByBookingId(bookingId);
        if (invoice == null) {
            throw new ApiException("Invoice not found");
        }

        if (!(paymentMethod.equals("CASH") || paymentMethod.equals("CARD"))) {
            throw new ApiException("Invalid payment method");
        }
        if (invoice.getIsPaid()) {
            throw new ApiException("You cannot add a payment method to an invoice that has already been paid");
        }

        invoice.setPaymentMethod(paymentMethod);
        invoiceRepository.save(invoice);
    }

    public void payInvoice(Integer bookingId) {
        Invoice invoice = invoiceRepository.findInvoiceByBookingId(bookingId);
        if (invoice == null) {
            throw new ApiException("Invoice not found");
        }
        if (invoice.getIsPaid()) {
            throw new ApiException("Invoice is already paid");
        }

        invoice.setIsPaid(true);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    public List<Invoice> getUnpaidInvoices() {
        return invoiceRepository.findByIsPaidFalse();
    }
}
