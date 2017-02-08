def test_terraform_exists(Command):
    version_result = Command("terraform --version")

    assert version_result.rc == 0
