defmodule BeVotisWalletWeb.LoginController.LoginParamsTest do
  use ExUnit.Case, async: true

  alias BeVotisWalletWeb.LoginController.LoginParams

  describe "changeset/2" do
    test "validates successfully with all required passkey parameters" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamped_body" => "binary_request_body",
        "stamp" => "signature_12345"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      assert changeset.valid?

      result = Ecto.Changeset.apply_changes(changeset)
      assert result.auth_type == "passkey"
      assert result.email == "test@example.com"
      assert result.stamped_body == "binary_request_body"
      assert result.stamp == "signature_12345"
    end

    test "validates successfully with all required oauth parameters" do
      attrs = %{
        "auth_type" => "oauth",
        "email" => "oauth@example.com",
        "stamped_body" => "oauth_binary_body",
        "stamp" => "oauth_signature_67890"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      assert changeset.valid?

      result = Ecto.Changeset.apply_changes(changeset)
      assert result.auth_type == "oauth"
      assert result.email == "oauth@example.com"
      assert result.stamped_body == "oauth_binary_body"
      assert result.stamp == "oauth_signature_67890"
    end

    test "requires auth_type field" do
      attrs = %{
        "email" => "test@example.com",
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).auth_type
    end

    test "requires email field" do
      attrs = %{
        "auth_type" => "passkey",
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).email
    end

    test "requires stamped_body field" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "is required" in errors_on(changeset).stamped_body
    end

    test "requires stamp field" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamped_body" => "body"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).stamp
    end

    test "rejects invalid auth_type values" do
      attrs = %{
        "auth_type" => "invalid",
        "email" => "test@example.com",
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "must be 'passkey' or 'oauth'" in errors_on(changeset).auth_type
    end

    test "rejects invalid email format" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "invalid_email_format",
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "must be a valid email" in errors_on(changeset).email
    end

    test "rejects empty email" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "",
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).email
    end

    test "rejects email that is too long" do
      # 262 chars total
      long_email = String.duplicate("a", 250) <> "@example.com"

      attrs = %{
        "auth_type" => "passkey",
        "email" => long_email,
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      error_messages = errors_on(changeset).email
      assert Enum.any?(error_messages, fn msg -> String.contains?(msg, "should be at most") end)
    end

    test "rejects empty stamp" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamped_body" => "body",
        "stamp" => ""
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).stamp
    end

    test "rejects nil stamped_body" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamped_body" => nil,
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "is required" in errors_on(changeset).stamped_body
    end

    test "rejects empty binary stamped_body" do
      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamped_body" => "",
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).stamped_body
    end

    test "handles atom keys in attributes" do
      attrs = %{
        auth_type: "passkey",
        email: "test@example.com",
        stamped_body: "body",
        stamp: "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      assert changeset.valid?
    end

    test "ignores unexpected fields" do
      attrs = %{
        "auth_type" => "oauth",
        "email" => "test@example.com",
        "stamped_body" => "body",
        "stamp" => "stamp",
        "unexpected_field" => "should_be_ignored"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      assert changeset.valid?

      result = Ecto.Changeset.apply_changes(changeset)
      # Verify unexpected field is not included
      refute Map.has_key?(result, :unexpected_field)
    end

    test "works with binary stamped_body for passkey" do
      binary_body = <<1, 2, 3, 4, 5>>

      attrs = %{
        "auth_type" => "passkey",
        "email" => "test@example.com",
        "stamped_body" => binary_body,
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      assert changeset.valid?

      result = Ecto.Changeset.apply_changes(changeset)
      assert result.stamped_body == binary_body
    end

    test "works with large binary stamped_body" do
      # Create a larger binary to test
      large_binary = :crypto.strong_rand_bytes(1024)

      attrs = %{
        "auth_type" => "oauth",
        "email" => "large@example.com",
        "stamped_body" => large_binary,
        "stamp" => "stamp"
      }

      changeset = LoginParams.changeset(%LoginParams{}, attrs)

      assert changeset.valid?

      result = Ecto.Changeset.apply_changes(changeset)
      assert result.stamped_body == large_binary
      assert byte_size(result.stamped_body) == 1024
    end

    test "both passkey and oauth require same core fields" do
      base_attrs = %{
        "email" => "test@example.com",
        "stamped_body" => "body",
        "stamp" => "stamp"
      }

      # Test passkey
      passkey_changeset =
        LoginParams.changeset(%LoginParams{}, Map.put(base_attrs, "auth_type", "passkey"))

      assert passkey_changeset.valid?

      # Test oauth
      oauth_changeset =
        LoginParams.changeset(%LoginParams{}, Map.put(base_attrs, "auth_type", "oauth"))

      assert oauth_changeset.valid?
    end

    test "accepts various valid email formats" do
      valid_emails = [
        "test@example.com",
        "user.name+tag@domain.co.uk",
        "complex_email123@subdomain.example.org",
        "a@b.co"
      ]

      for email <- valid_emails do
        attrs = %{
          "auth_type" => "passkey",
          "email" => email,
          "stamped_body" => "body",
          "stamp" => "stamp"
        }

        changeset = LoginParams.changeset(%LoginParams{}, attrs)
        assert changeset.valid?, "Expected #{email} to be valid"

        result = Ecto.Changeset.apply_changes(changeset)
        assert result.email == email
      end
    end
  end

  # Helper function to extract errors in a readable format
  defp errors_on(changeset) do
    Ecto.Changeset.traverse_errors(changeset, fn {message, opts} ->
      Regex.replace(~r"%{(\\w+)}", message, fn _, key ->
        opts |> Keyword.get(String.to_existing_atom(key), key) |> to_string()
      end)
    end)
  end
end
