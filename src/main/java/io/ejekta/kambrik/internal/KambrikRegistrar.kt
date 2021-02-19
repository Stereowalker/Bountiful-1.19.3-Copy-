package io.ejekta.kambrik.internal

import io.ejekta.kambrik.ext.register
import io.ejekta.kambrik.registration.KambricAutoRegistrar
import net.fabricmc.loader.api.entrypoint.EntrypointContainer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.SimpleRegistry

object KambrikRegistrar {
    //data class RegistryMap<T>(val registry: SimpleRegistry<T>, val items: MutableMap<String, T>)
    //data class ModRegistrar(val modId: String, val content: MutableList<RegistryMap<*>>)

    data class RegistrationEntry<T>(val registry: Registry<T>, val itemId: String, val item: T) {
        fun register(modId: String) = registry.register(Identifier(modId, itemId), item)
    }
    data class ModResistrar(val requestor: KambricAutoRegistrar, val content: MutableList<RegistrationEntry<*>> = mutableListOf())

    val registrars = mutableMapOf<KambricAutoRegistrar, ModResistrar>()

    operator fun get(requester: KambricAutoRegistrar): ModResistrar {
        return registrars.getOrPut(requester) { ModResistrar(requester) }
    }

    fun <T> register(requester: KambricAutoRegistrar, reg: Registry<T>, itemId: String, obj: T): T {
        println("Kambrik registering '${requester::class.qualifiedName} for $itemId' for autoregistration")
        this[requester].content.add(RegistrationEntry(reg, itemId, obj))
        return obj
    }

    fun doRegistrationFor(container: EntrypointContainer<KambrikMarker>) {
        println("Kambrik doing real registration for mod ${container.provider.metadata.id}")
        val registrar = this[container.entrypoint as? KambricAutoRegistrar ?: return]
        registrar.requestor.manualRegister()
        registrar.content.forEach { entry ->
            entry.register(container.provider.metadata.id)
        }
    }

}