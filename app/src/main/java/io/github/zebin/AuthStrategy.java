package io.github.zebin;

import io.github.andreyzebin.gitSql.git.GitAuth;
import io.github.zebin.javabash.process.TextTerminal;

import java.net.URI;

public class AuthStrategy {
    public static GitAuth userAndTokenEnv(final String username, final String token) {
        return new GitAuth() {
            public void beforeRemote(TextTerminal terminal) {
                GitAuth.openSecret(terminal, "GIT_USERNAME", System.getenv(username));
                GitAuth.openSecret(terminal, "GIT_TOKEN", System.getenv(token));
            }

            public void afterRemote(TextTerminal terminal) {
                // TODO close()
                // GitAuth.openSecret(terminal, "GIT_USERNAME", System.getenv(username));
                // GitAuth.openSecret(terminal, "GIT_TOKEN", System.getenv(token));
            }

            public String injectAuth(String origin) {
                URI uri = URI.create(origin);
                return uri.getScheme() + "://" +
                        "$(curl -s -w '%{url_effective}\\n' -G / --data-urlencode \"=$GIT_USERNAME\" | cut -c 3-)" + ":" +
                        "$(curl -s -w '%{url_effective}\\n' -G / --data-urlencode \"=$GIT_TOKEN\" | cut -c 3-)" + "@"
                        + uri.getAuthority() + uri.getPath();
            }
        };
    }
}
