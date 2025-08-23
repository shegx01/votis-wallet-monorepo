defmodule BeVotisWallet.HTTPClient.FinchClient do
  @moduledoc """
  Finch-based implementation of the HTTP client behaviour.
  
  This module provides a concrete implementation using Finch for making
  HTTP requests. It handles request building, execution, and response parsing
  according to the HTTPClient.Behaviour contract.
  """

  @behaviour BeVotisWallet.HTTPClient.Behaviour

  require Logger

  @impl BeVotisWallet.HTTPClient.Behaviour
  def build_payload(method, url, headers, body) do
    # Convert headers to the format expected by Finch
    finch_headers = normalize_headers(headers)
    
    # Build the Finch request
    Finch.build(method, url, finch_headers, body)
  end

  @impl BeVotisWallet.HTTPClient.Behaviour
  def request(finch_request) do
    Logger.debug("Making HTTP request", request: inspect(finch_request))
    
    case Finch.request(finch_request, BeVotisWallet.Finch) do
      {:ok, %Finch.Response{status: status, body: body, headers: headers}} ->
        handle_response(status, body, headers)
      
      {:error, reason} ->
        Logger.error("HTTP request failed", reason: inspect(reason))
        {:error, 0, reason}
    end
  end

  # Private functions

  defp normalize_headers(headers) when is_map(headers) do
    Enum.to_list(headers)
  end

  defp normalize_headers(headers) when is_list(headers) do
    headers
  end

  defp handle_response(status, body, headers) when status in 200..299 do
    case parse_json_body(body, headers) do
      {:ok, parsed_data} ->
        Logger.debug("HTTP request successful", status: status, body_length: byte_size(body))
        {:ok, parsed_data}
      
      {:error, reason} ->
        Logger.warning("Failed to parse JSON response", 
          status: status, 
          reason: inspect(reason),
          body: String.slice(body, 0, 200)
        )
        {:error, status, %{parse_error: reason, raw_body: body}}
    end
  end

  defp handle_response(status, body, _headers) do
    Logger.warning("HTTP request failed with error status", status: status, body: String.slice(body, 0, 200))
    
    # Try to parse error body as JSON for better error messages
    error_message = 
      case parse_json_body(body, []) do
        {:ok, parsed} -> parsed
        {:error, _} -> body
      end
    
    {:error, status, error_message}
  end

  defp parse_json_body("", _headers), do: {:ok, nil}
  
  defp parse_json_body(body, headers) do
    content_type = get_content_type(headers)
    
    if is_json_content_type?(content_type) do
      Jason.decode(body)
    else
      # If it's not JSON content type, return the raw body
      {:ok, body}
    end
  end

  defp get_content_type(headers) do
    headers
    |> Enum.find(fn {key, _value} -> 
      String.downcase(key) == "content-type" 
    end)
    |> case do
      {_key, value} -> String.downcase(value)
      nil -> ""
    end
  end

  defp is_json_content_type?(content_type) do
    String.contains?(content_type, "application/json") or
    String.contains?(content_type, "text/json")
  end
end
