package org.iota.ixi.utils;

import org.iota.ict.model.Bundle;
import org.iota.ict.model.BundleBuilder;
import org.iota.ict.model.TransactionBuilder;
import org.iota.ict.utils.Trytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VertexGenerator {

    public static Bundle generateVertex() {

        BundleBuilder bundleBuilder = new BundleBuilder();
        List<TransactionBuilder> transactions = new ArrayList<>();

        TransactionBuilder t1 = new TransactionBuilder();
        t1.extraDataDigest = "MYDATA999999999999999999999999999999999999999999999999999999999999999999999999999"; // = HASH OF DATA TX
        t1.signatureFragments = Trytes.randomSequenceOfLength(27*81); // = EDGES TO OTHER VERICES
        System.out.println("T1: "  + t1.signatureFragments);

        TransactionBuilder t2 = new TransactionBuilder();
        t2.signatureFragments = Trytes.randomSequenceOfLength(27*81); // = EDGES TO OTHER VERICES
        System.out.println("T2: "  + t2.signatureFragments);

        TransactionBuilder t3 = new TransactionBuilder();
        t3.signatureFragments = Trytes.randomSequenceOfLength(27*81); // = EDGES TO OTHER VERICES
        t3.tag = "AAAAAAAAAAAAAAAAAAAAAAAAAAA";                       // = TAG (LAST TX)
        System.out.println("T3: "  + t3.signatureFragments);

        transactions.add(t1);
        transactions.add(t2);
        transactions.add(t3);
        Collections.reverse(transactions);
        bundleBuilder.append(transactions);

        Bundle bundle = bundleBuilder.build();
        return bundle;

    }

}
