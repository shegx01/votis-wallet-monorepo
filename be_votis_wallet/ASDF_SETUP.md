# ASDF Version Management Setup

This project uses asdf to manage Elixir and Erlang versions for consistency across development environments.

## Installing asdf

### macOS (via Homebrew)
```bash
brew install asdf
echo -e "\n. /opt/homebrew/opt/asdf/libexec/asdf.sh" >> ~/.zshrc
source ~/.zshrc
```

### macOS (via Git)
```bash
git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.14.0
echo '. $HOME/.asdf/asdf.sh' >> ~/.zshrc
source ~/.zshrc
```

### Linux
```bash
git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.14.0
echo '. $HOME/.asdf/asdf.sh' >> ~/.bashrc
echo '. $HOME/.asdf/completions/asdf.bash' >> ~/.bashrc
source ~/.bashrc
```

## Installing Language Plugins

```bash
# Add Erlang and Elixir plugins
asdf plugin add erlang https://github.com/asdf-vm/asdf-erlang.git
asdf plugin add elixir https://github.com/asdf-vm/asdf-elixir.git
```

## Project Setup

This project includes a `.tool-versions` file that specifies:
- Erlang 28.0.2
- Elixir 1.18.4-otp-28

### Install Project Versions
```bash
# Navigate to project directory
cd be_votis_wallet

# Install versions specified in .tool-versions
asdf install

# Verify installation
asdf current
```

## Alternative Production Versions

For production stability, you might prefer slightly older versions:

```bash
# Install stable production versions
asdf install erlang 27.2
asdf install elixir 1.17.3-otp-27

# Set globally (optional)
asdf global erlang 27.2
asdf global elixir 1.17.3-otp-27
```

Update `.tool-versions` accordingly if using different versions:
```
erlang 27.2
elixir 1.17.3-otp-27
```

## Verification

After setup, verify everything works:

```bash
# Check versions
elixir --version
erl -version

# Install dependencies and run tests
mix deps.get
mix test
```

## Troubleshooting

### Common Issues

1. **Command not found**: Ensure asdf is properly sourced in your shell profile
2. **Plugin not found**: Make sure plugins are installed with `asdf plugin list`
3. **Version conflicts**: Use `asdf local` to set project-specific versions
4. **Build failures**: Install system dependencies for Erlang (see below)

### System Dependencies for Erlang

#### macOS
```bash
# Install build dependencies
brew install autoconf openssl wxmac libxslt fop
export LDFLAGS="-L$(brew --prefix openssl)/lib"
export CPPFLAGS="-I$(brew --prefix openssl)/include"
```

#### Ubuntu/Debian
```bash
sudo apt-get install build-essential autoconf m4 libncurses5-dev \
  libwxgtk3.0-gtk3-dev libwxgtk-webview3.0-gtk3-dev libgl1-mesa-dev \
  libglu1-mesa-dev libpng-dev libssh-dev unixodbc-dev xsltproc fop \
  libxml2-utils libncurses-dev openjdk-11-jdk
```

## IDE Integration

### VS Code with ElixirLS
ElixirLS will automatically detect asdf versions. Ensure your VS Code terminal uses the correct shell profile.

### IntelliJ/PhpStorm
Configure the Elixir plugin to use asdf-managed installations:
- Settings → Languages & Frameworks → Elixir
- Point to `~/.asdf/installs/elixir/<version>/bin/`

## Benefits of Using asdf

1. **Consistent environments** across team members
2. **Easy version switching** between projects
3. **No conflicts** between different Elixir/Erlang versions
4. **Simple upgrades** when new versions are released
5. **Project-specific versions** via `.tool-versions`

This ensures everyone on the team uses identical language runtime versions, reducing "works on my machine" issues.
