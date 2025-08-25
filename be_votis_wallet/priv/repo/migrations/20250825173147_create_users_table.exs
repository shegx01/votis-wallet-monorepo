defmodule BeVotisWallet.Repo.Migrations.CreateUsersTable do
  use Ecto.Migration

  def change do
    create table(:users, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :sub_org_id, :string, null: false
      add :wallet_id, :string
      add :root_user_ids, {:array, :string}
      add :sub_organization_name, :string, null: false
      add :email, :string, null: false
      add :authenticator_name, :string

      timestamps(type: :utc_datetime)
    end

    create unique_index(:users, [:email])
    create index(:users, [:sub_org_id])
    create index(:users, [:wallet_id])
    create index(:users, [:sub_organization_name])
    create index(:users, [:authenticator_name])
  end
end
