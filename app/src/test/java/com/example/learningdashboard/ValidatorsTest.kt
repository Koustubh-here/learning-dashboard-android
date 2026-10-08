package com.example.learningdashboard

import com.example.learningdashboard.domain.Validators
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {
    @Test fun email() {
        assertNotNull(Validators.email(""))
        assertNotNull(Validators.email("abc"))
        assertNull(Validators.email("a@b.com"))
    }
    @Test fun password() {
        assertNotNull(Validators.password("123"))
        assertNull(Validators.password("123456"))
    }
}
