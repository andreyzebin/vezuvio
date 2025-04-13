package io.github.zebin;

import io.github.andreyzebin.gitSql.config.ConfigTree;
import io.github.andreyzebin.gitSql.git.*;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.sandbox.BashUtils;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import io.github.zebin.javabash.sandbox.WorkingDirectory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class TestGitUtils {


    /**
     * echo "# git-ops" >> README.md
     * git init
     * git add README.md
     * git commit -m "first commit"
     * git branch -M master
     * // git remote add origin https://github.com/andreyzebin/git-ops.git
     * // git push -u origin master
     */
    @Test
    public void test() {

        FunnyTerminal te = new FunnyTerminal(new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime())));
        FileManager fm = new FileManager(te);
        PosixPath repoMock = null;
        try {
            repoMock = mockRepo(fm, fm.getCurrent());
            fm.go(repoMock);

            try (
                    LocalSource ls = new LocalSource(repoMock.toPath(), te);
                    Writer stateJson = ls.getDirectory().put(PosixPath.ofPosix("state.json"))
            ) {
                String oldestHash = ls.listCommits()
                        .map(Commit::getHash)
                        .reduce((a, b) -> b)
                        .get();

                List<String> changes = ls.getChanges(oldestHash)
                        .map(Change::getFile)
                        .toList();

                Assertions.assertIterableEquals(
                        List.of(
                                "foo/bar/conf.properties",
                                "foo/conf.properties",
                                "foo/wow/conf.properties"
                        ),
                        changes
                );

                stateJson.write(
                        String.format("""
                                {
                                    "current_commit_hash": "%s",
                                    "lock": {
                                            "executor_url": "url1",
                                            "obtained_unix_ts": "1743228644"
                                    }
                                }
                                """, oldestHash)
                );

                stateJson.close();
                ls.commit();

                ConfigTree configTree = new ConfigTree(ls.getDirectory());
                configTree.getLeafs().map(PosixPath::toString).forEach(log::info);
                Assertions.assertIterableEquals(
                        List.of("foo",
                                "foo/bar",
                                "foo/wow"),
                        configTree.getLeafs().map(PosixPath::toString).toList()
                );

                Assertions.assertIterableEquals(
                        List.of("io.github.gitOps.location"),
                        configTree.getEffectivePropertyKeys(PosixPath.ofPosix("foo/bar"))
                );

                Assertions.assertEquals(
                        "foo/bar",
                        configTree.getEffectiveProperty(PosixPath.ofPosix("foo/bar"), "io.github.gitOps.location")
                );
                Optional<String> prev = configTree.setProperty(
                        PosixPath.ofPosix("foo/bar"),
                        "io.github.gitOps.location",
                        "foo/bar/upd"
                );
                Assertions.assertEquals("foo/bar", prev.get());
                Assertions.assertEquals(
                        "foo/bar/upd",
                        configTree.getEffectiveProperty(PosixPath.ofPosix("foo/bar"), "io.github.gitOps.location")
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (repoMock != null) {
                fm.remove(repoMock);
            }
        }
    }


    public static class MutableMock {
        private final String gitURL;
        private final PosixPath gitBackup;
        private final FileManager tt;
        private final PosixPath tmp;

        public MutableMock(String gitURL, PosixPath gitBackup, FileManager tt, PosixPath tmp) {
            this.gitURL = gitURL;
            this.gitBackup = gitBackup;
            this.tt = tt;
            this.tmp = tmp;
        }

        public String getURL() {
            return gitURL;
        }

        public void reset() {
            // tt.go(gitBackup);
            Stream<BranchHead> branches = GitBindings.getRemoteBranches(getURL(), tt.getTerminal(), false);

            branches.filter(br -> !br.getName().equals("master"))
                    .forEach(br -> {
                        try (RemoteOrigin remote = new RemoteOrigin(
                                getURL(),
                                tt.getTerminal(),
                                GitAuth.ofSshAgent("~/.ssh/zebin"),
                                "request-001",
                                new GitConfigurations() {
                                    @Override
                                    public Path getHomeTemporaryDir() {
                                        return tmp.toPath();
                                    }
                                }
                        )) {
                            remote.withTerminal(ttt -> {
                                ttt.eval(String.format("git push -d origin %s", br.getName()));
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });


            tt.go(gitBackup);

            // git config --get remote.origin.url
            // git remote -v
            String remotes = tt.getTerminal().eval("git remote -v");
            if (remotes.lines().findAny().isEmpty()) {
                tt.getTerminal().eval(String.format("git remote add origin %s", getURL()));
            }

            tt.getTerminal().eval("git push -f origin request-001");
            tt.getTerminal().eval("git push -f origin master");


        }
    }

    public static MutableMock mockRemote(PosixPath buildRoot, FileManager fm) {

        PosixPath tmp = buildRoot.climb("tmp");
        PosixPath testEnv = buildRoot.climb("test-environment", "git-server");
        PosixPath testEnvRepos = tmp.climb("repos");

        PosixPath mockRepo = mockRepo(fm, tmp);

        fm.removeDir(testEnvRepos);
        fm.makeDir(testEnvRepos);
        fm.go(testEnvRepos);
        fm.getTerminal().eval(String.format("git clone --bare %s myrepo.git", mockRepo));

        fm.go(testEnv);
        String eval = fm.getTerminal().eval("docker container ls --filter name=git-server");
        if (eval.lines().count() > 1) {
            fm.getTerminal().eval("docker compose restart");
        }
        fm.getTerminal().eval("docker compose up -d");

        fm.go(tmp);
        return new MutableMock("ssh://git@127.0.0.1:2222/git-server/repos/myrepo.git", mockRepo, fm, tmp);

    }

    public static PosixPath mockRepo(FileManager fm, PosixPath tmp) {
        PosixPath mock = tmp.climb("mock-repo");
        fm.remove(mock);
        fm.makeDir(mock);
        fm.go(mock);
        WorkingDirectory wd = new WorkingDirectory(fm, mock, (f) -> {
        });

        fm.getTerminal().eval("echo \"# git-ops\" >> README.md");
        log.info("Initializing git repo...");
        fm.getTerminal().eval("git init");
        log.info("Naming branch as master...");
        fm.getTerminal().eval("git branch -M master");
        fm.getTerminal().eval("git add README.md");
        fm.getTerminal().eval("git commit -m \"first commit\"");


        try (Writer confL2 = wd.put(PosixPath.ofPosix("foo/bar/conf.properties"));
             Writer confL1 = wd.put(PosixPath.ofPosix("foo/conf.properties"));
        ) {
            confL1.write("io.github.gitOps.location=foo");
            confL2.write("io.github.gitOps.location=foo/bar");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fm.getTerminal().eval("git add foo/bar/conf.properties");
        fm.getTerminal().eval("git add foo/conf.properties");
        fm.getTerminal().eval("git commit -m \"second commit\"");


        log.info("Branching request-001 on top...");
        fm.getTerminal().eval("git branch request-001 HEAD");


        log.info("Adding commit to master...");
        try (Writer confL1 = wd.put(PosixPath.ofPosix("foo/wow/conf.properties"));
        ) {
            confL1.write("io.github.gitOps.location=foo/wow");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fm.getTerminal().eval("git add foo/wow/conf.properties");
        fm.getTerminal().eval("git commit -m \"third commit\"");
        log.info("Finished with master!");
        log.info("Moving to request-001");

        fm.getTerminal().eval("git checkout request-001");
        try (Writer confL1 = wd.put(PosixPath.ofPosix("foo/bar/conf.properties"));
        ) {
            confL1.write("io.github.gitOps.location=foo/bar/request-001");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fm.getTerminal().eval("git add foo/bar/conf.properties");
        fm.getTerminal().eval("git commit -m \"request-001 first commit\"");

        log.info("Moving to master...");
        fm.getTerminal().eval("git checkout master");
        fm.go(tmp);
        return mock;
    }
}
