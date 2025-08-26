[
  # Coveralls configuration
  #
  # This file allows you to customize coverage reporting behavior.
  # All options are optional - ExCoveralls works great with just the mix.exs config.

  # Coverage minimum threshold (optional)
  # minimum_coverage: 70

  # Skip files from coverage (optional)
  skip_files: [
    # Skip test files from coverage reports
    "test/",
    # Skip generated files
    "_build/",
    "deps/",
    # Skip configuration files that don't need coverage
    "lib/be_votis_wallet_web/endpoint.ex",
    "lib/be_votis_wallet_web/gettext.ex",
    "lib/be_votis_wallet/mailer.ex",
    "lib/be_votis_wallet/repo.ex"
  ]

  # Coverage output directory (optional, defaults to "cover/")
  # output_dir: "coverage/"

  # Whether to halt the build if coverage is below minimum (optional)
  # halt_on_failure: true

  # Custom coverage checker (optional)
  # custom_threshold: %{
  #   "lib/be_votis_wallet/services/" => 80,
  #   "lib/be_votis_wallet_web/controllers/" => 90
  # }
]
