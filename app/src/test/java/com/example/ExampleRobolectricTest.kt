package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SpendWise", appName)
  }

  @Test
  fun `verify SecurityManager pin and onboarding persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val securityManager = com.example.data.security.SecurityManager(context)

    // Initially no PIN set and onboarding not complete
    assertEquals(false, securityManager.isPinSet())
    assertEquals(false, securityManager.isOnboardingCompleted())

    // Save user PIN
    val saved = securityManager.saveUserPin("1234")
    assertEquals(true, saved)
    assertEquals(true, securityManager.isPinSet())
    assertEquals(true, securityManager.verifyUserPin("1234"))
    assertEquals(false, securityManager.verifyUserPin("9999"))

    // Onboarding status & baseline figures
    securityManager.setOnboardingCompleted(true)
    assertEquals(true, securityManager.isOnboardingCompleted())

    securityManager.setBaselineIncome(6500.0)
    securityManager.setBaselineFixedExpenses(2800.0)
    assertEquals(6500.0, securityManager.getBaselineIncome(), 0.01)
    assertEquals(2800.0, securityManager.getBaselineFixedExpenses(), 0.01)
  }

  @Test
  fun `verify default fixed and variable categories exist`() {
    val fixed = com.example.data.model.DefaultCategories.defaults.filter { it.type == "FIXED_EXPENSE" }
    val names = fixed.map { it.name }
    assertTrue(names.contains("Rent or Mortgage"))
    assertTrue(names.contains("Home Insurance"))
    assertTrue(names.contains("City Property Tax"))
    assertTrue(names.contains("Gas or Electricity Bill"))
    assertTrue(names.contains("Car Loan"))
    assertTrue(names.contains("Car Insurance"))
    assertTrue(names.contains("Internet Bill"))
    assertTrue(names.contains("Mobile Bill"))
  }
}
