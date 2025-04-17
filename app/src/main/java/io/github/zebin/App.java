/*
 * This source file was generated by the Gradle 'init' task
 */
package io.github.zebin;

import io.github.andreyzebin.gitSql.cache.FileManagerCacheProxy;
import io.github.andreyzebin.gitSql.cache.GitFsCacheProxy;
import io.github.andreyzebin.gitSql.config.ConfigTree;
import io.github.andreyzebin.gitSql.config.ConfigVersions;
import io.github.andreyzebin.gitSql.config.RequestTree;
import io.github.andreyzebin.gitSql.git.GitAuth;
import io.github.andreyzebin.gitSql.git.GitConfigurations;
import io.github.andreyzebin.gitSql.git.GitFs;
import io.github.andreyzebin.gitSql.git.RemoteOrigin;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.BashUtils;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import io.github.zebin.javabash.sandbox.WorkingDirectory;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class App {
    public static final String IO_GITHUB_VEZUVIO = "io.github.vezuvio";

    public static final String ORIGINS_CURRENT = "origins.current";
    public static final String CREDENTIALS_CURRENT = "credentials.current";
    public static final String BRANCHES_CURRENT = "branches.current";
    public static final String LEAFS_CURRENT = "leafs.current";
    private FileManager fm;
    private final Consumer<String> stdOUT;
    private final Consumer<String> stdERR;
    private final Configurations conf;

    public App(Consumer<String> stdOUT, Consumer<String> stdERR, Configurations conf) {
        this.stdOUT = stdOUT;
        this.stdERR = stdERR;
        this.conf = conf;
    }

    public static String fixWinPath(String corruptWinPath) {
        String[] split = corruptWinPath.split(":");
        boolean hasDisk = split.length > 1;
        // boolean isAbsolute = hasDisk;

        if (hasDisk) {
            String disk = split[0];
            String path = split[1];
            return "/" + disk.toLowerCase() + path;
        } else {
            return corruptWinPath;
        }
    }

    public static void main(String[] args) {
        TextTerminal terminal = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
        );
        FileManager fm = new FileManager(terminal);
        String workingDirOverride = System.getProperty(IO_GITHUB_VEZUVIO + ".workingDirectory");
        PosixPath wd;
        if (workingDirOverride != null) {
            log.info("Working directory is set via system property");
            wd = PosixPath.ofPosix(fixWinPath(workingDirOverride));
            fm.go(wd);
        } else {
            log.info("Working directory is set from java process");
            wd = fm.getCurrent();
        }

        Configurations cnf = new Configurations(wd, terminal, fm.dirExists(wd.climb(".vezuvio")) ?
                VirtualDirectoryTree.LOCAL :
                VirtualDirectoryTree.USER);
        new App(System.out::println, System.err::println, cnf).run(args);
    }

    public static <T> Stream<T> lastElements(Stream<T> l, int n) {
        LinkedList<T> ll = new LinkedList<>();

        l.forEach(el -> {
            ll.addFirst(el);

            if (ll.size() > n) {
                ll.removeLast();
            }
        });

        return ll.stream();
    }

    public void run(String[] args) {
        TextTerminal terminal = conf.getTerm();
        fm = new FileManager(terminal);
        String os = terminal.eval("echo $(uname)");

        if (test(args, "origins", "use", "*")) {
            setConf(ORIGINS_CURRENT, args);
        } else if (test(args, "credentials", "use", "*")) {
            setConf(CREDENTIALS_CURRENT, args);
        } else if (test(args, "branches", "use", "*")) {
            setConf(BRANCHES_CURRENT, args);
        } else if (test(args, "leafs", "use", "*")) {
            setConf(LEAFS_CURRENT, args);
        } else if (test(args, "leafs", "drop")) {
            conf.getConf().deleteProperty(VirtualDirectoryTree.USER, IO_GITHUB_VEZUVIO + "." + LEAFS_CURRENT);
        } else if (test(args, "branches", "list")) {
            withRequestTree(rt -> {
                rt.listBranches().forEach(stdOUT::accept);
            });
        } else if (test(args, "leafs", "list")) {
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                rt.getBranch(cBranch).getLeafs().map(PosixPath::toString).forEach(stdOUT);
            });
        } else if (test(args, "changes", "explode")) {
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                ConfigVersions cBr = rt.getBranch(cBranch);
                cBr.getExplodedChanges(rt.getOffset(cBranch), cBr.topVersion().get().getVersionHash())
                        .entrySet()
                        .stream()
                        .map(ce -> String.format("%s %s: %s -> %s",
                                ce.getKey().getKey(),
                                ce.getKey().getValue(),
                                ce.getValue().getBefore(),
                                ce.getValue().getAfter()))
                        .forEach(stdOUT);
            });
        } else if (test(args, "changes", "list")) {
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                ConfigVersions cBr = rt.getBranch(cBranch);
                cBr.getChanges(rt.getOffset(cBranch), cBr.topVersion().get().getVersionHash())
                        .entrySet()
                        .stream()
                        .map(ce -> String.format("%s %s: %s -> %s",
                                ce.getKey().getKey(),
                                ce.getKey().getValue(),
                                ce.getValue().getBefore(),
                                ce.getValue().getAfter()))
                        .forEach(stdOUT);
            });
        } else if (test(args, "changes", "merge")) {
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                ConfigVersions cBr = rt.getBranch(cBranch);
                rt.getTrunk().pull();
                rt.merge(cBranch, cBr.topVersion().get().getVersionHash());
            });
        } else if (test(args, "branches", "which")) {
            stdOUT.accept(getCOnf(BRANCHES_CURRENT));
        } else if (test(args, "leafs", "which")) {
            stdOUT.accept(getCOnf(LEAFS_CURRENT));
        } else if (test(args, "credentials", "which")) {
            stdOUT.accept(getCOnf(CREDENTIALS_CURRENT));
        } else if (test(args, "origins", "which")) {
            stdOUT.accept(getCOnf(ORIGINS_CURRENT));
        } else if (test(args, "properties", "list", "--format=lSPkEQv") ||
                test(args, "properties", "list")) {
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                        ConfigVersions branch = rt.getBranch(cBranch);
                        branch.getProperties(branch.topVersion().get().getVersionHash())
                                .forEach((k, v) -> stdOUT.accept(k.getKey() + " " + k.getValue() + "=" + v));
                    }
            );
        } else if (test(args, "properties", "explode", "--format=lSPkEQv") ||
                test(args, "properties", "explode")) {
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                ConfigVersions branch = rt.getBranch(cBranch);
                branch.getExplodedState(branch.topVersion().get().getVersionHash())
                        .forEach((key, value) -> stdOUT.accept(key.getKey() + " " + key.getValue() + "=" + value));
            });
        } else if (test(args, "properties", "*", "get")) {
            String cLeaf = getCOnf(LEAFS_CURRENT);
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                stdOUT.accept(rt.getBranch(cBranch).getEffectiveProperty(PosixPath.ofPosix(cLeaf), args[1]));
            });
        } else if (test(args, "properties", "*", "set", "*")) {
            String cLeaf = getCOnf(LEAFS_CURRENT);
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                ConfigVersions cBr = rt.getBranch(cBranch);
                cBr.setProperty(PosixPath.ofPosix(cLeaf), args[1], args[3]);
                cBr.commit();
                cBr.push();
            });
        } else if (test(args, "properties", "*", "delete")) {
            String cLeaf = getCOnf(LEAFS_CURRENT);
            String cBranch = getCOnf(BRANCHES_CURRENT);

            withRequestTree(rt -> {
                ConfigVersions cBr = rt.getBranch(cBranch);
                cBr.deleteProperty(PosixPath.ofPosix(cLeaf), args[1]);
                cBr.commit();
                cBr.push();
            });
        } else if (test(args, "--version")) {
            stdOUT.accept(System.getProperty(IO_GITHUB_VEZUVIO + ".version"));
        } else if (test(args, "--system.properties")) {
            System.getProperties()
                    .forEach((key, value) ->
                            stdOUT.accept(String.format("%s=%s", key, value)));
        }
    }

    private void setConf(String originsCurrent, String[] args) {
        conf.getConf().setProperty(conf.getConfLevel(), IO_GITHUB_VEZUVIO + "." + originsCurrent, args[2]);
    }

    private String getCOnf(String prop) {
        return conf.getConf().getEffectiveProperty(conf.getConfLevel(), IO_GITHUB_VEZUVIO + "." + prop);
    }

    private boolean test(String[] args, String arg1, String... argsOther) {
        return Stream.concat(Stream.of(arg1), Stream.of(argsOther))
                .map(ArgsMatcher::exact)
                .toList()
                .equals(Arrays.stream(args).map(ArgsMatcher::escape).toList());
    }

    public static Path toPath(PosixPath pp) {
        String osNameLowercased = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osNameLowercased.startsWith("windows");
        if (isWindows) {
            if (pp.isAbsolute()) {
                PosixPath disk = pp;
                while (disk.length() > 1) {
                    disk = disk.descend();
                }
                return Path.of(disk.getEnd().toUpperCase() + ":\\")
                        .resolve(pp.relativize(disk).toString().replace("/", "\\"));
            }
        }
        return pp.toPath();
    }

    public static PosixPath ofPath(Path pp) {
        String osNameLowercased = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osNameLowercased.startsWith("windows");
        if (isWindows) {
            if (pp.isAbsolute()) {
                Path disk = pp.getRoot();
                return PosixPath.ofPosix("/" + disk.toString().toLowerCase().replace("\\", "")
                        .replace(":", "")).climb(PosixPath.of(disk.relativize(pp)));
            }
        }
        return PosixPath.of(pp);
    }

    public void withRequestTree(Consumer<RequestTree> consumer) {
        // master, request-001
        AtomicReference<String> cControl = new AtomicReference<>();
        FileManagerCacheProxy fm = FileManagerCacheProxy.cachedProxy(new FileManager(conf.getTerm()), cControl);

        Map<String, GitFs> cache = new HashMap<>();
        Map<PosixPath, ConfigVersions> cache2 = new HashMap<>();
        RequestTree rt = new RequestTree(
                branchName -> cache.computeIfAbsent(branchName, ss ->
                        {
                            RemoteOrigin remoteOrigin = new RemoteOrigin(
                                    getCOnf(ORIGINS_CURRENT),
                                    fm,
                                    getAuthStrategy(),
                                    branchName,
                                    new GitConfigurations() {
                                        @Override
                                        public Path getHomeTemporaryDir() {
                                            return toPath(conf.getVezuvioLocalHome().climb("tmp"));
                                        }
                                    },
                                    WorkingDirectory::new
                            );

                            GitFsCacheProxy gfs = GitFsCacheProxy.cachedProxy(remoteOrigin, cControl);
                            gfs.addListener(fm);
                            gfs.setBranch(branchName);
                            return gfs;
                        }

                ),
                gitVersions -> {
                    //fm.go(gitVersions.getLocation());
                    return cache2.computeIfAbsent(gitVersions.getLocation(), (k) -> {
                        ConfigTree ct = new ConfigTree(gitVersions.getDirectory());
                        return new ConfigVersions(gitVersions, ct);
                    });
                }, "master", fm);

        consumer.accept(rt);
    }

    private GitAuth getAuthStrategy() {
        String[] authVals = getCOnf(CREDENTIALS_CURRENT).split(":");
        String authType = authVals[0];
        if (authType.equals("ssh-agent")) {
            return GitAuth.ofSshAgent(authVals[1]);
        } else if (authType.equals("user-token-env")) {
            return AuthStrategy.userAndTokenEnv(authVals[1], authVals[2]);
        }

        throw new IllegalArgumentException("Unknown auth type: " + authType);
    }

}
