package com.archeGlobal.one.controller

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelControllerTest {
    @Test
    fun treatsAdminLikeRolesAsAdmin() {
        assertTrue(TravelController.isAdminRole("Admin"))
        assertTrue(TravelController.isAdminRole("ADMIN"))
        assertTrue(TravelController.isAdminRole("admin"))
        assertTrue(TravelController.isAdminRole("Finance Admin"))
    }

    @Test
    fun treatsNonAdminRolesAsNonAdmin() {
        assertFalse(TravelController.isAdminRole(null))
        assertFalse(TravelController.isAdminRole(""))
        assertFalse(TravelController.isAdminRole("user"))
        assertFalse(TravelController.isAdminRole("manager"))
    }
}
