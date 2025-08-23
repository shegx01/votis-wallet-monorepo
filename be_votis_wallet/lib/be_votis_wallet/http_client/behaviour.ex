defmodule BeVotisWallet.HTTPClient.Behaviour do
  @moduledoc """
  Behaviour for generic HTTP client operations.

  This behaviour defines a contract for HTTP operations that can be used
  across different services (Turnkey, other external APIs, etc.).

  It provides two main operations:
  - `build_payload/4` - constructs the HTTP request payload
  - `request/1` - executes the HTTP request and handles response parsing

  Successful responses (status 200-299) return `{:ok, parsed_data}` where
  parsed_data is the JSON-decoded response body.

  Error responses return `{:error, status_code, error_message}`.
  """

  @type method :: :get | :post | :put | :patch | :delete | :head | :options
  @type url :: String.t()
  @type headers :: [{String.t(), String.t()}] | %{String.t() => String.t()}
  @type body :: iodata() | nil
  @type options :: keyword()
  @type request_payload :: term()
  @type parsed_data :: term()
  @type error_message :: term()

  @doc """
  Build an HTTP request payload from the given components.

  This function should construct whatever data structure the implementation
  needs to make the actual HTTP request (e.g., Finch.Request struct).

  ## Parameters
  - `method` - HTTP method (:get, :post, etc.)
  - `url` - Full URL for the request
  - `headers` - Request headers
  - `body` - Request body (can be nil for GET requests)

  ## Options
  Options may include:
  - `:timeout` - Request timeout in milliseconds
  - `:retry` - Retry configuration
  - `:pool` - Connection pool configuration
  """
  @callback build_payload(method(), url(), headers(), body()) :: request_payload()

  @doc """
  Execute an HTTP request using the built payload.

  ## Returns
  - `{:ok, parsed_data}` - Success response with JSON-parsed body (status 200-299)
  - `{:error, status_code, error_message}` - Error response with status code and message

  The `parsed_data` should be the result of `Jason.decode!` on the response body.
  The `error_message` can be any term representing the error (string, map, etc.).
  """
  @callback request(request_payload()) ::
              {:ok, parsed_data()} | {:error, integer(), error_message()}
end
