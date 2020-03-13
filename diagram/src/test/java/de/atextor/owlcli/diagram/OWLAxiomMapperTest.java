package de.atextor.owlcli.diagram;

import de.atextor.owlcli.diagram.graph.Edge;
import de.atextor.owlcli.diagram.graph.GraphElement;
import de.atextor.owlcli.diagram.graph.Node;
import de.atextor.owlcli.diagram.mappers.IdentifierMapper;
import de.atextor.owlcli.diagram.mappers.MappingConfiguration;
import de.atextor.owlcli.diagram.mappers.OWLAxiomMapper;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentDataPropertiesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubObjectPropertyOfAxiomImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OWLAxiomMapperTest extends MapperTestBase {
    private final MappingConfiguration mappingConfiguration = createTestMappingConfiguration();
    private final OWLAxiomMapper mapper = new OWLAxiomMapper( mappingConfiguration );

    @Test
    public void testOWLSubClassOfAxiom() {
        final OWLSubClassOfAxiom axiom = getAxiom( ":Foo rdfs:subClassOf :Bar ." );
        final List<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toList() );
        assertThat( result ).hasSize( 3 );

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 2 );
        assertThat( nodes ).anyMatch( isNodeWithId( "Foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Bar" ) );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 1 );

        final Edge theEdge = edges.get( 0 );
        assertThat( theEdge ).matches( isEdgeWithFromAndTo( "Foo", "Bar" ) );
        assertThat( theEdge.getType() ).isEqualTo( Edge.Type.HOLLOW_ARROW );
    }

    @Test
    public void testOWLNegativeObjectPropertyAssertionAxiom() {
        final String ontology = """
            :foo a owl:NamedIndividual .
            :bar a owl:NamedIndividual .
            :property a owl:ObjectProperty .
            [
               a owl:NegativeObjectPropertyAssertion ;
               owl:sourceIndividual :foo ;
               owl:assertionProperty :property ;
               owl:targetIndividual :bar
            ] .
            """;
        final OWLNegativeObjectPropertyAssertionAxiom axiom = getAxiom( ontology,
            AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION );

        final String complementId = "complementNode";
        testIdentifierMapper.pushAnonId( new Node.Id( complementId ) );

        final List<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toList() );
        assertThat( result ).hasSize( 7 );

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 4 );
        assertThat( nodes ).anyMatch( isNodeWithId( "foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "bar" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "property" ) );
        assertThat( nodes ).anyMatch( isComplement() );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );

        final Edge fooToComplement =
            edges.stream().filter( isEdgeWithFromAndTo( "foo", complementId ) ).findAny().get();
        final Edge complementToBar =
            edges.stream().filter( isEdgeWithFromAndTo( complementId, "bar" ) ).findAny().get();
        final Edge complementToProp =
            edges.stream().filter( isEdgeWithFromAndTo( complementId, "property" ) ).findAny().get();
        assertThat( fooToComplement.getType() ).isEqualTo( Edge.Type.DEFAULT_ARROW );
        assertThat( complementToBar.getType() ).isEqualTo( Edge.Type.DEFAULT_ARROW );
        assertThat( complementToProp.getType() ).isEqualTo( Edge.Type.DASHED_ARROW );
    }

    @Test
    public void testOWLAsymmetricObjectPropertyAxiom() {
    }

    @Test
    public void testOWLReflexiveObjectPropertyAxiom() {
    }

    @Test
    public void testOWLDisjointClassesAxiom() {
    }

    @Test
    public void testOWLDataPropertyDomainAxiom() {
    }

    @Test
    public void testOWLObjectPropertyDomainAxiom() {
    }

    @Test
    public void testOWLEquivalentObjectPropertiesAxiomNAry() {
        // The axiom variant with >2 arguments can not be expressed in Turtle,
        // therefore we use Functional Syntax here.
        final String ontology = """
            Declaration(ObjectProperty(:bar))
            Declaration(ObjectProperty(:baz))
            Declaration(ObjectProperty(:foo))
            EquivalentObjectProperties(:foo :bar :baz)
            """;
        final OWLEquivalentObjectPropertiesAxiom axiom = getAxiom( ontology, AxiomType.EQUIVALENT_OBJECT_PROPERTIES );

        final Set<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toSet() );
        assertEquivalentResult( result, iri( "foo" ), iri( "bar" ), iri( "baz" ) );
    }

    @Test
    public void testOWLEquivalentObjectPropertiesAxiom() {
        final String ontologyContent = """
            :foo a owl:ObjectProperty ;
               owl:equivalentProperty :bar .
            :bar a owl:ObjectProperty ;
               owl:equivalentProperty :baz .
            :baz a owl:ObjectProperty ;
               owl:equivalentProperty :foo .
            """;
        final OWLOntology ontology = createOntology( ontologyContent );
        final Set<GraphElement> result = ontology.axioms()
            .filter( axiom -> axiom.isOfType( AxiomType.EQUIVALENT_OBJECT_PROPERTIES ) )
            .map( axiom -> (OWLEquivalentObjectPropertiesAxiom) axiom )
            .flatMap( mapper::visit )
            .collect( Collectors.toSet() );

        assertEquivalentResult( result, iri( "foo" ), iri( "bar" ), iri( "baz" ) );
    }

    @Test
    public void testOWLNegativeDataPropertyAssertionAxiom() {
    }

    @Test
    public void testOWLDifferentIndividualsAxiom() {
    }

    @Test
    public void testOWLDisjointDataPropertiesAxiom() {
    }

    @Test
    public void testOWLDisjointObjectPropertiesAxiom() {
    }

    @Test
    public void testOWLObjectPropertyRangeAxiom() {
    }

    @Test
    public void testOWLObjectPropertyAssertionAxiom() {
        final IRI fooIri = IRI.create( "http://test.de#foo" );
        final IRI barIri = IRI.create( "http://test.de#bar" );
        final IRI propertyIri = IRI.create( "http://test.de#property" );

        final OWLIndividual foo = new OWLNamedIndividualImpl( fooIri );
        final OWLIndividual bar = new OWLNamedIndividualImpl( barIri );
        final OWLObjectPropertyExpression property = new OWLObjectPropertyImpl( propertyIri );
        final OWLObjectPropertyAssertionAxiom axiom = new OWLObjectPropertyAssertionAxiomImpl( foo,
            property, bar, Collections.emptyList() );

        final String invisibleId = "invisibleNode";
        testIdentifierMapper.pushAnonId( new Node.Id( invisibleId ) );

        final List<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toList() );
        assertThat( result ).hasSize( 7 );

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 4 );
        assertThat( nodes ).anyMatch( isNodeWithId( "foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "bar" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "property" ) );
        assertThat( nodes ).anyMatch( isInvisible() );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );

        final Edge fooToInvis = edges.stream().filter( isEdgeWithFromAndTo( "foo", invisibleId ) ).findAny().get();
        final Edge invisToBar = edges.stream().filter( isEdgeWithFromAndTo( invisibleId, "bar" ) ).findAny().get();
        final Edge invisToProp =
            edges.stream().filter( isEdgeWithFromAndTo( invisibleId, "property" ) ).findAny().get();
        assertThat( fooToInvis.getType() ).isEqualTo( Edge.Type.NO_ARROW );
        assertThat( invisToBar.getType() ).isEqualTo( Edge.Type.DEFAULT_ARROW );
        assertThat( invisToProp.getType() ).isEqualTo( Edge.Type.DASHED_ARROW );
    }

    @Test
    public void testOWLFunctionalObjectPropertyAxiom() {
    }

    @Test
    public void testOWLSubObjectPropertyOfAxiom() {
        final IRI fooIri = IRI.create( "http://test.de#foo" );
        final IRI barIri = IRI.create( "http://test.de#bar" );
        final OWLObjectPropertyExpression foo = new OWLObjectPropertyImpl( fooIri );
        final OWLObjectPropertyExpression bar = new OWLObjectPropertyImpl( barIri );
        final OWLSubObjectPropertyOfAxiom axiom = new OWLSubObjectPropertyOfAxiomImpl( foo, bar,
            Collections.emptyList() );

        final List<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toList() );
        assertThat( result ).hasSize( 3 );

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 2 );
        assertThat( nodes ).anyMatch( isNodeWithId( "foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "bar" ) );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 1 );

        final Edge theEdge = edges.get( 0 );
        assertThat( theEdge ).matches( isEdgeWithFromAndTo( "foo", "bar" ) );
        assertThat( theEdge.getType() ).isEqualTo( Edge.Type.HOLLOW_ARROW );
    }

    @Test
    public void testOWLDisjointUnionAxiom() {
    }

    @Test
    public void testOWLSymmetricObjectPropertyAxiom() {
    }

    @Test
    public void testOWLDataPropertyRangeAxiom() {
    }

    @Test
    public void testOWLFunctionalDataPropertyAxiom() {
    }

    @Test
    public void testOWLEquivalentDataPropertiesAxiom() {
        final IRI fooIri = IRI.create( "http://test.de#foo" );
        final IRI barIri = IRI.create( "http://test.de#bar" );
        final IRI bazIri = IRI.create( "http://test.de#baz" );
        final OWLDataPropertyExpression dataPropertyExpression1 = new OWLDataPropertyImpl( fooIri );
        final OWLDataPropertyExpression dataPropertyExpression2 = new OWLDataPropertyImpl( barIri );
        final OWLDataPropertyExpression dataPropertyExpression3 = new OWLDataPropertyImpl( bazIri );
        final OWLEquivalentDataPropertiesAxiom axiom =
            new OWLEquivalentDataPropertiesAxiomImpl( Arrays.asList( dataPropertyExpression1,
                dataPropertyExpression2, dataPropertyExpression3 ), Collections.emptyList() );

        final Set<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toSet() );

        assertEquivalentResult( result, fooIri, barIri, bazIri );
    }

    @Test
    public void testOWLClassAssertionAxiom() {
        final OWLClassAssertionAxiom axiom = getAxiom( ":Foo a owl:Thing ." );
        final List<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toList() );
        assertThat( result ).hasSize( 3 );

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 2 );
        assertThat( nodes ).anyMatch( isNodeWithId( "Foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "Thing" ) );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 1 );

        final Edge theEdge = edges.get( 0 );
        assertThat( theEdge ).matches( isEdgeWithFromAndTo( "Foo", "Thing" ) );
        assertThat( theEdge.getType() ).isEqualTo( Edge.Type.DEFAULT_ARROW );
    }

    @Test
    public void testOWLEquivalentClassesAxiom() {
        final IRI fooIri = IRI.create( "http://test.de#Foo" );
        final IRI barIri = IRI.create( "http://test.de#Bar" );
        final IRI bazIri = IRI.create( "http://test.de#Baz" );
        final OWLClassExpression classExpression1 = new OWLClassImpl( fooIri );
        final OWLClassExpression classExpression2 = new OWLClassImpl( barIri );
        final OWLClassExpression classExpression3 = new OWLClassImpl( bazIri );
        final OWLEquivalentClassesAxiom axiom = new OWLEquivalentClassesAxiomImpl( Arrays.asList( classExpression1,
            classExpression2, classExpression3 ), Collections.emptyList() );
        final Set<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toSet() );

        assertEquivalentResult( result, fooIri, barIri, bazIri );
    }

    @Test
    public void testOWLDataPropertyAssertionAxiom() {
        final String ontology = """
            :foo a owl:DatatypeProperty .
            :bar a owl:NamedIndividual .
            :bar :foo "hello" .
            """;
        final OWLDataPropertyAssertionAxiom axiom = getAxiom( ontology, AxiomType.DATA_PROPERTY_ASSERTION );

        final String literalNodeId = "hello";
        testIdentifierMapper.pushAnonId( new Node.Id( literalNodeId ) );

        final String helperNodeId = "helper";
        testIdentifierMapper.pushAnonId( new Node.Id( helperNodeId ) );

        final Set<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toSet() );
        assertThat( result ).isNotEmpty();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 4 );
        assertThat( nodes ).anyMatch( isNodeWithId( "foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "bar" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "hello" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "helper" ) );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( "bar", "helper" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( "helper", "hello" ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( "helper", "foo" ) );
    }

    @Test
    public void testOWLTransitiveObjectPropertyAxiom() {
    }

    @Test
    public void testOWLIrreflexiveObjectPropertyAxiom() {
    }

    @Test
    public void testOWLSubDataPropertyOfAxiom() {
    }

    @Test
    public void testOWLInverseFunctionalObjectPropertyAxiom() {
    }

    @Test
    public void testOWLSameIndividualAxiom() {
    }

    @Test
    public void testOWLSubPropertyChainOfAxiom() {
    }

    @Test
    public void testOWLInverseObjectPropertiesAxiom() {
    }

    @Test
    public void testOWLHasKeyAxiom() {
    }

    @Test
    public void testOWLDeclarationAxiom() {
        final OWLDeclarationAxiom axiom = getAxiom( ":Foo a owl:Class ." );
        final List<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toList() );
        assertThat( result ).hasSize( 1 );

        final Node theNode = nodes( result ).get( 0 );
        assertThat( theNode ).matches( isNodeWithId( "Foo" ) );
    }

    @Test
    public void testOWLDatatypeDefinitionAxiom() {
    }

    @Test
    public void testOWLAnnotationAssertionAxiom() {
    }

    @Test
    public void testOWLSubAnnotationPropertyOfAxiom() {
        final String ontology = """
            :foo a owl:AnnotationProperty .
            :bar a owl:AnnotationProperty ;
                 rdfs:subPropertyOf :foo .
            """;
        final OWLSubAnnotationPropertyOfAxiom axiom = getAxiom( ontology, AxiomType.SUB_ANNOTATION_PROPERTY_OF );
        final Set<GraphElement> result = mapper.visit( axiom ).collect( Collectors.toSet() );
        assertThat( result ).isNotEmpty();

        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 2 );
        assertThat( nodes ).anyMatch( isNodeWithId( "foo" ) );
        assertThat( nodes ).anyMatch( isNodeWithId( "bar" ) );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 1 );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( "bar", "foo" ) );
    }

    @Test
    public void testOWLAnnotationPropertyDomainAxiom() {
    }

    @Test
    public void testOWLAnnotationPropertyRangeAxiom() {
    }

    @Test
    public void testSWRLRule() {
    }

    private void assertEquivalentResult( final Set<GraphElement> result, final IRI fooIri, final IRI barIri,
                                         final IRI bazIri ) {
        final List<Node> nodes = nodes( result );
        assertThat( nodes ).hasSize( 3 );

        final IdentifierMapper identifierMapper = mappingConfiguration.getIdentifierMapper();
        final Node.Id foo = identifierMapper.getIdForIri( fooIri );
        final Node.Id bar = identifierMapper.getIdForIri( barIri );
        final Node.Id baz = identifierMapper.getIdForIri( bazIri );

        assertThat( nodes ).anyMatch( isNodeWithId( foo ) );
        assertThat( nodes ).anyMatch( isNodeWithId( bar ) );
        assertThat( nodes ).anyMatch( isNodeWithId( baz ) );

        final List<Edge> edges = edges( result );
        assertThat( edges ).hasSize( 3 );

        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( bar, foo ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( baz, foo ) );
        assertThat( edges ).anyMatch( isEdgeWithFromAndTo( bar, baz ) );

        assertThat( edges ).noneMatch( isEdgeWithFromAndTo( foo, bar ) );
        assertThat( edges ).noneMatch( isEdgeWithFromAndTo( foo, baz ) );
        assertThat( edges ).noneMatch( isEdgeWithFromAndTo( baz, bar ) );
        assertThat( edges ).noneMatch( isEdgeWithFromAndTo( foo, foo ) );
        assertThat( edges ).noneMatch( isEdgeWithFromAndTo( bar, bar ) );
        assertThat( edges ).noneMatch( isEdgeWithFromAndTo( baz, baz ) );
    }
}
