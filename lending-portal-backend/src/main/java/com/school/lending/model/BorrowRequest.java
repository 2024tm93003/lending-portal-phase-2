package com.school.lending.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserAccount requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Equipment gear;

    private LocalDate startDate;

    private LocalDate endDate;

    private int qty;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status = BorrowStatus.PENDING;

    private OffsetDateTime decisionDate;

    private String decisionNote;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    public BorrowRequest() {
    }

    public BorrowRequest(UserAccount requester, Equipment gear, LocalDate startDate, LocalDate endDate, int qty) {
        this.requester = requester;
        this.gear = gear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.qty = qty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserAccount getRequester() {
        return requester;
    }

    public void setRequester(UserAccount requester) {
        this.requester = requester;
    }

    public Equipment getGear() {
        return gear;
    }

    public void setGear(Equipment gear) {
        this.gear = gear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    public OffsetDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(OffsetDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
