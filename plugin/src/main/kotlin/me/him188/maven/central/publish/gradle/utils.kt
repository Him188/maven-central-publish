package me.him188.maven.central.publish.gradle

import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("RedundantNullableReturnType")
private val NULL: Any? = Any()

internal fun <T, R> T.lazyDefault(
    default: T.() -> R
): LazyDefaultProperty<T, R> = LazyDefaultProperty { default() }

/**
 * initializes on first query
 */
internal class LazyDefaultProperty<in T, R>(
    default: () -> R
) : ReadWriteProperty<T, R> {
    private val default by lazy(default) // credentials?.packageGroup ?: (project.group ?: project.rootProject.group).toString()
    private val value = AtomicReference<Any?>(NULL)

    override fun setValue(thisRef: T, property: KProperty<*>, value: R) {
        this.value.set(value)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): R {
        val current = this.value.get()
        @Suppress("UNCHECKED_CAST")
        if (current === NULL) {
            this.value.compareAndSet(NULL, default)
        } else {
            return current as R
        }
        return getValue(thisRef, property)
    }
}