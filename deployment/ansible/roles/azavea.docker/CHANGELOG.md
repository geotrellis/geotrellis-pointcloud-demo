## 2.0.1
- Update dependency role spec format

## 2.0.0

- Add `docker_keyserver` variable so that users can provide a custom key server
  URI.
- Bump Docker Engine and `docker-py` versions.
- Remove AUFS as the default storage engine.

## 1.0.2

- Switch APT repository key server to `pgp.mit.edu`.

## 1.0.1

- Use port `80` for retrieving APT repository key.

## 1.0.0

- Use new `dockerproject.org` APT repository. Ensure that `lxc-docker*` is
  pruged before applying this role.
- Set default storage driver to `aufs`.

## 0.2.0

- Ensure Docker API client for Python is installed as part of the role.
- The default versions for Docker and `docker-py` require Ansible 1.9.2+.

## 0.1.1

- Add support for passing options to the Docker daemon via `docker_options`.

## 0.1.0

- Initial release.
