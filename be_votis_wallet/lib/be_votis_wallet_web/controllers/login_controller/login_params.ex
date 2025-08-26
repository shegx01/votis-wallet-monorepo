defmodule BeVotisWalletWeb.LoginController.LoginParams do
  @moduledoc """
  Embedded schema for validating login controller parameters.
  
  The login endpoint accepts pre-signed requests from clients where all
  authentication details (including OAuth tokens, public keys, etc.) are
  already embedded in the stamped_body binary.
  
  Parameters:
  - auth_type: "passkey" or "oauth" to indicate the type of authentication
  - org_id: Organization identifier to validate against our database
  - stamped_body: Binary containing the complete signed request body for Turnkey
  - stamp: Client signature for the stamped_body
  """
  
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key false
  embedded_schema do
    field :auth_type, :string
    field :org_id, :string
    field :stamped_body, :binary
    field :stamp, :string
  end

  @doc """
  Validates login parameters.
  
  All fields are required:
  - auth_type: must be "passkey" or "oauth"
  - org_id: must be a non-empty string
  - stamped_body: must be a non-empty binary (contains the full Turnkey request)
  - stamp: must be a non-empty string (client signature)
  """
  def changeset(params \\ %__MODULE__{}, attrs) do
    params
    |> cast(attrs, [:auth_type, :org_id, :stamped_body, :stamp])
    |> validate_required([:auth_type, :org_id, :stamped_body, :stamp])
    |> validate_inclusion(:auth_type, ["passkey", "oauth"], message: "must be 'passkey' or 'oauth'")
    |> validate_length(:org_id, min: 1, message: "can't be blank")
    |> validate_length(:stamp, min: 1, message: "cannot be empty")
    |> validate_stamped_body()
  end
  
  defp validate_stamped_body(changeset) do
    case get_field(changeset, :stamped_body) do
      nil -> 
        add_error(changeset, :stamped_body, "is required")
      stamped_body when is_binary(stamped_body) and byte_size(stamped_body) > 0 -> 
        changeset
      _ -> 
        add_error(changeset, :stamped_body, "must be a non-empty binary")
    end
  end
end
