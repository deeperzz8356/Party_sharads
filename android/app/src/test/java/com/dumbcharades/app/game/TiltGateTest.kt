package com.dumbcharades.app.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TiltGateTest {
    @Test fun gesture_requires_neutral_reset_and_debounce() {
        val gate = TiltGate(threshold = 45f, debounceMs = 500)
        gate.calibrate(0f)
        assertEquals(TiltGesture.CORRECT, gate.accept(-50f, 1_000))
        assertNull(gate.accept(-55f, 2_000))
        assertNull(gate.accept(0f, 2_100))
        assertEquals(TiltGesture.SKIP, gate.accept(50f, 2_200))
    }

    @Test fun small_movements_are_ignored() {
        val gate = TiltGate(threshold = 45f)
        gate.calibrate(10f)
        assertNull(gate.accept(35f, 1_000))
    }
}

