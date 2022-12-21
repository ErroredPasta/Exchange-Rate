package com.example.exchangerate.data.repository.response

val unsupportedCodeResponse = Response(
    code = 404,
    body = """
    {
      "result": "error",
      "documentation": "https://www.exchangerate-api.com/docs",
      "terms-of-use": "https://www.exchangerate-api.com/terms",
      "error-type": "unsupported-code"
    }
    """.trimIndent()
)

val malformedRequestResponse = Response(
    code = 400,
    body = """
    {
      "result": "error",
      "documentation": "https://www.exchangerate-api.com/docs",
      "terms-of-use": "https://www.exchangerate-api.com/terms",
      "error-type": "malformed-request"
    }
    """.trimIndent()
)

val invalidKeyResponse = Response(
    code = 401,
    body = """
    {
      "result": "error",
      "documentation": "https://www.exchangerate-api.com/docs",
      "terms-of-use": "https://www.exchangerate-api.com/terms",
      "error-type": "invalid-key"
    }
    """.trimIndent()
)

val inactiveAccountResponse = Response(
    code = 403,
    body = """
    {
      "result": "error",
      "documentation": "https://www.exchangerate-api.com/docs",
      "terms-of-use": "https://www.exchangerate-api.com/terms",
      "error-type": "inactive-account"
    }
    """.trimIndent()
)

val quotaReachedResponse = Response(
    code = 403,
    body = """
    {
      "result": "error",
      "documentation": "https://www.exchangerate-api.com/docs",
      "terms-of-use": "https://www.exchangerate-api.com/terms",
      "error-type": "quota-reached"
    }
    """.trimIndent()
)