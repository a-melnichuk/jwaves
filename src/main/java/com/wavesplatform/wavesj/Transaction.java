package com.wavesplatform.wavesj;

import com.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.OpportunisticCurve25519Provider;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.wavesplatform.wavesj.Asset.isWaves;

public class Transaction {
    private static final byte ISSUE        = 3;
    private static final byte TRANSFER     = 4;
    private static final byte REISSUE      = 5;
    private static final byte BURN         = 6;
    private static final byte LEASE        = 8;
    private static final byte LEASE_CANCEL = 9;
    private static final byte ALIAS        = 10;

    private static final int MIN_BUFFER_SIZE = 120;
    private static final Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);

    public final String id;
    public final String signature;
    public final Map<String, Object> data;
    final String endpoint;

    private Transaction(PrivateKeyAccount account, ByteBuffer buffer, String endpoint, Object... items) {
        byte[] bytes = toBytes(buffer);
        this.id = hash(bytes); // base58_encode(blake2b256(bytes))
        this.signature = sign(account, bytes); // base58_encode(sign(randomBytes(64), privkey, message))

        System.out.println(
                        "Tx constructor:\n" +
                        "id: " + id + "\n" +
                        "signature: " + signature + "\n");

        this.endpoint = endpoint;

        HashMap<String, Object> map = new HashMap<>();
        for (int i=0; i<items.length; i+=2) {
            Object value = items[i+1];
            if (value != null) {
                map.put((String) items[i], value);
            }
        }
        this.data = Collections.unmodifiableMap(map);
    }

    public String getJson() {
        HashMap<String, Object> toJson = new HashMap<>(data);
        toJson.put("id", id);
        toJson.put("signature", signature);
        try {
            return new ObjectMapper().writeValueAsString(toJson);
        } catch (JsonProcessingException e) {
            // not expected to ever happen
            return null;
        }
    }

    private static byte[] toBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.position()];
        buffer.position(0);
        buffer.get(bytes);
        return bytes;
    }

    private static String hash(byte[] bytes) {
        return Base58.encode(Hash.hash(bytes, 0, bytes.length, Hash.BLAKE2B256));
    }

    private static String sign(PrivateKeyAccount account, byte[] bytes) {
        try {
            Field providerField = Curve25519.class.getDeclaredField("provider");
            providerField.setAccessible(true);
            OpportunisticCurve25519Provider provider = (OpportunisticCurve25519Provider) providerField.get(cipher);
            int[] randomIntArray = new int[] {64, 25, 196, 67, 26, 66, 35, 88, 53, 188, 8, 63, 113, 126, 31,
                    86, 171, 157, 191, 214, 52, 143, 63, 122, 165, 123, 194, 156, 225, 103, 87, 96, 79, 107, 185, 79, 45, 250, 141, 192, 120, 121, 20, 51, 47, 162, 221, 228, 84, 65, 72, 115, 183, 60, 13, 222, 232, 81, 254, 44, 109, 231, 121, 228};

            byte[] random = Util.arrayOfBytes(randomIntArray); // provider.getRandom(64);
            byte[] signature = provider.calculateSignature(random, account.getPrivateKey(), bytes);
            System.out.println(
                    "---Mocked signature---" + "\n" +
                    "provider: " + provider + "\n" +
                    "calculateSignature:\n" +
                    "random: " + Util.listOfUByte(random) + "\n" +
                    "private key: " + Util.hexString(account.getPrivateKey()) + "\n" +
                    "message: " + Util.hexString(bytes) + "\n" +
                    "signature: " + Util.hexString(signature) + " len: " + signature.length +  "\n");


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        System.out.println(
                "Tx singing:\n" +
                "private key: " + Util.hexString(account.getPrivateKey()) + "\n" +
                "signature hex bytes: " + Util.hexString(bytes) + "\n");

        return Base58.encode(cipher.calculateSignature(account.getPrivateKey(), bytes));
    }

    static String sign(PrivateKeyAccount account, ByteBuffer buffer) {
        return sign(account, toBytes(buffer));
    }

    private static void putAsset(ByteBuffer buffer, String assetId) {
        if (isWaves(assetId)) {
            buffer.put((byte) 0);
        } else {
            buffer.put((byte) 1).put(Base58.decode(assetId));
        }
    }

    public static Transaction makeIssueTx(
            PrivateKeyAccount account,
            String name,
            String description,
            long quantity,
            int decimals,
            boolean reissuable,
            long fee)
    {
        long timestamp = System.currentTimeMillis();
        int desclen = description == null ? 0 : description.length();
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE + name.length() + desclen);
        buf
                .put(ISSUE)
                .put(account.getPublicKey())
                .putShort((short) name.length())
                .put(name.getBytes())
                .putShort((short) desclen);
        if (desclen > 0) {
            buf.put(description.getBytes());
        }
        buf
                .putLong(quantity)
                .put((byte) decimals)
                .put((byte) (reissuable ? 1 : 0))
                .putLong(fee).putLong(timestamp);

       return new Transaction(account,
               buf,
               "/assets/broadcast/issue",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "name", name,
                "description", description,
                "quantity", quantity,
                "decimals", decimals,
                "reissuable", reissuable,
                "fee", fee,
                "timestamp", timestamp);
    }

    public static Transaction makeReissueTx(PrivateKeyAccount account, String assetId, long quantity, boolean reissuable, long fee) {
        if (isWaves(assetId)) {
            throw new IllegalArgumentException("Cannot reissue WAVES");
        }
        long timestamp = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE);
        buf.put(REISSUE).put(account.getPublicKey()).put(Base58.decode(assetId)).putLong(quantity)
                .put((byte) (reissuable ? 1 : 0))
                .putLong(fee).putLong(timestamp);
        return new Transaction(account, buf, "/assets/broadcast/reissue",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "assetId", assetId,
                "quantity", quantity,
                "reissuable", reissuable,
                "fee", fee,
                "timestamp", timestamp);
    }

    public static Transaction makeTransferTx(
            PrivateKeyAccount account,
            String toAddress,
            long amount,
            String assetId,
            long fee,
            String feeAssetId,
            String attachment)
    {
        byte[] attachmentBytes = (attachment == null ? "" : attachment).getBytes();
        int datalen =
                (isWaves(assetId) ? 0 : 32) +
                (isWaves(feeAssetId) ? 0 : 32) +
                attachmentBytes.length + MIN_BUFFER_SIZE;

        long timestamp = 1521393409455L;//System.currentTimeMillis();

        ByteBuffer buf = ByteBuffer.allocate(datalen);
        buf
                .put(TRANSFER)
                .put(account.getPublicKey());

        putAsset(buf, assetId);
        putAsset(buf, feeAssetId);
        System.out.println("assets");
        Util.printHexString(buf.array());
        buf
                .putLong(timestamp)
                .putLong(amount)
                .putLong(fee)
                .put(Base58.decode(toAddress))
                .putShort((short) attachmentBytes.length)
                .put(attachmentBytes);

        System.out.println(
                    "\n--------------------\n" +
                    "Tx buffer:\n" +
                    "timestamp: " + timestamp + "\n" +
                    "senderPublicKey: " + Base58.encode(account.getPublicKey()) + "\n" +
                    "buffer:");
        Util.printHexString(buf.array());

        return new Transaction(
                account, buf,
                "/assets/broadcast/transfer",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "recipient", toAddress,
                "amount", amount,
                "assetId", Asset.toJsonObject(assetId),
                "fee", fee,
                "feeAssetId", Asset.toJsonObject(feeAssetId),
                "timestamp", timestamp,
                "attachment", Base58.encode(attachmentBytes));
    }

    public static Transaction makeBurnTx(PrivateKeyAccount account, String assetId, long amount, long fee) {
        if (isWaves(assetId)) {
            throw new IllegalArgumentException("Cannot burn WAVES");
        }
        long timestamp = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE);
        buf
                .put(BURN)
                .put(account.getPublicKey())
                .put(Base58.decode(assetId))
                .putLong(amount)
                .putLong(fee)
                .putLong(timestamp);

        return new Transaction(account, buf,"/assets/broadcast/burn",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "assetId", assetId,
                "quantity", amount,
                "fee", fee,
                "timestamp", timestamp);
    }

    public static Transaction makeLeaseTx(PrivateKeyAccount account, String toAddress, long amount, long fee) {
        long timestamp = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE);
        buf.put(LEASE).put(account.getPublicKey()).put(Base58.decode(toAddress))
                .putLong(amount).putLong(fee).putLong(timestamp);
        return new Transaction(account, buf,"/leasing/broadcast/lease",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "recipient", toAddress,
                "amount", amount,
                "fee", fee,
                "timestamp", timestamp);
    }

    public static Transaction makeLeaseCancelTx(PrivateKeyAccount account, String txId, long fee) {
        long timestamp = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE);
        buf.put(LEASE_CANCEL).put(account.getPublicKey()).putLong(fee).putLong(timestamp).put(Base58.decode(txId));
        return new Transaction(account, buf,"/leasing/broadcast/cancel",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "txId", txId,
                "fee", fee,
                "timestamp", timestamp);
    }

    public static Transaction makeAliasTx(PrivateKeyAccount account, String alias, char scheme, long fee) {
        long timestamp = System.currentTimeMillis();
        int aliaslen = alias.length();
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE + aliaslen);
        buf.put(ALIAS).put(account.getPublicKey())
                .putShort((short) (alias.length() + 4)).put((byte) 0x02).put((byte) scheme)
                .putShort((short) alias.length()).put(alias.getBytes()).putLong(fee).putLong(timestamp);
        return new Transaction(account, buf,"/alias/broadcast/create",
                "senderPublicKey", Base58.encode(account.getPublicKey()), "alias", alias,
                "fee", fee,
                "timestamp", timestamp);
    }

    public static Transaction makeOrderTx(PrivateKeyAccount account, String matcherKey, Order.Type orderType,
            AssetPair assetPair, long price, long amount, long expiration, long matcherFee)
    {
        long timestamp = System.currentTimeMillis();
        int datalen = MIN_BUFFER_SIZE +
                (isWaves(assetPair.amountAsset) ? 0 : 32) +
                (isWaves(assetPair.priceAsset) ? 0 : 32);
        if (datalen == MIN_BUFFER_SIZE) {
            throw new IllegalArgumentException("Both spendAsset and receiveAsset are WAVES");
        }
        ByteBuffer buf = ByteBuffer.allocate(datalen);
        buf.put(account.getPublicKey()).put(Base58.decode(matcherKey));
        putAsset(buf, assetPair.amountAsset);
        putAsset(buf, assetPair.priceAsset);
        buf.put((byte) orderType.ordinal()).putLong(price).putLong(amount)
                .putLong(timestamp).putLong(expiration).putLong(matcherFee);

        return new Transaction(account, buf,"/matcher/orderbook",
                "senderPublicKey", Base58.encode(account.getPublicKey()),
                "matcherPublicKey", matcherKey,
                "assetPair", assetPair.toJsonObject(),
                "orderType", orderType.toJson(),
                "price", price,
                "amount", amount,
                "timestamp", timestamp,
                "expiration", expiration,
                "matcherFee", matcherFee);
    }

    public static Transaction makeOrderCancelTx(PrivateKeyAccount account, AssetPair assetPair, String orderId) {
        ByteBuffer buf = ByteBuffer.allocate(MIN_BUFFER_SIZE);
        buf.put(account.getPublicKey()).put(Base58.decode(orderId));
        return new Transaction(account, buf,"/matcher/orderbook/" + assetPair.amountAsset + '/' + assetPair.priceAsset + '/' + "cancel",
                "sender", Base58.encode(account.getPublicKey()),
                "orderId", orderId);
    }
}
