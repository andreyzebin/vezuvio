# vezuvio


## Development
### Run from source
```bash
./gradlew test
./gradlew app:run --args='--version'
./gradlew app:run --args='list versions'
./gradlew app:run --args='list branches'
./gradlew app:run --args='use branch master'

./gradlew app:run --args='list leafs'
./gradlew app:run --args='use leaf foo/bar'

./gradlew app:run --args='list properties'
./gradlew app:run --args='set io.github.gitOps.location babara/kad/abara'
./gradlew app:run --args='get io.github.gitOps.location'
```

Executor APIs
```bash
./gradlew app:run --args='use branch master'
./gradlew app:run --args='use leaf foo/bar'
./gradlew app:run --args='unuse version'

# get current property value for executor
./gradlew app:run --args='get offset'
./gradlew app:run --args='use version 88b6a30e384cda9c'
./gradlew app:run --args='get property io.github.gitOps.location'

# executor checks for newer versions and decides to execute work with respect to newer
# property value io.github.gitOps.location
./gradlew app:run --args='list versions'
./gradlew app:run --args='use version 526d3bc58db'
./gradlew app:run --args='get property io.github.gitOps.location'
# do work

# done work. commit success offset
./gradlew app:run --args='use lock'


./gradlew app:run --args='commit offset 526d3bc58db'
./gradlew app:run --args='unuse lock'
./gradlew app:run --args='get offset'

./gradlew app:run --args='use version 526d3bc58db'
./gradlew app:run --args='get property io.github.gitOps.location'
./gradlew app:run --args='unuse version'

# return back
./gradlew app:run --args='use lock'
./gradlew app:run --args='commit offset 88b6a30e384cda9c'
./gradlew app:run --args='unuse lock'
./gradlew app:run --args='get offset'
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