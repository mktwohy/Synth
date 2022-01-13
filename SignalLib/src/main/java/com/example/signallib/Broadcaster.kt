package com.example.signallib

import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

open class Broadcaster<T>{
    fun broadcast(value: T){
        callbacks.forEach{ it.invoke(value) }
        oneTimeCallbacks.forEach{ it.invoke(value) }
        oneTimeCallbacks.clear()
    }


    /* Note:
        callbacks and oneTimeCallbacks are treated like Sets rather than Queues.
        The only reason it's being used is because it's blocking,
        which prevents a concurrent modification exception.
        However, Neither Java or Kotlin have Blocking Sets.
     */

    private val callbacks: BlockingQueue< (T) -> Unit > = LinkedBlockingQueue()

    private val oneTimeCallbacks: BlockingQueue< (T) -> Unit > = LinkedBlockingQueue()

    fun registerListener(callback: (T) -> Unit){
        callbacks.add(callback)
    }

    fun registerOneTimeListener(callback: (T) -> Unit) {
        oneTimeCallbacks.add(callback)
    }
}

class StateBroadcaster<T>(value: T): Broadcaster<T>(){
    var value = value
        set(newValue){
            field = newValue
            this.broadcast(value)
        }
}

