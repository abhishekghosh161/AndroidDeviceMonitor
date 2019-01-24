package com.abhi.asm

import java.awt.BorderLayout
import java.io.File
import java.io.FileInputStream
import java.io.IOException

import javax.swing.JFrame

class AndroidMonitorLauncher @Throws(IOException::class) constructor(configFile: File) : JFrame() {
	init {
		val config = Config()

		FileInputStream(configFile).use { `in` -> config.load(`in`) }

		defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		title = "Abhishek Android Monitor"
		setSize(400, 850)
		isResizable = true

		val panel = AndroidMonitorControlPanel(config)
		panel.adbHelper = AndroidDebugBridgeHelper(config)
		contentPane.add(panel, BorderLayout.CENTER)

	}
}

fun main(args: Array<String>) {
	val configFile: File

	if (args.size == 0) {
		configFile = File("config.properties")
	} else {
		configFile = File(args[0])
	}

	try {
		val frame = AndroidMonitorLauncher(configFile)
		frame.isVisible = true
	} catch (ex: Exception) {
		ex.printStackTrace()
	}

}
