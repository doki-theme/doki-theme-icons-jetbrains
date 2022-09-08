package io.unthrottled.doki.icons.jetbrains.shared.tree

import io.unthrottled.doki.icons.jetbrains.shared.tools.toOptional
import java.util.Optional

internal class SchwiftyNode<T>(
  val value: T,
  var parent: SchwiftyNode<T>? = null,
  var child: SchwiftyNode<T>? = null,
  private val onNewHead: (SchwiftyNode<T>) -> Unit,
  private val onNewTail: (SchwiftyNode<T>) -> Unit,
) {

  fun bumpPriority() {
    val myParent = parent
    if (myParent != null) {
      val me = this
      val myChild = child

      val grandParent = myParent.parent
      me.parent = grandParent
      grandParent?.child = me

      myParent.parent = me // I am become my parent
      me.child = myParent

      myChild?.parent = myParent
      myParent.child = myChild

      if (grandParent == null) {
        onNewHead(this)
      }

      if (myChild == null) {
        onNewTail(myParent)
      }
    }
  }

  override fun toString(): String {
    return "val: $value, child ${child?.value}"
  }
}

class SchwiftyList<T : Any> {

  private var head: SchwiftyNode<T>? = null
  private var tail: SchwiftyNode<T>? = null

  fun first(predicate: (T) -> Boolean): Optional<T> {
    var currentHead = head
    while (currentHead != null) {
      val value = currentHead.value
      if (predicate(value)) {
        currentHead.bumpPriority()
        return value.toOptional()
      }
      currentHead = currentHead.child
    }

    return Optional.empty()
  }

  fun enqueue(itemToAdd: T): T {
    val schwiftyNode = SchwiftyNode(
      itemToAdd,
      null,
      null,
      {
        this.head = it
      }
    ) {
      this.tail = it
    }
    val currentTail = tail
    if (currentTail == null) {
      head = schwiftyNode
      tail = schwiftyNode
    } else {
      schwiftyNode.parent = currentTail
      currentTail.child = schwiftyNode
      tail = schwiftyNode
    }

    return itemToAdd
  }
}
