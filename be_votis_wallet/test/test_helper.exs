ExUnit.start()
Ecto.Adapters.SQL.Sandbox.mode(BeVotisWallet.Repo, :manual)

# Load test mocks
Code.require_file("support/mocks.ex", __DIR__)
