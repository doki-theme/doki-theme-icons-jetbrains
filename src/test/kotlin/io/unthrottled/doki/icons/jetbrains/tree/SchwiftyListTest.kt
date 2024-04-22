package io.unthrottled.doki.icons.jetbrains.tree

import org.assertj.core.api.Assertions
import org.junit.Test

internal class SchwiftyListTest {
  @Test
  fun shouldPreferItemsFound() {
    val schwiftyList = SchwiftyList<String>()

    Assertions.assertThat(
      schwiftyList.first {
        it === "Zero Two"
      }.orElseGet {
        "Best Girl"
      },
    ).isEqualTo("Best Girl")

    schwiftyList.enqueue("Ryuko")

    var predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseGet {
        "Best Girl"
      },
    ).isEqualTo("Best Girl")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Ryuko"
      }.orElseThrow(),
    ).isEqualTo("Ryuko")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    schwiftyList.enqueue("Kokkoro")
    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Ryuko"
      }.orElseThrow(),
    ).isEqualTo("Ryuko")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Kokkoro"
      }.orElseThrow(),
    ).isEqualTo("Kokkoro")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Kokkoro"
      }.orElseThrow(),
    ).isEqualTo("Kokkoro")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Ryuko"
      }.orElseThrow(),
    ).isEqualTo("Ryuko")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Ryuko"
      }.orElseThrow(),
    ).isEqualTo("Ryuko")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseGet { "Best Girl" },
    ).isEqualTo("Best Girl")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    schwiftyList.enqueue("Zero Two")

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(3)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Kokkoro"
      }.orElseThrow(),
    ).isEqualTo("Kokkoro")
    Assertions.assertThat(predicateCount).isEqualTo(3)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Kokkoro"
      }.orElseThrow(),
    ).isEqualTo("Kokkoro")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Kokkoro"
      }.orElseThrow(),
    ).isEqualTo("Kokkoro")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Kokkoro"
      }.orElseThrow(),
    ).isEqualTo("Kokkoro")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Ryuko"
      }.orElseThrow(),
    ).isEqualTo("Ryuko")
    Assertions.assertThat(predicateCount).isEqualTo(3)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(3)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(2)
    predicateCount = 0

    Assertions.assertThat(
      schwiftyList.first {
        predicateCount++
        it === "Zero Two"
      }.orElseThrow(),
    ).isEqualTo("Zero Two")
    Assertions.assertThat(predicateCount).isEqualTo(1)
    predicateCount = 0
  }
}
