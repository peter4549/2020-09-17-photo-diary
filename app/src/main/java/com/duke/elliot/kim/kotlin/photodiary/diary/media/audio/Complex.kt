package com.duke.elliot.kim.kotlin.photodiary.diary.media.audio

import kotlin.math.atan2
import kotlin.math.hypot

internal class Complex(
    private val real: Double,
    private var imaginary: Double
) {
    // return a string representation of the invoking Complex object
    override fun toString(): String {
        if (imaginary == 0.0)
            return real.toString() + ""
        if (real == 0.0)
            return imaginary.toString() + "i"

        return if (imaginary < 0) real.toString() + " - " + -imaginary + "i"
        else real.toString() + " + " + imaginary + "i"
    }

    // return abs/modulus/magnitude and angle/phase/argument
    fun abs(): Double {
        return hypot(real, imaginary)
    } // Math.sqrt(re*re + im*im)

    fun phase(): Double {
        return atan2(imaginary, real)
    } // between -pi and pi

    // return a new Complex object whose value is (this + b)
    operator fun plus(b: Complex): Complex {
        val real = this.real + b.real
        val imaginary = this.imaginary + b.imaginary
        return Complex(real, imaginary)
    }

    // return a new Complex object whose value is (this - b)
    operator fun minus(b: Complex): Complex {
        val a = this
        val real = a.real - b.real
        val imag = a.imaginary - b.imaginary
        return Complex(real, imag)
    }

    // return a new Complex object whose value is (this * b)
    operator fun times(b: Complex): Complex {
        val a = this
        val real = a.real * b.real - a.imaginary * b.imaginary
        val imag = a.real * b.imaginary + a.imaginary * b.real
        return Complex(real, imag)
    }

    // scalar multiplication
    // return a new object whose value is (this * alpha)
    operator fun times(alpha: Double): Complex {
        return Complex(alpha * real, alpha * imaginary)
    }

    // return a new Complex object whose value is the conjugate of this
    fun conjugate(): Complex {
        return Complex(real, -imaginary)
    }

    // return a new Complex object whose value is the reciprocal of this
    fun reciprocal(): Complex {
        val scale = real * real + imaginary * imaginary
        return Complex(real / scale, -imaginary / scale)
    }

    // return the real or imaginary part
    fun re(): Double {
        return real
    }

    fun im(): Double {
        return imaginary
    }

    // return a / b
    fun divides(b: Complex): Complex {
        val a = this
        return a.times(b.reciprocal())
    }

    // return a new Complex object whose value is the complex exponential of this
    fun exp(): Complex {
        return Complex(Math.exp(real) * Math.cos(imaginary), Math.exp(real) * Math.sin(imaginary))
    }

    // return a new Complex object whose value is the complex sine of this
    fun sin(): Complex {
        return Complex(Math.sin(real) * Math.cosh(imaginary), Math.cos(real) * Math.sinh(imaginary))
    }

    // return a new Complex object whose value is the complex cosine of this
    fun cos(): Complex {
        return Complex(Math.cos(real) * Math.cosh(imaginary), -Math.sin(real) * Math.sinh(imaginary))
    }

    // return a new Complex object whose value is the complex tangent of this
    fun tan(): Complex {
        return sin().divides(cos())
    }

    companion object {
        // a static version of plus
        fun plus(a: Complex, b: Complex): Complex {
            val real = a.real + b.real
            val imag = a.imaginary + b.imaginary
            return Complex(real, imag)
        }
    }
}