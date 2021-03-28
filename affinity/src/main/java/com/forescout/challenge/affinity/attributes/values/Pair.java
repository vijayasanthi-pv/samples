package com.forescout.challenge.affinity.attributes.values;

import java.util.Objects;

public class Pair<TypeFirst, TypeSecond> {

    private TypeFirst first = null;
    private TypeSecond second = null;

    public Pair() {
    }

    public Pair(TypeFirst first, TypeSecond second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Pair<TypeFirst, TypeSecond> o) {
        this.first = o.first;
        this.second = o.second;
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Pair<TypeFirst, TypeSecond> clone() {
        return new Pair<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().isAssignableFrom(this.getClass())) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Pair<?, ?> p = (Pair<?, ?>) obj;
        return Objects.equals(first, p.first)
                && Objects.equals(second, p.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair(" + first + ", " + second + ")";
    }

    public TypeFirst getFirst() {
        return first;
    }

    public void setFirst(final TypeFirst first) {
        this.first = first;
    }

    public TypeSecond getSecond() {
        return second;
    }

    public void setSecond(final TypeSecond second) {
        this.second = second;
    }
}
