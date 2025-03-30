/*
 * This source file was generated by the Gradle 'init' task
 */
package io.github.zebin;

import io.github.andreyzebin.gitSql.config.ConfigTree;
import io.github.andreyzebin.gitSql.git.LocalSource;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.sandbox.BashUtils;
import io.github.zebin.javabash.sandbox.DirectoryTree;
import io.github.zebin.javabash.sandbox.FileManager;
import io.github.zebin.javabash.sandbox.PosixPath;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class App {


    public static void main(String[] args) {
        FunnyTerminal terminal = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
        );
        FileManager fm = new FileManager(terminal);
        fm.goUp(); // .../.vezuvio/repository
        fm.goUp(); // .../.vezuvio
        PosixPath home = fm.getCurrent();
        PosixPath resources = fm.makeDir(PosixPath.ofPosix("resources"));
        PosixPath mockRepo = PosixPath.ofPosix("/home/andrey/tmp/mock-repo");


        try (LocalSource src = new LocalSource(mockRepo.toPath(), terminal);) {
            fm.go(mockRepo);
            DirectoryTree dt = src.getDirectory();
            ConfigTree ct = new ConfigTree(dt);


            String os = terminal.eval("echo $(uname)");
            log.debug("logger.root.level={}", System.getProperty("logger.root.level"));

            if (test(args, "list", "leafs")) {
                ct.getLeafs().map(PosixPath::toString).forEach(log::info);
            } else if (test(args, "list", "branches")) {
                String branch = getCurrent(terminal, resources, "branch");

            } else if (test(args, "list", "properties")) {
                String branch = getCurrent(terminal, resources, "branch");
                PosixPath leaf = PosixPath.ofPosix(getCurrent(terminal, resources, "leaf"));
                log.debug("Current branch is {}", branch);
                log.debug("Current leaf is {}", leaf);

                ct.getPropertyKeys(leaf).forEach(log::info);
            } else if (test(args, "use", /* key */ "branch", /* value */ "*")) {
                setCurrent(args[2], fm, resources, terminal, "branch");

                log.info("Branch {} has been set", args[2]);
            } else if (test(args, "use", /* key */ "leaf", /* value */ "*")) {
                setCurrent(args[2], fm, resources, terminal, "leaf");

                log.info("Leaf {} has been set", args[2]);
            } else if (test(args, "set", /* key */ "*", /* value */ "*")) {
                String branch = getCurrent(terminal, resources, "branch");
                PosixPath leaf = PosixPath.ofPosix(getCurrent(terminal, resources, "leaf"));
                log.debug("Current branch is {}", branch);
                log.debug("Current leaf is {}", leaf);

                ct.setProperty(leaf, args[1], args[2]);
                log.info("Property {}#{} has been set", leaf, args[1]);
            } else if (test(args, "get", /* key */ "*")) {
                String branch = getCurrent(terminal, resources, "branch");
                PosixPath leaf = PosixPath.ofPosix(getCurrent(terminal, resources, "leaf"));
                log.debug("Current branch is {}", branch);
                log.debug("Current leaf is {}", leaf);

                log.info("{}", ct.getProperty(leaf, args[1]));
            } else if (test(args, "shellenv")) {
                terminal.eval(String.format("export VEZUVIO_HOME=\"%s\"", home));
            } else if (test(args, "--version")) {
                log.info("vezuvio {}", System.getProperty("version"));
            }

        }

    }

    private static void setCurrent(String value, FileManager fm, PosixPath resources, FunnyTerminal terminal, String domain) {
        fm.go(resources);
        PosixPath audioPrefix = PosixPath.ofPosix(domain);
        fm.remove(audioPrefix.climb("current"));
        terminal.eval(
                String.format(
                        "ln -s %s %s",
                        fm.makeDir(audioPrefix.climb(PosixPath.ofPosix(value))),
                        audioPrefix.climb("current")
                )
        );
    }

    private static String getCurrent(FunnyTerminal terminal, PosixPath resources, String domain) {
        PosixPath audioDevice = PosixPath.ofPosix(
                terminal.eval(String.format("readlink %s", resources.climb(domain, "current")))
        );
        return audioDevice.relativize(resources.climb(domain)).toString();
    }

    private static boolean test(String[] args, String arg1, String... argsOther) {
        return Stream.concat(Stream.of(arg1), Stream.of(argsOther))
                .map(StringMatcher::exact)
                .toList()
                .equals(Arrays.stream(args).map(StringMatcher::escape).toList());
    }

    public static class StringMatcher {
        public static final StringMatcher ANY = new StringMatcher(true, "*");
        private final boolean isWild;
        private final String template;

        private StringMatcher(boolean isWild, String template) {
            this.isWild = isWild;
            this.template = template;
        }

        public static StringMatcher exact(String s) {
            if (s.equals("*")) {
                return ANY;
            }
            return new StringMatcher(false, s);
        }

        public static StringMatcher escape(String s) {
            return new StringMatcher(false, s);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            StringMatcher that = (StringMatcher) o;
            if (isWild || that.isWild) {
                return true;
            }
            return Objects.equals(template, that.template);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode("*");
        }
    }
}
