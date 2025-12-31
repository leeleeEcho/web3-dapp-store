package com.di.dappstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DAppStoreApplication

fun main(args: Array<String>) {
    runApplication<DAppStoreApplication>(*args)
}
