defmodule BeVotisWallet.Users.User do
  @moduledoc """
  User schema for Turnkey wallet integration.

  Stores user information including their Turnkey sub-organization details,
  wallet information, and authentication details.
  """
  use Ecto.Schema
  import Ecto.Changeset
  import Ecto.Query

  alias BeVotisWallet.Repo

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  @derive {Jason.Encoder, except: [:__meta__]}

  schema "users" do
    field :sub_org_id, :string
    field :wallet_id, :string
    field :root_user_ids, {:array, :string}
    field :sub_organization_name, :string
    field :email, :string
    field :authenticator_name, :string

    timestamps(type: :utc_datetime)
  end

  @doc """
  Create changeset for new user creation.

  Required fields: email, sub_org_id, sub_organization_name
  Optional fields: wallet_id, root_user_ids, authenticator_name
  """
  def changeset(user \\ %__MODULE__{}, attrs) do
    user
    |> cast(attrs, [
      :email,
      :sub_org_id,
      :sub_organization_name,
      :wallet_id,
      :root_user_ids,
      :authenticator_name
    ])
    |> validate_required([:email, :sub_org_id, :sub_organization_name])
    |> update_change(:email, &String.downcase/1)
    |> validate_format(:email, ~r/^[^\s]+@[^\s]+\.[^\s]+$/, message: "must be a valid email")
    |> validate_length(:email, max: 255)
    |> validate_length(:sub_organization_name, max: 255)
    |> unique_constraint(:email)
  end

  @doc """
  Update changeset for existing user modifications.

  Allows updating wallet_id, root_user_ids, and authenticator_name.
  Does not allow changing email or sub_organization details.
  """
  def update_changeset(user, attrs) do
    user
    |> cast(attrs, [:wallet_id, :root_user_ids, :authenticator_name])
    |> validate_length(:authenticator_name, max: 255)
  end

  @doc """
  Find user by email address.

  Returns `{:ok, user}` if found, `{:error, :not_found}` if not found.
  """
  def get_by_email(email) when is_binary(email) do
    case Repo.get_by(__MODULE__, email: String.downcase(email)) do
      nil -> {:error, :not_found}
      user -> {:ok, user}
    end
  end

  @doc """
  Create a new user with the given attributes.

  Returns `{:ok, user}` on success, `{:error, changeset}` on validation errors.
  """
  def create_user(attrs) do
    %__MODULE__{}
    |> changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Update an existing user with the given attributes.

  Returns `{:ok, user}` on success, `{:error, changeset}` on validation errors.
  """
  def update_user(user, attrs) do
    user
    |> update_changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Check if a user exists by email address.

  Returns `true` if user exists, `false` otherwise.
  """
  def exists_by_email?(email) when is_binary(email) do
    from(u in __MODULE__, where: u.email == ^String.downcase(email))
    |> Repo.exists?()
  end

  @doc """
  Check if a user exists by sub_org_id.

  Returns `true` if user exists, `false` otherwise.
  """
  def exists_by_sub_org_id?(sub_org_id) when is_binary(sub_org_id) do
    from(u in __MODULE__, where: u.sub_org_id == ^sub_org_id)
    |> Repo.exists?()
  end
end
