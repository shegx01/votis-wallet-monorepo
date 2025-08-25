defmodule BeVotisWallet.Users.UserTest do
  use BeVotisWallet.DataCase

  alias BeVotisWallet.Users.User

  describe "changeset/2" do
    test "valid changeset with required fields" do
      attrs = %{
        email: "user@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      changeset = User.changeset(%User{}, attrs)
      assert changeset.valid?
    end

    test "valid changeset with all fields" do
      attrs = %{
        email: "user@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization",
        wallet_id: "wallet_67890",
        root_user_ids: ["user_1", "user_2"],
        authenticator_name: "my-authenticator"
      }

      changeset = User.changeset(%User{}, attrs)
      assert changeset.valid?
    end

    test "invalid changeset when missing email" do
      attrs = %{
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      changeset = User.changeset(%User{}, attrs)
      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).email
    end

    test "invalid changeset when missing sub_org_id" do
      attrs = %{
        email: "user@example.com",
        sub_organization_name: "Test Organization"
      }

      changeset = User.changeset(%User{}, attrs)
      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).sub_org_id
    end

    test "invalid changeset when missing sub_organization_name" do
      attrs = %{
        email: "user@example.com",
        sub_org_id: "org_12345"
      }

      changeset = User.changeset(%User{}, attrs)
      refute changeset.valid?
      assert "can't be blank" in errors_on(changeset).sub_organization_name
    end

    test "invalid changeset with invalid email format" do
      attrs = %{
        email: "invalid-email",
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      changeset = User.changeset(%User{}, attrs)
      refute changeset.valid?
      assert "must be a valid email" in errors_on(changeset).email
    end

    test "invalid changeset with email too long" do
      long_email = String.duplicate("a", 250) <> "@example.com"

      attrs = %{
        email: long_email,
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      changeset = User.changeset(%User{}, attrs)
      refute changeset.valid?
      assert "should be at most 255 character(s)" in errors_on(changeset).email
    end

    test "invalid changeset with sub_organization_name too long" do
      long_name = String.duplicate("a", 256)

      attrs = %{
        email: "user@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: long_name
      }

      changeset = User.changeset(%User{}, attrs)
      refute changeset.valid?
      assert "should be at most 255 character(s)" in errors_on(changeset).sub_organization_name
    end
  end

  describe "update_changeset/2" do
    test "valid update changeset with allowed fields" do
      user = %User{
        email: "user@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      attrs = %{
        wallet_id: "wallet_67890",
        root_user_ids: ["user_1", "user_2"],
        authenticator_name: "my-authenticator"
      }

      changeset = User.update_changeset(user, attrs)
      assert changeset.valid?
    end

    test "update changeset ignores email changes" do
      user = %User{
        email: "user@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      attrs = %{
        email: "newemail@example.com",
        wallet_id: "wallet_67890"
      }

      changeset = User.update_changeset(user, attrs)
      assert changeset.valid?
      refute Map.has_key?(changeset.changes, :email)
    end

    test "invalid update changeset with authenticator_name too long" do
      user = %User{
        email: "user@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "Test Organization"
      }

      attrs = %{
        authenticator_name: String.duplicate("a", 256)
      }

      changeset = User.update_changeset(user, attrs)
      refute changeset.valid?
      assert "should be at most 255 character(s)" in errors_on(changeset).authenticator_name
    end
  end

  describe "get_by_email/1" do
    test "returns {:ok, user} when user exists" do
      user = insert(:user, email: "existing@example.com")

      assert {:ok, found_user} = User.get_by_email("existing@example.com")
      assert found_user.id == user.id
      assert found_user.email == user.email
    end

    test "returns {:ok, user} with case insensitive email" do
      user = insert(:user, email: "existing@example.com")

      assert {:ok, found_user} = User.get_by_email("EXISTING@EXAMPLE.COM")
      assert found_user.id == user.id
    end

    test "returns {:error, :not_found} when user does not exist" do
      assert {:error, :not_found} = User.get_by_email("nonexistent@example.com")
    end
  end

  describe "create_user/1" do
    test "creates user with valid attrs" do
      attrs = %{
        email: "newuser@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "New Organization"
      }

      assert {:ok, user} = User.create_user(attrs)
      assert user.email == "newuser@example.com"
      assert user.sub_org_id == "org_12345"
      assert user.sub_organization_name == "New Organization"
    end

    test "creates user with email converted to lowercase" do
      attrs = %{
        email: "NEWUSER@EXAMPLE.COM",
        sub_org_id: "org_12345",
        sub_organization_name: "New Organization"
      }

      assert {:ok, user} = User.create_user(attrs)
      assert user.email == "newuser@example.com"
    end

    test "returns error with invalid attrs" do
      attrs = %{
        email: "invalid-email",
        sub_org_id: "org_12345"
      }

      assert {:error, changeset} = User.create_user(attrs)
      refute changeset.valid?
    end

    test "returns error when email already exists" do
      insert(:user, email: "existing@example.com")

      attrs = %{
        email: "existing@example.com",
        sub_org_id: "org_12345",
        sub_organization_name: "Organization"
      }

      assert {:error, changeset} = User.create_user(attrs)
      assert "has already been taken" in errors_on(changeset).email
    end
  end

  describe "update_user/2" do
    test "updates user with valid attrs" do
      user = insert(:user)
      attrs = %{wallet_id: "new_wallet_123"}

      assert {:ok, updated_user} = User.update_user(user, attrs)
      assert updated_user.wallet_id == "new_wallet_123"
    end

    test "returns error with invalid attrs" do
      user = insert(:user)
      attrs = %{authenticator_name: String.duplicate("a", 256)}

      assert {:error, changeset} = User.update_user(user, attrs)
      refute changeset.valid?
    end
  end

  describe "exists_by_email?/1" do
    test "returns true when user exists" do
      insert(:user, email: "existing@example.com")

      assert User.exists_by_email?("existing@example.com")
    end

    test "returns true with case insensitive email" do
      insert(:user, email: "existing@example.com")

      assert User.exists_by_email?("EXISTING@EXAMPLE.COM")
    end

    test "returns false when user does not exist" do
      refute User.exists_by_email?("nonexistent@example.com")
    end
  end
end
