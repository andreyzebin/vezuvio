# vezuvio


## Development
### Run from source
```bash
./gradlew test

./gradlew app:run -PisProduction=1 --args='--version'
./gradlew app:run -PisProduction=1 --args='--system.properties'
./gradlew app:run --args='origins use ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git'
./gradlew app:run --args='origins which'
./gradlew app:run --args='credentials use ssh-agent:~/.ssh/zebin'
./gradlew app:run --args='branches list'
./gradlew app:run --args='branches use master'
./gradlew app:run --args='leafs list'
./gradlew app:run --args='leafs use foo/bar'

./gradlew app:run --args='branches use request-001'
./gradlew app:run --args='changes list'

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

Executor APIs TODO
```bash
./gradlew app:run --args='state.origin.url use ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git'
./gradlew app:run --args='auth use ssh-agent:zebin'
./gradlew app:run --args='branch use master'
```

## Use
Requirements
- Operating system: Linux or macOs

### Install
```bash
bash -c "$(curl -fsSL https://raw.githubusercontent.com/andreyzebin/vezuvio/refs/heads/master/install)"
```
also, if you are using gradle 7.x.x
```bash
bash -c "$(curl -fsSL https://raw.githubusercontent.com/andreyzebin/vezuvio/refs/heads/gradle-7/install)"
```

Install for CI
```bash
curl -fsSL https://raw.githubusercontent.com/andreyzebin/vezuvio/refs/heads/master/install | bash
```
- Override repositories configuration with your company's artifactory
```bash
export CI=1
export IO_GITHUB_VESUVIUS_GRADLE_INIT="$(pwd)/.vezuvio/repository/test-environment/init.gradle.kts"
```