package com.archeGlobal.one.controller

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelControllerTest {
    @Test
    fun treatsAdminLikeRolesAsApprovers() {
        assertTrue(TravelController.canApproveExpensesRole("Admin"))
        assertTrue(TravelController.canApproveExpensesRole("ADMIN"))
        assertTrue(TravelController.canApproveExpensesRole("admin"))
        assertTrue(TravelController.canApproveExpensesRole("Finance Admin"))
    }

    @Test
    fun treatsManagerLikeRolesAsApprovers() {
        assertTrue(TravelController.canApproveExpensesRole("manager"))
        assertTrue(TravelController.canApproveExpensesRole("Manager"))
        assertTrue(TravelController.canApproveExpensesRole("MANAGER"))
        assertTrue(TravelController.canApproveExpensesRole("Reporting Manager"))
    }

    @Test
    fun treatsOtherRolesAsNonApprovers() {
        assertFalse(TravelController.canApproveExpensesRole(null))
        assertFalse(TravelController.canApproveExpensesRole(""))
        assertFalse(TravelController.canApproveExpensesRole("user"))
        assertFalse(TravelController.canApproveExpensesRole("employee"))
    }
}
