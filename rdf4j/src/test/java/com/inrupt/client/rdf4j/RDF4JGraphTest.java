/*
 * Copyright 2022 Inrupt Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.inrupt.client.rdf4j;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.RDFNode;

import java.net.URI;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RDF4JGraphTest {

    private static RDF4JGraph rdf4jGraph;

    @BeforeAll
    static void setup() {
        final ModelBuilder builder = new ModelBuilder();

        builder.namedGraph(RDF4JTestModel.G_RDF4J)
                .subject(RDF4JTestModel.S_VALUE)
                    .add(RDF4JTestModel.P_VALUE, RDF4JTestModel.O_VALUE);

        builder.defaultGraph().subject(RDF4JTestModel.S1_VALUE).add(RDF4JTestModel.P_VALUE, RDF4JTestModel.O1_VALUE);

        final Model m = builder.build();
        rdf4jGraph = new RDF4JGraph(m);
    }

    @Test
    void testGetSubject() {
        assertAll("different versions of a subject",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> RDF4JGraph.getSubject(RDFNode.literal("subject"))
                );
                assertEquals("Subject cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(RDF4JTestModel.S_VALUE, RDF4JGraph.getSubject(RDF4JTestModel.S_RDFNode).toString()),
            () -> assertTrue(RDF4JGraph.getSubject(RDFNode.blankNode()).isBNode()),
            () -> assertTrue(RDF4JGraph.getSubject(RDFNode.blankNode("someID")).isBNode()),
            () -> assertEquals(
                "someID",
                RDF4JGraph.getSubject(RDFNode.blankNode("someID")).stringValue()
                ),
            () -> assertNull(RDF4JGraph.getSubject(null))
        );
    }

    @Test
    void testGetPredicate() {
        assertAll("different versions of a predicate",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> RDF4JGraph.getPredicate(RDFNode.literal("predicate"))
                );
                assertEquals("Predicate cannot be an RDF literal", exception.getMessage());
            },
            () -> assertEquals(RDF4JTestModel.P_VALUE, RDF4JGraph.getPredicate(RDF4JTestModel.P_RDFNode).toString()),
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> RDF4JGraph.getPredicate(RDFNode.blankNode())
                );
                assertEquals("Predicate cannot be a blank node", exception.getMessage());
            },
            () -> assertNull(RDF4JGraph.getPredicate(null))
        );
    }

    @Test
    void testGetObject() {
        assertAll("different versions of an object",
            () -> assertEquals(
                            "http://example.test/object",
                            RDF4JGraph.getObject(RDFNode.namedNode(URI.create("http://example.test/object"))).toString()
                ),
            () -> assertEquals("object", ((Literal)RDF4JGraph.getObject(RDFNode.literal("object"))).getLabel()),
            () -> assertTrue(RDF4JGraph.getObject(RDFNode.literal(
                                        "object",
                                        URI.create("http://www.w3.org/2004/02/skos/core#Concept")))
                                        .isLiteral()
                            ),
            () -> assertEquals(
                    "http://www.w3.org/2004/02/skos/core#Concept",
                    ((Literal)RDF4JGraph.getObject(
                                RDFNode.literal(
                                    "object",
                                    URI.create("http://www.w3.org/2004/02/skos/core#Concept"))
                                ))
                                .getDatatype()
                                .toString()),
            () -> assertEquals(
                    "en",
                    ((Literal)RDF4JGraph.getObject(RDFNode.literal("object", "en"))).getLanguage().get()
                ),
            () -> assertTrue(RDF4JGraph.getObject(RDFNode.blankNode()).isBNode()),
            () -> assertTrue(RDF4JGraph.getObject(RDFNode.blankNode("someID")).isBNode()),
            () -> assertEquals(
                "someID",
                RDF4JGraph.getObject(RDFNode.blankNode("someID")).stringValue()
                ),
            () -> assertNull(RDF4JGraph.getObject(null))
        );
    }

    @Test
    void testWithContextStream() {
        assertTrue(rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
            .findFirst().isPresent()
        );
        assertEquals(
            1,
            rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode).count()
        );
        assertEquals(
            RDF4JTestModel.P_RDFNode.getURI(),
            rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testEmptyContextStream() {
        assertTrue(
            rdf4jGraph.stream(
                RDF4JTestModel.S1_RDFNode,
                RDF4JTestModel.P_RDFNode,
                RDF4JTestModel.O1_RDFNode
            ).findFirst().isPresent()
        );
        assertEquals(
            1,
            rdf4jGraph.stream(RDF4JTestModel.S1_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O1_RDFNode).count()
        );
        assertEquals(
            RDF4JTestModel.P_RDFNode.getURI(),
            rdf4jGraph.stream(RDF4JTestModel.S1_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O1_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
    }

    @Test
    void testWithMoreContextStream() {
        assertEquals(2, rdf4jGraph.stream(null, RDF4JTestModel.P_RDFNode, null).count());
        assertEquals(2, rdf4jGraph.stream(null, null, null).count());
        assertTrue(rdf4jGraph.stream(null, RDF4JTestModel.P_RDFNode, null)
                            .map(r -> r.getSubject().getURI().toString())
                            .collect(Collectors.toList())
                            .contains(RDF4JTestModel.S_VALUE)
        );
        assertTrue(rdf4jGraph.stream(null, RDF4JTestModel.P_RDFNode, null)
                            .map(r -> r.getSubject().getURI().toString())
                            .collect(Collectors.toList())
                            .contains(RDF4JTestModel.S1_VALUE)
        );
        assertEquals(
            RDF4JTestModel.P_RDFNode.getURI(),
            rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getPredicate().getURI()
        );
        assertEquals(
            RDF4JTestModel.S_RDFNode.getURI(),
            rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getSubject().getURI()
        );
        assertEquals(
            RDF4JTestModel.O_RDFNode.getLiteral(),
            rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                .findFirst().get().getObject().getLiteral()
        );
    }

    @Test
    void testInvalidSubjectStream() {
        final RDFNode invalidSubject = RDFNode.literal("subject");

        assertAll("invalid subject in stream call",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> rdf4jGraph.stream(invalidSubject, RDF4JTestModel.P_RDFNode, RDF4JTestModel.O_RDFNode)
                );
                assertEquals("Subject cannot be an RDF literal", exception.getMessage());
            }
        );
    }

    @Test
    void testInvalidPredicateStream() {
        final RDFNode invalidPredicate = RDFNode.literal("predicate");

        assertAll("invalid predicate in stream call",
            () -> {
                final Throwable exception = assertThrows(IllegalArgumentException.class,
                    () -> rdf4jGraph.stream(RDF4JTestModel.S_RDFNode, invalidPredicate, RDF4JTestModel.O_RDFNode)
                );
                assertEquals("Predicate cannot be an RDF literal", exception.getMessage());
            }
        );
    }

    @Test
    void testNoParamsStream() {
        assertEquals(2, rdf4jGraph.stream().count());
        assertTrue(rdf4jGraph.stream()
                    .map(r -> r.getSubject().getURI().toString())
                    .collect(Collectors.toList())
                    .contains(RDF4JTestModel.S1_VALUE)
        );
    }

}