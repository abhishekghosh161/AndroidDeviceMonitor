package com.abhi.asm

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.MessageFormat

class AndroidDebugBridgeHelper(private val config: Config) {

	private fun executeShellCommand(cmd: String, out: OutputStream? = null) {
		executeCommand("shell $cmd", out)
	}

	private fun executeCommand(cmd: String, out: OutputStream?) {
		val cmdLine = config.adbCommand + " " + cmd

		val p: Process

		try {
			p = Runtime.getRuntime().exec(cmdLine)
		} catch (ex: IOException) {
			ex.printStackTrace()
			return
		}

		val outReader = StreamTunnel(p.inputStream, out)
		val errReader = StreamTunnel(p.errorStream, null)

		outReader.start()
		errReader.start()

		try {
			if (out != null) {
				p.waitFor()
				outReader.join()
			}
		} catch (ex: InterruptedException) {
			Thread.currentThread().interrupt()
			return
		}

	}

	fun sendClick(x: Int, y: Int) {
		println("Click at: $x/$y")
		executeShellCommand(MessageFormat.format("input tap {0,number,#####} {1,number,#####}", x, y))
	}

	fun sendText(text: String) {
		executeShellCommand(MessageFormat.format("input text {0}", text))
	}

	fun sendKey(key: AndroidDeviceKey) {
		executeShellCommand(MessageFormat.format("input keyevent {0}", key.code))
	}

	fun screenshot(target: File) {
		val fileName = config.phoneImageFilePath

		executeShellCommand(MessageFormat.format("screencap -p {0}", fileName), ByteArrayOutputStream())
		executeCommand(MessageFormat.format("pull {0} {1}", fileName, target.absolutePath), ByteArrayOutputStream())
	}

	fun sendSwipe(downX: Int, downY: Int, upX: Int, upY: Int) {
		println("Swipe from $downX/$downY to $upX/$upY")
		executeShellCommand(MessageFormat.format("input swipe {0,number,#####} {1,number,#####} {2,number,#####} {3,number,#####}", downX, downY, upX, upY))
	}
}
