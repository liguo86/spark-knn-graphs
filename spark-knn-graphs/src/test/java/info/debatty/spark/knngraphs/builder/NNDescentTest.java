/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.debatty.spark.knngraphs.builder;

import info.debatty.java.graphs.NeighborList;
import info.debatty.spark.knngraphs.DistributedGraph;
import info.debatty.spark.knngraphs.JWSimilarity;
import info.debatty.spark.knngraphs.KNNGraphCase;
import info.debatty.spark.knngraphs.Node;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

/**
 *
 * @author Thibault Debatty
 */
public class NNDescentTest extends KNNGraphCase {

    private static final int K = 10;
    private static final int ITERATIONS = 5;
    private static final double SUCCESS_RATIO = 0.9;

    /**
     * Test of computeGraph method, of class NNDescent.
     * @throws java.lang.Exception if we cannot build the graph
     */
    public final void testComputeGraph() throws Exception {
        System.out.println("NNDescent");
        System.out.println("=========");


        JavaPairRDD<Node<String>, NeighborList> exact_graph = readSpamGraph();
        JavaRDD<Node<String>> nodes = exact_graph.keys();

        NNDescent<String> builder = new NNDescent<>();
        builder.setK(K);
        builder.setSimilarity(new JWSimilarity());
        builder.setMaxIterations(ITERATIONS);

        // Compute the graph and force execution
        JavaPairRDD<Node<String>, NeighborList> graph =
                builder.computeGraphFromNodes(nodes);
        graph.cache();

        // Perform tests
        assertEquals(nodes.count(), graph.count());
        assertEquals(K, graph.first()._2.size());


        long correct_edges = DistributedGraph.countCommonEdges(
                exact_graph, graph);

        System.out.printf("Found %d correct edges\n", correct_edges);

        int correct_threshold = (int) (nodes.count() * K * SUCCESS_RATIO);

        assertTrue(
                "Not enough correct edges: " + correct_edges,
                correct_edges >= correct_threshold);
    }
}
