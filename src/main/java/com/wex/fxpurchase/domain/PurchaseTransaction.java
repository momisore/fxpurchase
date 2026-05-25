package com.wex.fxpurchase.domain;

// domain model for a purchase transaction

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;



@Entity
@Table(name = "purchase_transactions")
public class PurchaseTransaction {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 50)
   private String description;

   @Column(name = "transaction_date", nullable = false)
   private LocalDate transactionDate;

   @Column(name = "usd_amount", nullable = false, precision = 19, scale = 2)
   private BigDecimal usdAmount;

   @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @Column(name = "updated_at", nullable = false)
   private LocalDateTime updatedAt;

   public PurchaseTransaction() {
   }

   @PrePersist
   void onCreate() {
       LocalDateTime now = LocalDateTime.now();
       this.createdAt = now;
       this.updatedAt = now;
   }

   @PreUpdate
   void onUpdate() {
       this.updatedAt = LocalDateTime.now();
   }


   public Long getId() {
       return id;
   }    

   public void setId(Long id) {
       this.id = id;
   }

   public String getDescription() {
       return description;
   }

   public void setDescription(String description) {
       this.description = description;
   }

   public LocalDate getTransactionDate() {
       return transactionDate;
   }

   public void setTransactionDate(LocalDate transactionDate) {
       this.transactionDate = transactionDate;
   }

   public BigDecimal getUsdAmount() {
       return usdAmount;
   }

   public void setUsdAmount(BigDecimal usdAmount) {
       this.usdAmount = usdAmount;
   }

   
   public void setCreatedAt(LocalDateTime createdAt) {
       this.createdAt = createdAt;
   }
   public LocalDateTime getCreatedAt() {
       return createdAt;
   }


    public void setUpdatedAt(LocalDateTime updatedAt) {
         this.updatedAt = updatedAt;
    }
   public LocalDateTime getUpdatedAt() {
       return updatedAt;
   }


}
