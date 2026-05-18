package com.kdx.parkeer.core.ui

import java.text.NumberFormat
import java.util.Locale

private val idFormat = NumberFormat.getInstance(Locale("id", "ID"))

fun Int.toRupiah(): String = "Rp ${idFormat.format(this)}"
