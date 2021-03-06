package com.gianlu.internethacker.models;


import com.gianlu.internethacker.Utils;
import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DnsQuestion implements DnsWritable {
    public final List<String> qname;
    public final QType qtype;
    public final QClass qclass;

    DnsQuestion(DnsInputStream in) {
        qname = in.readLabels();
        qtype = QType.parse(in.readShort());
        qclass = QClass.parse(in.readShort());
    }

    private DnsQuestion(List<String> qname, QType qtype, QClass qclass) {
        this.qname = qname;
        this.qtype = qtype;
        this.qclass = qclass;
    }

    @NotNull
    public String getDomainName() {
        return String.join(".", qname);
    }

    @NotNull
    public Builder buildUpon() {
        return new Builder(this);
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        out.writeLabels(qname);
        out.writeShort(qtype.val);
        out.writeShort(qclass.val);
    }

    public enum QType {
        A(1), NS(2), MD(3), MF(4), CNAME(5), SOA(6), MB(7), MG(8), MR(9),
        NULL(10), WKS(11), PTR(12), HINFO(13), MINFO(14), MX(15), TXT(16),
        AAAA(28), AXFR(252), MAILB(253), MAILA(254), ALL(255);

        private final short val;

        QType(int val) {
            this.val = (short) val;
        }

        @NotNull
        public static QType parse(int val) {
            for (QType type : values())
                if (type.val == val)
                    return type;

            throw new IllegalArgumentException("Unknown QTYPE for " + val);
        }
    }

    public enum QClass {
        IN(1),
        CH(3),
        HS(4),
        ANY(255);

        private final short val;

        QClass(int val) {
            this.val = (short) val;
        }

        @NotNull
        public static QClass parse(int val) {
            for (QClass clazz : values())
                if (clazz.val == val)
                    return clazz;

            throw new IllegalArgumentException("Unknown QCLASS for " + val);
        }
    }

    public static class Builder {
        private final List<String> qname = new ArrayList<>();
        private QType qtype;
        private QClass qclass;

        private Builder(DnsQuestion question) {
            this.qname.addAll(question.qname);
            this.qtype = question.qtype;
            this.qclass = question.qclass;
        }

        public Builder() {
        }

        public Builder setQName(String qname) {
            this.qname.clear();
            this.qname.addAll(Arrays.asList(Utils.split(qname, '.')));
            return this;
        }

        public Builder setQType(QType qtype) {
            this.qtype = qtype;
            return this;
        }

        public Builder setQClass(QClass qclass) {
            this.qclass = qclass;
            return this;
        }

        @NotNull
        public DnsQuestion build() {
            return new DnsQuestion(qname, qtype, qclass);
        }
    }
}
