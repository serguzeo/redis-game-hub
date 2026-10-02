package com.gamehub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class GameHubApplication

fun main(args: Array<String>) {
    runApplication<GameHubApplication>(*args)
}
