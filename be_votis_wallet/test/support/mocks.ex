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
    # Setup mocks without global mode - each test process owns its own mock
    :ok
  end
end
