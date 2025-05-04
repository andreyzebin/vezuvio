package io.github.zebin;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ArgsMatcher {
    public static final ArgsMatcher ANY = new ArgsMatcher(true, "*");
    private final boolean isWildCard;
    private final Set<String> in;

    private ArgsMatcher(boolean isWildCard, String template) {
        this.isWildCard = isWildCard;
        this.in = Set.of(template);
    }

    private ArgsMatcher(boolean isWildCard, Set<String> template) {
        this.isWildCard = isWildCard;
        this.in = template;
    }

    public static ArgsMatcher exact(String s) {
        if (s.equals("*")) {
            return ANY;
        }
        return new ArgsMatcher(false, Arrays.stream(s.split("\\|")).collect(Collectors.toSet()));
    }

    public static ArgsMatcher escape(String s) {
        return new ArgsMatcher(false, s);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ArgsMatcher that = (ArgsMatcher) o;
        if (isWildCard || that.isWildCard) {
            return true;
        }
        return in.containsAll(that.in);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode("*");
    }
}
