package com.wavesplatform.wavesj;

import com.Util;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.KeccakDigest;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.wavesplatform.wavesj.Hash.hash;

public class PublicKeyAccount implements Account {
    private static final Digest KECCAK256 = new KeccakDigest(256);

    private final char scheme;
    private final byte[] publicKey;
    private final String address;

    public PublicKeyAccount(byte[] publicKey, char scheme) {
        this.scheme = scheme;
        this.publicKey = publicKey;
        this.address = Base58.encode(address(publicKey, scheme));
        System.out.println("address base58: " + address);
    }

    public PublicKeyAccount(String publicKey, char scheme) {
        this(Base58.decode(publicKey), scheme);
    }

    public final byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }

    public final String getAddress() {
        return address;
    }

    public final char getScheme() {
        return scheme;
    }

    static byte[] secureHash(byte[] message, int ofs, int len) {
        byte[] blake2b = hash(message, ofs, len, Hash.BLAKE2B256);
        System.out.println("secureHash BLAKE2b len:" + blake2b.length);
        Util.printUByteArray(blake2b);
        byte[] keccak = hash(blake2b, 0, blake2b.length, KECCAK256);
        System.out.println("secureHash keccak len:" + keccak.length + " digest: " + KECCAK256.getDigestSize());
        Util.printUByteArray(keccak);
        return keccak;
    }

    private static byte[] address(byte[] publicKey, char scheme) {
        ByteBuffer buf = ByteBuffer.allocate(26);
        byte[] hash = secureHash(publicKey, 0, publicKey.length);
        buf.put((byte) 1).put((byte) scheme).put(hash, 0, 20);
        System.out.println("address hash1 len:" + hash.length + " scheme:" + scheme + " buflen: " + buf);
        Util.printUByteArray(buf.array());
        byte[] checksum = secureHash(buf.array(), 0, 22);
        System.out.println("address checksum len:" + checksum.length);
        Util.printUByteArray(checksum);
        buf.put(checksum, 0, 4);
        System.out.println("address final len:" + buf.array().length);
        Util.printUByteArray(buf.array());
        return buf.array();
    }
}
