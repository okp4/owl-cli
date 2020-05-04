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
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnnotationSubjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitorEx;

/**
 * Captures the different parts of the ontology-to-graph mapping operation
 */
public interface MappingConfiguration {
    OWLAxiomVisitorEx<Graph> getOwlAxiomMapper();

    OWLClassExpressionVisitorEx<Graph> getOwlClassExpressionMapper();

    OWLIndividualVisitorEx<Graph> getOwlIndividualMapper();

    OWLPropertyExpressionVisitorEx<Graph> getOwlPropertyExpressionMapper();

    OWLObjectVisitorEx<Graph> getOwlObjectMapper();

    OWLDataVisitorEx<Graph> getOwlDataMapper();

    OWLEntityVisitorEx<Graph> getOwlEntityMapper();

    OWLAnnotationObjectVisitorEx<Graph> getOwlAnnotationObjectMapper();

    OWLAnnotationSubjectVisitorEx<Graph> getOwlAnnotationSubjectMapper();

    IdentifierMapper getIdentifierMapper();

    NameMapper getNameMapper();
}
