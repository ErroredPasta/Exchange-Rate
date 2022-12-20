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

val malformedResponse = Response(
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