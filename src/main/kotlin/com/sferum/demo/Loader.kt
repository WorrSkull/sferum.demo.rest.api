package com.sferum.demo

import com.beust.klaxon.Klaxon
import java.io.BufferedReader
import java.io.File

class Loader {
    fun loaderData(file: String ): LoaderJson {
        val bufferedReader: BufferedReader = File(file).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        return Klaxon().parse<LoaderJson>(inputString) ?: throw Exception("Файл не соответствует заданным требованиям")
    }
    fun loadBook(loadedData: LoaderJson): MarketProducts {
        val books = loadedData.books.iterator()
        var productId = 0
        val productsList = emptyList<MarketProduct>().toMutableList()
        books.forEach {
            val product = MarketProduct(
                id = productId, book = Book(
                    name = it.name, author = it.author
                ),
                price = it.price,
                amount = it.amount
            )
            productsList += listOf(product)
            productId += 1
        }
        return MarketProducts(products = productsList)
    }
    fun loadAccount(loadedData: LoaderJson): AccountData {
        return AccountData(balance = loadedData.account.money, books = emptyList())
    }
}