package com.sferum.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
class DemoApplication

data class Book (val name: String, var author: String)

data class AccountBooks (val book: Book, var amount: Int)
data class AccountData (var books: List<AccountBooks> = emptyList(), var balance: Int = 0)

data class MarketProduct (val id: Int, val book: Book, val price: Int, var amount: Int)
data class MarketProducts (val products: List<MarketProduct> = emptyList())

data class LoaderMoney (val money: Int)
data class LoaderBooks (val author: String, val name: String, val price: Int, val amount: Int)
data class LoaderJson (val account: LoaderMoney, val books: List<LoaderBooks>)

data class ErrorResponse (val error: String)
data class MarketClientPost (val id: Int, val amount: Int)

var account: AccountData = AccountData()
var books: MarketProducts = MarketProducts()

fun main(args: Array<String>) {
	val loader = Loader()
	val loadData = loader.loaderData(args[0])
	account = loader.loadAccount(loadData)
	books = loader.loadBook(loadData)
	runApplication<DemoApplication>(*args)
}

@RestController
@RequestMapping("/account")
class Account{
	@RequestMapping(method = [(RequestMethod.GET)])
	fun index(): ResponseEntity<AccountData> = ResponseEntity.ok(account)
}

@RestController
@RequestMapping("/market")
class Market{
	@RequestMapping(method = [(RequestMethod.GET)])
	fun index(): ResponseEntity<MarketProducts> {
		val booksList = books.products.iterator()
		val booksOut = emptyList<MarketProduct>().toMutableList()
		booksList.forEach {
			if (it.amount > 0) {
				booksOut.add(it)
			}
		}
		return ResponseEntity.ok(MarketProducts(products = booksOut))
	}

	@RequestMapping(path = ["/deal"], method = [(RequestMethod.POST)])
	fun post(@RequestBody clientBody: MarketClientPost): Any {
		val book = books.products.getOrNull(clientBody.id)
			?: return ResponseEntity(
				ErrorResponse("Книги с таким идентификатором нет в продаже"),
				HttpStatus.BAD_REQUEST
			)
		if (book.amount < clientBody.amount) {
			return ResponseEntity(
				ErrorResponse("Книги есть в продаже, но в недостаточном количестве"),
				HttpStatus.BAD_REQUEST
			)
		}
		if (book.price*clientBody.amount > account.balance) {
			return ResponseEntity(
				ErrorResponse("У аккаунта не хватает денег, чтобы оплатить покупку"),
				HttpStatus.BAD_REQUEST
			)
		}
		books.products[clientBody.id].amount -= clientBody.amount
		account.balance -= book.price*clientBody.amount
		val accountList = account.books
		if (accountList.isNotEmpty()) {
			accountList.forEach{
				if (it.book == books.products[clientBody.id].book) {
					val bookIndex = account.books.indexOf(it)
					account.books[bookIndex].amount += clientBody.amount
					return ResponseEntity.ok(account)
				}
			}
		}
		account.books += AccountBooks(book = books.products[clientBody.id].book, amount = clientBody.amount)
		return ResponseEntity.ok(account)
	}
}


