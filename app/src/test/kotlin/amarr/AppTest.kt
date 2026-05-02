package amarr

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AppTest : StringSpec({

    "should default amarr port to 8080" {
        amarrPort(emptyMap()) shouldBe 8080
    }

    "should read amarr port from environment" {
        amarrPort(mapOf("AMARR_PORT" to "9090")) shouldBe 9090
    }

    "should reject invalid amarr port" {
        shouldThrow<Exception> {
            amarrPort(mapOf("AMARR_PORT" to "not-a-port"))
        }
    }

    "should reject out of range amarr port" {
        shouldThrow<Exception> {
            amarrPort(mapOf("AMARR_PORT" to "70000"))
        }
    }

})
