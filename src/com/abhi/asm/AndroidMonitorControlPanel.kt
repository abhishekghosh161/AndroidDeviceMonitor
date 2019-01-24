package com.abhi.asm

import java.awt.Graphics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException

import javax.imageio.ImageIO
import javax.swing.JPanel

class AndroidMonitorControlPanel(private val config: Config) : JPanel(), MouseListener, KeyListener {
    var adbHelper: AndroidDebugBridgeHelper? = null
    private val imageFile: File
    private var image: BufferedImage? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var ratio: Double = 0.toDouble()
    protected var updateThread: Thread? = null
    private var downX: Int = 0
    private var downY: Int = 0


    init {

        imageFile = File(config.localImageFilePath)

        addComponentListener(object : ComponentAdapter() {
            override fun componentHidden(e: ComponentEvent?) {
                stopUpdateThread()
            }

            override fun componentResized(e: ComponentEvent?) {
                requestFocus()
                requestFocusInWindow()

                startUpdateThread()
            }
        })

        addMouseListener(this)
        addKeyListener(this)
    }

    protected fun stopUpdateThread() {
        if (updateThread != null) {
            updateThread!!.interrupt()
            updateThread = null
        }
    }

    protected fun startUpdateThread() {
        if (updateThread == null) {
            updateThread = object : Thread() {
                override fun run() {
                    while (!Thread.interrupted()) {
                        makeScreenshot()

                        try {
                            Thread.sleep(config.screenshotDelay)
                        } catch (ex: InterruptedException) {
                            break
                        }

                    }
                }
            }

            updateThread!!.start()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (image != null) {
            screenWidth = image!!.width
            screenHeight = image!!.height

            val width = width
            val height = height

            val ratioX = width.toDouble() / screenWidth.toDouble()
            val ratioY = height.toDouble() / screenHeight.toDouble()

            ratio = Math.min(1.0, Math.min(ratioX, ratioY))

            val scaledWidth = screenWidth.toDouble() * ratio
            val scaledHeight = screenHeight.toDouble() * ratio

            g.drawImage(image, 0, 0, scaledWidth.toInt(), scaledHeight.toInt(), null)
        }
    }

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}

    override fun mouseClicked(e: MouseEvent) {
        if (downX < 0 || downY < 0) {
            return
        }

        if (screenWidth <= 0) {
            return
        }

        val x = (e.x.toDouble() / ratio).toInt()
        val y = (e.y.toDouble() / ratio).toInt()

        if (adbHelper != null) {
            adbHelper!!.sendClick(x, y)
        }
    }

    override fun mousePressed(e: MouseEvent) {
        downX = e.x
        downY = e.y
    }

    override fun mouseReleased(e: MouseEvent) {
        val upX = e.x
        val upY = e.y

        val dx = Math.abs(downX - upX)
        val dy = Math.abs(downY - upY)

        if (dx < 5 && dy < 5) {
            return
        }

        val screenDownX = (downX.toDouble() / ratio).toInt()
        val screenDownY = (downY.toDouble() / ratio).toInt()
        val screenUpX = (upX.toDouble() / ratio).toInt()
        val screenUpY = (upY.toDouble() / ratio).toInt()

        adbHelper!!.sendSwipe(screenDownX, screenDownY, screenUpX, screenUpY)

        downX = -1
        downY = -1
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_ENTER -> {
                adbHelper!!.sendKey(AndroidDeviceKey.ENTER)
                return
            }

            KeyEvent.VK_ESCAPE -> {
                adbHelper!!.sendKey(AndroidDeviceKey.BACK)
                return
            }

            KeyEvent.VK_HOME -> {
                adbHelper!!.sendKey(AndroidDeviceKey.HOME)
                return
            }

            KeyEvent.VK_BACK_SPACE -> {
                adbHelper!!.sendKey(AndroidDeviceKey.DEL)
                return
            }

            KeyEvent.VK_UP -> {
                adbHelper!!.sendKey(AndroidDeviceKey.DPAD_UP)
                return
            }

            KeyEvent.VK_DOWN -> {
                adbHelper!!.sendKey(AndroidDeviceKey.DPAD_DOWN)
                return
            }

            KeyEvent.VK_LEFT -> {
                adbHelper!!.sendKey(AndroidDeviceKey.DPAD_LEFT)
                return
            }

            KeyEvent.VK_RIGHT -> {
                adbHelper!!.sendKey(AndroidDeviceKey.DPAD_RIGHT)
                return
            }
        }
    }

    private fun makeScreenshot() {
        adbHelper!!.screenshot(imageFile)
        loadImage(imageFile)
    }

    private fun loadImage(file: File) {
        try {
            image = ImageIO.read(file)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return
        }

        repaint()
    }

    override fun keyReleased(e: KeyEvent) {}

    override fun keyTyped(e: KeyEvent) {
        val c = e.keyChar

        if (c.toInt() > 32 && c.toInt() < 128) {
            adbHelper!!.sendText(String(charArrayOf(c)))
        }
    }
}
