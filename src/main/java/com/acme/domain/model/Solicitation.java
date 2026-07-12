package com.acme.domain.model;

import com.acme.domain.enums.SolicitationStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "solicitations")
public class Solicitation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reimbursement_id", nullable = false)
    private Reimbursement reimbursement;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SolicitationStatus status;

    @Column(name = "protocol_number", unique = true, length = 100)
    private String protocolNumber;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "reimbursement_date")
    private LocalDate reimbursementDate;

    @Column(name = "amount_received", precision = 10, scale = 2)
    private BigDecimal amountReceived;

    @Column(length = 500)
    private String note;

    public Reimbursement getReimbursement() {
        return reimbursement;
    }

    public void setReimbursement(Reimbursement reimbursement) {
        this.reimbursement = reimbursement;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public SolicitationStatus getStatus() {
        return status;
    }

    public void setStatus(SolicitationStatus status) {
        this.status = status;
    }

    public String getProtocolNumber() {
        return protocolNumber;
    }

    public void setProtocolNumber(String protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getReimbursementDate() {
        return reimbursementDate;
    }

    public void setReimbursementDate(LocalDate reimbursementDate) {
        this.reimbursementDate = reimbursementDate;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
