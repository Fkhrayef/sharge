package com.fkhrayef.sharge.Controller;

import com.fkhrayef.sharge.Api.ApiResponse;
import com.fkhrayef.sharge.Model.Invoice;
import com.fkhrayef.sharge.Service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllInvoices() {
        return ResponseEntity.status(HttpStatus.OK).body(invoiceService.getAllInvoices());
    }

    @GetMapping("/get/booking/{bookingId}")
    public ResponseEntity<?> getInvoiceByBookingId(@PathVariable("bookingId") Integer bookingId) {
        return ResponseEntity.status(HttpStatus.OK).body(invoiceService.getInvoiceByBookingId(bookingId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addInvoice(@Valid @RequestBody Invoice invoice, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        invoiceService.addInvoice(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Invoice added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable("id") Integer id, @Valid @RequestBody Invoice invoice, Errors errors) {
        if (errors.hasErrors()) {
            ArrayList<String> errorMessages = new ArrayList<>();
            for (FieldError error : errors.getFieldErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }

        invoiceService.updateInvoice(id, invoice);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Invoice updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable("id") Integer id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/booking/{bookingId}/payment-method/{paymentMethod}")
    public ResponseEntity<?> changePaymentMethod(@PathVariable("bookingId") Integer bookingId,
                                              @PathVariable("paymentMethod") String paymentMethod) {
        invoiceService.changePaymentMethod(bookingId, paymentMethod);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Payment method added successfully"));
    }

    @PutMapping("/booking/{bookingId}/pay")
    public ResponseEntity<?> payInvoice(@PathVariable("bookingId") Integer bookingId) {
        invoiceService.payInvoice(bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Invoice paid successfully"));
    }

    @GetMapping("/unpaid")
    public ResponseEntity<?> getUnpaidInvoices() {
        List<Invoice> unpaidInvoices = invoiceService.getUnpaidInvoices();
        return ResponseEntity.status(HttpStatus.OK).body(unpaidInvoices);
    }
}
