package com;

import com.wavesplatform.wavesj.Account;
import com.wavesplatform.wavesj.Asset;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.Transaction;

public class Main {
  public static void main(String[] args) {
    char accountType = Account.TESTNET;
    String aTestSeed = "ocean snow scan slogan truth upset liquid acoustic wage play sausage problem dinosaur pretty topic surface uniform pole able silent soap there print exile";
    String bTestSeed = "zero render track search kid victory shell abuse merge quality royal clip ugly lyrics rough nation huge struggle hard exercise ball provide duty now";

    PrivateKeyAccount senderAccount = PrivateKeyAccount.fromSeed(aTestSeed, 0, accountType);
    PrivateKeyAccount recipientAccount = PrivateKeyAccount.fromSeed(bTestSeed, 0, accountType);

    byte[] senderPrivateKey = senderAccount.getPrivateKey();
    byte[] recipientPrivateKey = recipientAccount.getPrivateKey();

    byte[] senderPublicKey = senderAccount.getPublicKey();
    byte[] recipientPublicKey = recipientAccount.getPublicKey();

    String senderAddress = senderAccount.getAddress();
    String recipientAddress = recipientAccount.getAddress();

    Transaction tx = Transaction.makeTransferTx(
            senderAccount,
            recipientAddress,
            1_00000000,
            Asset.WAVES,
            100_000,
            Asset.WAVES,
            "");

    System.out.println(
                    "Account\n" + accountType +
                    "Sender\n" +
                    "Private Key\n" +
                    Util.hexString(senderPrivateKey) + "\n" +
                    "Public Key\n" +
                    Util.hexString(senderPublicKey) + "\n" +
                    "Address\n" +
                    senderAddress + "\n");

    System.out.println(
                    "Account\n" + accountType +
                    "Recipient\n" +
                    "Private Key\n" +
                    Util.hexString(recipientPrivateKey) + "\n" +
                    "Public Key\n" +
                    Util.hexString(recipientPublicKey) + "\n" +
                    "Address\n" +
                    recipientAddress);


    /*

    String seed = "annual number shrimp enact sustain argue cram hire bridge light unique nominee few double sock search harbor slide mom pause column boat decade circle";
    //"health lazy lens fix dwarf salad breeze myself silly december endless rent faculty report beyond";
    System.out.println("TESTNET: " +  Account.TESTNET + " - " + ((byte) Account.TESTNET) + " U: "+ Byte.toUnsignedInt((byte) Account.TESTNET));
    System.out.println("MAINNET: " +  Account.MAINNET + " - " + ((byte) Account.MAINNET) + " U: "+ Byte.toUnsignedInt((byte) Account.MAINNET));
    PrivateKeyAccount account = PrivateKeyAccount.fromSeed(seed, 0, Account.MAINNET);
    byte[] publicKey = account.getPublicKey();
    String address = account.getAddress();
    */
  }
}