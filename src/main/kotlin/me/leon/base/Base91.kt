package me.leon.base

import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.roundToInt

object Base91 {

    private var DECODING_TABLE: ByteArray = ByteArray(256).apply { fill(-1) }
    private val ENCODING_TABLE =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\"".toByteArray()
    private val BASE = ENCODING_TABLE.size
    private const val AVERAGE_ENCODING_RATIO = 1.2297f
    fun encode(data: ByteArray): ByteArray {
        val estimatedSize = ceil((data.size * AVERAGE_ENCODING_RATIO).toDouble()).toInt()
        val output = ByteArrayOutputStream(estimatedSize)
        var ebq = 0
        var en = 0
        for (i in data.indices) {
            ebq = ebq or (data[i].toInt() and 255 shl en)
            en += 8
            // 仅在位数大于13时进行处理, 2^13 8192 91*91=8281, 需要13~14位才能编码
            if (en > 13) {
                // 取13位
                var ev = ebq and 8191
                if (ev > 88) {
                    // 右移 13位
                    ebq = ebq shr 13
                    en -= 13
                } else {
                    // 取14位
                    ev = ebq and 16383
                    // 右移 14位
                    ebq = ebq shr 14
                    en -= 14
                }
                output.write(ENCODING_TABLE[ev % BASE].toInt())
                output.write(ENCODING_TABLE[ev / BASE].toInt())
            }
        }
        if (en > 0) {
            output.write(ENCODING_TABLE[ebq % BASE].toInt())
            if (en > 7 || ebq > 90) {
                output.write(ENCODING_TABLE[ebq / BASE].toInt())
            }
        }
        return output.toByteArray()
    }

    fun decode(data: ByteArray): ByteArray {
        var dbq = 0
        var dn = 0
        var dv = -1
        val estimatedSize = (data.size / AVERAGE_ENCODING_RATIO).roundToInt()
        val output = ByteArrayOutputStream(estimatedSize)
        for (i in data.indices) {
            if (DECODING_TABLE[data[i].toInt()].toInt() == -1) continue
            if (dv == -1) dv = DECODING_TABLE[data[i].toInt()].toInt()
            else {
                dv += DECODING_TABLE[data[i].toInt()] * BASE
                dbq = dbq or (dv shl dn)
                dn += if (dv and 8191 > 88) 13 else 14
                do {
                    output.write(dbq)
                    dbq = dbq shr 8
                    dn -= 8
                } while (dn > 7)
                dv = -1
            }
        }
        if (dv != -1) {
            output.write(((dbq or dv shl dn).toByte()).toInt())
        }
        return output.toByteArray()
    }

    init {
        for (i in 0 until BASE) DECODING_TABLE[ENCODING_TABLE[i].toInt()] = i.toByte()
    }
}

fun String.base91() = toByteArray().base91()

fun ByteArray.base91() = String(Base91.encode(this))

fun String.base91Decode() = Base91.decode(toByteArray())

fun String.base91Decode2String() = String(base91Decode())
