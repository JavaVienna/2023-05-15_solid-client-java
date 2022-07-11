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
package com.inrupt.client.jena;

import static org.apache.jena.graph.NodeFactory.getType;

import com.inrupt.client.rdf.Graph;
import com.inrupt.client.rdf.RDFNode;
import com.inrupt.client.rdf.Triple;

import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;

class JenaGraph implements Graph {

    private final Model model;

    public JenaGraph(final Model model) {
        this.model = model;
    }

    public Model asJenaModel() {
        return model;
    }

    @Override
    public Stream<Triple> stream(final RDFNode subject, final RDFNode predicate, final RDFNode object) {

        final var s = getSubject(subject);
        final var p = getPredicate(predicate);
        final var o = getObject(object);

        final var iter = model.getGraph().find(s, p, o);
        return Iter.asStream(iter).map(JenaTriple::new);
    }

    @Override
    public Stream<Triple> stream() {
        final var iter = model.getGraph().find();
        return Iter.asStream(iter).map(JenaTriple::new);
    }


    static Node getSubject(final RDFNode subject) {
        if (subject != null) {
            if (subject.isLiteral()) {
                throw new IllegalArgumentException("Subject cannot be an RDF literal");
            }

            if (subject.isNamedNode()) {
                return NodeFactory.createURI(subject.getURI().toString());
            }
            return NodeFactory.createBlankNode();
        }
        return Node.ANY;
    }

    static Node getPredicate(final RDFNode predicate) {
        if (predicate != null) {
            if (predicate.isLiteral()) {
                throw new IllegalArgumentException("Predicate cannot be an RDF literal");
            }

            if (predicate.isBlankNode()) {
                throw new IllegalArgumentException("Predicate cannot be a blank node");
            }

            return NodeFactory.createURI(predicate.getURI().toString());
        }
        return Node.ANY;
    }

    static Node getObject(final RDFNode object) {

        if (object != null) {
            if (object.isNamedNode()) {
                return NodeFactory.createURI(object.getURI().toString());
            } else if (object.isLiteral()) {
                if (object.getDatatype() != null) {
                    return NodeFactory.createLiteral(object.getLiteral(), getType(object.getDatatype().toString()));
                } else if (object.getLanguage() != null) {
                    return NodeFactory.createLiteral(object.getLiteral(), object.getLanguage());
                } else {
                    return NodeFactory.createLiteral(object.getLiteral());
                }
            } else {
                return NodeFactory.createBlankNode();
            }
        }
        return Node.ANY;
    }
}