package com.example.signallib

open class Broadcaster<T>{
    fun broadcast(value: T){
        callbacks.forEach{ it.invoke(value) }
        oneTimeCallbacks.forEach{ it.invoke(value) }
        oneTimeCallbacks.clear()
    }

    private val callbacks = mutableSetOf< (T) -> Unit >()

    private val oneTimeCallbacks = mutableSetOf< (T) -> Unit >()

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

