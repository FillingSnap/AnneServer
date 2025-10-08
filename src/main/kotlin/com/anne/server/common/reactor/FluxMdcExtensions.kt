package com.anne.server.common.reactor

import io.micrometer.context.ContextSnapshotFactory
import reactor.core.publisher.Flux

fun <T> Flux<T>.withMdc(snapshotFactory: ContextSnapshotFactory): Flux<T> =
    this.contextWrite { snapshotFactory.captureAll().updateContext(it) }