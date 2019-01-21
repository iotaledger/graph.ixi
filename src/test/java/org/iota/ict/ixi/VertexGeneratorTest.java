package org.iota.ict.ixi;

import org.iota.ict.model.TransactionBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.iota.ict.ixi.utils.VertexGenerator.generateRandomVertex;

public class VertexGeneratorTest extends GraphTestTemplate {

    @Test
    public void invalidInputTest() {

        List<TransactionBuilder> builderList1 = generateRandomVertex(-3);
        Assert.assertEquals(0, builderList1.size());

        List<TransactionBuilder> builderList2 = generateRandomVertex(0);
        Assert.assertEquals(0, builderList2.size());

    }

    @Test
    public void generateExactlyOneTransactionWithMaximumEdges() {
        List<TransactionBuilder> builderList = generateRandomVertex(27);
        Assert.assertEquals(1, builderList.size());
    }

    @Test
    public void generateExactlyTwoTransactionWithOverflow() {
        List<TransactionBuilder> builderList = generateRandomVertex(28);
        Assert.assertEquals(2, builderList.size());
    }

    @Test
    public void generateExactlyTwoTransactionWithMaximumEdges() {
        List<TransactionBuilder> builderList = generateRandomVertex(54);
        Assert.assertEquals(2, builderList.size());
    }

    @Test
    public void generateExactlyThreeTransactionWithOverflow() {
        List<TransactionBuilder> builderList = generateRandomVertex(55);
        Assert.assertEquals(3, builderList.size());
    }

}
