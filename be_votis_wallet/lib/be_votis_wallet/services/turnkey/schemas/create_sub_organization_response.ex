defmodule BeVotisWallet.Services.Turnkey.Schemas.CreateSubOrganizationResponse do
  @moduledoc """
  Turnkey CreateSubOrganization API response schema.
  
  This module validates and parses ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7 responses
  using the RequestSchema utility for proper embedded schema validation.
  """
  
  use BeVotisWallet.Utils.RequestSchema
  
  # Embedded schema for wallet information
  defmodule Wallet do
    use BeVotisWallet.Utils.RequestSchema
    
    @fields ~w(wallet_id addresses)a
    @required_fields ~w(wallet_id)a
    
    embedded_schema do
      field :wallet_id, :string
      field :addresses, {:array, :string}, default: []
    end
    
    def changeset(wallet, attrs) do
      wallet
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Embedded schema for V7 result
  defmodule CreateSubOrganizationResultV7 do
    use BeVotisWallet.Utils.RequestSchema
    
    @fields ~w(sub_organization_id root_user_ids)a
    @required_fields ~w(sub_organization_id)a
    
    embedded_schema do
      field :sub_organization_id, :string
      field :root_user_ids, {:array, :string}, default: []
      embeds_one :wallet, Wallet
    end
    
    def changeset(result, attrs) do
      result
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
      |> cast_embed(:wallet)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Embedded schema for legacy result
  defmodule CreateSubOrganizationResult do
    use BeVotisWallet.Utils.RequestSchema
    
    @fields ~w(organization_id)a
    @required_fields ~w(organization_id)a
    
    embedded_schema do
      field :organization_id, :string
    end
    
    def changeset(result, attrs) do
      result
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Embedded schema for activity result
  defmodule ActivityResult do
    use BeVotisWallet.Utils.RequestSchema
    
    @fields ~w()a
    @required_fields ~w()a
    
    embedded_schema do
      embeds_one :create_sub_organization_result_v7, CreateSubOrganizationResultV7
      embeds_one :create_sub_organization_result, CreateSubOrganizationResult
    end
    
    def changeset(result, attrs) do
      result
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
      |> cast_embed(:create_sub_organization_result_v7)
      |> cast_embed(:create_sub_organization_result)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Embedded schema for inner activity
  defmodule InnerActivity do
    use BeVotisWallet.Utils.RequestSchema
    
    @fields ~w(id organization_id status type)a
    @required_fields ~w()a
    
    embedded_schema do
      field :id, :string
      field :organization_id, :string
      field :status, :string
      field :type, :string
      embeds_one :result, ActivityResult
    end
    
    def changeset(activity, attrs) do
      activity
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
      |> cast_embed(:result)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Embedded schema for activity result wrapper
  defmodule Result do
    use BeVotisWallet.Utils.RequestSchema
    
    @fields ~w()a
    @required_fields ~w()a
    
    embedded_schema do
      embeds_one :activity, InnerActivity
    end
    
    def changeset(result, attrs) do
      result
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
      |> cast_embed(:activity)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Main activity schema
  defmodule Activity do
    use BeVotisWallet.Utils.RequestSchema
    
    @valid_statuses ~w[
      ACTIVITY_STATUS_COMPLETED
      ACTIVITY_STATUS_PENDING
      ACTIVITY_STATUS_FAILED
    ]
    
    @valid_types ~w[
      ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7
      ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION
    ]
    
    @fields ~w(id status type organization_id timestamp_ms)a
    @required_fields ~w(id status)a
    
    embedded_schema do
      field :id, :string
      field :status, :string
      field :type, :string
      field :organization_id, :string
      field :timestamp_ms, :string
      embeds_one :result, Result
    end
    
    def changeset(activity, attrs) do
      activity
      |> cast(attrs, @fields)
      |> validate_required(@required_fields)
      |> validate_inclusion(:status, @valid_statuses)
      |> validate_inclusion(:type, @valid_types)
      |> cast_embed(:result)
    end
    
    def new(params \\ %{}) do
      %__MODULE__{}
      |> changeset(params)
      |> to_request()
    end
  end
  
  # Main response schema
  @fields ~w()a
  @required_fields ~w()a
  
  embedded_schema do
    embeds_one :activity, Activity
  end
  
  @doc """
  Create a new CreateSubOrganizationResponse from params.
  """
  def new(params \\ %{}) do
    params = underscore_params(params)
    
    %__MODULE__{}
    |> cast(params, @fields)
    |> validate_required(@required_fields)
    |> cast_embed(:activity, required: true)
    |> to_request()
  end
  
  @doc """
  Parse and validate a Turnkey response, returning extracted user data.
  
  Returns `{:ok, user_data}` on success or `{:error, reason}` on validation failure.
  """
  def parse_response(response_data) when is_map(response_data) do
    case new(response_data) do
      {:ok, response_struct} ->
        user_data = extract_user_data(response_struct)
        {:ok, user_data}
        
      {:error, {:malformed_params, error_message, _request}} ->
        {:error, error_message}
    end
  end
  
  @doc """
  Extract user data from a validated response struct.
  """
  def extract_user_data(%__MODULE__{} = response) do
    activity = response.activity
    
    # Handle case where activity is nil
    case activity do
      nil ->
        %{
          sub_org_id: nil,
          activity_id: nil,
          root_user_ids: [],
          wallet_id: nil,
          timestamp_ms: nil,
          status: nil
        }
      
      activity ->
        # Try V7 result first, then fallback to legacy
        {sub_org_id, wallet_id, root_user_ids} = 
          case get_in(activity, [Access.key(:result), Access.key(:activity), Access.key(:result), Access.key(:create_sub_organization_result_v7)]) do
            %CreateSubOrganizationResultV7{} = v7_result ->
              {
                v7_result.sub_organization_id,
                get_wallet_id(v7_result.wallet),
                v7_result.root_user_ids || []
              }
            
            _ ->
              # Fallback to legacy format
              case get_in(activity, [Access.key(:result), Access.key(:activity), Access.key(:result), Access.key(:create_sub_organization_result)]) do
                %CreateSubOrganizationResult{} = legacy_result ->
                  {legacy_result.organization_id, nil, []}
                
                _ ->
                  {nil, nil, []}
              end
          end
        
        %{
          sub_org_id: sub_org_id,
          activity_id: activity.id,
          root_user_ids: root_user_ids,
          wallet_id: wallet_id,
          timestamp_ms: activity.timestamp_ms,
          status: activity.status
        }
    end
  end
  
  # Helper to extract wallet ID safely
  defp get_wallet_id(%Wallet{} = wallet), do: wallet.wallet_id
  defp get_wallet_id(_), do: nil
end
