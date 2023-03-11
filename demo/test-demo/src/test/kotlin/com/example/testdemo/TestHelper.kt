package com.example.testdemo

import com.example.testdemo.proto.user.UserGrpcKt
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.kotlin.AbstractCoroutineStub
import io.grpc.stub.AbstractStub
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun getTestTime(): LocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0))

fun getChannel(vararg interceptors: ClientInterceptor): ManagedChannel {
    return ManagedChannelBuilder.forAddress("localhost", 6565)
        .intercept(*interceptors)
        .usePlaintext()
        .build()
}