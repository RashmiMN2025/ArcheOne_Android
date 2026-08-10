package com.archeGlobal.one.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelRequestValidationTest {

    @Test
    fun `own vehicle mode is normalized to own_vehicle`() {
        assertEquals("own_vehicle", normalizeModeOfTravel("Own vehicle"))
        assertEquals("own_vehicle", normalizeModeOfTravel("own vehicle"))
    }

    @Test
    fun `advance should be valid when it is not greater than estimated cost`() {
        assertTrue(isAdvanceAmountValid(100.0, 200.0))
        assertTrue(isAdvanceAmountValid(200.0, 200.0))
    }

    @Test
    fun `advance should be invalid when it is greater than estimated cost`() {
        assertFalse(isAdvanceAmountValid(201.0, 200.0))
    }

    @Test
    fun `date validation error is shown when end date is before start date`() {
        assertEquals("End date cannot be before start date", getDateValidationError(200L, 100L))
    }

    @Test
    fun `date validation error is empty when dates are in order`() {
        assertEquals("", getDateValidationError(100L, 200L))
    }

    @Test
    fun `travel detail action is hidden for approved status`() {
        assertFalse(shouldShowTravelDetailAction("Approved"))
        assertTrue(shouldShowTravelDetailAction("Pending"))
    }

    @Test
    fun `travel detail action is hidden for rejected status`() {
        assertFalse(shouldShowTravelDetailAction("Rejected"))
    }
}
