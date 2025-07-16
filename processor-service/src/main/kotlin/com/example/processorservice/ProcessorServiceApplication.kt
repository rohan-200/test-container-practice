package com.example.processorservice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProcessorServiceApplication

fun main(args: Array<String>) {
    runApplication<ProcessorServiceApplication>(*args)
}