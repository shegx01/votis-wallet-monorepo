defmodule BeVotisWallet.Utils.RequestSchema do
  alias Ecto.Changeset

  import Changeset

  defmacro __using__(_) do
    quote do
      use Ecto.Schema

      import Changeset
      import BeVotisWallet.Utils.RequestSchema

      @derive Jason.Encoder
      @primary_key false

      defp params_to_request(entity, fields, required_fields, params, as \\ &underscore_params/1) do
        entity
        |> cast(as.(params), fields)
        |> validate_required(required_fields)
        |> to_request()
      end
    end
  end

  @spec to_request(%Changeset{}) ::
          {:ok, struct} | {:error, {:malformed_params, String.t(), struct}}
  def to_request(%Changeset{} = changeset) do
    case apply_action(changeset, :insert) do
      {:ok, request} ->
        {:ok, request}

      {:error, broken_changeset} ->
        error_message = error_message(broken_changeset)
        request = apply_changes(broken_changeset)

        {:error, {:malformed_params, error_message, request}}
    end
  end

  defp error_message(changeset) do
    changeset
    |> Changeset.traverse_errors(fn {msg, opts} ->
      Enum.reduce(opts, msg, fn {key, value}, acc ->
        String.replace(acc, "%{#{key}}", to_string(value))
      end)
    end)
    |> flatten_errors()
    |> Enum.join(". ")
  end

  # Helper to flatten nested error structures
  defp flatten_errors(errors, path \\ []) when is_map(errors) do
    Enum.flat_map(errors, fn {field, error_data} ->
      current_path = path ++ [field]

      case error_data do
        errors when is_list(errors) and is_binary(hd(errors)) ->
          # Simple error list - format as "field error1, error2"
          ["#{Enum.join(current_path, ".")}: #{Enum.join(errors, ", ")}"]

        nested_errors when is_map(nested_errors) ->
          # Nested structure - recurse
          flatten_errors(nested_errors, current_path)

        errors when is_list(errors) ->
          # List might contain mixed content - handle each item
          Enum.flat_map(errors, fn
            error when is_binary(error) ->
              ["#{Enum.join(current_path, ".")}: #{error}"]

            nested when is_map(nested) ->
              flatten_errors(nested, current_path)

            other ->
              ["#{Enum.join(current_path, ".")}: #{inspect(other)}"]
          end)

        other ->
          ["#{Enum.join(current_path, ".")}: #{inspect(other)}"]
      end
    end)
  end

  @spec underscore_params(map()) :: map()
  def underscore_params(params) when is_map(params) do
    params
    |> Enum.reduce(Map.new(), fn {key, value}, acc ->
      key =
        if Kernel.is_bitstring(key) do
          Macro.underscore(key)
          |> String.downcase()
        else
          "#{key}"
          |> Macro.underscore()
          |> String.downcase()
          |> String.to_existing_atom()
        end

      # Recursively process nested maps
      value =
        cond do
          is_map(value) -> underscore_params(value)
          is_list(value) -> underscore_params(value)
          true -> value
        end

      Map.put(acc, key, value)
    end)
  end

  def underscore_params(params) when is_list(params) do
    Enum.map(params, &underscore_params/1)
  end

  def underscore_params(params), do: params

  @spec camelize_params(map()) :: map()
  def camelize_params(params) do
    params
    |> Enum.reduce(Map.new(), fn {key, value}, acc ->
      key =
        if Kernel.is_bitstring(key) do
          Macro.camelize(key)
          |> String.downcase()
        else
          "#{key}"
          |> Macro.camelize()
          |> String.downcase()
          |> String.to_existing_atom()
        end

      Map.put(acc, key, value)
    end)
  end
end
