defmodule BeVotisWallet.Services.Turnkey.Schemas.CreateSubOrganizationResponse do
  @moduledoc """
  Embedded schema for parsing and validating Turnkey CreateSubOrganization API responses.

  This schema represents the structure of the response from ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7.
  """
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key false
  embedded_schema do
    embeds_one :activity, Activity, primary_key: false do
      field :id, :string
      field :status, :string
      field :type, :string
      field :organization_id, :string, source: :organizationId
      field :timestamp_ms, :string, source: :timestampMs

      embeds_one :result, Result, primary_key: false do
        embeds_one :activity, InnerActivity, primary_key: false do
          embeds_one :result, InnerResult, primary_key: false do
            embeds_one :create_sub_organization_result_v7, CreateSubOrgResult,
              primary_key: false,
              source: :createSubOrganizationResultV7 do
              field :sub_organization_id, :string, source: :subOrganizationId
              field :root_user_ids, {:array, :string}, source: :rootUserIds, default: []

              embeds_one :wallet, Wallet, primary_key: false do
                field :wallet_id, :string, source: :walletId
                field :addresses, {:array, :string}, default: []
              end
            end
          end
        end
      end
    end
  end

  @doc """
  Parse and validate a Turnkey CreateSubOrganization response.

  Returns a changeset with the parsed and validated data.
  """
  def changeset(response_data) when is_map(response_data) do
    %__MODULE__{}
    |> cast(response_data, [])
    |> cast_embed(:activity, with: &activity_changeset/2, required: true)
  end

  defp activity_changeset(activity, attrs) do
    activity
    |> cast(attrs, [:id, :status, :type, :organization_id, :timestamp_ms])
    |> validate_required([:id, :status, :type, :organization_id])
    |> validate_inclusion(:status, [
      "ACTIVITY_STATUS_COMPLETED",
      "ACTIVITY_STATUS_PENDING",
      "ACTIVITY_STATUS_FAILED"
    ])
    |> validate_inclusion(:type, ["ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7"])
    |> cast_embed(:result, with: &result_changeset/2, required: true)
  end

  defp result_changeset(result, attrs) do
    result
    |> cast(attrs, [])
    |> cast_embed(:activity, with: &inner_activity_changeset/2, required: true)
  end

  defp inner_activity_changeset(activity, attrs) do
    activity
    |> cast(attrs, [])
    |> cast_embed(:result, with: &inner_result_changeset/2, required: true)
  end

  defp inner_result_changeset(result, attrs) do
    result
    |> cast(attrs, [])
    |> cast_embed(:create_sub_organization_result_v7,
      with: &create_sub_org_result_changeset/2,
      required: true
    )
  end

  defp create_sub_org_result_changeset(result, attrs) do
    result
    |> cast(attrs, [:sub_organization_id, :root_user_ids])
    |> validate_required([:sub_organization_id])
    |> cast_embed(:wallet, with: &wallet_changeset/2)
  end

  defp wallet_changeset(wallet, attrs) do
    wallet
    |> cast(attrs, [:wallet_id, :addresses])
  end

  @doc """
  Extract essential user data from a valid Turnkey response.

  Returns a map with the key fields needed for user creation.
  """
  def extract_user_data(%__MODULE__{} = response) do
    activity = response.activity
    create_result = activity.result.activity.result.create_sub_organization_result_v7

    %{
      sub_org_id: create_result.sub_organization_id,
      activity_id: activity.id,
      root_user_ids: create_result.root_user_ids || [],
      wallet_id: get_wallet_id(create_result.wallet),
      timestamp_ms: activity.timestamp_ms,
      status: activity.status
    }
  end

  defp get_wallet_id(%{wallet_id: wallet_id}), do: wallet_id
  defp get_wallet_id(_), do: nil

  @doc """
  Parse and extract user data from a raw Turnkey response map.

  Returns `{:ok, user_data}` on success or `{:error, changeset}` on validation failure.
  """
  def parse_response(response_data) when is_map(response_data) do
    changeset = changeset(response_data)

    if changeset.valid? do
      response_struct = Ecto.Changeset.apply_changes(changeset)
      user_data = extract_user_data(response_struct)
      {:ok, user_data}
    else
      {:error, changeset}
    end
  end
end
