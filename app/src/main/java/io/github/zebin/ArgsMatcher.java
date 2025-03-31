package io.github.zebin;

import java.util.Objects;

public class ArgsMatcher {
    public static final ArgsMatcher ANY = new ArgsMatcher(true, "*");
    private final boolean isWildCard;
    private final String template;

    private ArgsMatcher(boolean isWildCard, String template) {
        this.isWildCard = isWildCard;
        this.template = template;
    }

    public static ArgsMatcher exact(String s) {
        if (s.equals("*")) {
            return ANY;
        }
        return new ArgsMatcher(false, s);
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
        return Objects.equals(template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode("*");
    }
}
