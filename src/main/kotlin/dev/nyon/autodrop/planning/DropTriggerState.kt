package dev.nyon.autodrop.planning

data class ObservedStack<K>(val key: K, val count: Int)

/** Track editing and execution, and detect inventory changes other than AutoDrop's own predicted drops. */
class DropTriggerState<K> {
    var editing = false
        private set
    var executing = false
        private set
    private var previous: Map<Int, ObservedStack<K>>? = null

    fun setEditing(value: Boolean) {
        editing = value
    }

    fun begin(): Boolean {
        if (editing || executing) return false
        executing = true
        return true
    }

    fun finish() {
        executing = false
    }

    fun observe(snapshot: Map<Int, ObservedStack<K>>): Boolean {
        val before = previous
        previous = snapshot
        return before != null && before != snapshot && !editing && !executing
    }

    /** Accepts an own batch's predicted result; any later change, even picking items straight back up, triggers again. */
    fun afterOwnBatch(after: Map<Int, ObservedStack<K>>) {
        previous = after
    }

    fun reset() {
        previous = null; executing = false
    }
}
