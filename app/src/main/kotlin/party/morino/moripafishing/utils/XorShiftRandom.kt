package party.morino.moripafishing.utils
import java.security.MessageDigest

class XorShiftRandom(seed: Long = 88675123) {
    private var x: Int = 123456789
    private var y: Int = 362436069
    private var z: Int = 521288629
    private var w: Int = seed.toInt()

    fun next(): Int {
        val t = x xor (x shl 11)
        x = y
        y = z
        z = w
        w = w xor (w ushr 19) xor (t xor (t ushr 8))
        return w
    }

    fun nextInt(min: Int = 0, max: Int = 100): Int {
        val r = Math.abs(next())
        return min + (r % (max + 1 - min))
    }
    
}