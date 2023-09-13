package org.example.approach1;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Participant {
    private CompletableFuture<BigInteger> px;
    private BigInteger x;
    private CompletableFuture<BigInteger> X;
    private CompletableFuture<BigInteger> s;
    private String name;
    private int base = 10;

    public Participant(String name, BigInteger x) {
        long start = System.currentTimeMillis();
        this.name = name;
        this.px = new CompletableFuture<>();
        this.x = x;
        this.X = new CompletableFuture<>();
        this.s = new CompletableFuture<>(); // secret
        System.out.println("[x time] " + (System.currentTimeMillis() - start) + " ms");
    }

    public Participant(String name, BigInteger x, int base) {
        this(name, x);
        this.base = base;
    }

    public Thread calculateCandidateP() {
        Thread t = new Thread(() -> {
            long start = System.currentTimeMillis();
            BigInteger result = generatePCandidate(4096);
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

    private BigInteger generatePCandidate(int size) {
        BigInteger pCandidate;

        do {
            String bitString = generateRandomBitstring(size);
            pCandidate = new BigInteger(bitString, 2);

        } while (pCandidate.compareTo(this.x) <= 0);

        return pCandidate.nextProbablePrime();
    }

    public static String generateRandomBitstring (int len) {
        String response = "1";
        for(int i = 0; i < len - 1; i++){
            if( Math.random() > 0.5){
                response += "1";
            } else {
                response += "0";
            }
        }
        return response;
    }
}
