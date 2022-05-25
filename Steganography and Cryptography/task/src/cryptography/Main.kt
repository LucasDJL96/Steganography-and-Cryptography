package cryptography

import java.awt.Color
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val input = readln()) {
            "hide" -> try { hideMessage() } catch (e: IOException) { println(e.message) }
            "show" -> try { showMessage() } catch (e: IOException) { println(e.message) }
            "exit" -> {
                println("Bye!")
                return
            }
            else -> println("Wrong task: $input")
        }
    }
}

fun hideMessage() {
    println("Input image file:")
    val inputName = readln()
    val inputFile = File(inputName)
    val image = ImageIO.read(inputFile)
    println("Output image file:")
    val outputName = readln()
    val outputFile = File(outputName)
    println("Message to hide:")
    val message = readln().encodeToByteArray()
    println("Password:")
    val password = readln().encodeToByteArray()
    val byteMessage = (message.crypto(password) + byteArrayOf(0, 0, 3)).map { it.toInt() }
    val bits = byteMessage.size * 8
    if (bits > image.width * image.height) {
        println("The input image is not large enough to hold this message.")
        return
    }
    for (i in 0 until bits) {
        val col = i % image.width
        val row = i / image.width
        val color = Color(image.getRGB(col, row))
        // i / 8 is the byte position in the array, i % 8 is the bit position in the byte from left to right
        // shr 7 - i % 8 because we read the bits from most significant to least
        // and 1 to get the bit
        val bit = byteMessage[i / 8] shr (7 - i % 8) and 1
        // shr 1 shl 1 or x changes the least significant bit to x when x = 0, 1
        val newColor = Color(color.red, color.green, color.blue shr 1 shl 1 or bit)
        image.setRGB(col, row, newColor.rgb)
    }
    ImageIO.write(image, "png", outputFile)
    println("Message saved in $outputName.")
}

fun showMessage() {
    println("Input image file:")
    val inputName = readln()
    val inputFile = File(inputName)
    val image = ImageIO.read(inputFile)
    println("Password:")
    val password = readln().encodeToByteArray()
    val byteMessage = mutableListOf<Byte>()
    val end = mutableListOf<Byte>(0, 0, 3)
    var i = 0
    while (byteMessage.size < 3 || byteMessage.last() != 3.toByte()
        || byteMessage.subList(byteMessage.lastIndex - 2, byteMessage.size) != end) {
        val col = i % image.width
        val row = i / image.width
        // and 1 to get the least significant bit
        val bit = Color(image.getRGB(col, row)).blue and 1
        // i / 8 is the byte position in the array, i % 8 is the bit position in the byte from left to right
        if (i / 8 > byteMessage.lastIndex) byteMessage.add(0)
        // shift left 7 - i % 8 because we write the bits from most significant to least
        byteMessage[i / 8] = (byteMessage[i / 8] + (bit shl (7 - i % 8))).toByte()
        i++
    }
    repeat(3) {
        byteMessage.removeLast()
    }
    val message = byteMessage.toByteArray().crypto(password).toString(Charsets.UTF_8)
    println("Message:")
    println(message)
}

fun ByteArray.crypto(password: ByteArray): ByteArray {
    val result = mutableListOf<Byte>()
    for (i in 0..this.lastIndex) {
        result.add((this[i].toInt() xor password[i % password.size].toInt()).toByte())
    }
    return result.toByteArray()
}
