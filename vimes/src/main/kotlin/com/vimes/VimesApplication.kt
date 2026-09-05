package com.vimes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VimesApplication

fun main(args: Array<String>) {
	runApplication<VimesApplication>(*args)
}
