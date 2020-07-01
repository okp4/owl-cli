/*
 * This file is part of OWL-CLI.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2020, Andreas Textor.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www
 * .gnu.org/licenses/.
 */

package de.atextor.owlcli.diagram;

import de.atextor.owlcli.diagram.graph.Edge;
import de.atextor.owlcli.diagram.graph.GraphElement;
import de.atextor.owlcli.diagram.graph.Node;
import de.atextor.owlcli.diagram.graph.node.ObjectQualifiedMinimalCardinality;
import de.atextor.owlcli.diagram.graph.node.Rule;
import de.atextor.owlcli.diagram.mappers.MappingConfiguration;
import de.atextor.owlcli.diagram.mappers.OWLAxiomMapper;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.SWRLRule;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SWRLObjectMapperTest extends MapperTestBase {
    private final MappingConfiguration mappingConfiguration = createTestMappingConfiguration();
    private final OWLAxiomMapper mapper = new OWLAxiomMapper( mappingConfiguration );

    @Test
    public void testSWRLRuleWithObjectPropertyAtoms() {
        final String ontology = """
            :hasParent a owl:ObjectProperty .
            :hasBrother a owl:ObjectProperty .
            :hasUncle a owl:ObjectProperty .

            var:a a swrl:Variable .
            var:b a swrl:Variable .
            var:c a swrl:Variable .

            [
               a swrl:Imp ;
               swrl:body (
                  [
                     a swrl:IndividualPropertyAtom ;
                     swrl:propertyPredicate :hasParent ;
                     swrl:argument1 var:a ;
                     swrl:argument2 var:b
                  ]
                  [
                     a swrl:IndividualPropertyAtom ;
                     swrl:propertyPredicate :hasBrother ;
                     swrl:argument1 var:b ;
                     swrl:argument2 var:c
                  ]
               ) ;
               swrl:head (
                  [
                     a swrl:IndividualPropertyAtom ;
                     swrl:propertyPredicate :hasUncle ;
                     swrl:argument1 var:a ;
                     swrl:argument2 var:c
                  ]
               )
            ] .
            """;

        final SWRLRule rule = getAxiom( ontology, AxiomType.SWRL_RULE );

        final Set<GraphElement> result = rule.accept( mapper ).getElementSet();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 4 );
        assertThat( nodes ).anyMatch( node -> node.is( Rule.class ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "hasParent" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "hasBrother" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "hasUncle" ) );

        final Node ruleNode = nodes.stream().filter( node -> node.is( Rule.class ) ).findFirst().get();

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "hasParent" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "hasBrother" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "hasUncle" ) );
    }

    @Test
    public void testSWRLRuleWithClassAtoms() {
        final String ontology = """
            :Student a owl:Class .
            :Person a owl:Class .

            var:a a swrl:Variable .

            [
               a swrl:Imp ;
               swrl:body (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Student ;
                     swrl:argument1 var:a
                  ]
               ) ;
               swrl:head (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Person ;
                     swrl:argument1 var:a
                  ]
               )
            ] .
            """;

        final SWRLRule rule = getAxiom( ontology, AxiomType.SWRL_RULE );

        final Set<GraphElement> result = rule.accept( mapper ).getElementSet();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 3 );
        assertThat( nodes ).anyMatch( node -> node.is( Rule.class ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Student" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Person" ) );

        final Node ruleNode = nodes.stream().filter( node -> node.is( Rule.class ) ).findFirst().get();

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 2 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Student" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Person" ) );
    }

    @Test
    public void testSWRLRuleWithDataRangeAtom() {
        final String ontology = """
            :Person a owl:Class .
            :Adult a owl:Class .
            :age a owl:DatatypeProperty .

            var:a a swrl:Variable .
            var:p a swrl:Variable .

            [
               a swrl:Imp ;
               swrl:body (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Person ;
                     swrl:argument1 var:p
                  ]
                  [
                     a swrl:DataRangeAtom ;
                     swrl:dataRange [
                        a rdfs:Datatype ;
                        owl:onDatatype xsd:int ;
                        owl:withRestrictions ( [ xsd:minInclusive "18"^^xsd:int ] )
                     ] ;
                     swrl:argument1 var:a
                  ]
                  [
                     a swrl:DatavaluedPropertyAtom ;
                     swrl:propertyPredicate :age ;
                     swrl:argument1 var:p ;
                     swrl:argument2 var:a
                  ]
               ) ;
               swrl:head (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Adult ;
                     swrl:argument1 var:p
                  ]
               )
            ] .
            """;

        final SWRLRule rule = getAxiom( ontology, AxiomType.SWRL_RULE );

        final Set<GraphElement> result = rule.accept( mapper ).getElementSet();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 4 );
        assertThat( nodes ).anyMatch( node -> node.is( Rule.class ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Adult" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Person" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "age" ) );

        final Node ruleNode = nodes.stream().filter( node -> node.is( Rule.class ) ).findFirst().get();

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Adult" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Person" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "age" ) );
    }

    @Test
    public void testSWRLRuleWithClassAtomWithExpression() {
        final String ontology = """
            :Person a owl:Class .
            :Parent a owl:Class .

            :hasChild a owl:ObjectProperty .

            var:p a swrl:Variable .

            [
               a swrl:Imp ;
               swrl:body (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Person ;
                     swrl:argument1 var:p
                  ]
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate [
                        a owl:Restriction ;
                        owl:onProperty :hasChild ;
                        owl:minQualifiedCardinality "1"^^xsd:nonNegativeInteger ;
                        owl:onClass :Person
                     ] ;
                     swrl:argument1 var:p
                  ]
               );
               swrl:head (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Parent ;
                     swrl:argument1 var:p
                  ]
               )
            ] .
            """;

        final SWRLRule rule = getAxiom( ontology, AxiomType.SWRL_RULE );

        final Set<GraphElement> result = rule.accept( mapper ).getElementSet();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 5 );
        assertThat( nodes ).anyMatch( node -> node.is( Rule.class ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Person" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Parent" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "hasChild" ) );
        assertThat( nodes ).anyMatch( node -> node.is( ObjectQualifiedMinimalCardinality.class ) );

        final Node ruleNode = nodes.stream().filter( node -> node.is( Rule.class ) ).findFirst().get();

        final List<Edge> edges = edges( result );
        final Node cardinality =
            nodes.stream().filter( node -> node.is( ObjectQualifiedMinimalCardinality.class ) ).findFirst().get();
        assertThat( edges ).hasSize( 5 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Person" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Parent" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), cardinality.getId().getId() ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( cardinality.getId().getId(), "hasChild" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( cardinality.getId().getId(), "Person" ) );
    }

    @Test
    public void testSWRLBuiltinAtom() {
        final String ontology = """
            :Person a owl:Class .
            :Adult a owl:Class .
            :age a owl:DatatypeProperty .

            var:a a swrl:Variable .
            var:p a swrl:Variable .

            [
               a swrl:Imp ;
               swrl:body (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Person ;
                     swrl:argument1 var:p
                  ]
                  [
                     a swrl:DatavaluedPropertyAtom ;
                     swrl:propertyPredicate :age ;
                     swrl:argument1 var:p ;
                     swrl:argument2 var:a
                  ]
                  [
                     a swrl:BuiltinAtom ;
                     swrl:builtin swrlb:greaterThanOrEqual ;
                     swrl:arguments ( var:a 18 )
                  ]
               ) ;
               swrl:head (
                  [
                     a swrl:ClassAtom ;
                     swrl:classPredicate :Adult ;
                     swrl:argument1 var:p
                  ]
               )
            ] .
            """;

        final SWRLRule rule = getAxiom( ontology, AxiomType.SWRL_RULE );

        final Set<GraphElement> result = rule.accept( mapper ).getElementSet();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 4 );
        assertThat( nodes ).anyMatch( node -> node.is( Rule.class ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Person" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Adult" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "age" ) );

        final Node ruleNode = nodes.stream().filter( node -> node.is( Rule.class ) ).findFirst().get();

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Person" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "Adult" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( ruleNode.getId().getId(), "age" ) );
    }
}
