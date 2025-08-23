defmodule BeVotisWallet.Test.Mocks do
  @moduledoc """
  Test mocks setup using Mox.

  This module defines mock implementations for behaviours used in testing.
  All mocks are defined here to ensure consistency across test files.
  """

  import Mox

  # Define mock for HTTP client behaviour
  defmock(BeVotisWallet.HTTPClient.Mock, for: BeVotisWallet.HTTPClient.Behaviour)

  def setup_mocks do
    # Allow mock to be used across multiple processes
    Mox.set_mox_from_context(BeVotisWallet.HTTPClient.Mock)
    :ok
  end
end
