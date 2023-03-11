package mockk.demo

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk

class User(
    private val name: String
) {
    fun greet(word: String) = this.hello(word)

    private fun hello(word: String): String {
        val hello = "Hello, $name.\n"
        return hello + word
    }
}

internal class MockkTest : FunSpec({
    test("test greet") {
        val user = User("yamanaka")
        user.greet("") shouldBe "Hello, yamanaka.\n"
    }
    test("test hello mock") {
        val user = spyk(User("yamanaka"), recordPrivateCalls = true)
        every { user["hello"]("word") } returns "mock private function"

        user.greet("word") shouldBe "mock private function"
    }
    test("test mock greet") {
        val user = spyk(User("yamanaka"), recordPrivateCalls = true)
        every { user["hello"](allAny<String>()) } returns "mock private function"

        user.greet("word1") shouldBe "mock private function"
    }
})
