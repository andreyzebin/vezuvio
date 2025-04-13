/*
 * This source file was generated by the Gradle 'init' task
 */
package io.github.zebin;

import io.github.andreyzebin.gitSql.config.ConfigTree;
import io.github.andreyzebin.gitSql.config.ConfigVersions;
import io.github.andreyzebin.gitSql.config.RequestTree;
import io.github.andreyzebin.gitSql.git.GitAuth;
import io.github.andreyzebin.gitSql.git.GitConfigurations;
import io.github.andreyzebin.gitSql.git.LocalSource;
import io.github.andreyzebin.gitSql.git.RemoteOrigin;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.BashUtils;
import io.github.zebin.javabash.sandbox.DirectoryTree;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class App {
    public static final String IO_GITHUB_VEZUVIO = "io.github.vezuvio";
    public static final String STATE_ORIGIN_URL = "state.origin.url";
    public static final String STATE_ORIGIN_AUTH = "state.origin.auth";
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

    public static void main(String[] args) {
        TextTerminal terminal = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
        );
        FileManager fm = new FileManager(terminal);

        new App(System.out::println,
                System.err::println,
                new Configurations(fm.getCurrent(), terminal)).run(args);
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
        //log.info("Working directory is {}", conf.getWorkDir());
        //log.info("Local home is {}", conf.getVezuvioLocalHome());


        String repoUrl = conf.getConf().getEffectiveProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + STATE_ORIGIN_URL);
        String curBanrch = conf.getConf().getEffectiveProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + BRANCHES_CURRENT);
        String authProp = conf.getConf().getEffectiveProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + STATE_ORIGIN_AUTH);

        String resourcesLocation = System.getProperty("VEZUVIO_resources_path");
        String repoLocation = System.getProperty("VEZUVIO_repository_location");

        fm.makeDir(PosixPath.ofPosix(resourcesLocation));
        fm.go(PosixPath.ofPosix(resourcesLocation));
        PosixPath home = fm.getCurrent();
        PosixPath resources = fm.makeDir(PosixPath.ofPosix("resources"));
        PosixPath mockRepo = PosixPath.ofPosix(repoLocation);

        try (LocalSource src = new LocalSource(mockRepo.toPath(), terminal)) {
            fm.go(mockRepo);
            DirectoryTree dt = src.getDirectory();
            ConfigTree ct = new ConfigTree(dt);
            ConfigVersions cf = new ConfigVersions(src, ct);

            String os = terminal.eval("echo $(uname)");
            log.debug("logger.root.level={}", System.getProperty("logger.root.level"));

            if (test(args, STATE_ORIGIN_URL, "use", "*")) {
                conf.getConf().setProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + STATE_ORIGIN_URL, args[2]);
                //src.branch().ifPresent(stdOUT);

            } else if (test(args, STATE_ORIGIN_AUTH, "use", "*")) {
                conf.getConf().setProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + STATE_ORIGIN_AUTH, args[2]);
                //src.branch().ifPresent(stdOUT);

            } else if (test(args, "branches", "use", "*")) {
                conf.getConf().setProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + BRANCHES_CURRENT, args[2]);
                //src.branch().ifPresent(stdOUT);

            } else if (test(args, "leafs", "use", "*")) {
                conf.getConf().setProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + LEAFS_CURRENT, args[2]);
            } else if (test(args, "branches", "list")) {
                withRequestTree(rt -> {
                    rt.listBranches().forEach(stdOUT::accept);
                });
            } else if (test(args, "leafs", "list")) {
                String cBranch = getCOnf(BRANCHES_CURRENT);

                withRequestTree(rt -> {
                    rt.getBranch(cBranch).getLeafs().map(PosixPath::toString).forEach(stdOUT);
                });
            } else if (test(args, "changes", "list")) {
                String cBranch = getCOnf(BRANCHES_CURRENT);

                withRequestTree(rt -> {
                    ConfigVersions cBr = rt.getBranch(cBranch);
                    cBr.getExplodedChanges(rt.getOffset(cBranch), cBr.topVersion().get().getVersionHash())
                            .entrySet()
                            .stream()
                            .map(ce -> String.format("[%s] %s: %s -> %s",
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
            } else if (test(args, "*", "which")) {
                String prop = getCOnf(args[0]);
                stdOUT.accept(prop);

            } else if (test(args, "properties", "list")) {
                String cLeaf = getCOnf(LEAFS_CURRENT);
                String cBranch = getCOnf(BRANCHES_CURRENT);

                withRequestTree(rt ->
                        rt.getBranch(cBranch).getEffectivePropertyKeys(PosixPath.ofPosix(cLeaf))
                                .forEach(cp -> stdOUT.accept(cp))
                );
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
            } else if (test(args, "--version")) {
                stdOUT.accept(System.getProperty("version"));
            }
        }
    }

    private boolean test(String[] args, String arg1, String... argsOther) {
        return Stream.concat(Stream.of(arg1), Stream.of(argsOther))
                .map(ArgsMatcher::exact)
                .toList()
                .equals(Arrays.stream(args).map(ArgsMatcher::escape).toList());
    }


    public void withRequestTree(Consumer<RequestTree> consumer) {
        // master, request-001
        Map<String, RemoteOrigin> cache = new HashMap<>();
        Map<PosixPath, ConfigVersions> cache2 = new HashMap<>();
        RequestTree rt = new RequestTree(
                branchName -> cache.computeIfAbsent(branchName, ss ->
                        {
                            RemoteOrigin remoteOrigin = new RemoteOrigin(
                                    getCOnf(STATE_ORIGIN_URL),
                                    conf.getTerm(),
                                    GitAuth.ofSshAgent(getCOnf(STATE_ORIGIN_AUTH).split(":")[1]),
                                    branchName,
                                    new GitConfigurations() {
                                        @Override
                                        public Path getHomeTemporaryDir() {
                                            return conf.getVezuvioLocalHome().climb("tmp").toPath();
                                        }
                                    });
                            remoteOrigin.setBranch(branchName);
                            return remoteOrigin;
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

    private String getCOnf(String stateOriginUrl) {
        return conf.getConf().getEffectiveProperty(VirtualDirectoryTree.RUNTIME, IO_GITHUB_VEZUVIO + "." + stateOriginUrl);
    }

}
