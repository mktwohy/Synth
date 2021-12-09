package com.example.signallib

import kotlin.properties.Delegates

class Broadcaster<T>(value: T){
    var value: T = value
        set(v){
            field = v
            callbacks.forEach{ it.invoke(value) }
        }

    private val callbacks = mutableSetOf< (T) -> Unit >()

    fun registerListener(callback: (T) -> Unit){
        callbacks.add(callback)
    }
}


