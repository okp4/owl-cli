package de.atextor.owldiagram.mappers;

import de.atextor.owldiagram.graph.Node;
import de.atextor.owldiagram.graph.NodeType;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.stream.Stream;

public class OWLIndividualMapper implements OWLIndividualVisitorEx<Result> {
    private MappingConfiguration mappingConfig;

    public OWLIndividualMapper( final MappingConfiguration mappingConfig ) {
        this.mappingConfig = mappingConfig;
    }

    @Override
    public Result visit( final OWLAnonymousIndividual individual ) {
        final Node node = new NodeType.Individual( mappingConfig.getIdentifierMapper().getSyntheticId(), "[]" );
        return new Result( node, Stream.empty() );
    }

    @Override
    public Result visit( final OWLNamedIndividual individual ) {
        final Node.Id id = mappingConfig.getIdentifierMapper().getIdForIri( individual.getIRI() );
        final String label = id.getId();
        final Node node = new NodeType.Individual( id, label );
        return new Result( node, Stream.empty() );
    }
}
