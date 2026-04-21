package com.example.backend.facade;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.AdminDashboardResponse;
import com.example.backend.dto.AdminGameResponse;
import com.example.backend.dto.AdminPaymentResponse;
import com.example.backend.dto.AdminRefundRequestResponse;
import com.example.backend.dto.AdminUserResponse;
import com.example.backend.dto.AdminVenueResponse;
import com.example.backend.model.Booking;
import com.example.backend.service.AdminGameManagementService;
import com.example.backend.service.AdminPaymentManagementService;
import com.example.backend.service.AdminUserManagementService;
import com.example.backend.service.AdminVenueManagementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFacade {

    private final AdminUserManagementService adminUserManagementService;
    private final AdminVenueManagementService adminVenueManagementService;
    private final AdminGameManagementService adminGameManagementService;
    private final AdminPaymentManagementService adminPaymentManagementService;

    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
            adminUserManagementService.getTotalUsers(),
            adminVenueManagementService.getTotalVenues(),
            adminGameManagementService.getTotalGames(),
            adminPaymentManagementService.getTotalBookings());
    }

    public List<AdminUserResponse> getAllUsers() {
        return adminUserManagementService.getAllUsers();
    }

    public List<AdminVenueResponse> getAllVenues() {
        return adminVenueManagementService.getAllVenues();
    }

    public List<AdminGameResponse> getRecentGames(int limit) {
        return adminGameManagementService.getRecentGames(limit);
    }

    @Transactional
    public void suspendUser(UUID userId) {
        adminUserManagementService.suspendUser(userId);
    }

    @Transactional
    public void disableVenue(UUID venueId) {
        adminVenueManagementService.disableVenue(venueId);
    }

    @Transactional
    public void cancelGame(UUID gameId) {
        adminGameManagementService.cancelGame(gameId);
    }

    public List<AdminPaymentResponse> getAllPayments() {
        return adminPaymentManagementService.getAllPayments();
    }

    public List<AdminRefundRequestResponse> getRefundRequests() {
        return adminPaymentManagementService.getRefundRequests();
    }

    @Transactional
    public Booking processRefund(UUID bookingId) {
        return adminPaymentManagementService.processRefund(bookingId);
    }
}
