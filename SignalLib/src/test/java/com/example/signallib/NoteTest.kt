package com.example.signallib

import com.example.signallib.Note.Companion.minus
import com.example.signallib.Note.Companion.plus
import org.junit.Assert
import org.junit.Test

class NoteTest {
    @Test
    fun `plus()`() {
        fun test(note: Note, addBy: Int, expected: Note){
            Assert.assertEquals("Testing Note.plus", expected, note + addBy)
        }

        test(Note.A_4, 1, Note.As4)
        test(Note.A_4, 2, Note.B_4)
        test(Note.B_8, 2, Note.B_8)
    }

    @Test
    fun `minus()`() {
        fun test_subtract(note: Note, subBy: Int, expected: Note){
            Assert.assertEquals("Testing Note.minus", expected, note - subBy)
        }

        test_subtract(Note.A_4, 1, Note.Gs4)
        test_subtract(Note.A_4, 12, Note.A_3)
        test_subtract(Note.C_0, 2, Note.C_0)
    }
}