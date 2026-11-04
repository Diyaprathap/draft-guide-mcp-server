# Contributing to Open Liberty guides

Our [Code of Conduct](https://github.com/OpenLiberty/open-liberty/blob/release/CODE_OF_CONDUCT.md) outlines expectations for all contributors.

## How to contribute

To contribute, open a pull request (PR) against the `prod` branch. The PR will be reviewed by the Open Liberty guides team.

For substantial changes, open an issue first to discuss the proposed change before submitting a PR.

## Building the guide locally

Clone the repository and run the finished application:

```bash
git clone https://github.com/openliberty/guide-mcp-server.git
cd guide-mcp-server/finish
./mvnw liberty:run
```

Run the integration tests:

```bash
./mvnw verify
```
