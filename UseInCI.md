# How to use this plugin in a CI environment

Make sure you have got ***credentials***. If not, read [UseInLocalProject.md](UseInLocalProject.md).

Just like in local projects, ensure GnuPG installed and ***credentials*** is set.

## GitHub Actions workflow example

1. Add ***credentials*** to GitHub project secrets with name `PUBLICATION_CREDENTAILS` on `$projectUrl/settings/secrets/actions` or GitHub organization secrets on `$organizationUrl/settings/secrets/actions`
2. Add this file to `.github/workflows/publish.yml`

```yaml
name: Publish to Maven Central

# Publish when new release created
on:
  release:
    types:
      - created

jobs:
  build-on-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v1
        with:
          java-version: 1.8
      - run: chmod +x gradlew
      - run: ./gradlew clean
      - run: ./gradlew build
      - run: ./gradlew checkPublicationCredentials
        env: 
          PUBLICATION_CREDENTIALS: ${{ secrets.PUBLICATION_CREDENTAILS }}  
      - run: ./gradlew publish
```

It can also run on Ubuntu or macOS.
