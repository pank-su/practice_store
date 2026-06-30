package us.panks.data.api

class ApiException(val statusCode: Int, message: String) : Exception(message)
