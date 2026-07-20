package com.kae.engine.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EventBusTest {

    data class TestEvent(val value: Int)
    data class AnotherEvent(val message: String)

    @Test
    fun `subscriber receives published event`() {
        val bus = EventBus()
        var received: TestEvent? = null

        bus.subscribe<TestEvent> { received = it }
        bus.publish(TestEvent(42))

        assertNotNull(received)
        assertEquals(42, received!!.value)
    }

    @Test
    fun `unsubscribe stops event delivery`() {
        val bus = EventBus()
        var callCount = 0

        val subscription = bus.subscribe<TestEvent> { callCount++ }
        bus.publish(TestEvent(1))
        assertEquals(1, callCount)

        bus.unsubscribe(subscription)
        bus.publish(TestEvent(2))
        assertEquals(1, callCount)
    }

    @Test
    fun `multiple subscribers receive same event`() {
        val bus = EventBus()
        var count1 = 0
        var count2 = 0

        bus.subscribe<TestEvent> { count1++ }
        bus.subscribe<TestEvent> { count2++ }
        bus.publish(TestEvent(1))

        assertEquals(1, count1)
        assertEquals(1, count2)
    }

    @Test
    fun `different event types are independent`() {
        val bus = EventBus()
        var testEventReceived = false
        var anotherEventReceived = false

        bus.subscribe<TestEvent> { testEventReceived = true }
        bus.subscribe<AnotherEvent> { anotherEventReceived = true }

        bus.publish(TestEvent(1))
        assertTrue(testEventReceived)
        assertFalse(anotherEventReceived)
    }

    @Test
    fun `clear removes all subscriptions`() {
        val bus = EventBus()
        var callCount = 0

        bus.subscribe<TestEvent> { callCount++ }
        bus.subscribe<AnotherEvent> { callCount++ }
        bus.clear()

        bus.publish(TestEvent(1))
        bus.publish(AnotherEvent("test"))
        assertEquals(0, callCount)
    }

    @Test
    fun `getSubscriptionCount returns correct count`() {
        val bus = EventBus()
        assertEquals(0, bus.getSubscriptionCount())

        val sub1 = bus.subscribe<TestEvent> {}
        assertEquals(1, bus.getSubscriptionCount())

        bus.subscribe<AnotherEvent> {}
        assertEquals(2, bus.getSubscriptionCount())

        bus.unsubscribe(sub1)
        assertEquals(1, bus.getSubscriptionCount())
    }

    @Test
    fun `event data is correctly passed`() {
        val bus = EventBus()
        var receivedMessage = ""

        bus.subscribe<AnotherEvent> { receivedMessage = it.message }
        bus.publish(AnotherEvent("hello world"))

        assertEquals("hello world", receivedMessage)
    }

    @Test
    fun `subscribe with class parameter works`() {
        val bus = EventBus()
        var received: TestEvent? = null

        bus.subscribe(TestEvent::class.java) { received = it }
        bus.publish(TestEvent(99))

        assertNotNull(received)
        assertEquals(99, received!!.value)
    }
}
