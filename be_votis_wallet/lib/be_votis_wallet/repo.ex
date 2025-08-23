defmodule BeVotisWallet.Repo do
  use Ecto.Repo,
    otp_app: :be_votis_wallet,
    adapter: Ecto.Adapters.Postgres
end
