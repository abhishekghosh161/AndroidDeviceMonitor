package com.abhi.asm

import java.io.IOException
import java.io.InputStream
import java.util.Properties

class Config {
    var adbCommand: String? = null
    var screenshotDelay: Long = 0
    var localImageFilePath: String? = null
    var phoneImageFilePath: String? = null

    @Throws(IOException::class)
    fun load(`in`: InputStream) {
        val properties = Properties()
        properties.load(`in`)

        adbCommand = properties.getProperty("adbCommand")
        screenshotDelay = java.lang.Long.parseLong(properties.getProperty("screenshotDelay"))
        localImageFilePath = properties.getProperty("localImageFilePath")
        phoneImageFilePath = properties.getProperty("phoneImageFilePath")
    }
}
