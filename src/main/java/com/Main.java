package com;

import com.wavesplatform.wavesj.Account;
import com.wavesplatform.wavesj.PrivateKeyAccount;

public class Main {
  public static void main(String[] args) {

    String seed = "annual number shrimp enact sustain argue cram hire bridge light unique nominee few double sock search harbor slide mom pause column boat decade circle";
    //"health lazy lens fix dwarf salad breeze myself silly december endless rent faculty report beyond";
    PrivateKeyAccount account = PrivateKeyAccount.fromSeed(seed, 0, Account.TESTNET);
    byte[] publicKey = account.getPublicKey();
    String address = account.getAddress();
  }
}