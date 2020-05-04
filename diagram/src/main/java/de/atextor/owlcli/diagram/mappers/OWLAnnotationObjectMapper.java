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

package de.atextor.owlcli.diagram.mappers;

import de.atextor.owlcli.diagram.graph.Graph;
import de.atextor.owlcli.diagram.graph.Node;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnnotationSubjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;

/**
 * Maps {@link org.semanticweb.owlapi.model.OWLAnnotationObject}s to {@link Graph}s
 */
public class OWLAnnotationObjectMapper implements OWLAnnotationObjectVisitorEx<Graph>,
    OWLAnnotationSubjectVisitorEx<Graph> {
    private final MappingConfiguration mappingConfig;

    public OWLAnnotationObjectMapper( final MappingConfiguration mappingConfig ) {
        this.mappingConfig = mappingConfig;
    }

    @Override
    public Graph visit( final @Nonnull OWLAnnotation annotation ) {
        return annotation.getProperty().accept( mappingConfig.getOwlPropertyExpressionMapper() );
    }

    @Override
    public Graph visit( final @Nonnull IRI iri ) {
        final Node.Id id = mappingConfig.getIdentifierMapper().getSyntheticId();
        return Graph.of( new Node.IRIReference( id, iri ) );
    }

    @Override
    public Graph visit( final @Nonnull OWLAnonymousIndividual individual ) {
        return individual.accept( mappingConfig.getOwlIndividualMapper() );
    }

    @Override
    public Graph visit( final @Nonnull OWLLiteral node ) {
        return node.accept( mappingConfig.getOwlDataMapper() );
    }
}
