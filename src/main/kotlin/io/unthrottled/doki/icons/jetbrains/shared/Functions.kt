package io.unthrottled.doki.icons.jetbrains.shared

fun toLongArray(bytArray: ByteArray): LongArray {
  val digest = LongArray(bytArray.size)
  bytArray.forEachIndexed { index, byte -> digest[index] = byte.toLong() }
  return digest
}

fun toByteArray(longArray: LongArray): ByteArray {
  val digest = ByteArray(longArray.size)
  longArray.forEachIndexed { index, byte -> digest[index] = byte.toByte() }
  return digest
}
