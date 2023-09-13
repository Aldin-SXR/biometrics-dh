package org.example.approach2;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Participant {
    private CompletableFuture<BigInteger> px;
    private BigInteger x;
    private String fingerprint;
    private BigInteger pCandidate;
    private CompletableFuture<BigInteger> X;
    private CompletableFuture<BigInteger> s;
    private String name;
    private int base = 16;

    public Participant(String name, String fingerprint) {
        long start = System.currentTimeMillis();
        this.name = name;
        this.px = new CompletableFuture<>();
        this.fingerprint = fingerprint;

        this.generateBitStrings();

        this.X = new CompletableFuture<>();
        this.s = new CompletableFuture<>(); // secret
        System.out.println("[x time] " + (System.currentTimeMillis() - start) + " ms");
    }

    public Participant(String name, String fingerprint, int base) {
        this(name, fingerprint);
        this.base = base;
    }

    public Thread calculateCandidateP() {
        Thread t = new Thread(() -> {
            long start = System.currentTimeMillis();
            BigInteger result = generatePCandidate();
            px.complete(result);
            System.out.println(this.name + "'s px:\t" + result.toString(this.base));
            System.out.println("[px time] " + (System.currentTimeMillis() - start) + " ms");
        });

        t.start();
        return t;
    }

    public Thread calculatePublicKey (BigInteger g, BigInteger p) {
        Thread t = new Thread(() -> {
            long start = System.currentTimeMillis();
            BigInteger result = g.modPow(this.x, p);
            System.out.println(this.name + "'s X:\t" + result.toString(this.base));
            X.complete(result);
            System.out.println("[X time] " + (System.currentTimeMillis() - start) + " ms");
        });

        t.start();
        return t;
    }

    public Thread calculateSharedSecret (BigInteger otherPublicKey, BigInteger p) {
        Thread t = new Thread(() -> {
            long start = System.currentTimeMillis();
            BigInteger result = otherPublicKey.modPow(this.x, p);
            System.out.println(this.name + "'s s:\t" + result.toString(this.base));
            s.complete(result);
            System.out.println("[s time] " + (System.currentTimeMillis() - start) + " ms");
        });

        t.start();
        return t;
    }

    public BigInteger getPx() throws ExecutionException, InterruptedException {
        return this.px.get();
    }

    public BigInteger getPublicKey() throws ExecutionException, InterruptedException {
        return this.X.get();
    }
    public BigInteger getSharedSecret() throws ExecutionException, InterruptedException {
        return this.s.get();
    }

    private void generateBitStrings() {
        // Split and shuffle the chunks
        String[] parts = this.fingerprint.split("(?<=\\G.{8})");
        String[] part1;
        String[] part2;
        do {
            List<String> chunks = Arrays.asList(parts);
            Collections.shuffle(chunks);
            chunks.toArray(parts);

            part1 = Arrays.copyOfRange(parts, 0, 64);
            part2 = Arrays.copyOfRange(parts, 65, 128);
        } while (part1[0].startsWith("0") || part2[0].startsWith("0"));

        String b1 = String.join("", part1);
        String b2 = String.join("", part2);

        System.out.println(b1);
        System.out.println(b2);

        // Select private key (smaller integer)
        BigInteger p1 = new BigInteger(b1, this.base);
        BigInteger p2 = new BigInteger(b2, this.base);

        if (p1.compareTo(p2) > 0) {
            this.x = p2;
            this.pCandidate = p1;
        } else {
            this.x = p1;
            this.pCandidate = p2;
        }
    }

    private BigInteger generatePCandidate() {
        if (this.pCandidate.isProbablePrime(100)) {
            return pCandidate;
        }
        return pCandidate.nextProbablePrime();
    }
}
