package com.example.testdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestDemoApplication

fun main(args: Array<String>) {
	runApplication<TestDemoApplication>(*args)
}
