package com.erp.repository;

import com.erp.model.finance.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByType(Invoice.InvoiceType type);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
}
