package com.kae.engine.core

class EventBus {

    class Subscription internal constructor(
        internal val eventType: Class<*>,
        internal val id: Long
    )

    private var nextId: Long = 0
    private val subscriptions = mutableMapOf<Class<*>, MutableMap<Long, (Any) -> Unit>>()

    inline fun <reified T : Any> subscribe(noinline handler: (T) -> Unit): Subscription {
        return subscribe(T::class.java, handler)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> subscribe(eventType: Class<T>, handler: (T) -> Unit): Subscription {
        val id = nextId++
        val subscription = Subscription(eventType, id)

        val handlerMap = subscriptions.getOrPut(eventType) { mutableMapOf() }
        handlerMap[id] = { event -> handler(event as T) }

        return subscription
    }

    fun <T : Any> publish(event: T) {
        val eventType = event::class.java
        val handlerMap = subscriptions[eventType]
        handlerMap?.values?.forEach { handler ->
            handler(event)
        }
    }

    fun unsubscribe(subscription: Subscription) {
        val handlerMap = subscriptions[subscription.eventType]
        handlerMap?.remove(subscription.id)
        if (handlerMap?.isEmpty() == true) {
            subscriptions.remove(subscription.eventType)
        }
    }

    fun clear() {
        subscriptions.clear()
    }

    fun getSubscriptionCount(): Int {
        return subscriptions.values.sumOf { it.size }
    }
}
