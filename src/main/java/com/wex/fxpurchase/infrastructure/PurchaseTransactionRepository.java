package com.wex.fxpurchase.infrastructure;

import com.wex.fxpurchase.domain.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
}