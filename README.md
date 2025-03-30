# vezuvio


## Development
### Run from source
```bash
./gradlew test
./gradlew app:run --args='--version'
./gradlew app:run --args='list branches'
./gradlew app:run --args='use branch master'

./gradlew app:run --args='list leafs'
./gradlew app:run --args='use leaf foo/bar'

./gradlew app:run --args='list properties'

./gradlew app:run --args='set io.github.gitOps.location babara/kad/abara'
./gradlew app:run --args='get io.github.gitOps.location'
```

## Use
Requirements
- Operating system: Linux or macOs

### Install
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/andreyzebin/vezuvio/refs/heads/master/install)"
```
also, if you are using gradle 7.x.x
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/andreyzebin/vezuvio/refs/heads/gradle-7/install)"
```

Install for CI
```bash
CI=1 /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/andreyzebin/vezuvio/refs/heads/master/install)"
alias vezuvio="$VEZUVIO_HOME/bin/vezuvio"
```