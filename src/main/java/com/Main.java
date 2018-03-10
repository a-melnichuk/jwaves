package com;

import com.wavesplatform.wavesj.Account;
import com.wavesplatform.wavesj.PrivateKeyAccount;

public class Main {
  public static void main(String[] args) {

    String seed = "annual number shrimp enact sustain argue cram hire bridge light unique nominee few double sock search harbor slide mom pause column boat decade circle";
    //"health lazy lens fix dwarf salad breeze myself silly december endless rent faculty report beyond";
    System.out.println("TESTNET: " +  Account.TESTNET + " - " + ((byte) Account.TESTNET) + " U: "+ Byte.toUnsignedInt((byte) Account.TESTNET));
    System.out.println("MAINNET: " +  Account.MAINNET + " - " + ((byte) Account.MAINNET) + " U: "+ Byte.toUnsignedInt((byte) Account.MAINNET));
    PrivateKeyAccount account = PrivateKeyAccount.fromSeed(seed, 0, Account.MAINNET);
    byte[] publicKey = account.getPublicKey();
    String address = account.getAddress();
  }
}