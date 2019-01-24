package com.abhi.asm

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class StreamTunnel(private val `in`: InputStream, private val out: OutputStream?) : Thread() {

    override fun run() {
        val buffer = ByteArray(8192)

        try {
            while (true) {
                if (Thread.interrupted()) {
                    break
                }

                val count = `in`.read(buffer)

                if (count < 0) {
                    break
                }

                if (count > 0 && out != null) {
                    out.write(buffer, 0, count)
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }
}
