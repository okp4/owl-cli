package de.atextor.owlcli.diagram;

import de.atextor.owlcli.diagram.graph.Edge;
import de.atextor.owlcli.diagram.graph.Graph;
import de.atextor.owlcli.diagram.graph.GraphElement;
import de.atextor.owlcli.diagram.graph.Node;
import de.atextor.owlcli.diagram.graph.NodeType;
import de.atextor.owlcli.diagram.mappers.OWLAnnotationObjectMapper;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OWLAnnotationObjectMapperTest extends MapperTestBase {
    private final OWLAnnotationObjectMapper mapper = new OWLAnnotationObjectMapper( createTestMappingConfiguration() );

    @Test
    public void testOWLAnnotation() {
        final String ontology = """
            :comment a owl:AnnotationProperty .
            :Dog a owl:Class ;
                :comment :Foo .
            """;

        final String fooId = "Foo";
        testIdentifierMapper.pushAnonId( new Node.Id( fooId ) );

        final OWLAnnotationAssertionAxiom axiom = getAxiom( ontology, AxiomType.ANNOTATION_ASSERTION );
        final Graph graph = mapper.visit( axiom.getAnnotation() );

        assertThat( graph.getNode().getClass() ).isEqualTo( NodeType.AnnotationProperty.class );

        final Set<GraphElement> remainingElements = graph.getOtherElements().collect( Collectors.toSet() );
        assertThat( remainingElements ).isNotEmpty();

        final List<Node> nodes = nodes( remainingElements );
        assertThat( nodes ).hasSize( 1 );
        final Node theNode = nodes.get( 0 );
        assertThat( theNode.is( NodeType.IRIReference.class ) ).isTrue();
        final NodeType.IRIReference reference = theNode.as( NodeType.IRIReference.class );
        assertThat( reference.getIri().toString() ).isEqualTo( iri( "Foo" ).toString() );

        final List<Edge> edges = edges( remainingElements );
        assertThat( edges ).hasSize( 1 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( graph.getNode().getId(), reference.getId() ) );
    }

    @Test
    public void testIRI() {
        final String ontology = """
            :comment a owl:AnnotationProperty .
            :Dog a owl:Class ;
                :comment :Foo .
            """;

        final String fooId = "Foo";
        testIdentifierMapper.pushAnonId( new Node.Id( fooId ) );

        final OWLAnnotationAssertionAxiom axiom = getAxiom( ontology, AxiomType.ANNOTATION_ASSERTION );
        final Graph graph = mapper.visit( axiom.getValue().asIRI().get() );

        assertThat( graph.getNode() ).matches( isNodeWithId( fooId ) );
        assertThat( graph.getOtherElements() ).isEmpty();
    }

    @Test
    public void testOWLAnonymousIndividual() {
        new OWLIndividualMapperTest().testOWLAnonymousIndividual();
    }

    @Test
    public void testOWLLiteral() {
        new OWLDataMapperTest().testOWLLiteral();
    }
}
