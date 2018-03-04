package com;

import com.wavesplatform.wavesj.Account;
import com.wavesplatform.wavesj.PrivateKeyAccount;

public class Main {
  public static void main(String[] args) {

    String seed = "health lazy lens fix dwarf salad breeze myself silly december endless rent faculty report beyond";
    PrivateKeyAccount account = PrivateKeyAccount.fromSeed(seed, 0, Account.TESTNET);
    byte[] publicKey = account.getPublicKey();
    String address = account.getAddress();
    Util.printByteArray(publicKey);
    Util.printUByteArray(publicKey);
    System.out.println("HELLO");
  }
}