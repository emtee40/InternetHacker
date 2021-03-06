package com.gianlu.internethacker.models;

import com.gianlu.internethacker.io.DnsInputStream;
import com.gianlu.internethacker.io.DnsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gianlu
 */
public class DnsMessage implements DnsWritable {
    public final DnsHeaderWrapper header;
    public final List<DnsQuestion> questions;
    public final List<DnsResourceRecord> answers;
    public final List<DnsResourceRecord> authorities;
    public final List<DnsResourceRecord> additional;
    private final LabelsManager labelsManager = new LabelsManager();

    public DnsMessage(byte[] data) {
        DnsInputStream in = new DnsInputStream(labelsManager, data);

        DnsBareHeader bareHeader = new DnsBareHeader(in);

        questions = new ArrayList<>(bareHeader.qdcount);
        for (int i = 0; i < bareHeader.qdcount; i++)
            questions.add(new DnsQuestion(in));

        answers = new ArrayList<>(bareHeader.ancount);
        for (int i = 0; i < bareHeader.ancount; i++)
            answers.add(new DnsResourceRecord(in));

        authorities = new ArrayList<>(bareHeader.nscount);
        for (int i = 0; i < bareHeader.nscount; i++)
            authorities.add(new DnsResourceRecord(in));

        additional = new ArrayList<>(bareHeader.arcount);
        for (int i = 0; i < bareHeader.arcount; i++)
            additional.add(new DnsResourceRecord(in));

        header = DnsHeaderWrapper.parse(this, bareHeader);
    }

    private DnsMessage(DnsBareHeader header, List<DnsQuestion> questions, List<DnsResourceRecord> answers, List<DnsResourceRecord> authorities, List<DnsResourceRecord> additional) {
        this.questions = questions;
        this.answers = answers;
        this.authorities = authorities;
        this.additional = additional;
        this.header = DnsHeaderWrapper.parse(this, header);
    }

    @NotNull
    public DnsOutputStream write() {
        DnsOutputStream out = new DnsOutputStream(labelsManager);
        write(out);
        return out;
    }

    @Override
    public void write(@NotNull DnsOutputStream out) {
        header.write(out);

        for (DnsQuestion question : questions)
            question.write(out);

        for (DnsResourceRecord rr : answers)
            rr.write(out);

        for (DnsResourceRecord rr : authorities)
            rr.write(out);

        for (DnsResourceRecord rr : additional)
            rr.write(out);
    }

    @NotNull
    public DnsOutputStream createEmptyStream() {
        return new DnsOutputStream(labelsManager);
    }

    @NotNull
    public DnsInputStream createInputStream(byte[] rdata) {
        return new DnsInputStream(labelsManager, rdata);
    }

    @NotNull
    public Builder buildUpon() {
        return new Builder(this);
    }

    public static class Builder {
        private final List<DnsQuestion> questions = new ArrayList<>();
        private final List<DnsResourceRecord> answers = new ArrayList<>();
        private final List<DnsResourceRecord> authorities = new ArrayList<>();
        private final List<DnsResourceRecord> additional = new ArrayList<>();
        private DnsBareHeader header;

        private Builder(DnsMessage copy) {
            this.header = new DnsBareHeader(copy.header.header);
            this.questions.addAll(copy.questions);
            this.answers.addAll(copy.answers);
            this.authorities.addAll(copy.authorities);
            this.additional.addAll(copy.additional);
        }

        public Builder setHeader(DnsBareHeader header) {
            this.header = header;
            return this;
        }

        public Builder addQuestion(DnsQuestion question) {
            questions.add(question);
            return this;
        }

        public Builder addAnswer(DnsResourceRecord rr) {
            answers.add(rr);
            return this;
        }

        public Builder addAuhtority(DnsResourceRecord rr) {
            authorities.add(rr);
            return this;
        }

        public Builder addAdditional(DnsResourceRecord rr) {
            additional.add(rr);
            return this;
        }

        @NotNull
        public DnsMessage build() {
            if (header == null) throw new IllegalStateException("Missing header!");
            if (header.qdcount != questions.size()) throw new IllegalStateException();
            if (header.ancount != answers.size()) throw new IllegalStateException();
            if (header.nscount != authorities.size()) throw new IllegalStateException();
            if (header.arcount != additional.size()) throw new IllegalStateException();
            return new DnsMessage(header, questions, answers, authorities, additional);
        }
    }
}
