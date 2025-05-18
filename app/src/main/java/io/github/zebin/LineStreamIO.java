package io.github.zebin;

import io.github.andreyzebin.gitSql.FileSystemUtils;
import lombok.Setter;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class LineStreamIO {
    @Setter
    protected Consumer<String> stdOUT;
    @Setter
    protected Consumer<String> stdERR;

    public LineStreamIO() {
    }

    public LineStreamIO(Consumer<String> stdOUT, Consumer<String> stdERR) {
        this.stdOUT = stdOUT;
        this.stdERR = stdERR;
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

    static String anyWord() {
        return "*";
    }

    public static boolean test(String[] args, String arg1, String... argsOther) {
        List<ArgsMatcher> expectedArgs = Stream.concat(Stream.of(arg1), Stream.of(argsOther))
                .map(ArgsMatcher::exact)
                .toList();
        List<ArgsMatcher> testArgs = Arrays.stream(args).map(ArgsMatcher::escape).toList();
        return expectedArgs.equals(testArgs);
    }

    protected <T, V> void toHTMlState(
            Map<T, V> state,
            Comparator<Map.Entry<T, V>> rowComparator,
            StringBuffer bw,
            String cBranch,
            Consumer<Map.Entry<T, V>> rowRenderer,
            Predicate<Map.Entry<T, V>> rowFilter,
            String templateAddress,
            String baseBranch
    ) {
        state
                .entrySet()
                .stream()
                .filter(rowFilter)
                .sorted(rowComparator)
                .forEach(rowRenderer);

        InputStreamReader template = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(templateAddress)
        );

        String html = FileSystemUtils.loadFile(template).replace("$props", bw)
                .replace("$requestBranch", cBranch)
                .replace("$baseBranch", baseBranch);
        html.lines().forEach(stdOUT);
    }

    abstract void run(String[] args);
}
