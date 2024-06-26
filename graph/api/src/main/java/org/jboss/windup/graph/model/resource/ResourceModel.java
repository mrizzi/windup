package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;

import org.jboss.windup.graph.model.WindupVertexFrame;

public interface ResourceModel extends WindupVertexFrame {
    InputStream asInputStream();

    File asFile() throws RuntimeException;
}
